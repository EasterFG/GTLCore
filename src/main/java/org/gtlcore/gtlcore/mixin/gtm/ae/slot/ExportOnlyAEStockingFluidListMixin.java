package org.gtlcore.gtlcore.mixin.gtm.ae.slot;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMESlot;
import org.gtlcore.gtlcore.config.ConfigHolder;
import org.gtlcore.gtlcore.integration.ae2.AEUtils;
import org.gtlcore.gtlcore.mixin.gtm.ae.machine.MEHatchPartMachineAccessor;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlotList;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;

@Mixin(targets = "com.gregtechceu.gtceu.integration.ae2.machine.MEStockingHatchPartMachine$ExportOnlyAEStockingFluidList", remap = false)
public abstract class ExportOnlyAEStockingFluidListMixin extends ExportOnlyAEFluidListMixin implements IConfigurableSlotList {

    protected ObjectArrayList<AEFluidKey> configList;

    protected IntArrayList configIndexList;

    private static final boolean ENABLE_ULTIMATE_ME_STOCKING = ConfigHolder.INSTANCE.enableUltimateMEStocking;

    @SuppressWarnings("target")
    @Shadow(remap = false)
    @Final
    MEStockingHatchPartMachine this$0;

    public ExportOnlyAEStockingFluidListMixin(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    @SuppressWarnings("target")
    @Inject(method = "<init>(Lcom/gregtechceu/gtceu/integration/ae2/machine/MEStockingHatchPartMachine;Lcom/gregtechceu/gtceu/api/machine/MetaMachine;I)V",
            at = @At("TAIL"))
    private void gtl$onInit(MEStockingHatchPartMachine holder, MetaMachine slots, int par3, CallbackInfo ci) {
        configList = new ObjectArrayList<>();
        configIndexList = new IntArrayList();
        for (ExportOnlyAEFluidSlot exportOnlyAEFluidSlot : inventory) {
            ((IMESlot) exportOnlyAEFluidSlot).setOnConfigChanged(this::onConfigChanged);
        }
    }

    @Override
    public void clearInventory(int startIndex) {
        for (int i = startIndex; i < this.getConfigurableSlots(); ++i) {
            IConfigurableSlot slot = this.getConfigurableSlot(i);
            ((IMESlot) slot).setConfigWithoutNotify(null);
            slot.setStock(null);
        }
    }

    @Unique
    @Override
    public void onConfigChanged() {
        configList.clear();
        configIndexList.clear();
        for (int i = 0, inventoryLength = inventory.length; i < inventoryLength; i++) {
            final var config = inventory[i].getConfig();
            if (config != null && config.what() instanceof AEFluidKey key) {
                configList.add(key);
                configIndexList.add(i);
            }
        }
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
        if (io != IO.IN || left.isEmpty()) {
            return left;
        }
        IGrid grid = this$0.getMainNode().getGrid();
        if (grid == null) {
            return left;
        }

        MEStorage aeNetwork = grid.getStorageService().getInventory();
        boolean changed = false;
        var listIterator = left.listIterator();

        while (listIterator.hasNext()) {
            FluidIngredient ingredient = listIterator.next();
            if (ingredient.isEmpty()) {
                listIterator.remove();
            } else {
                long amount = ingredient.getAmount();
                if (amount < 1) listIterator.remove();
                else {
                    for (int i = 0, configListSize = configList.size(); i < configListSize; i++) {
                        AEFluidKey aeFluidKey = configList.get(i);
                        if (AEUtils.testFluidIngredient(ingredient, aeFluidKey)) {
                            long extracted = aeNetwork.extract(aeFluidKey, amount, simulate ? Actionable.SIMULATE : Actionable.MODULATE, ((MEHatchPartMachineAccessor) this$0).getActionSource());
                            if (extracted > 0) {
                                changed = true;
                                amount -= extracted;
                                if (!simulate) {
                                    var slot = this.inventory[configIndexList.getInt(i)];
                                    if (slot.getStock() != null) {
                                        long amt = slot.getStock().amount() - extracted;
                                        if (amt == 0) slot.setStock(null);
                                        else slot.setStock(new GenericStack(aeFluidKey, amt));
                                    }
                                }
                            }
                        }
                        if (amount <= 0L) {
                            listIterator.remove();
                            break;
                        }
                    }
                }
            }
        }
        if (!simulate && changed) {
            setChanged(true);
            this.onContentsChanged();
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public @NotNull List<FluidStack> getMEFluidList() {
        if (ENABLE_ULTIMATE_ME_STOCKING || getChanged()) {
            setChanged(false);
            final var fluidList = getFluidList();
            fluidList.clear();
            final MEStorage aeNetwork = Objects.requireNonNull(this$0.getMainNode().getGrid()).getStorageService().getInventory();
            final IActionSource actionSource = ((MEHatchPartMachineAccessor) this$0).getActionSource();
            for (var key : configList) {
                long extracted = aeNetwork.extract(key, Long.MAX_VALUE, Actionable.SIMULATE, actionSource);
                if (extracted > 0) {
                    fluidList.add(FluidStack.create(key.getFluid(), extracted));
                }
            }
        }
        return getFluidList();
    }
}
