package org.gtlcore.gtlcore.mixin.ae2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets = "appeng.me.cells.CreativeCellInventory")
public class CreativeCellInventoryMixin {

    @ModifyArg(method = "getAvailableStacks",
               at = @At(value = "INVOKE",
                        target = "Lappeng/api/stacks/KeyCounter;add(Lappeng/api/stacks/AEKey;J)V"),
               index = 1,
               remap = false)
    public long getAvailableStacks(long amount) {
        return Long.MAX_VALUE;
    }
}
