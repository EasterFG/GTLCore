package org.gtlcore.gtlcore.api.recipe;

import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class RecipeText {

    public static MutableComponent getRecipeInputText(GTRecipe recipe) {
        return getIngredientText(recipe, true);
    }

    public static MutableComponent getRecipeOutputText(GTRecipe recipe) {
        return getIngredientText(recipe, false);
    }

    private static MutableComponent getIngredientText(GTRecipe recipe, boolean io) {
        if (recipe != null) {
            MutableComponent buffer = io ?
                    Component.translatable("gtceu.top.recipe_input").withStyle(ChatFormatting.WHITE) :
                    Component.translatable("gtceu.top.recipe_output").withStyle(ChatFormatting.WHITE);
            var items = io ? recipe.getInputContents(ItemRecipeCapability.CAP) : recipe.getOutputContents(ItemRecipeCapability.CAP);
            var fluids = io ? recipe.getInputContents(FluidRecipeCapability.CAP) : recipe.getOutputContents(FluidRecipeCapability.CAP);
            for (var item : items) {
                var stacks = ItemRecipeCapability.CAP.of(item.content).getItems();
                if (stacks.length == 0) continue;
                var stack = stacks[0];
                int count = stack.getCount();
                float chance = 100.0F * (float) item.chance / (float) item.maxChance;
                String percent = FormattingUtil.formatPercent(chance);
                if (chance == 100.0F) {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.0", stack.getHoverName(), count).withStyle(ChatFormatting.GRAY));
                } else if (chance == 0.0F) {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.2", stack.getHoverName(), count).withStyle(ChatFormatting.GRAY));
                } else {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.1", stack.getHoverName(), count, percent).withStyle(ChatFormatting.GRAY));
                }
            }
            for (var fluid : fluids) {
                var stacks = FluidRecipeCapability.CAP.of(fluid.content).getStacks();
                if (stacks.length == 0) continue;
                var stack = stacks[0];
                long amount = stack.getAmount();
                String s;
                if (amount < 1000) s = amount + "mB";
                else s = NumberUtils.formatLong(amount / 1000) + "B";
                float chance = 100.0F * (float) fluid.chance / (float) fluid.maxChance;
                String percent = FormattingUtil.formatPercent(chance);
                if (chance == 100.0F) {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.0", stack.getDisplayName(), s).withStyle(ChatFormatting.GRAY));
                } else if (chance == 0.0F) {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.2", stack.getDisplayName(), s).withStyle(ChatFormatting.GRAY));
                } else {
                    buffer.append(Component.translatable("gtceu.machine.lockrecipe.line.1", stack.getDisplayName(), s, percent).withStyle(ChatFormatting.GRAY));
                }
            }
            return buffer;
        }
        return null;
    }
}
