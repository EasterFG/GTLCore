package org.gtlcore.gtlcore.common.item;

import org.gtlcore.gtlcore.api.gui.BlockMapSelectorWidget;
import org.gtlcore.gtlcore.api.gui.ExtendLabelWidget;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.*;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.*;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.*;

import com.hepdd.gtmthings.api.gui.widget.TerminalInputWidget;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static net.minecraft.network.chat.Component.translatable;
import static org.gtlcore.gtlcore.api.gui.BlockMapSelectorWidget.*;
import static org.gtlcore.gtlcore.api.pattern.AdvancedBlockPattern.getAdvancedBlockPattern;
import static org.gtlcore.gtlcore.common.block.BlockMap.*;

/**
 * 代码参考自gtmthings
 * &#064;line <a href="https://github.com/liansishen/GTMThings">...</a>
 */

public class UltimateTerminalBehavior implements IItemUIFactory {

    public UltimateTerminalBehavior() {}

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            var level = context.getLevel();
            var blockPos = context.getClickedPos();
            var metaMachine = MetaMachine.getMachine(level, blockPos);
            if (context.getPlayer() != null && !level.isClientSide() &&
                    metaMachine instanceof IMultiController controller) {
                var autoBuildSetting = getAutoBuildSetting(context.getPlayer().getMainHandItem());

                if (!controller.isFormed()) {
                    getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                } else if (metaMachine instanceof WorkableMultiblockMachine machine && autoBuildSetting.isReplaceMode()) {
                    getAdvancedBlockPattern(controller.getPattern()).autoBuild(context.getPlayer(), controller.getMultiblockState(), autoBuildSetting);
                    machine.onPartUnload();
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private AutoBuildSetting getAutoBuildSetting(ItemStack mainHandItem) {
        var autoBuildSetting = new AutoBuildSetting();
        var tag = mainHandItem.getOrCreateTag();
        if (!tag.isEmpty()) {
            autoBuildSetting.setTier(tag.getInt("Tier"));
            autoBuildSetting.setRepeatCount(tag.getInt("RepeatCount"));
            autoBuildSetting.setNoHatchMode(tag.getBoolean("NoHatchMode"));
            autoBuildSetting.setReplaceMode(tag.getBoolean("ReplaceMode"));
            autoBuildSetting.setFlipped(tag.getBoolean("IsFlipped"));
            String block = tag.getString("blocks");
            if (!block.isEmpty()) {
                autoBuildSetting.tierBlock = tierBlockMap.get(block).get();
                autoBuildSetting.blocks = new ObjectOpenHashSet<>(autoBuildSetting.tierBlock);
            }
        }
        return autoBuildSetting;
    }

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder heldItemHolder, Player player) {
        return (new ModularUI(166, 166, heldItemHolder, player)).widget(this.createWidget(player));
    }

    private Widget createWidget(Player player) {
        final var handItem = player.getMainHandItem();
        WidgetGroup group = new WidgetGroup(0, 0, 200, 136);
        var contain = new DraggableScrollableWidgetGroup(4, 4, 192, 128)
                .setBackground(GuiTextures.DISPLAY).setYScrollBarWidth(2)
                .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F));
        contain.addWidget(new ExtendLabelWidget(65, 8, translatable("gui.gtlcore.ultimate_terminal_settings")))
                .addWidget(new ExtendLabelWidget(14, 26, translatable("gui.gtlcore.tier_blocks")))
                .addWidget(new ExtendLabelWidget(14, 46, translatable("gui.gtlcore.repeat_count")))
                .addWidget(new ExtendLabelWidget(14, 66, translatable("gui.gtlcore.no_hatch_mode"))
                        .setHoverTooltips(translatable("tooltip.gtlcore.no_hatch_mode")))
                .addWidget(new ExtendLabelWidget(14, 86, translatable("gui.gtlcore.replace_mode"))
                        .setHoverTooltips(translatable("tooltip.gtlcore.replace_mode")))
                .addWidget(new ExtendLabelWidget(14, 106, translatable("gui.gtlcore.mirror_mode"))
                        .setHoverTooltips(translatable("tooltip.gtlcore.mirror_mode")))
                .addWidget(new TerminalInputWidget(140, 45, 36, 12,
                        () -> getRepeatCount(handItem), (v) -> setRepeatCount(v, handItem)).setMax(648).setMin(0))
                .addWidget(new SwitchWidget(140, 63, 36, 14,
                        (c, b) -> setIsBuildHatches(!getIsBuildHatches(handItem), handItem)).setPressed(getIsBuildHatches(handItem))
                        .setTexture(new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("OFF")),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ON"))))
                .addWidget(new SwitchWidget(140, 83, 36, 14,
                        (c, b) -> setReplaceMode(!getReplaceMode(handItem), handItem)).setPressed(getReplaceMode(handItem))
                        .setTexture(new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("OFF")),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ON"))))
                .addWidget(new SwitchWidget(140, 103, 36, 14,
                        (c, b) -> setIsFlip(!getIsFlip(handItem), handItem)).setPressed(getIsFlip(handItem))
                        .setTexture(new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("OFF")),
                                new GuiTextureGroup(GuiTextures.BUTTON, new TextTexture("ON"))));
        var blockLabel = new ExtendLabelWidget(47, 26, getBlockComponent(handItem));
        var blockMap = new BlockMapSelectorWidget(group.getSizeHeight() + 4, contain.getSizeWidth(), (s, i) -> {
            if (s != null && i != null) {
                CompoundTag tag = handItem.getOrCreateTag();
                tag.putString("blocks", s);
                tag.putInt("Tier", i);
                handItem.setTag(tag);
                blockLabel.setComponent(Component.literal("(").append(getBlock(s))
                        .append(Component.literal(" : "))
                        .append(tierBlockMap.get(s).get()[i].getName())
                        .append(Component.literal(")")));
            }
        });
        blockMap.setInit(handItem);
        var open = new SwitchWidget(14, 26, 30, 16, (c, f) -> blockMap.showType(f))
                .setHoverTooltips(Component.translatable("gui.gtlcore.open.config.map"));
        contain.addWidget(open).addWidget(blockLabel);
        group.addWidget(contain).addWidget(blockMap).setBackground(GuiTextures.BACKGROUND_INVERSE);
        return group;
    }

