package org.gtlcore.gtlcore.common.item;

import org.gtlcore.gtlcore.common.machine.multiblock.part.ae.MEPatternBufferPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class MEPatternBufferCopyBehavior extends TooltipBehavior implements IInteractionItem {

    public static final MEPatternBufferCopyBehavior INSTANCE = new MEPatternBufferCopyBehavior((list -> {
        list.add(Component.translatable("tooltip.gtlcore.copy_pattern_buffer_sneak_right_click"));
        list.add(Component.translatable("tooltip.gtlcore.apply_pattern_buffer_right_click"));
    }));

    public MEPatternBufferCopyBehavior(@NotNull Consumer<List<Component>> tooltips) {
        super(tooltips);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack itemStack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity machineBlock) {
            if (machineBlock.getMetaMachine() instanceof MEPatternBufferPartMachine partMachine) {
                if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                    var tags = getBehaviorsTag(itemStack);
                    if (!serverPlayer.isShiftKeyDown()) {
                        if (tags.isEmpty()) serverPlayer.displayClientMessage(Component.translatable("message.gtlcore.pattern_buffer_not_copied"), true);
                        else partMachine.copyFromTag(tags.getCompound("tag"), serverPlayer);
                    } else {
                        tags = partMachine.copyToTag(tags);
                        if (tags.isEmpty()) serverPlayer.displayClientMessage(Component.translatable("message.gtlcore.pattern_buffer_copy_failed"), true);
                        else serverPlayer.displayClientMessage(Component.translatable("message.gtlcore.pattern_buffer_copied"), true);
                    }
                }
            }
        }
        return InteractionResult.PASS;
    }
}
