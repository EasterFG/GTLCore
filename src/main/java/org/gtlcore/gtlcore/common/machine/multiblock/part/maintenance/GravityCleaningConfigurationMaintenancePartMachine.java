package org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;

import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

/**
 * @author EasterFG on 2024/10/17
 */
public class GravityCleaningConfigurationMaintenancePartMachine extends AutoConfigurationMaintenanceHatchPartMachine implements IGravityPartMachine {

    @Persisted
    private int gravity = 0;

    ICleanroomProvider cleanroomTypes;

    public GravityCleaningConfigurationMaintenancePartMachine(IMachineBlockEntity blockEntity) {
        super(blockEntity);
    }

    public GravityCleaningConfigurationMaintenancePartMachine(IMachineBlockEntity blockEntity, ICleanroomProvider cleanroom) {
        super(blockEntity);
        this.cleanroomTypes = cleanroom;
    }

    @Override
    public void addedToController(@NotNull IMultiController controller) {
        super.addedToController(controller);
        ICleaningRoom.addedToController(controller, cleanroomTypes);
    }

    @Override
    public void removedFromController(@NotNull IMultiController controller) {
        super.removedFromController(controller);
        ICleaningRoom.removedFromController(controller, cleanroomTypes);
    }

    @Override
    public @NotNull Widget createUIWidget() {
        WidgetGroup group = (WidgetGroup) super.createUIWidget();
        group.addWidget(new IntInputWidget(10, 35, 80, 10, this::getCurrentGravity, this::setCurrentGravity).setMin(0).setMax(100));
        return group;
    }

    @Override
    public int getTier() {
        return GTValues.UXV;
    }

    @Override
    public int getCurrentGravity() {
        return gravity;
    }

    @Override
    public void setCurrentGravity(int gravity) {
        this.gravity = Mth.clamp(gravity, 0, 100);
    }
}
