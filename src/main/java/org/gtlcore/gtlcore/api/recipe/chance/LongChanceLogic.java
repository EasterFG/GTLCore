package org.gtlcore.gtlcore.api.recipe.chance;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.boost.ChanceBoostFunction;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

import static org.gtlcore.gtlcore.api.recipe.IAdvancedContentModifier.preciseDivision;

public abstract class LongChanceLogic extends ChanceLogic {

    public static final LongChanceLogic OR;

    public LongChanceLogic(String id) {
        super(id);
    }

    public static int getChance(@NotNull Content entry, @NotNull ChanceBoostFunction boostFunction, int baseTier, int machineTier) {
        return boostFunction.getBoostedChance(entry, baseTier, machineTier);
    }

    public static int getCachedChance(Content entry, @Nullable Object2IntMap<?> cache) {
        if (cache == null) return GTValues.RNG.nextInt(entry.maxChance);
        return cache.getOrDefault(entry.content, GTValues.RNG.nextInt(entry.maxChance));
    }

    @SuppressWarnings("all")
    public static void updateCachedChance(Object ingredient, @Nullable Object2IntMap<?> cache, int chance) {
        if (cache != null) {
            ((Object2IntMap) cache).put(ingredient, chance);
        }
    }

    /**
     * Safe calculation (times * chance) / maxChance and (times * chance) % maxChance
     * <=> times = q * maxChance + r
     * <=> times * chance = q * maxChance * chance + r * chance
     */
    public static void modifyByChanceSafe(@Nullable Object2IntMap<?> cache, long times, RecipeCapability<?> cap, List<Content> out, Content entry, int maxChance, int chance) {
        long timesQuotient = times / maxChance;
        long timesRemainder = times % maxChance;

        // guaranteed = (times * chance) / maxChance
        // = (timesQuotient * maxChance * chance + timesRemainder * chance) / maxChance
        // = timesQuotient * chance + (timesRemainder * chance) / maxChance
        long guaranteed = timesQuotient * chance + (timesRemainder * chance) / maxChance;

        if (guaranteed > 0) out.add(entry.copy(cap, preciseDivision(guaranteed, times)));

        // newChance = (times * chance) % maxChance
        // = (timesRemainder * chance) % maxChance
        // timesRemainder < maxChance && chance < maxChance, < Int.MAX
        int newChance = (int) ((timesRemainder * chance) % maxChance);

        int cached = getCachedChance(entry, cache);
        int chanceSum = newChance + cached;
        if (chanceSum >= maxChance) {
            int bonusCount = chanceSum / maxChance;
            out.add(entry.copy(cap, preciseDivision(bonusCount, times)));
            newChance -= bonusCount * maxChance;
        }

        updateCachedChance(entry.content, cache, newChance / 2 + cached);
    }

    @Nullable
    @Unmodifiable
    public abstract List<@NotNull Content> roll(
                                                @NotNull @Unmodifiable List<@NotNull Content> chancedEntries,
                                                @NotNull ChanceBoostFunction boostFunction,
                                                int baseTier, int machineTier,
                                                @Nullable Object2IntMap<?> cache, long times,
                                                RecipeCapability<?> cap);

    static {
        GTRegistries.CHANCE_LOGICS.unfreeze();
        OR = new LongChanceLogic("longOr") {

            @Override
            public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull List<Content> chancedEntries,
                                                                       @NotNull ChanceBoostFunction boostFunction,
                                                                       int baseTier,
                                                                       int machineTier,
                                                                       @Nullable Object2IntMap<?> cache,
                                                                       long times,
                                                                       RecipeCapability<?> cap) {
                List<Content> out = new ObjectArrayList<>(chancedEntries.size());

                for (Content entry : chancedEntries) {
                    int maxChance = entry.maxChance;
                    int newChance = getChance(entry, boostFunction, baseTier, machineTier);
                    modifyByChanceSafe(cache, times, cap, out, entry, maxChance, newChance);
                }

                return out.isEmpty() ? null : out;
            }

            @Override
            public @Nullable @Unmodifiable List<@NotNull Content> roll(@NotNull List<Content> chancedEntries,
                                                                       @NotNull ChanceBoostFunction boostFunction,
                                                                       int baseTier,
                                                                       int machineTier,
                                                                       @Nullable Object2IntMap<?> cache,
                                                                       int times,
                                                                       RecipeCapability<?> cap) {
                List<Content> out = new ObjectArrayList<>(chancedEntries.size());

                for (Content entry : chancedEntries) {
                    int maxChance = entry.maxChance;
                    int newChance = getChance(entry, boostFunction, baseTier, machineTier);
                    modifyByChanceSafe(cache, times, cap, out, entry, maxChance, newChance);
                }

                return out.isEmpty() ? null : out;
            }

            @Override
            public @NotNull Component getTranslation() {
                return Component.translatable("gtceu.chance_logic.or");
            }

            public String toString() {
                return "LongChanceLogic{OR}";
            }
        };
        GTRegistries.CHANCE_LOGICS.freeze();
    }
}
