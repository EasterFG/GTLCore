package org.gtlcore.gtlcore.mixin.gtm.registry;

import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.utils.GTUtil;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GTRecipeSchema.GTRecipeJS.class)
public abstract class GTRecipeJSMixin {

    @Shadow(remap = false)
    public abstract GTRecipeSchema.GTRecipeJS addData(String key, int data);

    @Inject(method = "EUt", at = @At("HEAD"), remap = false)
    public void EUt(long eu, CallbackInfoReturnable<GTRecipeSchema.GTRecipeJS> cir) {
        this.addData("euTier", GTUtil.getTierByVoltage(eu > 0 ? eu : -eu));
    }
}
