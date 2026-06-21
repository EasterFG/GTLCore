package org.gtlcore.gtlcore.mixin.gtm.machine;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(LargeTurbineMachine.class)
public class LargeTurbineMachineMixin extends WorkableElectricMultiblockMachine {

    RotorHolderPartMachine rotorHolderPart;

    public LargeTurbineMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private @Nullable IRotorHolderMachine getRotorHolder() {
        return this.rotorHolderPart;
    }

    @Override
    public boolean onWorking() {
        if (rotorHolderPart == null || !rotorHolderPart.onWorking(this)) return false;
        return super.onWorking();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHatchPart) {
                rotorHolderPart = rotorHatchPart;
            }
        }
    }
}
