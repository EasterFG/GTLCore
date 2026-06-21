package org.gtlcore.gtlcore.api.gui;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;

import net.minecraft.network.chat.Component;

public class ExtendLabelWidget extends LabelWidget {

    public ExtendLabelWidget(int xPosition, int yPosition, Component component) {
        super(xPosition, yPosition, component);
        this.setText(component.getString());
    }
}
