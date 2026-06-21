package org.gtlcore.gtlcore.api.machine.multiblock;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.common.machine.trait.MultipleRecipesLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CoilWorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;

import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

import static org.gtlcore.gtlcore.common.data.GTLRecipeModifiers.getHatchParallel;

/**
 * @author EasterFG on 2024/12/5
 */
public class CoilWorkableElectricMultipleRecipesMachine extends CoilWorkableElectricMultiblockMachine implements ParallelMachine {

    protected static final BiPredicate<CompoundTag, IRecipeLogicMachine> EBF_CHECK = (data, machine) -> {
        var tm = (CoilWorkableElectricMultiblockMachine) machine;
        var temp = tm.getCoilType().getCoilTemperature() + 100L * Math.max(0, tm.getTier() - GTValues.MV);
        if (temp > data.getInt("ebf_temp")) return true;
        else {
            RecipeResult.of(machine, RecipeResult.FAIL_NO_ENOUGH_TEMPERATURE);
            return false;
        }
    };

    public CoilWorkableElectricMultipleRecipesMachine(IMachineBlockEntity holder, Object @NotNull... args) {
        super(holder);
        if (args.length == 2 && args[0] instanceof Number reductionEUt && args[1] instanceof Number reductionDuration) {
            getRecipeLogic().setReduction(reductionEUt.doubleValue(), reductionDuration.doubleValue());
        }
    }

    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull... args) {
        return new MultipleRecipesLogic(this, EBF_CHECK);
    }

    @NotNull
    @Override
    public MultipleRecipesLogic getRecipeLogic() {
        return (MultipleRecipesLogic) super.getRecipeLogic();
    }

    public int getMaxParallel() {
        return getHatchParallel(this);
    }
}
