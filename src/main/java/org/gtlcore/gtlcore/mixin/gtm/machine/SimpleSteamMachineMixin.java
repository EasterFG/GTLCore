package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.steam.SimpleSteamMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.recipe.condition.VentCondition;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SimpleSteamMachine.class)
public class SimpleSteamMachineMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static @Nullable GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params, @NotNull OCResult result) {
        if (machine instanceof SimpleSteamMachine steamMachine) {
            if (RecipeHelper.getRecipeEUtTier(recipe) <= 1 && steamMachine.checkVenting()) {
                GTRecipe modified = recipe.copy();
                modified.conditions.add(VentCondition.INSTANCE);
                if (steamMachine.isHighPressure) {
                    result.init(RecipeHelper.getInputEUt(recipe) * 2L, modified.duration, params.getOcAmount());
                } else {
                    result.init(RecipeHelper.getInputEUt(recipe), modified.duration * 2, params.getOcAmount());
                }
                return modified;
            }
            if (!steamMachine.checkVenting()) {
                RecipeResult.of(steamMachine, RecipeResult.fail(Component.translatable("gtceu.recipe.fail.no.venting")));
            } else if (RecipeHelper.getRecipeEUtTier(recipe) > 1) {
                RecipeResult.of(steamMachine, RecipeResult.FAIL_VOLTAGE_TIER);
            }
        }
        return null;
    }
}
