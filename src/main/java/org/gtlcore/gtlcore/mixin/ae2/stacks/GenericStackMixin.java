package org.gtlcore.gtlcore.mixin.ae2.stacks;

import net.minecraft.nbt.CompoundTag;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(GenericStack.class)
public abstract class GenericStackMixin {

    /**
     * @author Dragons
     * @reason 修复序列化问题
     */
    @Overwrite(remap = false)
    public static CompoundTag writeTag(@Nullable GenericStack stack) {
        if (stack == null) {
            return new CompoundTag();
        } else {
            CompoundTag tag = stack.what().toTagGeneric().copy();
            tag.putLong("#", stack.amount());
            return tag;
        }
    }
}
