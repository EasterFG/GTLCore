package org.gtlcore.gtlcore.mixin.gtm;

import org.gtlcore.gtlcore.api.gui.PatternPreviewWidget;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoWrapper;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MultiblockInfoWrapper.class)
public class MultiblockInfoWrapperMixin {

    @ModifyArg(method = "<init>",
               at = @At(value = "INVOKE",
                        target = "Lcom/lowdragmc/lowdraglib/jei/ModularWrapper;<init>(Lcom/lowdragmc/lowdraglib/gui/widget/Widget;)V"))
    private static Widget MultiblockInfoWrapper(Widget par1, @Local(ordinal = 0, argsOnly = true) MultiblockMachineDefinition definition) {
        return PatternPreviewWidget.getPatternWidget(definition);
    }
}
