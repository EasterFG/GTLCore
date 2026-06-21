package org.gtlcore.gtlcore.common.machine.multiblock.noenergy;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.api.recipe.RecipeRunnerHelper;
import org.gtlcore.gtlcore.utils.MachineIO;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;

import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HeatExchangerMachine extends NoEnergyMultiblockMachine {

    public HeatExchangerMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof HeatExchangerMachine hMachine) {
            if (FluidRecipeCapability.CAP.of(recipe.inputs.get(FluidRecipeCapability.CAP)
                    .get(1).getContent()).getStacks()[0].getFluid() == GTMaterials.Water.getFluid()) {
                return GTRecipeModifiers.accurateParallel(machine, recipe, Integer.MAX_VALUE, false).getFirst();
            }

            // for recipe cache
            final var fluidIngredient = FluidRecipeCapability.CAP.of(recipe.inputs.get(FluidRecipeCapability.CAP).get(0).getContent());
            final var prefix = Objects.requireNonNull(ForgeRegistries.FLUID_TYPES.get().getKey(fluidIngredient.getStacks()[0].getFluid().getFluidType())).getPath();
            final GTRecipe plasmaRecipe = new GTRecipeBuilder(GTLCore.id("heat_exchanger_" + prefix), GTRecipeTypes.DUMMY_RECIPES)
                    .inputFluids(fluidIngredient)
                    .outputFluids(recipe.outputs.get(FluidRecipeCapability.CAP).stream().map(c -> FluidRecipeCapability.CAP.of(c.getContent())).toArray((FluidIngredient[]::new)))
                    .duration(200)
                    .buildRawRecipe();

            if (!RecipeRunnerHelper.matchRecipeInputNoMEInnerCache(hMachine, plasmaRecipe)) return null;

            final Pair<GTRecipe, Integer> result = GTRecipeModifiers.accurateParallel(machine, plasmaRecipe, Integer.MAX_VALUE, false);

            long count = result.getSecond() * FluidRecipeCapability.CAP.of(recipe.inputs.get(FluidRecipeCapability.CAP)
                    .get(1).getContent()).getStacks()[0].getAmount();

            if (MachineIO.inputFluid(hMachine, GTMaterials.DistilledWater.getFluid(count))) {
                return result.getFirst();
            } else RecipeResult.of((IRecipeLogicMachine) machine, RecipeResult.fail(Component.translatable("gtceu.recipe.fail.no.enough.distilledwater")));
        }
        return null;
    }
}
