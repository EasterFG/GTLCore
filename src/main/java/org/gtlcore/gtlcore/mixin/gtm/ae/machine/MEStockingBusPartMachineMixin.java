package org.gtlcore.gtlcore.mixin.gtm.ae.machine;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMESlot;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.stacks.*;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import org.spongepowered.asm.mixin.*;

import java.util.function.Predicate;

@Mixin(MEStockingBusPartMachine.class)
public abstract class MEStockingBusPartMachineMixin extends MEInputBusPartMachine {

    @Shadow(remap = false)
    private Predicate<GenericStack> autoPullTest;

    @Shadow(remap = false)
    public void setAutoPull(boolean autoPull) {
        throw new AssertionError();
    }

    public MEStockingBusPartMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    /**
     * @author Dragons
     * @reason 设置完所有slot才进行onConfigChanged
     */
    @Overwrite(remap = false)
    private void refreshList() {
        IGrid grid = this.getMainNode().getGrid();
        if (grid == null) {
            this.aeItemHandler.clearInventory(0);
        } else {
            MEStorage networkStorage = grid.getStorageService().getInventory();
            var counter = networkStorage.getAvailableStacks();

            int index = 0;
            for (Object2LongMap.Entry<AEKey> entry : counter) {
                if (index >= 16) {
                    break;
                }

                AEKey what = entry.getKey();
                long amount = entry.getLongValue();
                if (amount > 0L && what instanceof AEItemKey itemKey) {
                    long request = networkStorage.extract(what, amount, Actionable.SIMULATE, this.actionSource);
                    if (request != 0L && (this.autoPullTest == null || this.autoPullTest.test(new GenericStack(itemKey, amount)))) {
                        ExportOnlyAEItemSlot slot = this.aeItemHandler.getInventory()[index];
                        ((IMESlot) slot).setConfigWithoutNotify(new GenericStack(what, 1L));
                        slot.setStock(new GenericStack(what, request));
                        ++index;
                    }
                }
            }

            this.aeItemHandler.clearInventory(index);

            ((IMEPartMachine) this.aeItemHandler).onConfigChanged();
        }
    }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {
        if (tag.getBoolean("AutoPull")) {
            this.setAutoPull(true);
            this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
        } else {
            this.setAutoPull(false);
            if (tag.contains("ConfigStacks")) {
                CompoundTag configStacks = tag.getCompound("ConfigStacks");

                final var inventory = this.aeItemHandler.getInventory();

                for (int i = 0; i < 16; ++i) {
                    String key = Integer.toString(i);
                    if (configStacks.contains(key)) {
                        CompoundTag configTag = configStacks.getCompound(key);
                        ((IMESlot) inventory[i]).setConfigWithoutNotify(GenericStack.readTag(configTag));
                    } else {
                        ((IMESlot) inventory[i]).setConfigWithoutNotify(null);
                    }
                }

                ((IMEPartMachine) this.aeItemHandler).onConfigChanged();
            }

            if (tag.contains("GhostCircuit")) {
                this.circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(tag.getByte("GhostCircuit")));
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().tell(new TickTask(1, () -> ((IMEPartMachine) this.aeItemHandler).onConfigChanged()));
        }
    }
}
