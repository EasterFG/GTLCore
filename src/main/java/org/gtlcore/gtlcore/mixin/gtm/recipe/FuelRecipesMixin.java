package org.gtlcore.gtlcore.mixin.gtm.recipe;

import com.gregtechceu.gtceu.data.recipe.misc.FuelRecipes;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * @author EasterFG on 2024/10/18
 */
@Mixin(FuelRecipes.class)
public class FuelRecipesMixin {

    @ModifyArg(method = "init",
               at = @At(value = "INVOKE",
                        target = "Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;duration(I)Lcom/gregtechceu/gtceu/data/recipe/builder/GTRecipeBuilder;",
                        ordinal = 1),
               remap = false)
    private static int fix(int duration) {
        if (duration < 0) return Integer.MAX_VALUE;
        return duration;
    }
}
