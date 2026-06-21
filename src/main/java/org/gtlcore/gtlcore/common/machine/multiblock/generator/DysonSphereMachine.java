package org.gtlcore.gtlcore.common.machine.multiblock.generator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.CWURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
public class DysonSphereMachine extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DysonSphereMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int DysonSphereData;
    @Persisted
    private int DysonSpheredamageData;

    protected ConditionalSubscriptionHandler nightSubs;

    public DysonSphereMachine(IMachineBlockEntity holder) {
        super(holder);
        this.nightSubs = new ConditionalSubscriptionHandler(this, this::nightUpdate, () -> DysonSphereData > 0);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected void nightUpdate() {
        if (getOffsetTimer() % 10 == 0 && getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.setDayTime(18000L);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        nightSubs.initialize(getLevel());
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (getRecipeLogic().getProgress() == 199 && getDysonSphereData() < 10000 &&
                getRecipeLogic().getDuration() == 200) {
            if (getDysonSpheredamageData() > 60) {
                this.DysonSpheredamageData = 0;
            } else {
                this.DysonSphereData++;
            }
        }
        if (getRecipeLogic().getDuration() == 20 && getRecipeLogic().getProgress() == 19 &&
                Math.random() < 0.01 * (1 + (double) getDysonSphereData() / 128) && getDysonSphereData() > 0) {
            if (getDysonSpheredamageData() > 99) {
                this.DysonSphereData--;
                this.DysonSpheredamageData = 0;
            } else {
                this.DysonSpheredamageData++;
            }
        }
        return value;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        final BlockPos pos = getPos();
        final Level level = getLevel();
        BlockPos[] coordinates = new BlockPos[] {
                pos.offset(4, 14, 0),
                pos.offset(-4, 14, 0),
                pos.offset(0, 14, 4),
                pos.offset(0, 14, -4) };
        for (BlockPos blockPos : coordinates) {
            if (Objects.equals(level.kjs$getBlock(blockPos).getId(), "kubejs:dyson_receiver_casing")) {
                for (int i = -6; i < 7; i++) {
                    for (int j = -6; j < 7; j++) {
                        if (i != 0 && j != 0 && level.kjs$getBlock(blockPos.offset(i, 1, j)).getSkyLight() == 0) {
                            getRecipeLogic().resetRecipeLogic();
                            return false;
                        }
                    }
                }
            }
        }
        if (recipe != null && isLaunch(recipe)) {
            return getDysonSphereData() <= 10000;
        } else {
            return getDysonSphereData() > 0;
        }
    }

    private boolean isLaunch(GTRecipe recipe) {
        return RecipeHelper.getOutputEUt(recipe) != GTValues.V[GTValues.MAX];
    }

    private double getEfficiency() {
        return (double) (50 - Math.max(0, getDysonSpheredamageData() - 60)) / 50;
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        return (int) ((1 - getEfficiency()) * 15);
    }

    @Override
    public long getOverclockVoltage() {
        return (long) (GTValues.V[GTValues.MAX] * getDysonSphereData() * getEfficiency());
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof DysonSphereMachine engineMachine) {
            if (!engineMachine.isLaunch(recipe)) {
                GTRecipe recipe1 = recipe.copy();
                recipe1.tickOutputs.put(EURecipeCapability.CAP, List.of(new Content(
                        engineMachine.getOverclockVoltage(),
                        ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
                recipe1.tickInputs.put(CWURecipeCapability.CAP, List.of(new Content(
                        engineMachine.getDysonSphereData(),
                        ChanceLogic.getMaxChancedValue(), ChanceLogic.getMaxChancedValue(), 0, null, null)));
                return recipe1;
            }
            return recipe;
        }
        return null;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        textList.add(Component.translatable("gtceu.machine.dyson_sphere.number", getDysonSphereData()));
        textList.add(Component.translatable("gtceu.machine.dyson_sphere.voltage",
                (getDysonSphereData() > 0 ? getOverclockVoltage() : 0)));
        textList.add(Component.translatable("gtceu.machine.fission_reactor.damaged",
                getDysonSpheredamageData()).append("%"));
    }
}
