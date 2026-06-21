package org.gtlcore.gtlcore.api.machine.trait.MEPart;

import org.gtlcore.gtlcore.api.machine.trait.IMERecipeHandlerTrait;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.NotNull;

public interface IMEFilterIOPartMachine extends IMEIOPartMachine {

    Pair<IMERecipeHandlerTrait<Ingredient, ItemStack>, IMERecipeHandlerTrait<FluidIngredient, FluidStack>> getMERecipeHandlerTraits();

    @NotNull
    IMEFilterIOTrait getMETrait();
}