    private static Component getBlockComponent(ItemStack itemStack) {
        var tag = itemStack.getOrCreateTag();
        if (!tag.isEmpty()) {
            var block = tag.getString("blocks");
            if (!block.isEmpty()) {
                int tier = tag.getInt("Tier");
                return Component.literal("(").append(getBlock(block))
                        .append(Component.literal(" : "))
                        .append(tierBlockMap.get(block).get()[tier].getName())
                        .append(Component.literal(")"));
            }
        }
        return Component.literal("");
    }

    private static int getRepeatCount(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() ? tag.getInt("RepeatCount") : 0;
    }

    private static void setRepeatCount(int repeatCount, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putInt("RepeatCount", repeatCount);
        itemStack.setTag(tag);
    }

    private static boolean getIsBuildHatches(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return tag.isEmpty() || tag.getBoolean("NoHatchMode");
    }

    private static void setIsBuildHatches(boolean isBuildHatches, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("NoHatchMode", isBuildHatches);
        itemStack.setTag(tag);
    }

    private static boolean getReplaceMode(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("ReplaceMode");
    }

    private static void setReplaceMode(boolean isReplace, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("ReplaceMode", isReplace);
        itemStack.setTag(tag);
    }

    private static boolean getIsFlip(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        return !tag.isEmpty() && tag.getBoolean("IsFlipped");
    }

    private static void setIsFlip(boolean isFlip, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        tag.putBoolean("IsFlipped", isFlip);
        itemStack.setTag(tag);
    }

    @Setter
    @Getter
    public static class AutoBuildSetting {

        Block[] tierBlock;
        Set<Block> blocks = Collections.emptySet();
        private int tier, repeatCount;
        private boolean noHatchMode, replaceMode, isFlipped;

        public AutoBuildSetting() {
            this.tier = 0;
            this.repeatCount = 0;
            this.noHatchMode = true;
            this.replaceMode = false;
            this.isFlipped = false;
        }

        public List<ItemStack> apply(BlockInfo[] blockInfos) {
            List<ItemStack> candidates = new ObjectArrayList<>();
            if (blockInfos != null) {
                for (var info : blockInfos) {
                    if (this.tierBlock != null && this.tier >= 0 && blockInfos.length > 1 &&
                            this.blocks.contains(info.getBlockState().getBlock())) {
                        candidates.add(tierBlock[Math.min(this.tier, blockInfos.length - 1)].asItem().getDefaultInstance());
                        return candidates;
                    }
                    if (info.getBlockState().getBlock() != Blocks.AIR) candidates.add(info.getItemStackForm());
                }
            }
            return candidates;
        }

        public boolean isPlaceHatch(BlockInfo[] blockInfos) {
            if (!this.noHatchMode) return true;
            if (blockInfos != null && blockInfos.length > 0) {
                var blockInfo = blockInfos[0];
                return !(blockInfo.getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) ||
                        !Hatch.Set.contains(machineBlock);
            }
            return true;
        }

        private static final class Hatch {

            public static final Set<Block> Set = new ObjectOpenHashSet<>();

            static {
                GTRegistries.MACHINES.forEach(d -> {
                    if (d.getRecipeTypes() != null || d instanceof MultiblockMachineDefinition) return;
                    var block = d.getBlock();
                    if (d.createMetaMachine((IMachineBlockEntity) d.getBlockEntityType().create(BlockPos.ZERO, block.defaultBlockState())) instanceof MultiblockPartMachine) Set.add(block);
                });
            }
        }
    }
}
