package org.gtlcore.gtlcore.mixin.ae2;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.integration.ae2.storage.FastInfinityCellInventory;
import org.gtlcore.gtlcore.integration.ae2.storage.InfinityCellDataStorage;
import org.gtlcore.gtlcore.integration.ae2.storage.InfinityCellInventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(AbstractContainerMenu.class)
public abstract class CursedInternalSlotMixin {

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    public abstract void setCarried(ItemStack stack);

    @Inject(method = "doClick", at = @At(value = "INVOKE", target = "net/minecraft/world/item/ItemStack.copyWithCount (I)Lnet/minecraft/world/item/ItemStack;"), slice = @Slice(from = @At(value = "INVOKE", target = "net/minecraft/world/inventory/Slot.hasItem()Z", ordinal = 1)), cancellable = true)
    public void doClick(int slotIndex, int button, ClickType actionType, Player player, CallbackInfo ci) {
        Slot i = slots.get(slotIndex);
        if (InfinityCellInventory.hasDiskUUID(i.getItem())) {
            InfinityCellDataStorage storage = GTLCore.STORAGE_INSTANCE
                    .getOrCreateDisk(i.getItem().getOrCreateTag().getUUID("diskuuid"), false);
            ItemStack newStack = new ItemStack(i.getItem().getItem());
            UUID id = UUID.randomUUID();
            newStack.getOrCreateTag().putUUID("diskuuid", id);
            newStack.getOrCreateTag().putLong("count", Double.doubleToLongBits(storage.totalAmount));
            GTLCore.STORAGE_INSTANCE.updateDisk(id, storage);
            newStack.setCount(newStack.getMaxStackSize());
            setCarried(newStack);
            ci.cancel();
        } else if (FastInfinityCellInventory.hasDiskUUID(i.getItem())) {
            InfinityCellDataStorage storage = GTLCore.STORAGE_INSTANCE
                    .getOrCreateDisk(i.getItem().getOrCreateTag().getUUID("diskuuid"), true);
            ItemStack newStack = new ItemStack(i.getItem().getItem());
            UUID id = UUID.randomUUID();
            newStack.getOrCreateTag().putUUID("diskuuid", id);
            newStack.getOrCreateTag().putLong("count", Double.doubleToLongBits(storage.totalAmount));
            GTLCore.STORAGE_INSTANCE.updateDisk(id, storage);
            newStack.setCount(newStack.getMaxStackSize());
            setCarried(newStack);
            ci.cancel();
        }
    }
}
