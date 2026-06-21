package org.gtlcore.gtlcore.mixin.gtm;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.common.recipe.condition.RockBreakerCondition;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RockBreakerCondition.class)
public abstract class RockBreakerConditionMixin extends RecipeCondition {

    /**
     * @author
     * @reason
     */
    @Overwrite(remap = false)
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var fluidA = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidA")));
        var fluidB = BuiltInRegistries.FLUID.get(new ResourceLocation(recipe.data.getString("fluidB")));
        boolean hasFluidA = false, hasFluidB = false;
        if (recipeLogic.machine instanceof WorkableElectricMultiblockMachine MMachine) {
            var handlers = MMachine.getCapabilitiesProxy().get(IO.IN, FluidRecipeCapability.CAP);
            if (handlers == null) return false;
            for (com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler<?> handler : handlers) {
                if (handler instanceof NotifiableFluidTank tank) {
                    if (tank.getFluidInTank(0).getFluid() == fluidA) hasFluidA = true;
                    if (tank.getFluidInTank(0).getFluid() == fluidB) hasFluidB = true;
                    if (hasFluidA && hasFluidB) return true;
                    else RecipeResult.of(recipeLogic.machine, RecipeResult.FAIL_LACK_FLUID);
                }
            }
        } else {
            var level = recipeLogic.machine.self().getLevel();
            var pos = recipeLogic.machine.self().getPos();
            for (Direction side : GTUtil.DIRECTIONS) {
                if (side.getAxis() != Direction.Axis.Y) {
                    var fluid = level.getFluidState(pos.relative(side));
                    if (fluid.getType() == fluidA) hasFluidA = true;
                    if (fluid.getType() == fluidB) hasFluidB = true;
                    if (hasFluidA && hasFluidB) return true;
                    else RecipeResult.of(recipeLogic.machine, RecipeResult.FAIL_LACK_FLUID);
                }
            }
        }
        return false;
    }
}
