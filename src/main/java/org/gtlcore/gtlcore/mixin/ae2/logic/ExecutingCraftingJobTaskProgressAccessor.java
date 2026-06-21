package org.gtlcore.gtlcore.mixin.ae2.logic;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "appeng.crafting.execution.ExecutingCraftingJob$TaskProgress")
public interface ExecutingCraftingJobTaskProgressAccessor {

    @Accessor(value = "value", remap = false)
    long getValue();

    @Accessor(value = "value", remap = false)
    void setValue(long v);
}
