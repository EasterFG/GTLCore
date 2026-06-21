package org.gtlcore.gtlcore.mixin.gtm;

import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.utils.NumberUtils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.integration.jade.provider.RecipeLogicProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static net.minecraft.ChatFormatting.*;
import static org.gtlcore.gtlcore.utils.TextUtil.GTL_CORE$VC;

/**
 * @author EasterFG on 2024/12/10
 */
@Mixin(RecipeLogicProvider.class)
public abstract class RecipeLogicProviderMixin extends CapabilityBlockProvider<RecipeLogic> {

    protected RecipeLogicProviderMixin(ResourceLocation uid) {
        super(uid);
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lcom/gregtechceu/gtceu/api/machine/trait/RecipeLogic;)V", at = @At("HEAD"), remap = false)
    protected void write(CompoundTag data, RecipeLogic capability, CallbackInfo ci) {
        if (capability instanceof IRecipeStatus status) {
            if (status.getRecipeStatus() != null && status.getRecipeStatus().reason() != null)
                data.putString("reason", status.getRecipeStatus().reason().getString());
            if (status.getWorkingStatus() != null && status.getWorkingStatus().reason() != null)
                data.putString("work_reason", status.getWorkingStatus().reason().getString());
        }
    }

    @Override
    protected void addTooltip(CompoundTag capData, ITooltip tooltip, Player player, BlockAccessor block,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (capData.getBoolean("Working")) {
            var recipeInfo = capData.getCompound("Recipe");
            if (!recipeInfo.isEmpty()) {
                var EUt = recipeInfo.getLong("EUt");
                var isInput = recipeInfo.getBoolean("isInput");

                long absEUt = Math.abs(EUt);

                // Default behavior, if this TE is not a steam machine (or somehow not instanceof
                // IGregTechTileEntity...)
                var tier = GTUtil.getTierByVoltage(absEUt);
                Component text = Component.literal(NumberUtils.formatLong(absEUt)).withStyle(RED)
                        .append(Component.literal(" EU/t").withStyle(RESET)
                                .append(Component.literal(" (").withStyle(GREEN)
                                        .append(Component
                                                .translatable("gtceu.top.electricity",
                                                        FormattingUtil.formatNumber2Places(absEUt / ((float) GTValues.V[tier])),
                                                        GTValues.VNF[tier])
                                                .withStyle(style -> style.withColor(GTL_CORE$VC[tier])))
                                        .append(Component.literal(")").withStyle(GREEN))));

                if (EUt > 0) {
                    if (isInput) {
                        tooltip.add(Component.translatable("gtceu.top.energy_consumption").append(" ").append(text));
                    } else {
                        tooltip.add(Component.translatable("gtceu.top.energy_production").append(" ").append(text));
                    }
                }
            }
            String reason = capData.getString("work_reason");
            if (reason.isEmpty()) return;
            tooltip.add(Component.translatable("gtceu.recipe.fail.reason", reason).withStyle(RED));
        } else {
            String reason = capData.getString("reason");
            if (reason.isEmpty()) return;
            tooltip.add(Component.translatable("gtceu.recipe.fail.reason", reason).withStyle(RED));
        }
    }
}
