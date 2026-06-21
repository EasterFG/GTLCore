package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.DataBankMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.DataAccessHatchMachine;
import com.gregtechceu.gtceu.common.recipe.condition.ResearchCondition;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Set;

@Mixin(DataAccessHatchMachine.class)
public abstract class DataAccessHatchMachineMixin extends TieredPartMachine implements IDataAccessHatch {

    @Mutable
    @Final
    @Shadow(remap = false)
    private final Set<GTRecipe> recipes;

    @Shadow(remap = false)
    public abstract boolean isCreative();

    @Shadow(remap = false)
    public abstract GTRecipe modifyRecipe(GTRecipe recipe);

    public DataAccessHatchMachineMixin(IMachineBlockEntity holder, int tier, Set<GTRecipe> recipes) {
        super(holder, tier);
        this.recipes = recipes;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean isRecipeAvailable(@NotNull GTRecipe recipe, @NotNull Collection<IDataAccessHatch> seen) {
        seen.add(this);
        if (recipe.conditions.stream().noneMatch(ResearchCondition.class::isInstance) ||
                this.recipes.contains(recipe))
            return true;
        else {
            for (var c : this.getControllers()) {
                if (c instanceof DataBankMachine) continue;
                RecipeResult.of((IRecipeLogicMachine) c, RecipeResult.FAIL_NO_FIND_RESEARCHED);
            }
            return false;
        }
    }
}
