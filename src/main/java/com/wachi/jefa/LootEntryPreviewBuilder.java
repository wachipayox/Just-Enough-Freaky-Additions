package com.wachi.jefa;

import com.wachi.jefa.mixins.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.*;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.text.DecimalFormat;
import java.util.*;

import static com.wachi.jefa.NumberProviderUtil.IntRange;
import static com.wachi.jefa.NumberProviderUtil.getIntRange;

public final class LootEntryPreviewBuilder {

    public record PreviewResult(ItemStack stack, double chancePerRoll) {}

    /**
     * ==========
     * NUEVO: wiki-style por loot table completa
     * ==========
     */
    public static List<PreviewResult> buildPreviewsForLootTable(ResourceLocation rootTable) {
        // Probabilidades por LootItem (no por Item), ya con pools+rolls+nested+condiciones
        Map<LootItem, Double> pByLootItem = LootProbEngine.pLootItemAppearsInTable(rootTable);

        // Ahora construimos stacks/lore con TU lógica, y agrupamos por "firma" del resultado (stack sin la línea de prob).
        Map<StackKey, Aggregated> agg = new HashMap<>();

        for (var e : pByLootItem.entrySet()) {
            LootItem li = e.getKey();
            double p = e.getValue();
            if (p <= 0) continue;

            Item item = ((LootItemAccessorMixin) (Object) li).getItem().value();
            ItemStack stack = new ItemStack(item);

            List<Component> lore = new ArrayList<>();
            boolean enchant = false;

            List<LootItemFunction> functions =
                    ((LootPoolSingletonContainerAccessorMixin) (Object) li).getFunctions();

            boolean unknowndata = false;
            for (LootItemFunction fn : functions) {
                switch (fn) {
                    case SetOminousBottleAmplifierFunction fc ->
                        applySetOminousEffectAmplifier(fc, lore);
                    case SetItemDamageFunction ignored ->
                            lore.add(Component.translatable("jefa.lore.damaged"));
                    case SetPotionFunction sp ->
                            applySetPotion(stack, (SetPotionFunctionAccessorMixin) (Object) sp, lore);
                    case SetEnchantmentsFunction se -> {
                        describeSetEnchantments(stack, (SetEnchantmentsFunctionAccessorMixin) (Object) se, lore);
                        enchant = true;
                    }
                    case SetItemCountFunction sic ->
                            describeSetItemCount((SetItemCountFunctionAccessorMixin) (Object) sic, lore);
                    case EnchantWithLevelsFunction ewl -> {
                        describeEnchantWithLevels(stack, (EnchantWithLevelsFunctionAccessorMixin) (Object) ewl, lore);
                        enchant = true;
                    }
                    case EnchantRandomlyFunction erf -> {
                        describeEnchantRandomly(stack, (EnchantRandomlyFunctionAccessorMixin) (Object) erf, lore);
                        enchant = true;
                    }
                    case null, default -> unknowndata = true;
                }
            }
            if(unknowndata) lore.addLast(Component.translatable("jefa.lore.unknown_loot_function"));

            if (enchant && item.equals(Items.BOOK)) stack = stack.transmuteCopy(Items.ENCHANTED_BOOK);

            // key sin la probabilidad (para poder sumar probabilidades de "mismo resultado")
            StackKey key = StackKey.of(stack, lore);

            ItemStack finalStack = stack;
            agg.computeIfAbsent(key, k -> new Aggregated(finalStack, new ArrayList<>(lore)))
                    .prob += p;
        }

        DecimalFormat df = new DecimalFormat("#.##");
        List<PreviewResult> out = new ArrayList<>();

        for (Aggregated a : agg.values()) {
            double prob = clamp01(a.prob);
            List<Component> finalLore = new ArrayList<>(a.loreWithoutProb);
            finalLore.add(Component.literal(Component.translatable("jefa.lore.chance").getString() + ": " + df.format(prob * 100.0) + "%"));

            ItemStack finalStack = a.stack.copy();
            finalStack.set(DataComponents.LORE, new ItemLore(finalLore));

            out.add(new PreviewResult(finalStack, prob));
        }

        out.sort(Comparator.comparingDouble(PreviewResult::chancePerRoll).reversed());
        return out;
    }

