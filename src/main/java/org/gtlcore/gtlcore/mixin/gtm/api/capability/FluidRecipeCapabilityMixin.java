package org.gtlcore.gtlcore.mixin.gtm.api.capability;

import org.gtlcore.gtlcore.api.recipe.IParallelLogic;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.*;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;

import com.lowdragmc.lowdraglib.gui.widget.TankWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.common.primitives.Ints;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;
import java.util.function.BiConsumer;

import static com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability.CAP;

@Mixin(FluidRecipeCapability.class)
public class FluidRecipeCapabilityMixin extends RecipeCapability<FluidIngredient> {

    protected FluidRecipeCapabilityMixin(String name, int color, boolean doRenderSlot, int sortIndex, IContentSerializer<FluidIngredient> serializer) {
        super(name, color, doRenderSlot, sortIndex, serializer);
    }

    /**
     * @author Adonis
     * @reason 流体输入输出上限改为long
     */
    @Overwrite(remap = false)
    public FluidIngredient copyWithModifier(FluidIngredient content, ContentModifier modifier) {
        if (content.isEmpty()) {
            return content.copy();
        } else {
            FluidIngredient copy = content.copy();
            copy.setAmount(modifier.apply(copy.getAmount()).longValue());
            return copy;
        }
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public int limitParallel(GTRecipe recipe, IRecipeCapabilityHolder holder, int multiplier) {
        return Ints.saturatedCast(IParallelLogic.getOutputFluidParallel(holder, recipe, recipe.getOutputContents(CAP), multiplier));
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public int getMaxParallelRatio(IRecipeCapabilityHolder holder, GTRecipe recipe, int parallelAmount) {
        return Ints.saturatedCast(IParallelLogic.getInputFluidParallel(holder, recipe, parallelAmount));
    }

    @OnlyIn(Dist.CLIENT)
    @ModifyArg(method = "applyWidgetInfo",
               at = @At(value = "INVOKE",
                        target = "Lcom/lowdragmc/lowdraglib/gui/widget/TankWidget;setOnAddedTooltips(Ljava/util/function/BiConsumer;)Lcom/lowdragmc/lowdraglib/gui/widget/TankWidget;"),
               remap = false)
    public BiConsumer<TankWidget, List<Component>> applyWidgetInfo(BiConsumer onAddedTooltips,
                                                                   @Local(name = "content") Content content,
                                                                   @Local(name = "recipe") GTRecipe recipe,
                                                                   @Local(name = "index") int index,
                                                                   @Local(name = "io") IO io) {
        return (w, tooltips) -> {
            var ingredient = FluidRecipeCapability.CAP.of(content.content);
            if (ingredient.getStacks().length > 0) {
                var stack = ingredient.getStacks()[0];
                TooltipsHandler.appendFluidTooltips(stack.getFluid(),
                        stack.getAmount(), tooltips::add, TooltipFlag.NORMAL);
            }
            GTRecipeWidget.setConsumedChance(content, ChanceLogic.OR, tooltips);
            if (isTickSlot(index, io, recipe)) {
                tooltips.add(Component.translatable("gtceu.gui.content.per_tick"));
            }
        };
    }
}
