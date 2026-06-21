package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.machine.trait.AECraft.IMECraftPatternContainer;
import org.gtlcore.gtlcore.integration.ae2.AEUtils;
import org.gtlcore.gtlcore.integration.ae2.widget.AEPatternViewExtendSlotWidget;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import appeng.crafting.pattern.EncodedPatternItem;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class MECraftPatternContainerPartMachine extends MultiblockPartMachine implements IMECraftPatternContainer, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MECraftPatternContainerPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    private static final int PATTERNS_PER_ROW = 9;

    @Getter
    @Persisted
    protected final ItemStackTransfer patternInventory;

    @DescSynced
    @Persisted
    private boolean shouldOpen = true;

    public MECraftPatternContainerPartMachine(IMachineBlockEntity holder) {
        super(holder);
        patternInventory = new ItemStackTransfer(12 * PATTERNS_PER_ROW);
        patternInventory.setFilter(itemStack -> AEUtils.molecularFilter(itemStack, getLevel()));
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public BlockPos getBlockPos() {
        return getPos();
    }

    @Override
    public int getSlots() {
        return this.patternInventory.getSlots();
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(patternInventory);
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        shouldOpen = true;
    }

    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        shouldOpen = false;
    }

    @Override
    public @NotNull Widget createUIWidget() {
        final int cowSize = 14;
        final int totalCount = patternInventory.getSlots();
        final int rowSize = (int) Math.ceil((double) totalCount / cowSize);
        var group = new WidgetGroup(0, 0, 18 * cowSize + 16, 18 * rowSize + 16);

        int index = 0;
        for (int y = 0; y < rowSize; ++y) {
            for (int x = 0; x < cowSize; ++x) {
                if (index >= totalCount) break;
                var slot = new AEPatternViewExtendSlotWidget(patternInventory, index++, x * 18 + 8, y * 18 + 14)
                        .setOccupiedTexture(GuiTextures.SLOT)
                        .setItemHook(stack -> {
                            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem iep) {
                                final ItemStack out = iep.getOutput(stack);
                                if (!out.isEmpty()) return out;
                            }
                            return stack;
                        })
                        .setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                group.addWidget(slot);
            }
        }
        return group;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return shouldOpen;
    }

    @Override
    public boolean canShared() {
        return false;
    }
}
