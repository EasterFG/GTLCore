package org.gtlcore.gtlcore.api.machine.trait.AECraft;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public interface IMolecularAssemblerHandler {

    void handleRecipeOutput(GTRecipe recipe);

    /**
     * extract from MolecularAssemblerSupportedPatterns, limited by parallelAmount
     */
    GTRecipe extractGTRecipe(long parallelAmount, int tickDuration);
}
