package org.gtlcore.gtlcore.mixin.ae2.crafting;

import net.minecraft.world.level.Level;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.*;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ICraftingInventory;
import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(CraftingCpuHelper.class)
public class CraftingCpuHelperMixin {

    /**
     * @author .
     * @reason 提升性能
     */
    @Overwrite(remap = false)
    public static Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv,
                                                                IPatternDetails.IInput input, Level level) {
        var possibleInputs = input.getPossibleInputs();
        List<InputTemplate> substitutes = new ObjectArrayList<>(possibleInputs.length);
        for (var stack : possibleInputs) {
            for (var fuzz : inv.findFuzzyTemplates(stack.what())) {
                if (fuzz instanceof AEItemKey aeItemKey && !aeItemKey.matches(stack)) continue;
                else if (fuzz instanceof AEFluidKey aeFluidKey && !aeFluidKey.matches(stack)) continue;
                substitutes.add(new InputTemplate(fuzz, stack.amount()));
            }
        }
        return Iterables.filter(substitutes, stack -> input.isValid(stack.key(), level));
    }
}
