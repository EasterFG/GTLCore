package org.gtlcore.gtlcore.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.*;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

/**
 * @author EasterFG on 2025/3/21
 */
public class GTLUtil {

    public static String getItemId(Item item) {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

    public static String getFluidId(Fluid fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid).toString();
    }

    @SuppressWarnings("deprecation")
    public static ItemStack loadItemStack(CompoundTag compoundTag) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
            ItemStack stack = new ItemStack(item, 1);
            if (compoundTag.contains("tag", Tag.TAG_COMPOUND)) {
                stack.setTag(compoundTag.getCompound("tag"));
                if (stack.getTag() != null) {
                    stack.getItem().verifyTagAfterLoad(stack.getTag());
                }
            }

            if (stack.getItem().canBeDepleted()) {
                stack.setDamageValue(stack.getDamageValue());
            }
            return stack;
        } catch (RuntimeException var2) {
            GTCEu.LOGGER.debug("Tried to load invalid item: {}", compoundTag, var2);
            return ItemStack.EMPTY;
        }
    }

    /**
     * 代码参考自gto
     * &#064;line <a href="https://github.com/GregTech-Odyssey/GTOCore">...</a>
     */

    public static Tag serializeNBT(GTRecipe recipe) {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", recipe.id.toString());
        tag.put("recipe", GTRecipeSerializer.CODEC.encodeStart(NbtOps.INSTANCE, recipe).result().orElse(new CompoundTag()));
        return tag;
    }

    public static @Nullable GTRecipe deserializeNBT(Tag tag) {
        if (tag instanceof CompoundTag ctag) {
            var id = ResourceLocation.tryParse(ctag.getString("id"));
            var recipe = GTRecipeSerializer.CODEC.parse(NbtOps.INSTANCE, ctag.get("recipe")).result().orElse(null);
            if (recipe == null || id == null) return null;
            recipe.setId(id);
            return recipe;
        }
        return null;
    }
}
