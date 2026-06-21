package org.gtlcore.gtlcore.mixin.gtm.ae.slot;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;

import net.minecraft.world.item.ItemStack;

import appeng.api.stacks.AEItemKey;
import com.google.common.primitives.Ints;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ExportOnlyAEItemSlot.class)
public abstract class ExportOnlyAEItemSlotMixin extends ExportOnlyAESlot {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0 && this.stock != null) {
            return this.stock.what() instanceof AEItemKey itemKey ?
                    itemKey.toStack(Ints.saturatedCast(this.stock.amount())) :
                    ItemStack.EMPTY;
        }
        return ItemStack.EMPTY;
    }
}
