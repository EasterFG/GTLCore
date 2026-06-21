package org.gtlcore.gtlcore.mixin.gtm.ae.slot;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;
import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.GenericStack;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;
import java.util.function.Function;

@Mixin(ExportOnlyAEItemList.class)
public abstract class ExportOnlyAEItemListMixin extends NotifiableItemStackHandler implements IMEPartMachine {

    @Getter
    protected final Object2LongOpenHashMap<ItemStack> itemMap = new Object2LongOpenHashMap<>();
    @Setter
    protected boolean changed = true;

    @Shadow(remap = false)
    protected ExportOnlyAEItemSlot[] inventory;

    public ExportOnlyAEItemListMixin(MetaMachine machine, int slots, @NotNull IO handlerIO, @NotNull IO capabilityIO, Function<Integer, ItemStackTransfer> transferFactory) {
        super(machine, slots, handlerIO, capabilityIO, transferFactory);
    }

    @Override
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName, boolean simulate) {
        if (io == IO.IN) {
            boolean changed = false;
            var listIterator = left.listIterator();
            while (listIterator.hasNext()) {
                Ingredient ingredient = listIterator.next();
                if (ingredient.isEmpty()) {
                    listIterator.remove();
                } else {
                    long amount;
                    if (ingredient instanceof LongIngredient li) amount = li.getActualAmount();
                    else if (ingredient instanceof SizedIngredient si) amount = si.getAmount();
                    else amount = 1;
                    if (amount < 1) listIterator.remove();
                    else {
                        for (ExportOnlyAEItemSlot i : this.inventory) {
                            GenericStack stored = i.getStock();
                            if (stored != null && stored.amount() != 0) {
                                if (ingredient.test(i.getStackInSlot(0))) {
                                    ItemStack extracted = i.extractItem(0, Ints.saturatedCast(amount), simulate, !simulate);
                                    if (extracted.getCount() > 0) {
                                        changed = true;
                                        amount -= extracted.getCount();
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
    public Object2LongOpenHashMap<ItemStack> getMEItemMap() {
        if (changed) {
            changed = false;
            itemMap.clear();
            for (var slot : inventory) {
                GenericStack stock = slot.getStock();
                if (stock != null && stock.amount() != 0L) {
                    ItemStack stack = slot.getStackInSlot(0);
                    if (!stack.isEmpty()) this.itemMap.addTo(stack, stock.amount());
                }
            }
        }
        return itemMap.isEmpty() ? null : itemMap;
    }

    @Override
    public boolean getChanged() {
        return changed;
    }

    @Override
    public List<Object> getContents() {
        var itemMap = this.getMEItemMap();
        if (itemMap == null) return Collections.emptyList();
        return Arrays.asList(itemMap.keySet().toArray());
    }
}
