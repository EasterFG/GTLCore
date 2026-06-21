package org.gtlcore.gtlcore.mixin.ae2.stacks;

import net.minecraft.world.item.Item;

import appeng.api.stacks.AEItemKey;
import org.spongepowered.asm.mixin.*;

/**
 * 代码参考自gto
 * &#064;line <a href="https://github.com/GregTech-Odyssey/GTOCore">...</a>
 */

@Mixin(AEItemKey.class)
public abstract class AEItemKeyMixin {

    private int fuzzySearchMaxValue = -1;

    @Shadow(remap = false)
    @Final
    @Mutable
    private final Item item;

    public AEItemKeyMixin(Item item) {
        this.item = item;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public int getFuzzySearchMaxValue() {
        if (this.fuzzySearchMaxValue < 0) this.fuzzySearchMaxValue = this.item.getMaxDamage();
        return fuzzySearchMaxValue;
    }
}
