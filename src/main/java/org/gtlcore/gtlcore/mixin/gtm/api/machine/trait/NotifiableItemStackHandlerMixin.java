package org.gtlcore.gtlcore.mixin.gtm.api.machine.trait;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import com.google.common.primitives.Ints;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientAction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(NotifiableItemStackHandler.class)
public abstract class NotifiableItemStackHandlerMixin {

    @Shadow(remap = false)
    @Final
    public IO handlerIO;

    @Shadow(remap = false)
    @Final
    public ItemStackTransfer storage;

    /**
     * @author Dragons
     * @reason 适配LongIngredient
     */
    @Overwrite(remap = false)
    public List<Ingredient> handleRecipeInner(IO io, GTRecipe recipe, List<Ingredient> left, @Nullable String slotName,
                                              boolean simulate) {
        return handleLongIngredient(io, recipe, left, simulate, this.handlerIO, storage);
    }

    @Unique
    @Nullable
    private static List<Ingredient> handleLongIngredient(IO io, GTRecipe recipe, List<Ingredient> left, boolean simulate,
                                                         IO handlerIO, ItemStackTransfer storage) {
        if (io != handlerIO) return left;
        if (io != IO.IN && io != IO.OUT) return left.isEmpty() ? null : left;

        // Temporarily remove listener so that we can broadcast the entire set of transactions once
        Runnable listener = storage.getOnContentsChanged();
        storage.setOnContentsChanged(() -> {});
        boolean changed = false;

        // Store the ItemStack in each slot after an operation
        // Necessary for simulation since we don't actually modify the slot's contents
        // Doesn't hurt for execution, and definitely cheaper than copying the entire storage
        ItemStack[] visited = new ItemStack[storage.getSlots()];
        for (var it = left.listIterator(); it.hasNext();) {
            var ingredient = it.next();
            if (ingredient.isEmpty()) {
                it.remove();
                continue;
            }

            ItemStack[] items;
            long amount;
            if (io == IO.OUT && ingredient instanceof IntProviderIngredient provider) {
                provider.setItemStacks(null);
                provider.setSampledCount(-1);
            }

            items = ingredient.getItems();
            if (items.length == 0 || items[0].isEmpty()) {
                it.remove();
                continue;
            }
            if (ingredient instanceof LongIngredient li) amount = li.getActualAmount();
            else amount = items[0].getCount();

            for (int slot = 0; slot < storage.getSlots(); ++slot) {
                ItemStack current = visited[slot] == null ? storage.getStackInSlot(slot) : visited[slot];
                int count = current.getCount();

                if (io == IO.IN) {
                    if (current.isEmpty()) continue;
                    if (ingredient.test(current)) {
                        var extracted = getActioned(storage, slot, recipe.ingredientActions);
                        if (extracted == null) extracted = storage.extractItem(slot, Ints.saturatedCast(Math.min(count, amount)), simulate);
                        if (!extracted.isEmpty()) {
                            changed = true;
                            visited[slot] = extracted.copyWithCount(count - extracted.getCount());
                        }
                        amount -= extracted.getCount();
                    }
                } else {
                    ItemStack template = items[0];
                    // Only try this slot if not visited or if visited with the same type of item
                    if (visited[slot] == null || ItemStack.isSameItemSameTags(visited[slot], template)) {
                        int slotLimit = storage.getSlotLimit(slot);
                        int canHold = slotLimit - count;
                        if (canHold > 0) {
                            int tryPut = Ints.saturatedCast(Math.min(amount, canHold));
                            ItemStack remainder = getActioned(storage, slot, recipe.ingredientActions);
                            if (remainder == null) remainder = storage.insertItem(slot, template.copyWithCount(tryPut), simulate);
                            int actuallyPut = tryPut - remainder.getCount();
                            if (actuallyPut > 0) {
                                changed = true;
                                visited[slot] = template.copyWithCount(count + actuallyPut);
                                amount -= actuallyPut;
                            }
                        }
                    }
                }

                if (amount <= 0) {
                    it.remove();
                    break;
                }
            }
            // Modify ingredient if we didn't finish it off
            if (amount > 0) {
                if (ingredient instanceof LongIngredient li) {
                    li.setActualAmount(amount);
                } else {
                    items[0].setCount(Ints.saturatedCast(amount));
                }
            }
        }

        storage.setOnContentsChanged(listener);
        if (changed && !simulate) listener.run();

        return left.isEmpty() ? null : left;
    }

    @Unique
    private static @Nullable ItemStack getActioned(ItemStackTransfer storage, int index, List<?> actions) {
        if (!GTCEu.isKubeJSLoaded()) return null;
        // noinspection unchecked
        var actioned = NotifiableItemStackHandler.KJSCallWrapper.applyIngredientAction(storage, index, (List<IngredientAction>) actions);
        if (!actioned.isEmpty()) return actioned;
        return null;
    }
}
