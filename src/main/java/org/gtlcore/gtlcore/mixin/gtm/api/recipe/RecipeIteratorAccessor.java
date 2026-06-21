package org.gtlcore.gtlcore.mixin.gtm.api.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeIterator;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Predicate;

@Mixin(RecipeIterator.class)
public interface RecipeIteratorAccessor {

    @Accessor(value = "index", remap = false)
    int getIndex();

    @Accessor(value = "index", remap = false)
    void setIndex(int index);

    @Invoker("<init>")
    static RecipeIterator newRecipeIterator(@NotNull GTRecipeType recipeMap, List<List<AbstractMapIngredient>> ingredients, @NotNull Predicate<GTRecipe> canHandle) {
        throw new AssertionError();
    }
}
