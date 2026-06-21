package org.gtlcore.gtlcore.mixin.gtm.machine;

import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;

import com.lowdragmc.lowdraglib.gui.widget.*;

import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RotorHolderPartMachine.class)
public abstract class RotorHolderPartMachineMixin implements IRotorHolderMachine {

    @Mutable
    @Final
    @Shadow(remap = false)
    public final NotifiableItemStackHandler inventory;

    protected RotorHolderPartMachineMixin(NotifiableItemStackHandler inventory) {
        this.inventory = inventory;
    }

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void RotorHolderPartMachine(IMachineBlockEntity holder, int tier, CallbackInfo ci) {
        this.inventory.setFilter((itemstack) -> itemstack.is(GTItems.TURBINE_ROTOR.asItem()));
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        GTRecipe modifiedRecipe = this.isFrontFaceFree() && this.hasRotor() ? recipe : null;
        if (modifiedRecipe != null) return modifiedRecipe;
        else if (this.getRotorStack().isEmpty()) {
            RecipeResult.of((IRecipeLogicMachine) this.getControllers().get(0),
                    RecipeResult.fail(Component.translatable("gtceu.recipe.fail.rotor.isEmpty")));
        } else {
            RecipeResult.of((IRecipeLogicMachine) this.getControllers().get(0),
                    RecipeResult.fail(Component.translatable("gtceu.multiblock.universal.rotor_obstructed")));
        }
        return null;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public Widget createUIWidget() {
        WidgetGroup group = new WidgetGroup(0, 0, 34, 34);
        WidgetGroup container = new WidgetGroup(4, 4, 26, 26);
        container.addWidget((new SlotWidget(this.inventory.storage, 0, 4, 4, true, true))
                .setBackground(GuiTextures.SLOT, GuiTextures.TURBINE_OVERLAY)
                .setHoverTooltips(Component.translatable("tooltip.gtlcore.turbine_rotor_only")));
        container.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(container);
        return group;
    }
}
