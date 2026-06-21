package org.gtlcore.gtlcore.mixin.gtm.api.recipe.condition;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.ICleanroomReceiver;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.condition.CleanroomCondition;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(CleanroomCondition.class)
public class CleanroomConditionMixin {

    @Shadow(remap = false)
    private CleanroomType cleanroom;

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        if (ConfigHolder.INSTANCE.machines.enableCleanroom) {
            MetaMachine machine = recipeLogic.getMachine();
            if (machine instanceof ICleanroomReceiver receiver) {
                if (this.cleanroom != null) {
                    if (ConfigHolder.INSTANCE.machines.cleanMultiblocks && machine instanceof IMultiController) return true;
                    ICleanroomProvider provider = receiver.getCleanroom();
                    if (provider != null && provider.isClean() && provider.getTypes().contains(this.cleanroom))
                        return true;
                    else {
                        RecipeResult.of((IRecipeLogicMachine) machine,
                                RecipeResult.fail(Component.translatable("gtceu.recipe.fail.cleanroom",
                                        Component.translatable(this.cleanroom.getTranslationKey()).getString())));
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