    public static final class LootProbEngine {
        public static final Map<ResourceLocation, Map<LootItem, Double>> CACHE = new HashMap<>();
        public static ThreadLocal<Set<ResourceLocation>> VISITING = ThreadLocal.withInitial(HashSet::new);

        static Map<LootItem, Double> pLootItemAppearsInTable(ResourceLocation tableId) {
            var cached = CACHE.get(tableId);
            if (cached != null) return cached;

            Set<ResourceLocation> visiting = VISITING.get();
            if (!visiting.add(tableId)) return Collections.emptyMap(); // ciclo

            Map<LootItem, Double> pTotal = new HashMap<>();
            List<LootPool> pools = ClientLootTableReader.getLootPools(tableId);

            for (LootPool pool : pools) {
                Map<LootItem, Double> pPool = pLootItemAppearsInPool(pool);

                for (var e : pPool.entrySet()) {
                    LootItem li = e.getKey();
                    double pp = clamp01(e.getValue());
                    double old = pTotal.getOrDefault(li, 0.0);
                    pTotal.put(li, clamp01(old + pp - old * pp));
                }
            }

            visiting.remove(tableId);
            CACHE.put(tableId, pTotal);
            return pTotal;
        }

        private static Map<LootItem, Double> pLootItemAppearsInPool(LootPool pool) {
            double condMult = poolConditionMultiplier(pool);

            Map<LootItem, Double> p1 = pLootItemInOneRoll(pool);

            int minR = 1, maxR = 1;
            try {
                NumberProvider rolls = pool.getRolls();
                IntRange r = getIntRange(rolls);
                if (r != null) {
                    minR = Math.max(0, r.min());
                    maxR = Math.max(0, r.max());
                }
            } catch (Throwable ignored) {}

            Map<LootItem, Double> pPool = new HashMap<>();
            for (var e : p1.entrySet()) {
                double p = clamp01(e.getValue());
                double pAtLeastOnce = pAtLeastOnceWithRolls(p, minR, maxR);

                pAtLeastOnce *= condMult;
                if (pAtLeastOnce > 0) pPool.put(e.getKey(), clamp01(pAtLeastOnce));
            }
            return pPool;
        }

        private static Map<LootItem, Double> pLootItemInOneRoll(LootPool pool) {
            List<LootPoolEntryContainer> entries = ((LootPoolAccessorMixin) (Object) pool).getEntries();

            int totalW = 0;
            List<LootPoolSingletonContainer> singles = new ArrayList<>();
            for (LootPoolEntryContainer e : entries) {
                if (e instanceof LootPoolSingletonContainer s) {
                    singles.add(s);
                    totalW += ((LootPoolSingletonContainerAccessorMixin) (Object) s).getWeight();
                }
            }
            if (totalW <= 0) return Collections.emptyMap();

            Map<LootItem, Double> p1 = new HashMap<>();

            for (LootPoolSingletonContainer s : singles) {
                int w = ((LootPoolSingletonContainerAccessorMixin) (Object) s).getWeight();
                if (w <= 0) continue;

                double pick = w / (double) totalW;

                if (s instanceof LootItem li) {
                    p1.merge(li, pick, Double::sum);
                } else if (s instanceof NestedLootTable nlt) {
                    ResourceLocation nestedId = extractNestedId((NestedLootTableAccessorMixin) (Object) nlt);
                    if (nestedId == null) continue;

                    Map<LootItem, Double> nested = pLootItemAppearsInTable(nestedId);
                    for (var ne : nested.entrySet()) {
                        p1.merge(ne.getKey(), pick * clamp01(ne.getValue()), Double::sum);
                    }
                }
            }

            // clamp
            for (var it = p1.entrySet().iterator(); it.hasNext();) {
                var e = it.next();
                double v = clamp01(e.getValue());
                if (v <= 0) it.remove();
                else e.setValue(v);
            }
            return p1;
        }

        private static Double tryResolveConstant(NumberProvider p) {
            if (p instanceof ConstantValue(float value)) {
                return (double) value;
            }
            return null;
        }

