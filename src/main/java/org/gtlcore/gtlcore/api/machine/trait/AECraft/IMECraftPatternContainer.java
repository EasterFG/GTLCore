package org.gtlcore.gtlcore.api.machine.trait.AECraft;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.core.BlockPos;

public interface IMECraftPatternContainer {

    static int sumNonEmpty(IItemTransfer itemTransfer) {
        int sum = 0;
        for (int i = 0, n = itemTransfer.getSlots(); i < n; i++) {
            var stack = itemTransfer.getStackInSlot(i);
            if (!stack.isEmpty()) sum++;
        }
        return sum;
    }

    BlockPos getBlockPos();

    int getSlots();
}
