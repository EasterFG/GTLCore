package org.gtlcore.gtlcore.mixin.gtm.ae.slot;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMESlot;

import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;

import appeng.api.stacks.GenericStack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import static com.lowdragmc.lowdraglib.LDLib.isRemote;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine$ExportOnlyAEStockingItemSlot", remap = false)
public class ExportOnlyAEStockingItemSlotMixin extends ExportOnlyAEItemSlot implements IMESlot {

    @Setter
    @Getter
    private Runnable onConfigChanged;

    @Override
    public void setConfig(@Nullable GenericStack config) {
        super.setConfig(config);
        if (!isRemote()) onConfigChanged.run();
    }

    @Override
    public void setConfigWithoutNotify(@Nullable GenericStack config) {
        this.config = config;
    }
}