        private static double poolConditionMultiplier(LootPool pool) {
            double m = 1.0;

            List<LootItemCondition> conds = ((LootPoolAccessorMixin)(Object)pool).getConditions();
            if (conds == null) return 1.0;

            for (LootItemCondition c : conds) {
                if (c instanceof LootItemRandomChanceCondition(NumberProvider chance)) {
                    Double v = tryResolveConstant(chance);
                    if (v != null) m *= v;
                }
            }
            return Math.max(0.0, Math.min(1.0, m));
        }

        private static double pAtLeastOnceWithRolls(double p, int minR, int maxR) {
            p = clamp01(p);
            if (maxR < minR) { int t = maxR; maxR = minR; minR = t; }

            if (minR == maxR) {
                return 1.0 - Math.pow(1.0 - p, minR);
            }

            int a = minR, b = maxR;
            int n = b - a + 1;
            if (n <= 0) return 0.0;

            double sumNot = 0.0;
            for (int r = a; r <= b; r++) {
                sumNot += Math.pow(1.0 - p, r);
            }
            return 1.0 - (sumNot / n);
        }

        private static ResourceLocation extractNestedId(NestedLootTableAccessorMixin nested) {
            var v = nested.getContents();
            if (v.left().isPresent()) return v.left().get().location();
            if (v.right().isPresent()) return v.right().get().getLootTableId();
            return null;
        }
    }

    private record StackKey(Item item, PotionContents potion, List<String> loreLines) {
        static StackKey of(ItemStack stack, List<Component> lore) {
            PotionContents pc = stack.get(DataComponents.POTION_CONTENTS);
            List<String> lines = new ArrayList<>(lore.size());
            for (Component c : lore) lines.add(c.getString());
            return new StackKey(stack.getItem(), pc, Collections.unmodifiableList(lines));
        }
    }

    private static final class Aggregated {
        final ItemStack stack;
        final List<Component> loreWithoutProb;
        double prob;

        Aggregated(ItemStack stack, List<Component> loreWithoutProb) {
            this.stack = stack;
            this.loreWithoutProb = loreWithoutProb;
        }
    }

