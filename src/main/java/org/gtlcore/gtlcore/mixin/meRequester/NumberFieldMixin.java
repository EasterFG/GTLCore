package org.gtlcore.gtlcore.mixin.meRequester;

import com.almostreliable.merequester.client.widgets.NumberField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(NumberField.class)
public class NumberFieldMixin {

    @ModifyConstant(
                    method = "<init>",
                    constant = @Constant(intValue = 7))
    private int replaceMaxLength(int seven) {
        return 15;
    }
}
