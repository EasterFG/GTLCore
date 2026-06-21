package org.gtlcore.gtlcore.integration.extendedae;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import appeng.api.stacks.AEKey;
import appeng.util.prioritylist.IPartitionList;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class ItemTagPriority implements IPartitionList {

    private final Set<TagKey<?>> whiteSet;
    private final Set<TagKey<?>> blackSet;
    private final String tagExp;
    private final Reference2BooleanMap<Object> memory = new Reference2BooleanOpenHashMap<>();

    public ItemTagPriority(Set<TagKey<?>> whiteSet, Set<TagKey<?>> blackSet, String tagExp) {
        this.whiteSet = whiteSet;
        this.blackSet = blackSet;
        this.tagExp = tagExp;
    }

    @Override
    public boolean isListed(AEKey aeKey) {
        Object key = aeKey.getPrimaryKey();
        return this.memory.computeIfAbsent(key, this::eval);
    }

    @Override
    public boolean isEmpty() {
        return tagExp.isEmpty();
    }

    @Override
    public Iterable<AEKey> getItems() {
        return List.of();
    }

    private boolean eval(@NotNull Object obj) {
        Holder<?> refer = null;
        if (obj instanceof Item item) {
            refer = ForgeRegistries.ITEMS.getHolder(item).orElse(null);
        } else if (obj instanceof Fluid) {
            return false;
        }

        if (refer != null) {
            if (this.whiteSet.isEmpty()) {
                return false;
            }

            boolean pass = refer.tags().anyMatch(whiteSet::contains);
            if (pass) {
                if (!this.blackSet.isEmpty()) {
                    return refer.tags().noneMatch(blackSet::contains);
                }
                return true;
            }
        }
        return false;
    }
}
