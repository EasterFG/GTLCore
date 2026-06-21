package org.gtlcore.gtlcore.mixin.gtm.ae.machine;

import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;

import appeng.api.networking.security.IActionSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MEHatchPartMachine.class)
public interface MEHatchPartMachineAccessor {

    @Accessor(value = "actionSource", remap = false)
    IActionSource getActionSource();
}
