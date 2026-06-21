package org.gtlcore.gtlcore.mixin.gtm.gui;

import org.gtlcore.gtlcore.api.gui.MachineModeConfigurator;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.MachineModeFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.widget.*;

import org.spongepowered.asm.mixin.*;

@Mixin(MachineModeFancyConfigurator.class)
public class MachineModeFancyConfiguratorMixin {

    @Shadow(remap = false)
    protected IRecipeLogicMachine machine;

    /**
     * @author .
     * @reason 配方类型选择界面调整
     */
    @Overwrite(remap = false)
    public Widget createMainPage(FancyMachineUIWidget widget) {
        int length = machine.getRecipeTypes().length;
        var group = new MachineModeConfigurator(0, 0, 140, 20 * Math.min(length, 6) + 4, machine);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        if (length > 6) {
            DraggableScrollableWidgetGroup widgetGroup = new DraggableScrollableWidgetGroup(2, 2, 136, 6 * 20);
            addWidgets(widgetGroup, length);
            group.addWidget(widgetGroup);
        } else addWidgets(group, length);
        return group;
    }

    private void addWidgets(WidgetGroup group, int length) {
        for (int i = 0; i < length; i++) {
            int finalI = i;
            group.addWidget(new ButtonWidget(length < 7 ? 2 : 0, (length < 7 ? 2 : 0) + i * 20, 136, 20, IGuiTexture.EMPTY,
                    cd -> machine.setActiveRecipeType(finalI)));
            group.addWidget(new ImageWidget(length < 7 ? 2 : 0, (length < 7 ? 2 : 0) + i * 20, 136, 20,
                    () -> new GuiTextureGroup(
                            ResourceBorderTexture.BUTTON_COMMON.copy()
                                    .setColor(machine.getActiveRecipeType() == finalI ? ColorPattern.CYAN.color : -1),
                            new TextTexture(machine.getRecipeTypes()[finalI].registryName.toLanguageKey()).setWidth(136)
                                    .setType(TextTexture.TextType.ROLL))));

        }
    }
}
