package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableEnergyContainer;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;

@Mixin(FusionReactorMachine.class)
public class FusionReactorMachineMixin extends WorkableElectricMultiblockMachine {

    @Shadow(remap = false)
    protected long heat = 0L;
    @Mutable
    @Final
    @Shadow(remap = false)
    protected final NotifiableEnergyContainer energyContainer;

    @Shadow(remap = false)
    protected void updatePreHeatSubscription() {}

    public FusionReactorMachineMixin(IMachineBlockEntity holder, NotifiableEnergyContainer energyContainer, Object... args) {
        super(holder, args);
        this.energyContainer = energyContainer;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static @Nullable GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params, @NotNull OCResult result) {
        if (machine instanceof FusionReactorMachineMixin fm) {
            if (RecipeHelper.getRecipeEUtTier(recipe) <= fm.getTier() && recipe.data.contains("eu_to_start")) {
                if (recipe.data.getLong("eu_to_start") <= fm.energyContainer.getEnergyCapacity()) {
                    long heatDiff = recipe.data.getLong("eu_to_start") - fm.heat;
                    if (heatDiff <= 0L) {
                        return RecipeHelper.applyOverclock(new OverclockingLogic(0.5F, 2.0F, false), recipe, fm.getMaxVoltage(), params, result);
                    } else if (fm.energyContainer.getEnergyStored() < heatDiff) {
                        return null;
                    } else {
                        fm.energyContainer.removeEnergy(heatDiff);
                        fm.heat += heatDiff;
                        fm.updatePreHeatSubscription();
                        return RecipeHelper.applyOverclock(new OverclockingLogic(0.5F, 2.0F, false), recipe, fm.getMaxVoltage(), params, result);
                    }
                }
                RecipeResult.of(fm, RecipeResult.fail(Component.translatable("gtceu.recipe.fail.no.enough.cache.energy")));
            }
        }
        return null;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor_energy", this.energyContainer.getEnergyStored() / 1000000, this.energyContainer.getEnergyCapacity() / 1000000));
            textList.add(Component.translatable("gtceu.multiblock.fusion_reactor.heat", this.heat));
        }
    }
}
