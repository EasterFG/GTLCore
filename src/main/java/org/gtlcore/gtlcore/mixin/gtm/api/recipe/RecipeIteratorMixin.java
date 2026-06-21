package org.gtlcore.gtlcore.mixin.gtm.api.recipe;

import org.gtlcore.gtlcore.api.recipe.IAdditionalRecipeIterator;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.AbstractMapIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeIterator;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.*;
import java.util.function.Predicate;

import static org.gtlcore.gtlcore.api.recipe.IRecipeIterator.diveIngredientTreeFindRecipeCollection;

@Implements(@Interface(
                       iface = IAdditionalRecipeIterator.class,
                       prefix = "gTLCore$"))
@Mixin(RecipeIterator.class)
public class RecipeIteratorMixin {

    @Shadow(remap = false)
    int index;

    @Shadow(remap = false)
    List<List<AbstractMapIngredient>> ingredients;
    @Shadow(remap = false)
    @NotNull
    GTRecipeType recipeMap;

    @Shadow(remap = false)
    @NotNull
    Predicate<GTRecipe> canHandle;

    @Unique
    private List<GTRecipe> gtlCore$additionalRecipes = null;

    @Unique
    private Iterator<GTRecipe> gtlCore$presentIndexGTRecipesIterator = null;

    @Unique
    private int gtlCore$additionalIndex = 0;

    @Unique
    private boolean gtlCore$useOriginal = true;

    @Unique
    private boolean gtlCore$useDiveIngredientTreeFind = false;

    @Unique
    private GTRecipe gtlCore$singleRecipe = null;

    @Unique
    private GTRecipe gtlCore$cachedNextAdditional = null;

    // ==================== IAdditionalRecipeIterator ====================

    @Unique
    public void gTLCore$setUseDiveIngredientTreeFind(boolean useDiveIngredientTreeFind) {
        this.gtlCore$useDiveIngredientTreeFind = useDiveIngredientTreeFind;
    }

    @Unique
    public void gTLCore$setAdditionalRecipes(@NotNull List<@NotNull GTRecipe> additionalRecipes) {
        if (!additionalRecipes.isEmpty()) {
            this.gtlCore$additionalRecipes = additionalRecipes;
            this.gtlCore$additionalIndex = 0;
            this.gtlCore$useOriginal = true;
            this.gtlCore$cachedNextAdditional = null;
        }
    }

    @Unique
    @Nullable
    public List<GTRecipe> gTLCore$getAdditionalRecipes() {
        return this.gtlCore$additionalRecipes;
    }

    // ==================== Iterator ====================

    /**
     * @author Dragons
     * @reason 支持双重迭代器逻辑，优化单配方模式性能
     */
    @Overwrite(remap = false)
    public boolean hasNext() {
        if (gtlCore$useOriginal) {
            // 非dive模式：检查是否有单个配方缓存
            if (!gtlCore$useDiveIngredientTreeFind && gtlCore$singleRecipe != null) {
                return true;
            }

            // dive模式：如果当前迭代器还有元素，直接返回true
            if (gtlCore$useDiveIngredientTreeFind && gtlCore$presentIndexGTRecipesIterator != null && gtlCore$presentIndexGTRecipesIterator.hasNext()) {
                return true;
            }

            // 尝试查找下一个有效的ingredient集合或单个配方
            if (gtlCore$findNextRecipeSet()) {
                return true;
            }

            // 原始配方已遍历完，切换到额外配方
            gtlCore$useOriginal = false;
        }

        // 检查额外配方，使用index遍历并应用canHandle测试
        return gtlCore$hasValidAdditionalRecipe();
    }

    /**
     * @author Dragons
     * @reason 使其可以找全每个HandlePart对应List<AbstractMapIngredient>中的所有配方，优化单配方模式性能
     *         不会返回null
     */
    @Overwrite(remap = false)
    @NotNull
    public GTRecipe next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        if (gtlCore$useOriginal) {
            // 非dive模式：优先使用单个配方缓存
            if (!gtlCore$useDiveIngredientTreeFind) {
                GTRecipe recipe = gtlCore$singleRecipe;
                gtlCore$singleRecipe = null;
                return recipe;
            }

            // dive模式：使用迭代器
            return gtlCore$presentIndexGTRecipesIterator.next();
        } else {
            return gtlCore$getNextValidAdditionalRecipe();
        }
    }

    /**
     * @author Dragons
     * @reason 重置所有状态
     */
    @Overwrite(remap = false)
    public void reset() {
        this.index = 0;
        this.gtlCore$singleRecipe = null;
        this.gtlCore$useOriginal = true;
        this.gtlCore$presentIndexGTRecipesIterator = null;
        this.gtlCore$additionalIndex = 0;
        this.gtlCore$cachedNextAdditional = null; // 清空缓存
    }

    @Unique
    private boolean gtlCore$findNextRecipeSet() {
        if (ingredients == null) return false;

        if (gtlCore$useDiveIngredientTreeFind) {
            // 使用diveIngredientTreeFind模式，收集所有配方
            ObjectOpenHashSet<GTRecipe> recipeSet = new ObjectOpenHashSet<>();
            while (this.index < this.ingredients.size()) {
                diveIngredientTreeFindRecipeCollection(ingredients.get(this.index),
                        recipeMap.getLookup().getLookup(),
                        canHandle, recipeSet);
                this.index++;

                recipeSet.remove(null);
                if (!recipeSet.isEmpty()) {
                    gtlCore$presentIndexGTRecipesIterator = recipeSet.iterator();
                    return true;
                }
            }
        } else {
            // 非dive模式：直接缓存单个配方，不使用迭代器
            while (this.index < this.ingredients.size()) {
                GTRecipe recipe = this.recipeMap.getLookup().recurseIngredientTreeFindRecipe(
                        this.ingredients,
                        this.recipeMap.getLookup().getLookup(),
                        this.canHandle,
                        this.index,
                        0,
                        1L << this.index);

                this.index++;

                if (recipe != null) {
                    gtlCore$singleRecipe = recipe;
                    return true;
                }
            }
        }

        gtlCore$presentIndexGTRecipesIterator = null;
        return false;
    }

    @Unique
    private boolean gtlCore$hasValidAdditionalRecipe() {
        if (gtlCore$cachedNextAdditional != null) {
            return true;
        }

        if (gtlCore$additionalRecipes == null) return false;

        while (gtlCore$additionalIndex < gtlCore$additionalRecipes.size()) {
            GTRecipe recipe = gtlCore$additionalRecipes.get(gtlCore$additionalIndex);
            gtlCore$additionalIndex++;

            if (canHandle.test(recipe)) {
                gtlCore$cachedNextAdditional = recipe; // 缓存找到的配方
                return true;
            }
        }
        return false;
    }

    @Unique
    private GTRecipe gtlCore$getNextValidAdditionalRecipe() {
        GTRecipe recipe = gtlCore$cachedNextAdditional;
        gtlCore$cachedNextAdditional = null;

        if (recipe == null) {
            throw new NoSuchElementException();
        }
        return recipe;
    }
}
