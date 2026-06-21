package org.gtlcore.gtlcore.mixin.ae2.pattern;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.KeyCounter;
import appeng.crafting.pattern.AEProcessingPattern;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(AEProcessingPattern.class)
public abstract class AEProcessingPatternMixin implements IPatternDetails {

    /**
     * @author Dragons
     * @reason Performance
     */
    @Overwrite(remap = false)
    public void pushInputsToExternalInventory(KeyCounter[] inputHolder, IPatternDetails.PatternInputSink inputSink) {
        IPatternDetails.super.pushInputsToExternalInventory(inputHolder, inputSink);
    }
}
