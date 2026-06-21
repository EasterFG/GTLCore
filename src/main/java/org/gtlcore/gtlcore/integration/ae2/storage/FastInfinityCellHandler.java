package org.gtlcore.gtlcore.integration.ae2.storage;

import org.gtlcore.gtlcore.integration.ae2.FastInfinityCell;
import org.gtlcore.gtlcore.utils.NumberUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import appeng.api.storage.cells.ICellHandler;
import appeng.api.storage.cells.ISaveProvider;

import java.util.List;

public class FastInfinityCellHandler implements ICellHandler {

    public static final FastInfinityCellHandler INSTANCE = new FastInfinityCellHandler();

    @Override
    public boolean isCell(ItemStack is) {
        return is.getItem() instanceof FastInfinityCell;
    }

    @Override
    public FastInfinityCellInventory getCellInventory(ItemStack is, ISaveProvider container) {
        return FastInfinityCellInventory.createInventory(is, container);
    }

    public void addCellInformationToTooltip(ItemStack stack, List<Component> lines) {
        FastInfinityCellInventory handler = getCellInventory(stack, null);
        if (handler != null && handler.hasDiskUUID()) {
            lines.add(Component.literal("UUID: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(handler.getDiskUUID().toString()).withStyle(ChatFormatting.AQUA)));
            lines.add(Component.literal("Byte: ").withStyle(ChatFormatting.GRAY)
                    .append(NumberUtils.numberText(handler.getNbtItemCount()).withStyle(ChatFormatting.GREEN)));
        }
    }
}
