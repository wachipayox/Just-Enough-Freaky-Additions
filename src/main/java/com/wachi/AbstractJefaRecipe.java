package com.wachi;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;

import java.util.ArrayList;
import java.util.List;

public class AbstractJefaRecipe {

    private final List<IRecipeSlotDrawable> slots = new ArrayList<>();

    public void setSlots(List<IRecipeSlotDrawable> slots) {
        this.slots.clear();
        this.slots.addAll(slots);
    }

    public List<IRecipeSlotDrawable> getSlots(){
        return this.slots;
    }
}
