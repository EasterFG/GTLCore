package org.gtlcore.gtlcore.mixin.gtm.recipe.Ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(IntProviderIngredient.class)
public interface IntProviderIngredientAccessor {

    @Accessor(value = "itemStacks", remap = false)
    ItemStack[] getItemStack();

    @Accessor(value = "sampledCount", remap = false)
    Integer getSampledCount();
}
