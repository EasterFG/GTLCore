package org.gtlcore.gtlcore.mixin.extendedae;

import org.gtlcore.gtlcore.config.GTLConfigHolder;

import com.glodblock.github.extendedae.common.parts.PartExPatternProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PartExPatternProvider.class)
public class PartExPatternProviderMixin {

    @ModifyConstant(method = "createLogic", remap = false, constant = @Constant(intValue = 36))
    private int modifyContainer(int constant) {
        return GTLConfigHolder.INSTANCE.exPatternProvider;
    }
}
