package org.gtlcore.gtlcore.mixin.gtm.recipe;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import dev.latvian.mods.kubejs.item.InputItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(GTRecipeSchema.GTRecipeJS.class)
public abstract class GTRecipeJSMixin {

    @Shadow(remap = false)
    public int chance;

    /**
     * Special handling for kubejs:ingot_field_shape: convert quantity from 64 to 8
     */
    @ModifyArg(
               method = "notConsumable(Ldev/latvian/mods/kubejs/item/InputItem;)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTRecipeJS;",
               at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTRecipeJS;inputItems([Ldev/latvian/mods/kubejs/item/InputItem;)Lcom/gregtechceu/gtceu/integration/kjs/recipe/GTRecipeSchema$GTRecipeJS;"),
               index = 0,
               remap = false)
    private InputItem[] modifyInputItemInNotConsumable(InputItem[] inputs) {
        // Check if we have exactly one input item and it needs modification
        if (inputs.length == 1) {
            InputItem itemStack = inputs[0];
            if (itemStack.count == 64) {
                Ingredient ingredient = itemStack.ingredient;
                ItemStack[] items = ingredient.getItems();

                if (items.length > 0) {
                    String itemId = BuiltInRegistries.ITEM.getKey(items[0].getItem()).toString();
                    if ("kubejs:ingot_field_shape".equals(itemId)) {
                        // Return a new array with modified InputItem (count 8 instead of 64)
                        return new InputItem[] { InputItem.of(ingredient, 8) };
                    }
                }
            }
        }

        // Return the original inputs array if no modification needed
        return inputs;
    }
}
