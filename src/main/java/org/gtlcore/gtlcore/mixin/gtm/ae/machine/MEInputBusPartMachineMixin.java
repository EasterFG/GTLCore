package org.gtlcore.gtlcore.mixin.gtm.ae.machine;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;

import appeng.api.networking.IGridNodeListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MEInputBusPartMachine.class)
public abstract class MEInputBusPartMachineMixin extends MEBusPartMachine {

    @Shadow(remap = false)
    protected ExportOnlyAEItemList aeItemHandler;

    public MEInputBusPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, io, args);
    }

    @Inject(method = "autoIO",
            at = @At(value = "INVOKE",
                     target = "Lcom/gregtechceu/gtceu/integration/ae2/machine/MEInputBusPartMachine;syncME()V",
                     shift = At.Shift.AFTER),
            remap = false)
    public void autoIO(CallbackInfo ci) {
        if (aeItemHandler instanceof IMEPartMachine machine) {
            machine.setChanged(true);
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.@NotNull State reason) {
        super.onMainNodeStateChanged(reason);
        if (getMainNode().isOnline()) aeItemHandler.notifyListeners();
    }
}
