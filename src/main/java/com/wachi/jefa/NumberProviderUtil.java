package com.wachi.jefa;

import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class NumberProviderUtil {

    public record IntRange(Integer min, Integer max, boolean approximate) {}

    public static IntRange getIntRange(NumberProvider provider) {
        if (provider instanceof ConstantValue cv) {
            int v = Math.round(cv.value());
            return new IntRange(v, v, false);
        }
        if (provider instanceof UniformGenerator uni) {
            NumberProvider minProv = uni.min();
            NumberProvider maxProv = uni.max();
            if (minProv instanceof ConstantValue minCv && maxProv instanceof ConstantValue maxCv) {
                int min = Math.round(minCv.value());
                int max = Math.round(maxCv.value());
                return new IntRange(min, max, true);
            }
        }
        if (provider instanceof BinomialDistributionGenerator) {
            return null;
        }

        return null;
    }

    private NumberProviderUtil() {}
}
