package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.api.pattern.util.IValueContainer;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AdvancedAssemblyLineMachine extends WorkableElectricMultiblockMachine {

    public AdvancedAssemblyLineMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedAssemblyLineMachine.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int speed = 0;

    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, OCParams params, OCResult result) {
        if (machine instanceof AdvancedAssemblyLineMachine lineMachine) {
            GTRecipe recipe1 = GTRecipeModifiers.hatchParallel(machine, recipe, false, params, result);
            if (recipe1 == null) return null;
            recipe1.duration = Math.max(lineMachine.speed * recipe1.duration / 100, 1);
            return RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK,
                    recipe1, lineMachine.getOverclockVoltage(), params, result);
        }
        return null;
    }

    @Override
    public void onStructureFormed() {
        IValueContainer<?> container = getMultiblockState().getMatchContext()
                .getOrCreate("UnitValue", IValueContainer::noop);
        if (container.getValue() instanceof Integer integer) {
            speed = 100 - integer;
        }
        super.onStructureFormed();
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        textList.add(Component.translatable("gtceu.machine.duration_multiplier.tooltip", speed).append("%"));
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
