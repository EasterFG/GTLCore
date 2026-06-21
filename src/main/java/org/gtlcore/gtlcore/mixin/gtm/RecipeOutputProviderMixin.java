package org.gtlcore.gtlcore.mixin.gtm;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.utils.GTLUtil;
import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeOutputProvider;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author EasterFG on 2025/3/8
 */
@Mixin(RecipeOutputProvider.class)
public abstract class RecipeOutputProviderMixin extends CapabilityBlockProvider<RecipeLogic> {

    @Shadow(remap = false)
    protected abstract void addFluidTooltips(ITooltip iTooltip, List<FluidStack> outputFluids);

    @Shadow(remap = false)
    protected abstract Component getItemName(ItemStack stack);

    protected RecipeOutputProviderMixin(ResourceLocation uid) {
        super(uid);
    }

    @Override
    protected void write(CompoundTag data, RecipeLogic recipeLogic) {
        if (recipeLogic.isWorking()) {
            data.putBoolean("Working", recipeLogic.isWorking());
            var recipe = recipeLogic.getLastRecipe();
            if (recipe != null) {
                Map<String, CompoundTag> cache = new HashMap<>();
                // int count = 0;
                ListTag itemTags = new ListTag();
                for (Ingredient outputContent : RecipeHelper.getOutputContents(recipe, ItemRecipeCapability.CAP)) {
                    final var stack = outputContent.getItems()[0];
                    if (stack == null || stack.isEmpty()) continue;
                    String sid = GTLUtil.getItemId(stack.getItem());
                    if (outputContent instanceof LongIngredient longIngredient) {
                        if (cache.containsKey(sid)) {
                            CompoundTag tag = cache.get(sid);
                            if (tag != null) {
                                long amount = tag.getLong("Count");
                                if (amount > 0) {
                                    tag.putLong("Count", amount + longIngredient.getActualAmount());
                                }
                            }
                        } else {
                            var itemTag = new CompoundTag();
                            itemTag.putString("id", sid);
                            itemTag.putLong("Count", longIngredient.getActualAmount());
                            if (stack.getTag() != null) {
                                itemTag.put("tag", stack.getTag().copy());
                            }
                            cache.put(sid, itemTag);
                            itemTags.add(itemTag);
                        }
                    } else {
                        if (cache.containsKey(sid)) {
                            CompoundTag tag = cache.get(sid);
                            if (tag != null) {
                                long amount = tag.getLong("Count");
                                if (amount > 0) {
                                    tag.putLong("Count", amount + stack.getCount());
                                }
                            }
                        } else {
                            var itemTag = new CompoundTag();
                            itemTag.putString("id", sid);
                            itemTag.putLong("Count", stack.getCount());
                            if (stack.getTag() != null) {
                                itemTag.put("tag", stack.getTag().copy());
                            }
                            cache.put(sid, itemTag);
                            itemTags.add(itemTag);
                        }
                    }
                }
                if (!itemTags.isEmpty()) {
                    data.put("OutputItems", itemTags);
                }
                ListTag fluidTags = new ListTag();
                for (var stack : RecipeHelper.getOutputFluids(recipe)) {
                    if (stack != null && !stack.isEmpty()) {
                        var fid = GTLUtil.getFluidId(stack.getFluid());
                        if (cache.containsKey(fid)) {
                            CompoundTag tag = cache.get(fid);
                            if (tag != null) {
                                long amount = tag.getLong("Amount");
                                if (amount > 0) {
                                    tag.putLong("Amount", amount + stack.getAmount());
                                }
                            }
                        } else {
                            var fluidTag = new CompoundTag();
                            fluidTag.putString("FluidName", fid);
                            fluidTag.putLong("Amount", stack.getAmount());

                            if (stack.getTag() != null) {
                                fluidTag.put("Tag", stack.getTag().copy());
                            }
                            cache.put(fid, fluidTag);
                            fluidTags.add(fluidTag);
                        }
                    }
                }
                if (!fluidTags.isEmpty()) {
                    data.put("OutputFluids", fluidTags);
                }
                // data.putInt("ExtraOutput", count);
            }
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (capData.getBoolean("Working")) {
            List<CompoundTag> outputItems = new ArrayList<>();
            if (capData.contains("OutputItems", Tag.TAG_LIST)) {
                ListTag itemTags = capData.getList("OutputItems", Tag.TAG_COMPOUND);
                for (Tag tag : itemTags) {
                    if (tag instanceof CompoundTag tCompoundTag) {
                        outputItems.add(tCompoundTag);
                    }
                }
            }
            List<FluidStack> outputFluids = new ArrayList<>();
            if (capData.contains("OutputFluids", Tag.TAG_LIST)) {
                ListTag fluidTags = capData.getList("OutputFluids", Tag.TAG_COMPOUND);
                for (Tag tag : fluidTags) {
                    if (tag instanceof CompoundTag tCompoundTag) {
                        var stack = FluidStack.loadFromTag(tCompoundTag);
                        if (!stack.isEmpty()) {
                            outputFluids.add(stack);
                        }
                    }
                }
            }
            if (!outputItems.isEmpty() || !outputFluids.isEmpty()) {
                tooltip.add(Component.translatable("gtceu.top.recipe_output"));
            }
            gTLCore$addItemTooltips(tooltip, outputItems);
            addFluidTooltips(tooltip, outputFluids);
        }
    }

    @Unique
    private void gTLCore$addItemTooltips(ITooltip iTooltip, List<CompoundTag> outputItems) {
        IElementHelper helper = iTooltip.getElementHelper();
        for (CompoundTag tag : outputItems) {
            if (tag != null && !tag.isEmpty()) {
                ItemStack stack = GTLUtil.loadItemStack(tag);
                long count = tag.getLong("Count");
                iTooltip.add(helper.smallItem(stack));
                Component text = Component.literal(" ")
                        .append(NumberUtils.formatLong(count))
                        .append("× ")
                        .append(getItemName(stack))
                        .withStyle(ChatFormatting.WHITE);
                iTooltip.append(text);
            }
        }
    }
}
