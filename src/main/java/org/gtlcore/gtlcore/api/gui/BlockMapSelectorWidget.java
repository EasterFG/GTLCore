package org.gtlcore.gtlcore.api.gui;

import org.gtlcore.gtlcore.common.block.BlockMap;

import com.gregtechceu.gtceu.api.gui.GuiTextures;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.function.*;

import static net.minecraft.network.chat.Component.translatable;

public class BlockMapSelectorWidget extends WidgetGroup {

    public static final Component SC = translatable("gui.gtlcore.stellar_thermal_container");
    public static final Component SEPM = translatable("gui.gtlcore.space_elevator_module");
    public static final Component CAL = translatable("gui.gtlcore.component_assembly_casing");
    public static final Component COIL = translatable("gui.gtlcore.coil");

    private final BiConsumer<String, Integer> onChanged;
    private List<Block> blocks;
    private String currentType;

    public BlockMapSelectorWidget(int y, int width, BiConsumer<String, Integer> onChanged) {
        super(0, y, width, 56);
        this.onChanged = onChanged;
        this.setVisible(false);
    }

    public static Component getBlock(String string) {
        return switch (string) {
            case "sc" -> SC;
            case "sepm" -> SEPM;
            case "cal" -> CAL;
            case "coil" -> COIL;
            default -> throw new IllegalStateException("Unexpected value: " + string);
        };
    }

    public void setInit(ItemStack itemStack) {
        var tag = itemStack.getOrCreateTag();
        var block = tag.getString("blocks");
        if (!block.isEmpty()) {
            this.blocks = Arrays.stream(BlockMap.tierBlockMap.get(block).get()).toList();
            this.currentType = block;
        }
    }

    public void showType(boolean isShow) {
        if (!isShow) this.setVisible(false);
        else {
            WidgetGroup group = new WidgetGroup(0, 2, 80, 52);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            var blockType = new DraggableScrollableWidgetGroup(4, 4, 72, 44);
            blockType.setYScrollBarWidth(2)
                    .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F))
                    .setBackground(GuiTextures.DISPLAY);
            int y = 0;
            for (var key : BlockMap.tierBlockMap.keySet()) {
                blockType.addWidget((new WidgetGroup(2, 2 + y, 66, 15))
                        .addWidget(new ExtendLabelWidget(0, 0, getBlock(key)))
                        .addWidget(new ButtonWidget(0, 0, 64, 15, (cd) -> {
                            showTier(Arrays.stream(BlockMap.tierBlockMap.get(key).get()).toList());
                            currentType = key;
                        })));
                y += 15;
            }
            this.addWidget(group.addWidget(blockType));
            this.setVisible(true);
            if (this.blocks != null && !this.blocks.isEmpty()) showTier(this.blocks);
        }
    }

    public void showTier(List<Block> blocks) {
        if (!blocks.isEmpty()) {
            this.blocks = blocks;
            WidgetGroup group = new WidgetGroup(80, 2, 120, 52);
            group.setBackground(GuiTextures.BACKGROUND_INVERSE);
            var blockTier = new DraggableScrollableWidgetGroup(4, 4, 112, 44);
            blockTier.setYScrollBarWidth(2)
                    .setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1.0F))
                    .setBackground(GuiTextures.DISPLAY);
            for (int i = 0; i < blocks.size(); i++) {
                int finalI = i;
                var block = blocks.get(finalI);
                blockTier.addWidget(new WidgetGroup(2, 2 + i * 20, 110, 22)
                        .addWidget(new ImageWidget(2, 0, 18, 18,
                                new ItemStackTexture(block.asItem().getDefaultInstance())))
                        .addWidget(new ExtendLabelWidget(20, 4, block.getName()))
                        .addWidget(new ButtonWidget(20, 0, 88, 18, (cd) -> {
                            if (onChanged != null) onChanged.accept(currentType, finalI);
                        })));
            }
            this.addWidget(group.addWidget(blockTier));
        }
    }
}
