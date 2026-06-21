package org.gtlcore.gtlcore.mixin.gtm.ae.machine;

import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEOutputPart;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.integration.ae2.machine.MEHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEOutputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import appeng.api.networking.IGrid;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@SuppressWarnings("all")
@Mixin(MEOutputHatchPartMachine.class)
public abstract class MEOutputHatchPartMachineMixin extends MEHatchPartMachine implements IMEOutputPart, IGridConnectedMachine {

    @Unique
    private byte gTLCore$time;

    @Shadow(remap = false)
    private KeyStorage internalBuffer;

    public MEOutputHatchPartMachineMixin(IMachineBlockEntity holder, IO io, Object... args) {
        super(holder, io, args);
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    protected void autoIO() {
        if (this.isReturn()) {
            this.gTLCore$time++;
            if (this.updateMEStatus()) {
                IGrid grid = this.getMainNode().getGrid();
                if (grid != null && !this.internalBuffer.isEmpty()) {
                    this.internalBuffer.insertInventory(grid.getStorageService().getInventory(), this.actionSource);
                }
                this.updateTankSubscription();
            }
        } else this.gTLCore$time++;
    }

    @Override
    public void attachConfigurators(@NotNull ConfiguratorPanel configuratorPanel) {
        super.attachConfigurators(configuratorPanel);
        IMEOutputPart.attachRecipeLockable(configuratorPanel, this);
    }

    @Override
    public void returnStorage() {
        this.gTLCore$time = 0;
    }

    @Override
    public byte getTime() {
        return gTLCore$time;
    }

    @Override
    public void setTime(byte time) {
        gTLCore$time = time;
    }
}
