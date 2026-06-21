package org.gtlcore.gtlcore.mixin.mc;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static org.gtlcore.gtlcore.utils.NumberUtils.UNITS;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin {

    @Redirect(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/String;valueOf(I)Ljava/lang/String;"))
    public String renderItemDecorations(int i, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
        return core$format(stack.getCount());
    }

    @Unique
    private String core$format(int number) {
        double temp = number;
        int unitIndex = 0;
        while (temp >= 1000 && unitIndex < UNITS.length - 1) {
            temp /= 1000;
            unitIndex++;
        }
        return FormattingUtil.DECIMAL_FORMAT_0F.format(temp) + UNITS[unitIndex];
    }
}
