package org.gtlcore.gtlcore.mixin.gtm.ae.slot;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(ExportOnlyAEFluidList.class)
public abstract class ExportOnlyAEFluidListMixin extends NotifiableFluidTank implements IMEPartMachine {

    @Getter
    protected final ObjectArrayList<FluidStack> fluidList = new ObjectArrayList<>();

    @Setter
    private boolean changed = true;

    @Shadow(remap = false)
    protected ExportOnlyAEFluidSlot[] inventory;

    public ExportOnlyAEFluidListMixin(MetaMachine machine, int slots, long capacity, IO io, IO capabilityIO) {
        super(machine, slots, capacity, io, capabilityIO);
    }

    @Override
    public List<FluidIngredient> handleRecipeInner(IO io, GTRecipe recipe, List<FluidIngredient> left, @Nullable String slotName, boolean simulate) {
        if (io == IO.IN) {
            boolean changed = false;
            var listIterator = left.listIterator();
            while (listIterator.hasNext()) {
                FluidIngredient ingredient = listIterator.next();
                if (ingredient.isEmpty()) {
                    listIterator.remove();
                } else {
                    long count = ingredient.getAmount();
                    if (count < 1) listIterator.remove();
                    else {
                        for (ExportOnlyAEFluidSlot i : this.inventory) {
                            GenericStack stored = i.getStock();
                            if (stored != null) {
                                long amount = stored.amount();
                                if (amount != 0L) {
                                    if (ingredient.test(i.getFluid())) {
                                        FluidStack drained = i.drain(count, simulate, !simulate);
                                        if (drained.getAmount() > 0L) {
                                            changed = true;
                                            count -= drained.getAmount();
                                            ingredient.setAmount(count);
                                        }
                                    }
                                    if (count <= 0L) {
                                        listIterator.remove();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (!simulate && changed) {
                this.changed = true;
                this.onContentsChanged();
            }
        }
        return left.isEmpty() ? null : left;
    }

    @Override
    public void onContentsChanged() {
        super.onContentsChanged();
        this.changed = true;
    }

    @Override
    public boolean getChanged() {
        return changed;
    }

    @Override
    public @NotNull List<FluidStack> getMEFluidList() {
        if (changed) {
            changed = false;
            fluidList.clear();
            for (var slot : inventory) {
                GenericStack stock = slot.getStock();
                if (stock != null && stock.amount() != 0L) {
                    FluidStack stack = slot.getFluid();
                    if (!stack.isEmpty()) fluidList.add(stack);
                }
            }
        }
        return fluidList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getContents() {
        return (List<Object>) (List<?>) getMEFluidList();
    }
}
