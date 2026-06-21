package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.capability.IInt128EnergyContainer;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.ActiveTransformerMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ActiveTransformerMachine.class)
public abstract class ActiveTransformerMachineMixin extends WorkableElectricMultiblockMachine {

    @Shadow(remap = false)
    private IEnergyContainer powerOutput;
    @Shadow(remap = false)
    private IEnergyContainer powerInput;

    public ActiveTransformerMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author Dragons
     * @reason Fix overflow
     */
    @Overwrite(remap = false)
    protected boolean isSubscriptionActive() {
        if (!isFormed()) return false;

        if (powerInput == null || powerInput.getEnergyStored() <= 0) return false;
        if (powerOutput == null) return false;
        return ((IInt128EnergyContainer) powerOutput).getInt128EnergyStored().compareTo(((IInt128EnergyContainer) powerOutput).getInt128EnergyCapacity()) < 0;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        // super.addDisplayText(textList); idek what it does stop doing what you do for a minute pls
        // Assume That the Structure is ALWAYS formed, and has at least 1 In and 1 Out, there is never a case where this
        // does not occur.
        if (isFormed()) {
            if (!isWorkingEnabled()) {
                textList.add(Component.translatable("gtceu.multiblock.work_paused"));
            } else if (isActive()) {
                textList.add(Component.translatable("gtceu.multiblock.running"));
                textList.add(Component
                        .translatable("gtceu.multiblock.active_transformer.max_input",
                                FormattingUtil.formatNumbers(
                                        Math.abs(powerInput.getInputVoltage() * powerInput.getInputAmperage()))));
                textList.add(Component
                        .translatable("gtceu.multiblock.active_transformer.max_output",
                                FormattingUtil.formatNumbers(
                                        Math.abs(powerOutput.getOutputVoltage() * powerOutput.getOutputAmperage()))));
                textList.add(Component
                        .translatable("gtceu.multiblock.active_transformer.average_in", ((IInt128EnergyContainer) powerInput).getInt128InputPerSec().divide(20).toFormattedString()));
                textList.add(Component
                        .translatable("gtceu.multiblock.active_transformer.average_out", ((IInt128EnergyContainer) powerOutput).getInt128OutputPerSec().divide(20).toFormattedString()));
                if (!ConfigHolder.INSTANCE.machines.harmlessActiveTransformers) {
                    textList.add(Component
                            .translatable("gtceu.multiblock.active_transformer.danger_enabled"));
                }
            } else {
                textList.add(Component.translatable("gtceu.multiblock.idling"));
            }
        }
    }
}
