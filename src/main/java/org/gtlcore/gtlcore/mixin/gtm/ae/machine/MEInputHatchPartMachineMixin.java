package org.gtlcore.gtlcore.mixin.gtm.ae.machine;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;

import appeng.api.networking.IGridNodeListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MEInputHatchPartMachine.class)
public abstract class MEInputHatchPartMachineMixin extends MEHatchPartMachine {

    @Shadow(remap = false)
    protected ExportOnlyAEFluidList aeFluidHandler;

    public MEInputHatchPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, io, args);
    }

    @Inject(method = "autoIO",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEInputHatchPartMachine;syncME()V",
                     shift = At.Shift.AFTER),
            remap = false)
    public void autoIO(CallbackInfo ci) {
        if (aeFluidHandler instanceof IMEPartMachine machine) {
            machine.setChanged(true);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.@NotNull State reason) {
        super.onMainNodeStateChanged(reason);
        if (getMainNode().isOnline()) aeFluidHandler.notifyListeners();
    }
}
