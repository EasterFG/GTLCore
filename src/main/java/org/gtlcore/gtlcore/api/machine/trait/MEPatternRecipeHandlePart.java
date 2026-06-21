package org.gtlcore.gtlcore.api.machine.trait;

import org.gtlcore.gtlcore.api.capability.IMERecipeHandler;
import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEPatternPartMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEPatternTrait;
import org.gtlcore.gtlcore.utils.datastructure.GTRecipe2IntBiMultiMap;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class MEPatternRecipeHandlePart extends MEIORecipeHandlePart<IMEPatternTrait> implements IRecipeHandlePart {

    private final GTRecipe2IntBiMultiMap recipes2SlotsMap = new GTRecipe2IntBiMultiMap();

    public MEPatternRecipeHandlePart(@NotNull IMEPatternTrait meTrait, @NotNull IMERecipeHandler<Ingredient, ItemStack> itemHandler, @NotNull IMERecipeHandler<FluidIngredient, FluidStack> fluidHandler) {
        super(meTrait, itemHandler, fluidHandler);
    }

    public static MEPatternRecipeHandlePart of(IMEPatternPartMachine machine) {
        var meTrait = machine.getMETrait();
        var pair = machine.getMERecipeHandlerTraits();
        return new MEPatternRecipeHandlePart(meTrait, pair.left(), pair.right());
    }

    public IMERecipeHandler<?, ?>[] getMERecipeHandlers() {
        return this.handlers;
    }

    public @NotNull ObjectSet<@NotNull GTRecipe> getCachedGTRecipe() {
        return meTrait.getCachedGTRecipe();
    }

    public void setLastRecipe2Slot(@NotNull GTRecipe gTRecipe, int slot) {
        recipes2SlotsMap.put(gTRecipe, slot);
    }

    public void restoreMachineCache(BiConsumer<GTRecipe, IRecipeHandlePart> consumer) {
        recipes2SlotsMap.clear();
        for (var entry : Int2ReferenceMaps.fastIterable(meTrait.getSlot2RecipesCache())) {
            int slot = entry.getIntKey();
            for (GTRecipe recipe : entry.getValue()) {
                recipes2SlotsMap.put(recipe, slot);
            }
        }
        meTrait.setOnPatternChange(recipes2SlotsMap::removeByValue);

        for (var key : recipes2SlotsMap.keySet()) {
            consumer.accept(key, this);
        }
    }

    /**
     * @param cap    ItemCAP or FluidCAP
     * @param recipe GTRecipe -> slots to collect contents
     * @return contents in specific slot, especially the one cached for this GTRecipe during the last match, or an empty
     *         map if no available slot is found
     */
    public <I extends Predicate<S>, S> Object2LongMap<S> getMEContent(RecipeCapability<I> cap, GTRecipe recipe) {
        return this.<I, S>getMECapability(cap).getStackMapFromFirstAvailableSlot(recipes2SlotsMap.getValues(recipe));
    }

    /**
     * List<Object> won't be empty
     * 
     * @return fail: -1, success and hasCache: -2, success and unCache: slot
     */
    public int handleRecipe(GTRecipe recipe,
                            Reference2ObjectMap<RecipeCapability<?>, List<Object>> contents,
                            boolean simulate, boolean setSlotCache) {
        List<Object> itemContent = null;
        List<Object> fluidContent = null;

        for (var entry : Reference2ObjectMaps.fastIterable(contents)) {
            var key = entry.getKey();
            if (key == ItemRecipeCapability.CAP) itemContent = entry.getValue();
            else if (key == FluidRecipeCapability.CAP) fluidContent = entry.getValue();
            else throw new AssertionError("Invalid recipe capability entry");
        }

        // Priority 1: Init Contents
        boolean hasItem = itemContent != null;
        boolean hasFluid = fluidContent != null;
        if (hasItem) itemHandler.initMEHandleContents(recipe, itemContent, simulate);
        if (hasFluid) fluidHandler.initMEHandleContents(recipe, fluidContent, simulate);

        // Priority 2: Try all cached slots
        IntSet cachedSlots = this.recipes2SlotsMap.getValues(recipe);
        if (!cachedSlots.isEmpty()) {
            for (int slot : cachedSlots) {
                if (hasItem && !itemHandler.meHandleRecipe(recipe, simulate, slot)) continue;
                if (hasFluid && !fluidHandler.meHandleRecipe(recipe, simulate, slot)) continue;
                return slot; // all success
            }
        }

        // Priority 3: Try all active slots
        for (int slot : itemHandler.getActiveSlots()) {
            if (cachedSlots.contains(slot)) continue;
            if (hasItem && !itemHandler.meHandleRecipe(recipe, simulate, slot)) continue;
            if (hasFluid && !fluidHandler.meHandleRecipe(recipe, simulate, slot)) continue;

            // all success, then cache
            if (!meTrait.hasCacheInSlot(slot) && setSlotCache) meTrait.setSlotCacheRecipe(slot, recipe);
            return slot;
        }

        return -1;
    }
}
