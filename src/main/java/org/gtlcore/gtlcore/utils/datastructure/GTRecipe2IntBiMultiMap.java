package org.gtlcore.gtlcore.utils.datastructure;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.BiConsumer;

public class GTRecipe2IntBiMultiMap {

    private final Object2ReferenceMap<GTRecipe, IntSet> keyToValues = new Object2ReferenceOpenHashMap<>();
    private final Int2ReferenceMap<ObjectSet<@NotNull GTRecipe>> valueToKeys = new Int2ReferenceOpenHashMap<>();

    public void put(@NotNull GTRecipe key, int value) {
        keyToValues.computeIfAbsent(key, k -> new IntArraySet()).add(value);
        valueToKeys.computeIfAbsent(value, v -> new ObjectArraySet<>()).add(key);
    }

    public IntSet getValues(GTRecipe key) {
        return keyToValues.getOrDefault(key, IntSets.emptySet());
    }

    public ObjectSet<GTRecipe> getKeys(int value) {
        return valueToKeys.getOrDefault(value, ObjectSets.emptySet());
    }

    public void remove(GTRecipe key, int value) {
        Optional.ofNullable(keyToValues.get(key)).ifPresent(set -> {
            set.remove(value);
            if (set.isEmpty()) keyToValues.remove(key);
        });
        Optional.ofNullable(valueToKeys.get(value)).ifPresent(set -> {
            set.remove(key);
            if (set.isEmpty()) valueToKeys.remove(value);
        });
    }

    public void removeByKey(GTRecipe key) {
        IntSet values = keyToValues.remove(key);
        if (values != null) {
            for (int v : values) {
                ObjectSet<GTRecipe> ks = valueToKeys.get(v);
                if (ks != null) {
                    ks.remove(key);
                    if (ks.isEmpty()) valueToKeys.remove(v);
                }
            }
        }
    }

    public void removeByValue(int value) {
        ObjectSet<GTRecipe> keys = valueToKeys.remove(value);
        if (keys != null) {
            for (GTRecipe k : keys) {
                IntSet vs = keyToValues.get(k);
                if (vs != null) {
                    vs.remove(value);
                    if (vs.isEmpty()) keyToValues.remove(k);
                }
            }
        }
    }

    public ObjectSet<GTRecipe> keySet() {
        return ObjectSets.unmodifiable(keyToValues.keySet());
    }

    public IntSet valueSet() {
        return IntSets.unmodifiable(valueToKeys.keySet());
    }

    public void clear() {
        keyToValues.clear();
        valueToKeys.clear();
    }

    public int size() {
        return keyToValues.values().stream().mapToInt(IntSet::size).sum();
    }

    public void forEach(BiConsumer<GTRecipe, Integer> action) {
        for (var entry : keyToValues.entrySet()) {
            GTRecipe key = entry.getKey();
            for (int value : entry.getValue()) {
                action.accept(key, value);
            }
        }
    }
}
