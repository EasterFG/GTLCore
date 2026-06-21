package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;
import org.gtlcore.gtlcore.common.data.GTLBlocks;
import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers;
import org.gtlcore.gtlcore.utils.MachineUtil;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SpaceElevatorModuleMachine extends WorkableElectricMultiblockMachine
                                        implements IModularMachineModule<SpaceElevatorMachine, SpaceElevatorModuleMachine>, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(SpaceElevatorModuleMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    public SpaceElevatorModuleMachine(IMachineBlockEntity holder, boolean sepmTier, Object... args) {
        super(holder, args);
        this.sepmTier = sepmTier;
    }

    @DescSynced
    private int spaceElevatorTier = 0;
    private int moduleTier = 0;

    private final boolean sepmTier;

    @Persisted
    @Nullable
    @Getter
    @Setter
    private BlockPos hostPosition;

    @Nullable
    @Getter
    @Setter
    private SpaceElevatorMachine host;

    @Override
    public @NotNull Class<SpaceElevatorMachine> getHostType() {
        return SpaceElevatorMachine.class;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ========================================
    // Elevator connection
    // ========================================

    @Override
    public void onConnected(@NotNull SpaceElevatorMachine host) {
        getSpaceElevatorTier();
        recipeLogic.updateTickSubscription();
    }

    @Override
    public BlockPos[] getHostScanPositions() {
        final BlockPos pos = getPos();
        BlockPos[] powerCorePositions = new BlockPos[] {
                pos.offset(8, -2, 3),
                pos.offset(8, -2, -3),
                pos.offset(-8, -2, 3),
                pos.offset(-8, -2, -3),
                pos.offset(3, -2, 8),
                pos.offset(-3, -2, 8),
                pos.offset(3, -2, -8),
                pos.offset(-3, -2, -8)
        };

        if (getLevel() instanceof ServerLevel serverLevel) {
            for (BlockPos i : powerCorePositions) {
                if (serverLevel.getBlockState(i).getBlock() == GTLBlocks.POWER_CORE.get()) {
                    return new BlockPos[] {
                            i.offset(3, 2, 0),
                            i.offset(-3, 2, 0),
                            i.offset(0, 2, 3),
                            i.offset(0, 2, -3)
                    };
                }
            }
        }
        return MachineUtil.EMPTY_POS_ARRAY;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!findAndConnectToHost()) {
            removeFromHost(this.host);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        removeFromHost(this.host);
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        removeFromHost(this.host);
    }

    @Override
    public void onMachineRemoved() {
        removeFromHost(this.host);
    }

    // ========================================
    // Recipe Tier
    // ========================================

    private void getSpaceElevatorTier() {
        if (this.host != null) {
            final RecipeLogic logic = host.getRecipeLogic();
            if (logic.isWorking() && logic.getProgress() > 80) {
                spaceElevatorTier = host.getTier() - GTValues.ZPM;
                moduleTier = host.getCasingTier();
            } else if (!logic.isWorking()) {
                spaceElevatorTier = 0;
                moduleTier = 0;
            }
        } else {
            spaceElevatorTier = 0;
            moduleTier = 0;
        }
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof SpaceElevatorModuleMachine moduleMachine) {
            moduleMachine.getSpaceElevatorTier();
            if (moduleMachine.spaceElevatorTier < 1) {
                return null;
            }
            if (moduleMachine.sepmTier && recipe.data.getInt("SEPMTier") > moduleMachine.moduleTier) {
                return null;
            }
            GTRecipe recipe1 = GTLRecipeModifiers.reduction(machine, recipe, 1, Math.pow(0.8, moduleMachine.spaceElevatorTier - 1));
            if (recipe1 != null) {
                recipe1 = GTRecipeModifiers.accurateParallel(machine, recipe1, (int) Math.pow(4, moduleMachine.moduleTier - 1), false).getFirst();
                if (recipe1 != null) return RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK, recipe1, moduleMachine.getOverclockVoltage(), params, result);
            }
        }
        return null;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            getSpaceElevatorTier();
            if (spaceElevatorTier < 1) {
                getRecipeLogic().setProgress(0);
            }
        }
        return value;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        if (getOffsetTimer() % 10 == 0) {
            getSpaceElevatorTier();
        }
        textList.add(Component.translatable("gtceu.multiblock.parallel", Component.literal(FormattingUtil.formatNumbers(Math.pow(4, moduleTier - 1))).withStyle(ChatFormatting.DARK_PURPLE)).withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(spaceElevatorTier < 1 ? "tooltip.gtlcore.space_elevator_not_connected" : "tooltip.gtlcore.space_elevator_connected"));
        textList.add(Component.translatable("gtceu.machine.duration_multiplier.tooltip", FormattingUtil.formatPercent(Math.pow(0.8, spaceElevatorTier - 1))));
    }
}