    private static double clamp01(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static void applySetOminousEffectAmplifier(SetOminousBottleAmplifierFunction fc, List<Component> lore){
        MutableComponent add = Component.translatable("jefa.lore.ominous_amplifier").append(": ");
        if(fc.amplifier() instanceof UniformGenerator(NumberProvider min, NumberProvider max)) {
            if (min instanceof ConstantValue(float min1) && max instanceof ConstantValue(float max1))
                add
                        .append(convertLevelIntToRomanString((int) min1 + 1))
                        .append("-")
                        .append(convertLevelIntToRomanString((int) max1 + 1));
            else if(min instanceof ConstantValue(float min1))
                add
                        .append(convertLevelIntToRomanString((int) min1 + 1))
                        .append("-?");
            else if(max instanceof ConstantValue(float max1))
                add
                        .append("?-")
                        .append(convertLevelIntToRomanString((int) max1 + 1));
            else add.append("?");
        }
        lore.add(add);
    }

    private static void applySetPotion(ItemStack stack,
                                       SetPotionFunctionAccessorMixin acc,
                                       List<Component> lore) {
        stack.set(DataComponents.POTION_CONTENTS, new PotionContents(acc.getPotion()));
    }

    private static void describeSetEnchantments(ItemStack stack, SetEnchantmentsFunctionAccessorMixin acc,
                                                List<Component> lore) {
        Map<Holder<Enchantment>, NumberProvider> map = acc.getEnchantments();
        boolean add = acc.isAdd();
        map.keySet().stream().filter(
                stack::supportsEnchantment
        ).toList().forEach(map::remove);

        for (Map.Entry<Holder<Enchantment>, NumberProvider> e : map.entrySet()) {
            Enchantment ench = e.getKey().value();
            NumberProvider provider = e.getValue();
            IntRange range = getIntRange(provider);

            String enchName = ench.description().getString();

            String text;
            if (range == null) {
                text = enchName + "(" + Component.translatable("jefa.lore.enchantment.random_level").getString() + ")";
            } else if (Objects.equals(range.min(), range.max())) {
                text = enchName + " " + convertLevelIntToRomanString(range.min());
            } else {
                text = enchName + " " + convertLevelIntToRomanString(range.min()) + "-" + convertLevelIntToRomanString(range.max()) + " ";
            }

            if (add) text += " [" + Component.translatable("jefa.lore.enchantment.add").getString() + "]";
            lore.add(Component.literal(text));
        }
    }

    private static void describeSetItemCount(SetItemCountFunctionAccessorMixin acc,
                                             List<Component> lore) {
        NumberProvider value = acc.getValue();
        IntRange range = getIntRange(value);

        String text;
        if (range == null) text = Component.translatable("jefa.lore.quantity.random").getString();
        else if (Objects.equals(range.min(), range.max())) text = Component.translatable("jefa.lore.quantity").getString() + ": " + range.min();
        else text = Component.translatable("jefa.lore.quantity").getString() + ": " + range.min() + "-" + range.max();

        lore.add(Component.literal(text));
    }

    private static String convertLevelIntToRomanString(int number){
        return number > 0 && number < 11 ? Component.translatable("enchantment.level." + number).getString() : "" + number;
    }

    private static void describeEnchantWithLevels(ItemStack stack, EnchantWithLevelsFunctionAccessorMixin acc,
                                                  List<Component> lore) {
        NumberProvider levels = acc.getLevels();
        Optional<HolderSet<Enchantment>> optSet = acc.getEnchantments();


        IntRange range = getIntRange(levels);

        String base = Component.translatable("jefa.lore.enchantment.random").getString() + " ";
        if (range != null) {
            if (Objects.equals(range.min(), range.max())) base += Component.translatable("jefa.lore.enchantment.level").getString() + " " + convertLevelIntToRomanString(range.min());
            else base += Component.translatable("jefa.lore.enchantment.level").getString() + " " + convertLevelIntToRomanString(range.min()) + "-" + convertLevelIntToRomanString(range.max());
        } else base += "(" + Component.translatable("jefa.lore.enchantment.random_level").getString() + ")";

        if (optSet.isPresent()) {
            HolderSet<Enchantment> set = optSet.get();
            List<String> names = new ArrayList<>();
            for (Holder<Enchantment> h : set.stream().filter(h -> stack.supportsEnchantment(h) || stack.getItem().equals(Items.BOOK) || stack.getItem().equals(Items.ENCHANTED_BOOK)).toList()) names.add(h.value().description().getString());
            names.sort(String.CASE_INSENSITIVE_ORDER);
            if (!names.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                sb.append(base).append(" ").append(Component.translatable("jefa.lore.literal.between").getString()).append(": ");
                for (int i = 0; i < names.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(names.get(i));
                }
                lore.add(Component.literal(sb.toString()));
                return;
            }
        }

        lore.add(Component.literal(base + " (" + Component.translatable("jefa.lore.enchantment.random_valid").getString() + ")"));
    }

    private static void describeEnchantRandomly(ItemStack stack, EnchantRandomlyFunctionAccessorMixin acc,
                                                List<Component> lore) {
        Optional<HolderSet<Enchantment>> optSet = acc.getEnchantments();

        if (optSet.isEmpty()) {
            lore.add(Component.translatable("jefa.lore.enchantment.random").append(": ").append(Component.translatable("jefa.lore.enchantment.random_valid")));
            return;
        }

        HolderSet<Enchantment> set = optSet.get();
        List<String> names = new ArrayList<>();
        for (Holder<Enchantment> h : set.stream().filter(h -> stack.supportsEnchantment(h) || stack.getItem().equals(Items.BOOK) || stack.getItem().equals(Items.ENCHANTED_BOOK)).toList()) names.add(h.value().description().getString());

        if (names.isEmpty()) {
            lore.add(Component.translatable("jefa.lore.enchantment.random"));
            return;
        }

        StringBuilder sb = new StringBuilder(names.size() > 1 ? Component.translatable("jefa.lore.enchantment.random").getString() + " " + Component.translatable("jefa.lore.literal.between").getString() + ": \n" : "");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(names.get(i));
        }
        sb.append(" (").append(Component.translatable("jefa.lore.enchantment.random_level").getString()).append(")");
        lore.add(Component.literal(sb.toString()));
    }

    private LootEntryPreviewBuilder() {}
}
