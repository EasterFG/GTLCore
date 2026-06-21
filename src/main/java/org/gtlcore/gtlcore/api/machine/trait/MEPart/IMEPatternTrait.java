package org.gtlcore.gtlcore.api.machine.trait.MEPart;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.jetbrains.annotations.NotNull;

public interface IMEPatternTrait extends IMEFilterIOTrait {

    @NotNull
    ObjectSet<@NotNull GTRecipe> getCachedGTRecipe();

    void setSlotCacheRecipe(int index, GTRecipe recipe);

    @NotNull
    Int2ReferenceMap<ObjectSet<@NotNull GTRecipe>> getSlot2RecipesCache();

    void setOnPatternChange(IntConsumer removeMapOnSlot);

    boolean hasCacheInSlot(int slot);
}
