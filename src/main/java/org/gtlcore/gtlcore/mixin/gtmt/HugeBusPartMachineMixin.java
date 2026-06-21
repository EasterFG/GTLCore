package org.gtlcore.gtlcore.mixin.gtmt;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredIOPartMachine;

import com.hepdd.gtmthings.common.block.machine.multiblock.part.HugeBusPartMachine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HugeBusPartMachine.class)
public class HugeBusPartMachineMixin extends TieredIOPartMachine {

    public HugeBusPartMachineMixin(IMachineBlockEntity holder, int tier, IO io) {
        super(holder, tier, io);
    }

    @Inject(method = "getInventorySize", at = @At("RETURN"), remap = false, cancellable = true)
    protected void getInventorySize(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(cir.getReturnValue() - 1);
    }

    @Inject(method = "setDistinct", at = @At("RETURN"), remap = false)
    public void setDistinct(boolean isDistinct, CallbackInfo ci) {
        for (var controller : this.getControllers()) {
            if (controller instanceof IRecipeCapabilityMachine machine && !machine.isDistinct()) {
                machine.upDate();
            }
        }
    }
}
