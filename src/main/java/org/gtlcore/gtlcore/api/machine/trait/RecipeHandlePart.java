package org.gtlcore.gtlcore.api.machine.trait;

import org.gtlcore.gtlcore.api.machine.trait.MEStock.IMEPartMachine;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;

import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class RecipeHandlePart implements IRecipeHandlePart {

    public static final Comparator<RecipeHandlePart> COMPARATOR = (h1, h2) -> {
        int cmp = Long.compare(h1.getPriority(), h2.getPriority());
        if (cmp != 0) return cmp;
        boolean b1 = h1.getTotalContentAmount() > 0;
        boolean b2 = h2.getTotalContentAmount() > 0;
        return Boolean.compare(b1, b2);
    };

    private final Reference2ObjectMap<RecipeCapability<?>, List<IRecipeHandler<?>>> handlerMap = new Reference2ObjectOpenHashMap<>();
    private final List<IRecipeHandler<FluidIngredient>> sharedFluidHandlers = new ObjectArrayList<>();

    public RecipeHandlePart(Iterable<IRecipeHandler<?>> handlers) {
        for (var handler : handlers) {
            handlerMap.computeIfAbsent(handler.getCapability(), c -> new ObjectArrayList<>()).add(handler);
        }
    }

    public static RecipeHandlePart of(IO io, Iterable<IRecipeHandler<?>> handlers) {
        RecipeHandlePart rhl = new RecipeHandlePart(handlers);
        if (io == IO.OUT) {
            for (var entry : rhl.getHandlerFastIterable()) {
                entry.getValue().sort(IRecipeHandler.ENTRY_COMPARATOR);
            }
        }
        return rhl;
    }

    public void addSharedFluidHandlers(Iterable<IRecipeHandler<FluidIngredient>> handlers) {
        for (var handler : handlers) {
            if (handler.getCapability() == FluidRecipeCapability.CAP) sharedFluidHandlers.add(handler);
        }
    }

    public ObjectIterable<Reference2ObjectMap.Entry<RecipeCapability<?>, List<IRecipeHandler<?>>>> getHandlerFastIterable() {
        return Reference2ObjectMaps.fastIterable(handlerMap);
    }

    @SuppressWarnings("unchecked")
    public <T extends Predicate<S>, S> Object2LongMap<S> getSelfContent(RecipeCapability<T> cap) {
        if (cap == ItemRecipeCapability.CAP) {
            return (Object2LongMap<S>) createItemMap(this.getCapability(cap), new Object2LongOpenHashMap<>());
        } else if (cap == FluidRecipeCapability.CAP) {
            return (Object2LongMap<S>) createFluidMap(this.getCapability(cap), new Object2LongOpenHashMap<>());
        }
        return Object2LongMaps.EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    public <T extends Predicate<S>, S> Object2LongMap<S> getContentWithShared(RecipeCapability<T> cap) {
        if (cap == ItemRecipeCapability.CAP) {
            return (Object2LongMap<S>) createItemMap(this.getCapability(cap), new Object2LongOpenHashMap<>());
        } else if (cap == FluidRecipeCapability.CAP) {
            return (Object2LongMap<S>) createFluidMap(this.sharedFluidHandlers, createFluidMap(this.getCapability(cap), new Object2LongOpenHashMap<>()));
        }
        return Object2LongMaps.EMPTY_MAP;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <T> List<IRecipeHandler<T>> getCapability(RecipeCapability<T> cap) {
        List<?> handlers = handlerMap.getOrDefault(cap, Collections.emptyList());
        return (List<IRecipeHandler<T>>) handlers;
    }

    public Reference2ObjectOpenHashMap<RecipeCapability<?>, List<Object>> handleRecipe(IO io, GTRecipe recipe,
                                                                                       Map<RecipeCapability<?>, List<Object>> contents,
                                                                                       boolean simulate) {
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        if (!handlerMap.isEmpty()) {
            for (var it = copy.reference2ObjectEntrySet().fastIterator(); it.hasNext();) {
                var entry = it.next();
                var handlerList = getCapability(entry.getKey());
                for (var handler : handlerList) {
                    var left = handler.handleRecipe(io, recipe, entry.getValue(), null, simulate);
                    if (left == null) {
                        it.remove();
                        break;
                    } else entry.setValue(new ArrayList<>(left));
                }
            }
        }
        return copy;
    }

    @Nullable
    public List<?> handleRecipe(IO io, GTRecipe recipe, RecipeCapability<?> cap, List<?> contents, boolean simulate) {
        var handlerList = getCapability(cap);
        for (var handler : handlerList) {
            contents = handler.handleRecipe(io, recipe, contents, null, simulate);
            if (contents == null) return null;
        }
        return contents;
    }

    private static <T> Object2LongOpenHashMap<ItemStack> createItemMap(List<IRecipeHandler<T>> handlers, Object2LongOpenHashMap<ItemStack> itemContent) {
        for (var handler : handlers) {
            if (handler instanceof CatalystItemStackHandler || handler instanceof NotifiableCircuitItemStackHandler) continue;
            if (handler instanceof IMEPartMachine aeItemHandler) {
                final var map = aeItemHandler.getMEItemMap();
                if (map != null) {
                    for (var it = Object2LongMaps.fastIterator(map); it.hasNext();) {
                        var entry = it.next();
                        itemContent.addTo(entry.getKey(), entry.getLongValue());
                    }
                }
            } else {
                for (var o : handler.getContents()) {
                    if (o instanceof ItemStack stack) {
                        itemContent.addTo(stack, stack.getCount());
                    }
                }
            }
        }
        return itemContent;
    }

    private static <T> Object2LongOpenHashMap<FluidStack> createFluidMap(List<IRecipeHandler<T>> handlers, Object2LongOpenHashMap<FluidStack> fluidContent) {
        for (var fluid : handlers) {
            if (fluid instanceof CatalystFluidStackHandler) continue;
            for (var o : fluid.getContents()) {
                if (o instanceof FluidStack stack) {
                    fluidContent.addTo(stack, stack.getAmount());
                }
            }
        }
        return fluidContent;
    }

    private long getPriority() {
        long priority = 0;
        for (List<IRecipeHandler<?>> list : handlerMap.values()) {
            for (IRecipeHandler<?> handler : list) {
                priority += handler.getPriority();
            }
        }
        return priority;
    }

    private double getTotalContentAmount() {
        double sum = 0;
        for (List<IRecipeHandler<?>> list : handlerMap.values()) {
            for (IRecipeHandler<?> handler : list) {
                sum += handler.getTotalContentAmount();
            }
        }
        return sum;
    }
}
