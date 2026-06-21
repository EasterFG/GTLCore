package org.gtlcore.gtlcore.mixin.gtm.api.recipe;

import org.gtlcore.gtlcore.api.recipe.chance.LongChanceLogic;

import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(targets = "com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic$1")
public abstract class ChanceLogicOrMixin {

    /**
     * @author Dragons
     * @reason 修复1/Parallel导致的精度问题
     */
    @Overwrite(remap = false)
    public @Nullable List<Content> roll(
                                        List<Content> chancedEntries,
                                        @NotNull ChanceBoostFunction boostFunction,
                                        int baseTier,
                                        int machineTier,
                                        @Nullable Object2IntMap<?> cache,
                                        int times,
                                        RecipeCapability<?> cap) {
        List<Content> out = new ObjectArrayList<>(chancedEntries.size());

        for (Content entry : chancedEntries) {
            int maxChance = entry.maxChance;
            int newChance = LongChanceLogic.getChance(entry, boostFunction, baseTier, machineTier);
            LongChanceLogic.modifyByChanceSafe(cache, times, cap, out, entry, maxChance, newChance);
        }

        return out.isEmpty() ? null : out;
    }
}
