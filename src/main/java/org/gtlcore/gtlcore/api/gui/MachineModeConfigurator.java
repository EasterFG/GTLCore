package org.gtlcore.gtlcore.api.gui;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.network.FriendlyByteBuf;

public class MachineModeConfigurator extends WidgetGroup {

    protected IRecipeLogicMachine machine;

    public MachineModeConfigurator(int x, int y, int width, int height, IRecipeLogicMachine machine) {
        super(x, y, width, height);
        this.machine = machine;
    }

    @Override
    public void writeInitialData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(machine.getActiveRecipeType());
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        machine.setActiveRecipeType(buffer.readVarInt());
    }

    @Override
    public void detectAndSendChanges() {
        this.writeUpdateInfo(0, buf -> buf.writeVarInt(machine.getActiveRecipeType()));
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) machine.setActiveRecipeType(buffer.readVarInt());
    }
}
