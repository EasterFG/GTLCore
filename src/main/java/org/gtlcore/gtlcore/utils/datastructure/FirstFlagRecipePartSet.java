package org.gtlcore.gtlcore.utils.datastructure;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeHandlePart;

import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FirstFlagRecipePartSet {

    private final ReferenceSortedSet<@NotNull IRecipeHandlePart> handlers = new ReferenceLinkedOpenHashSet<>();

    public void addOrSetActive(@NotNull IRecipeHandlePart handler) {
        if (handlers.size() > 1) handlers.remove(handler);
        handlers.add(handler);
    }

    @NotNull
    public ReferenceSet<@NotNull IRecipeHandlePart> getAll() {
        return handlers;
    }

    @Nullable
    public IRecipeHandlePart getActive() {
        return handlers.isEmpty() ? null : handlers.last();
    }

    @NotNull
    public ObjectIterator<@NotNull IRecipeHandlePart> getReverseIterator() {
        return ReverseIteratorBuilder.build(handlers);
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    public void clear() {
        handlers.clear();
    }
}
