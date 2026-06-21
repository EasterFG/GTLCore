package org.gtlcore.gtlcore.api.recipe;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IAdditionalRecipeIterator {

    void setAdditionalRecipes(@NotNull List<@NotNull GTRecipe> additionalRecipes);

    @Nullable
    List<GTRecipe> getAdditionalRecipes();

    void setUseDiveIngredientTreeFind(boolean useDiveIngredientTreeFind);
}
