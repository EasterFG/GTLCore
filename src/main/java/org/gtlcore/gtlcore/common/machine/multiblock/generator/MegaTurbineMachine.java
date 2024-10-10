package org.gtlcore.gtlcore.common.machine.multiblock.generator;

import org.gtlcore.gtlcore.common.machine.multiblock.part.RotorHatchPartMachine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.RotorHolderPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MegaTurbineMachine extends WorkableElectricMultiblockMachine implements ITieredMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MegaTurbineMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public static final int MIN_DURABILITY_TO_WARN = 10;

    private final int baseEuOutput;
    @Getter
    private final int tier;
    private int excessVoltage;

    private Set<RotorHolderPartMachine> rotorHolderMachines;
    private RotorHatchPartMachine rotorHatchPartMachine = null;
    protected ConditionalSubscriptionHandler rotorSubs;

    public MegaTurbineMachine(IMachineBlockEntity holder, int tier, int am) {
        super(holder);
        this.tier = tier;
        this.baseEuOutput = (int) GTValues.V[tier] * am;
        this.rotorSubs = new ConditionalSubscriptionHandler(this, this::rotorUpdate, this::isFormed);
    }

    private void rotorUpdate() {
        if (rotorHatchPartMachine != null && getOffsetTimer() % 20 == 0) {
            if (rotorHatchPartMachine.getInventory().isEmpty()) return;
            ItemStackTransfer storage = rotorHatchPartMachine.getInventory().storage;
            for (RotorHolderPartMachine part : rotorHolderMachines) {
                if (!part.hasRotor()) {
                    part.setRotorStack(storage.getStackInSlot(0));
                    storage.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        for (IMultiPart part : getParts()) {
            if (part instanceof RotorHolderPartMachine rotorHolderPartMachine) {
                rotorHolderMachines = Objects.requireNonNullElseGet(rotorHolderMachines, HashSet::new);
                rotorHolderMachines.add(rotorHolderPartMachine);
            }

            if (part instanceof RotorHatchPartMachine rotorHatchPart) {
                rotorHatchPartMachine = rotorHatchPart;
            }
        }
        rotorSubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        rotorHolderMachines = null;
        rotorHatchPartMachine = null;
    }

    @Nullable
    private RotorHolderPartMachine getRotorHolder() {
        if (rotorHolderMachines != null) {
            for (RotorHolderPartMachine part : rotorHolderMachines) {
                return part;
            }
        }
        return null;
    }

    @Override
    public long getOverclockVoltage() {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor())
            return (long) baseEuOutput * rotorHolder.getTotalPower() / 100;
        return 0;
    }

    protected long boostProduction(long production) {
        var rotorHolder = getRotorHolder();
        if (rotorHolder != null && rotorHolder.hasRotor()) {
            int maxSpeed = rotorHolder.getMaxRotorHolderSpeed();
            int currentSpeed = rotorHolder.getRotorSpeed();
            if (currentSpeed >= maxSpeed)
                return production;
            return (long) (production * Math.pow(1.0 * currentSpeed / maxSpeed, 2));
        }
        return 0;
    }

    //////////////////////////////////////
    // ****** Recipe Logic *******//
    //////////////////////////////////////
    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof MegaTurbineMachine turbineMachine) {
            RotorHolderPartMachine rotorHolder = turbineMachine.getRotorHolder();
            long EUt = RecipeHelper.getOutputEUt(recipe);
            if (rotorHolder == null || EUt <= 0) return null;
            var turbineMaxVoltage = (int) turbineMachine.getOverclockVoltage();
            if (turbineMachine.excessVoltage >= turbineMaxVoltage) {
                turbineMachine.excessVoltage -= turbineMaxVoltage;
                return null;
            }

            double holderEfficiency = rotorHolder.getTotalEfficiency() / 100.0;

            var maxParallel = (int) ((turbineMaxVoltage - turbineMachine.excessVoltage) / (EUt * holderEfficiency));

            turbineMachine.excessVoltage += (int) (maxParallel * EUt * holderEfficiency - turbineMaxVoltage);
            var parallelResult = GTRecipeModifiers.fastParallel(turbineMachine, recipe, Math.max(1, maxParallel), false);
            recipe = parallelResult.getFirst() == recipe ? recipe.copy() : parallelResult.getFirst();

            long eut = turbineMachine.boostProduction((long) (EUt * holderEfficiency * parallelResult.getSecond()));
            recipe.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(eut,
                    ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));

            return recipe;
        }
        return null;
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    @Override
    public boolean canVoidRecipeOutputs(RecipeCapability<?> capability) {
        return capability != EURecipeCapability.CAP;
    }

    //////////////////////////////////////
    // ******* GUI ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            var rotorHolder = getRotorHolder();

            if (rotorHolder != null && rotorHolder.getRotorEfficiency() > 0) {
                textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_speed",
                        FormattingUtil.formatNumbers(rotorHolder.getRotorSpeed()),
                        FormattingUtil.formatNumbers(rotorHolder.getMaxRotorHolderSpeed())));
                textList.add(Component.translatable("gtceu.multiblock.turbine.efficiency",
                        rotorHolder.getTotalEfficiency()));

                long maxProduction = getOverclockVoltage();
                long currentProduction = isActive() ? boostProduction((int) maxProduction) : 0;
                String voltageName = GTValues.VNF[GTUtil.getTierByVoltage(currentProduction)];

                if (isActive()) {
                    textList.add(3, Component.translatable("gtceu.multiblock.turbine.energy_per_tick",
                            FormattingUtil.formatNumbers(currentProduction), voltageName));
                }

                int rotorDurability = rotorHolder.getRotorDurabilityPercent();
                if (rotorDurability > MIN_DURABILITY_TO_WARN) {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability));
                } else {
                    textList.add(Component.translatable("gtceu.multiblock.turbine.rotor_durability", rotorDurability)
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                }
            }
        }
    }
}
