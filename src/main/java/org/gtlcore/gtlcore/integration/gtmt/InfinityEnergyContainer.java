package org.gtlcore.gtlcore.integration.gtmt;

import org.gtlcore.gtlcore.api.capability.IInt128EnergyContainer;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InfinityEnergyContainer extends NotifiableEnergyContainer implements IInt128EnergyContainer {

    public InfinityEnergyContainer(MetaMachine machine, long maxCapacity, long maxInputVoltage, long maxInputAmperage, long maxOutputVoltage, long maxOutputAmperage) {
        super(machine, maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, @Nullable String slotName, boolean simulate) {
        return null;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long oldEnergyStored = getEnergyStored();
        long newEnergyStored = (getEnergyCapacity() - oldEnergyStored < energyToAdd) ? getEnergyCapacity() : (oldEnergyStored + energyToAdd);
        if (newEnergyStored < 0) newEnergyStored = 0;
        final long change = newEnergyStored - oldEnergyStored;
        addEnergyPerSec(change);
        return change;
    }

    @Override
    public void checkOutputSubscription() {}

    @Override
    public void serverTick() {}

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        return 0;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return false;
    }

    @Override
    public long getEnergyStored() {
        return this.getEnergyCapacity();
    }
}
