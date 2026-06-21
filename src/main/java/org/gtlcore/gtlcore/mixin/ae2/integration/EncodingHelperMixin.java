package org.gtlcore.gtlcore.mixin.ae2.integration;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import appeng.api.stacks.*;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.InventoryActionPacket;
import appeng.helpers.InventoryAction;
import appeng.integration.modules.jeirei.EncodingHelper;
import appeng.menu.me.common.GridInventoryEntry;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.parts.encoding.EncodingMode;
import appeng.util.CraftingRecipeUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.*;
import java.util.function.Predicate;

@Mixin(EncodingHelper.class)
public abstract class EncodingHelperMixin {

    @Shadow(remap = false)
    @Final
    static Comparator<GridInventoryEntry> ENTRY_COMPARATOR;

    @Shadow(remap = false)
    public static Map<AEKey, Integer> getIngredientPriorities(MEStorageMenu menu, Comparator<GridInventoryEntry> comparator) {
        return null;
    }

    /**
     * @author .
     * @reason 优先选择通用电路
     */
    @Overwrite(remap = false)
    private static GenericStack findBestIngredient(Map<AEKey, Integer> ingredientPriorities, List<GenericStack> possibleIngredients) {
        var list = possibleIngredients.stream()
                .map(gi -> Pair.of(gi, ingredientPriorities.getOrDefault(gi.what(), Integer.MIN_VALUE)))
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Pair::getLeft).toList();
        return list.stream().filter(gi -> gi.what() instanceof AEItemKey aeItemKey &&
                aeItemKey.getItem().kjs$getId().contains("universal_circuit"))
                .findFirst().orElseGet(() -> list.stream().findFirst().orElseThrow());
    }

    /**
     * @author .
     * @reason 优先选择通用电路
     */
    @Overwrite(remap = false)
    public static void encodeCraftingRecipe(PatternEncodingTermMenu menu,
                                            @Nullable Recipe<?> recipe,
                                            List<List<GenericStack>> genericIngredients,
                                            Predicate<ItemStack> visiblePredicate) {
        if (recipe != null && recipe.getType().equals(RecipeType.STONECUTTING)) {
            menu.setMode(EncodingMode.STONECUTTING);
            menu.setStonecuttingRecipeId(recipe.getId());
        } else if (recipe != null && recipe.getType().equals(RecipeType.SMITHING)) {
            menu.setMode(EncodingMode.SMITHING_TABLE);
        } else menu.setMode(EncodingMode.CRAFTING);

        // Note that this runs on the client and getClientRepo() is guaranteed to be available there.
        var prioritizedNetworkInv = getIngredientPriorities(menu, ENTRY_COMPARATOR);

        var encodedInputs = NonNullList.withSize(menu.getCraftingGridSlots().length, ItemStack.EMPTY);

        if (recipe != null) {
            // When we have access to a crafting recipe, we'll switch modes and try to find suitable
            // ingredients based on the recipe ingredients, which allows for fuzzy-matching.
            var ingredients3x3 = CraftingRecipeUtil.ensure3by3CraftingMatrix(recipe);

            // Find a good match for every ingredient
            for (int slot = 0; slot < ingredients3x3.size(); slot++) {
                var ingredient = ingredients3x3.get(slot);
                if (ingredient.isEmpty()) continue; // Skip empty slots

                // Due to how some crafting recipes work, the ingredient can match more than just one item in the
                // network inventory. We'll find all network inventory entries that it matches and sort them
                // according to their suitability for encoding a pattern
                var bestNetworkIngredient = prioritizedNetworkInv.entrySet().stream()
                        .filter(ni -> ni.getKey() instanceof AEItemKey itemKey && itemKey.matches(ingredient))
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .map(entry -> entry.getKey() instanceof AEItemKey itemKey ? itemKey.toStack() : null).toList();

                var bestIngredient = bestNetworkIngredient.stream()
                        .filter(itemStack -> itemStack.getItem().kjs$getId().contains("universal_circuit"))
                        .findFirst().orElseGet(() -> bestNetworkIngredient.stream().findFirst().orElseGet(() -> {

                            // To avoid encoding hidden entries, we'll cycle through the ingredient and try to find a
                            // visible
                            // stack, otherwise we'll use the first entry.
                            for (var stack : ingredient.getItems()) {
                                if (visiblePredicate.test(stack)) return stack;
                            }
                            return ingredient.getItems()[0];
                        }));

                encodedInputs.set(slot, bestIngredient);
            }
        } else {
            for (int slot = 0; slot < genericIngredients.size(); slot++) {
                var genericIngredient = genericIngredients.get(slot);
                if (genericIngredient.isEmpty()) continue; // Skip empty slots

                var bestIngredient = findBestIngredient(prioritizedNetworkInv, genericIngredient).what();

                // Clamp amounts to 1 in crafting table mode
                if (bestIngredient instanceof AEItemKey itemKey) {
                    encodedInputs.set(slot, itemKey.toStack());
                } else {
                    encodedInputs.set(slot, GenericStack.wrapInItemStack(bestIngredient, 1));
                }
            }
        }

        for (int i = 0; i < encodedInputs.size(); i++) {
            ItemStack encodedInput = encodedInputs.get(i);
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, menu.getCraftingGridSlots()[i].index, encodedInput));
        }

        // Clear out the processing outputs
        for (var outputSlot : menu.getProcessingOutputSlots()) {
            NetworkHandler.instance().sendToServer(new InventoryActionPacket(
                    InventoryAction.SET_FILTER, outputSlot.index, ItemStack.EMPTY));
        }
    }
}
