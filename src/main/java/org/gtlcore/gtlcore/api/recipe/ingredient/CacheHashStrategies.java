package org.gtlcore.gtlcore.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.core.mixins.*;
import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IntersectionIngredient;
import net.minecraftforge.common.crafting.PartialNBTIngredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.Hash;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class CacheHashStrategies {

    public static class IngredientHashStrategy implements Hash.Strategy<Ingredient> {

        public static final IngredientHashStrategy INSTANCE = new IngredientHashStrategy();
        private static final ItemStackHashStrategy ITEM_TAG_STRATEGY = ItemStackHashStrategy.comparingAllButCount();
        private static final ItemStackHashStrategy ITEM_STRATEGY = ItemStackHashStrategy.builder().compareItem(true).build();

        @Override
        public int hashCode(Ingredient o) {
            int hashCode = 537;
            if (o instanceof StrictNBTIngredientAccessor strict) {
                hashCode *= 31 * ITEM_TAG_STRATEGY.hashCode(strict.getStack());
            } else if (o instanceof PartialNBTIngredientAccessor partial) {
                hashCode *= 31 * partial.getNbt().hashCode();
                hashCode *= 31 * partial.getItems().hashCode();
            } else if (o instanceof IntersectionIngredientAccessor intersection) {
                for (Ingredient ingredient : intersection.getChildren()) {
                    hashCode *= 31 * this.hashCode(ingredient);
                }
            } else if (o instanceof IngredientAccessor ingredient) {
                for (Ingredient.Value value : ingredient.getValues()) {
                    if (value instanceof TagValueAccessor tagValue) {
                        hashCode *= 31 * tagValue.getTag().hashCode();
                    } else {
                        for (ItemStack stack : value.getItems()) {
                            hashCode *= 31 * ITEM_STRATEGY.hashCode(stack);
                        }
                    }
                }
            }
            return hashCode;
        }

        @Override
        public boolean equals(Ingredient a, Ingredient b) {
            return IngredientEquality.ingredientEquals(a, b);
        }
    }

    /**
     * FluidIngredient Hash策略 - 忽略amount数量
     * 基于FluidIngredient源代码的完整实现，支持TagValue和FluidValue
     */
    public static class FluidIngredientHashStrategy implements Hash.Strategy<FluidIngredient> {

        public static final FluidIngredientHashStrategy INSTANCE = new FluidIngredientHashStrategy();

        @Override
        public int hashCode(FluidIngredient ingredient) {
            if (ingredient == null) return 0;

            int result = Arrays.hashCode(ingredient.values);
            result = 31 * result + Objects.hashCode(ingredient.getNbt());
            return result;
        }

        @Override
        public boolean equals(FluidIngredient a, FluidIngredient b) {
            return fluidIngredientEqualsIgnoreAmount(a, b);
        }

        private static boolean fluidIngredientEqualsIgnoreAmount(FluidIngredient a, FluidIngredient b) {
            if (a == b) return true;
            if (a == null || b == null) return false;

            if (!Objects.equals(a.getNbt(), b.getNbt())) {
                return false;
            }

            if (a.values.length != b.values.length) {
                return false;
            }

            for (FluidIngredient.Value value1 : a.values) {
                for (FluidIngredient.Value value2 : b.values) {
                    if (value1 instanceof FluidIngredient.TagValue tagValue1) {
                        if (!(value2 instanceof FluidIngredient.TagValue tagValue2)) {
                            return false;
                        }
                        if (tagValue1.getTag() != tagValue2.getTag()) {
                            return false;
                        }
                    } else if (value1 instanceof FluidIngredient.FluidValue first) {
                        if (!(value2 instanceof FluidIngredient.FluidValue second)) {
                            return false;
                        }
                        if (first.hashCode() != second.hashCode()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
    }

    public class IngredientEquality {

        public static final Comparator<Item> ITEM_COMPARATOR = Comparator.comparing(BuiltInRegistries.ITEM::getKey);

        public static final Comparator<Ingredient.Value> INGREDIENT_VALUE_COMPARATOR = (value1, value2) -> {
            if (value1 instanceof TagValueAccessor first) {
                if (!(value2 instanceof TagValueAccessor second)) {
                    return 10;
                }
                if (first.getTag() != second.getTag()) {
                    return 1;
                }
            } else if (value1 instanceof ItemValueAccessor first) {
                if (!(value2 instanceof ItemValueAccessor second)) {
                    return 10;
                }
                return ITEM_COMPARATOR.compare(first.getItem().getItem(), second.getItem().getItem());
            }
            return 0;
        };

        public static final Comparator<Ingredient> INGREDIENT_COMPARATOR = new Comparator<>() {

            @Override
            public int compare(Ingredient first, Ingredient second) {
                if (first instanceof StrictNBTIngredient strict1) {
                    if (second instanceof StrictNBTIngredientAccessor strict2) {
                        return strict1.test(strict2.getStack()) ? 0 : 1;
                    }
                    return 1;
                }
                if (first instanceof PartialNBTIngredient partial1) {
                    if (second instanceof PartialNBTIngredient partial2) {
                        if (partial1.getItems().length != partial2.getItems().length)
                            return 1;
                        for (ItemStack stack : partial1.getItems()) {
                            if (!partial2.test(stack)) {
                                return 1;
                            }
                        }
                        return 0;
                    }
                    return 1;
                }

                if (first instanceof IntersectionIngredient intersection1) {
                    if (second instanceof IntersectionIngredient intersection2) {
                        List<Ingredient> ingredients1 = Lists
                                .newArrayList(((IntersectionIngredientAccessor) intersection1).getChildren());
                        List<Ingredient> ingredients2 = Lists
                                .newArrayList(((IntersectionIngredientAccessor) intersection2).getChildren());
                        if (ingredients1.size() != ingredients2.size()) return 1;

                        ingredients1.sort(this);
                        ingredients2.sort(this);

                        for (int i = 0; i < ingredients1.size(); ++i) {
                            Ingredient ingredient1 = ingredients1.get(i);
                            Ingredient ingredient2 = ingredients2.get(i);
                            int result = compare(ingredient1, ingredient2);
                            if (result != 0) {
                                return result;
                            }
                        }
                        return 0;
                    }
                    return 1;
                }

                Ingredient.Value[] firstValues = ((IngredientAccessor) first).getValues();
                Ingredient.Value[] secondValues = ((IngredientAccessor) second).getValues();
                if (firstValues.length != secondValues.length) return 1;

                firstValues = firstValues.clone();
                secondValues = secondValues.clone();
                Arrays.parallelSort(firstValues, INGREDIENT_VALUE_COMPARATOR);
                Arrays.parallelSort(secondValues, INGREDIENT_VALUE_COMPARATOR);

                for (int i = 0; i < firstValues.length; ++i) {
                    Ingredient.Value value1 = firstValues[i];
                    Ingredient.Value value2 = secondValues[i];
                    int result = INGREDIENT_VALUE_COMPARATOR.compare(value1, value2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };

        public static boolean ingredientEquals(Ingredient first, Ingredient second) {
            if (first == second) return true;
            if (first == null || second == null) return false;

            first = getInner(first);
            second = getInner(second);
            return cmp(first, second);
        }

        private static boolean cmp(Ingredient first, Ingredient second) {
            return INGREDIENT_COMPARATOR.compare(first, second) == 0;
        }

        private static Ingredient getInner(Ingredient ingredient) {
            if (ingredient instanceof SizedIngredient sizedIngredient) {
                return getInner(sizedIngredient.getInner());
            } else if (ingredient instanceof IntProviderIngredient intProviderIngredient) {
                return getInner(intProviderIngredient.getInner());
            }
            return ingredient;
        }
    }
}
