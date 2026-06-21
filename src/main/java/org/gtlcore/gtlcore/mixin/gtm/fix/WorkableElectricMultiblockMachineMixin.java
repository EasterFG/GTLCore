package org.gtlcore.gtlcore.mixin.gtm.fix;

import org.gtlcore.gtlcore.api.machine.trait.ICheckPatternMachine;
import org.gtlcore.gtlcore.api.machine.trait.ILockRecipe;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeCapabilityMachine;
import org.gtlcore.gtlcore.api.machine.trait.IRecipeStatus;
import org.gtlcore.gtlcore.api.recipe.RecipeText;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.ResearchStationMachine;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorkableElectricMultiblockMachine.class)
public abstract class WorkableElectricMultiblockMachineMixin extends WorkableMultiblockMachine implements IFancyUIMachine, ICheckPatternMachine {

    public WorkableElectricMultiblockMachineMixin(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Shadow(remap = false)
    protected EnergyContainerList energyContainer;

    @Shadow(remap = false)
    public abstract EnergyContainerList getEnergyContainer();

    @Shadow(remap = false)
    protected int tier;

    @Shadow(remap = false)
    public boolean isGenerator() {
        throw new AssertionError();
    }

    /**
     * @author mod_author, Dragons
     * @reason always 1A amperage, Fix 2 64A EnergyHatch
     */
    @Overwrite(remap = false)
    public long getOverclockVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = this.getEnergyContainer();
        }

        return Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());
    }

    /**
     * @author Dragons
     * @reason always 1A amperage
     */
    @Overwrite(remap = false)
    public long getMaxVoltage() {
        if (this.energyContainer == null) {
            this.energyContainer = getEnergyContainer();
        }
        return this.isGenerator() ? energyContainer.getOutputVoltage() : energyContainer.getNumHighestInputContainers() > 1 ? GTValues.V[Math.min(GTUtil.getTierByVoltage(energyContainer.getHighestInputVoltage()) + 1, GTValues.MAX)] : energyContainer.getHighestInputVoltage();
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        configuratorPanel.attachConfigurators(new IFancyConfiguratorButton.Toggle(
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0, 1, 0.5),
                GuiTextures.BUTTON_POWER.getSubTexture(0, 0.5, 1, 0.5),
                this::isWorkingEnabled, (clickData, pressed) -> this.setWorkingEnabled(pressed))
                .setTooltipsSupplier(pressed -> List.of(
                        Component.translatable(pressed ? "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"))));
        ICheckPatternMachine.attachConfigurators(configuratorPanel, self());
        if (this.self() instanceof ResearchStationMachine) return;
        IRecipeCapabilityMachine.attachConfigurators(configuratorPanel, (WorkableElectricMultiblockMachine) self());
        ILockRecipe.attachRecipeLockable(configuratorPanel, this.getRecipeLogic());
    }

    @Inject(method = "addDisplayText", at = @At(value = "INVOKE", target = "Lcom/gregtechceu/gtceu/api/machine/multiblock/WorkableElectricMultiblockMachine;getDefinition()Lcom/gregtechceu/gtceu/api/machine/MultiblockMachineDefinition;"), remap = false)
    public void addDisplayText(List<Component> textList, CallbackInfo ci) {
        if (this.isFormed()) {
            if (this.getRecipeLogic() instanceof IRecipeStatus status &&
                    status.getRecipeStatus() != null &&
                    status.getRecipeStatus().reason() != null) {
                textList.add(status.getRecipeStatus().reason().copy().withStyle(ChatFormatting.RED));
                if (status.getWorkingStatus() != null && status.getWorkingStatus().reason() != null)
                    textList.add(status.getWorkingStatus().reason().copy().withStyle(ChatFormatting.RED));
            }
        }
        if (this.getRecipeLogic() instanceof ILockRecipe iLockRecipe) {
            if (iLockRecipe.isLock() && iLockRecipe.getLockRecipe() != null) {
                textList.add(Component.translatable("gui.gtlcore.recipe_lock.recipe")
                        .withStyle((style -> style.withHoverEvent((new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                RecipeText.getRecipeInputText(iLockRecipe.getLockRecipe())
                                        .append(RecipeText.getRecipeOutputText(iLockRecipe.getLockRecipe()))))))));
            } else {
                textList.add(Component.translatable("gui.gtlcore.recipe_lock.no_recipe"));
            }
        }
    }

    @Override
    public boolean hasButton() {
        return true;
    }
}
