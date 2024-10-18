package org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.AutoMaintenanceHatchPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author EasterFG on 2024/10/12
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GravityCleaningMaintenancePartMachine extends AutoMaintenanceHatchPartMachine
                                                   implements IGravityPartMachine {

    @Persisted
    private int currentGravity = 0;

    ICleanroomProvider cleanroomTypes;

    public GravityCleaningMaintenancePartMachine(IMachineBlockEntity blockEntity) {
        super(blockEntity);
    }

    public GravityCleaningMaintenancePartMachine(IMachineBlockEntity blockEntity, ICleanroomProvider cleanroomTypes) {
        super(blockEntity);
        this.cleanroomTypes = cleanroomTypes;
    }

    @Override
    public void addedToController(IMultiController controller) {
        ICleaningRoom.addedToController(controller, cleanroomTypes);
    }

    @Override
    public void removedFromController(IMultiController controller) {
        super.removedFromController(controller);
        ICleaningRoom.removedFromController(controller, cleanroomTypes);
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup gravityGroup = new WidgetGroup(0, 0, 100, 20);
        gravityGroup.addWidget(new IntInputWidget(this::getCurrentGravity, this::setCurrentGravity)
                .setMin(0).setMax(100));
        return gravityGroup;
    }

    @Override
    public int getTier() {
        return GTValues.UEV;
    }

    @Override
    public int getCurrentGravity() {
        return currentGravity;
    }

    @Override
    public void setCurrentGravity(int gravity) {
        this.currentGravity = Mth.clamp(gravity, 0, 100);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GravityCleaningMaintenancePartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
