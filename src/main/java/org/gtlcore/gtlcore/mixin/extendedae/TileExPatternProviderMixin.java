package org.gtlcore.gtlcore.mixin.extendedae;

import com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider;
import org.gtlcore.gtlcore.config.ConfigHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(TileExPatternProvider.class)
public class TileExPatternProviderMixin {

    @ModifyConstant(method = "createLogic", remap = false, constant = @Constant(intValue = 36))
    private int modifyContainer(int constant) {
        return ConfigHolder.INSTANCE.exPatternProvider;
    }
}