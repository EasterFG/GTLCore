package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMufflerMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MufflerPartMachine;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(MufflerPartMachine.class)
public abstract class MufflerPartMachineMixin implements IMufflerMachine {

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        GTRecipe modifiedRecipe = !this.isFrontFaceFree() ? null : recipe;
        if (modifiedRecipe == null) {
            RecipeResult.of((IRecipeLogicMachine) this.getControllers().get(0),
                    RecipeResult.fail(Component.translatable("gtceu.multiblock.universal.muffler_obstructed")));
        }
        return modifiedRecipe;
    }
}
