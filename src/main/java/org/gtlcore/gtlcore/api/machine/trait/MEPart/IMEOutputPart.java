package org.gtlcore.gtlcore.api.machine.trait.MEPart;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IMEOutputPart {

    int ME_UPDATE_INTERVAL = ConfigHolder.INSTANCE.compat.ae2.updateIntervals;

    default boolean isReturn() {
        return this.getTime() % IMEOutputPart.ME_UPDATE_INTERVAL == 0;
    }

    byte getTime();

    void setTime(byte time);

    void returnStorage();

    static void attachRecipeLockable(ConfiguratorPanel configuratorPanel, MetaMachine machine) {
        if (machine instanceof IMEOutputPart part) {
            configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                    GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubTexture(0, 0, 1, 0.5),
                    GuiTextures.BUTTON_ALLOW_IMPORT_EXPORT.getSubTexture(0, 0.5, 1, 0.5),
                    part::isReturn, (clickData, pressed) -> part.returnStorage())
                    .setTooltipsSupplier(pressed -> List.of(Component.translatable("config.gtceu.option.hand.output"))));
        }
    }
}
