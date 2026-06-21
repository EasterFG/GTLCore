package org.gtlcore.gtlcore.api.machine.multiblock;

import org.gtlcore.gtlcore.api.machine.trait.AECraft.*;
import org.gtlcore.gtlcore.api.machine.trait.IMolecularAssemblerMachine;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;
import org.gtlcore.gtlcore.common.machine.trait.MolecularAssemblerRecipesLogic;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.ICleanroomProvider;
import com.gregtechceu.gtceu.api.machine.feature.IMufflableMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IWorkableMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.primitives.Ints;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class MolecularAssemblerMultiblockMachineBase extends MultiblockControllerMachine implements IWorkableMultiController, IMufflableMachine, ParallelMachine, IMolecularAssemblerMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MolecularAssemblerMultiblockMachineBase.class, MultiblockControllerMachine.MANAGED_FIELD_HOLDER);

    private final GTRecipeType[] recipeTypes = this.getDefinition().getRecipeTypes();
    protected final List<ISubscription> traitSubscriptions;

    @Persisted
    @DescSynced
    public final RecipeLogic recipeLogic;

    @Persisted
    private int activeRecipeType = 0;
    protected @Nullable LongSet activeBlocks;

    @Setter
    @Getter
    @Persisted
    @DescSynced
    protected boolean isMuffled;
    protected boolean previouslyMuffled = true;

    @Nullable
    protected IMolecularAssemblerHandler molecularAssemblerHandler = null;

    @Getter
    protected int tickDuration = 40;

    @Getter
    protected int maxParallel = 0;

    @Getter
    protected int patternSize = 0;

    public MolecularAssemblerMultiblockMachineBase(IMachineBlockEntity holder, Object... args) {
        super(holder);
        this.recipeLogic = this.createRecipeLogic(args);
        this.traitSubscriptions = new ObjectArrayList<>();
    }

    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MolecularAssemblerRecipesLogic(this);
    }

    @Override
    public IMolecularAssemblerHandler getMAHandler() {
        return molecularAssemblerHandler;
    }

    public void updateActiveBlocks(boolean active) {
        if (activeBlocks != null) {
            for (long pos : activeBlocks) {
                var blockPos = BlockPos.of(pos);
                var blockState = Objects.requireNonNull(getLevel()).getBlockState(blockPos);
                var block = blockState.getBlock();
                if (block instanceof ActiveBlock activeBlock) {
                    BlockState newState = activeBlock.changeActive(blockState, active);
                    if (newState != blockState) {
                        this.getLevel().setBlockAndUpdate(blockPos, newState);
                    }
                }
            }
        }
    }

    // ========================================
    // Structure
    // ========================================

    @Override
    public void onUnload() {
        super.onUnload();
        this.traitSubscriptions.forEach(ISubscription::unsubscribe);
        this.traitSubscriptions.clear();
        this.recipeLogic.inValid();
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        this.activeBlocks = null;
        this.traitSubscriptions.forEach(ISubscription::unsubscribe);
        this.traitSubscriptions.clear();
        this.recipeLogic.updateTickSubscription();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.activeBlocks = this.getMultiblockState().getMatchContext().getOrDefault("vaBlocks", LongSets.emptySet());
        this.traitSubscriptions.forEach(ISubscription::unsubscribe);
        this.traitSubscriptions.clear();

        if (!isRemote()) update();

        RecipeResult.of(this, null);
        RecipeResult.ofWorking(this, null);
    }

    private void update() {
        final ObjectSet<BlockPos> patternInventoryPos = new ObjectArraySet<>();
        int speedTier = 0;
        int totalSlots = 0;
        long totalParallel = 0;
        IMECraftIOPart meCraftIOPart = null;

        for (IMultiPart part : this.getParts()) {
            if (part instanceof IMECraftPatternContainer craftPatternContainer) {
                patternInventoryPos.add(craftPatternContainer.getBlockPos());
                totalSlots += craftPatternContainer.getSlots();
            } else if (part instanceof IMECraftParallelCore parallelCore) {
                totalParallel += parallelCore.getParallel();

            } else if (part instanceof IMECraftSpeedCore speedCore) {
                speedTier += speedCore.getSpeedTier();
            } else if (part instanceof IMECraftIOPart ioPart) {
                if (meCraftIOPart == null) {
                    meCraftIOPart = ioPart;
                } else {
                    throw new IllegalStateException("MolecularAssemblerIOMachine already exists");
                }
            }
        }

        if (totalParallel == 0) totalParallel = 1;
        if (meCraftIOPart == null) throw new IllegalStateException("MolecularAssemblerIOMachine doesn't exist");

        this.patternSize = totalSlots;
        this.tickDuration = speedTier <= 39 ? 40 - speedTier : 1;
        this.maxParallel = Ints.saturatedCast(totalParallel);
        meCraftIOPart.init(patternInventoryPos);

        var handlerTrait = meCraftIOPart.getNotifiableMAHandlerTrait();
        this.traitSubscriptions.add(handlerTrait.addChangedListener(recipeLogic::updateTickSubscription));
        this.molecularAssemblerHandler = handlerTrait;
        this.recipeLogic.updateTickSubscription();
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.activeBlocks = null;
        this.traitSubscriptions.forEach(ISubscription::unsubscribe);
        this.traitSubscriptions.clear();
        this.patternSize = 0;
        this.tickDuration = 40;
        this.maxParallel = 0;
        this.molecularAssemblerHandler = null;
        this.recipeLogic.resetRecipeLogic();
    }

    @Override
    public void clientTick() {
        super.clientTick();
        if (this.previouslyMuffled != this.isMuffled) {
            this.previouslyMuffled = this.isMuffled;
            if (this.recipeLogic != null) {
                this.recipeLogic.updateSound();
            }
        }
    }

    // ========================================
    // Other
    // ========================================

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public @NotNull GTRecipeType[] getRecipeTypes() {
        return new GTRecipeType[0];
    }

    @Override
    public @NotNull GTRecipeType getRecipeType() {
        return this.recipeTypes[this.activeRecipeType];
    }

    @Override
    public int getActiveRecipeType() {
        return this.activeRecipeType;
    }

    @Override
    public void setActiveRecipeType(int activeRecipeType) {
        this.activeRecipeType = activeRecipeType;
    }

    @Override
    public @NotNull RecipeLogic getRecipeLogic() {
        return this.recipeLogic;
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
        return null;
    }

    @Override
    public void setCleanroom(ICleanroomProvider iCleanroomProvider) {}

    @Override
    public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
        return ImmutableTable.of();
    }

    @Override
    public boolean keepSubscribing() {
        return false;
    }

    @Override
    public void notifyStatusChanged(RecipeLogic.Status oldStatus, RecipeLogic.Status newStatus) {
        IWorkableMultiController.super.notifyStatusChanged(oldStatus, newStatus);
        if (newStatus == RecipeLogic.Status.WORKING || oldStatus == RecipeLogic.Status.WORKING) {
            this.updateActiveBlocks(newStatus == RecipeLogic.Status.WORKING);
        }
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        return this.isFormed && !this.getMultiblockState().hasError();
    }

    public void afterWorking() {
        for (IMultiPart part : this.getParts()) {
            part.afterWorking(this);
        }

        IWorkableMultiController.super.afterWorking();
    }

    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        for (IMultiPart part : this.getParts()) {
            if (!part.beforeWorking(this)) {
                return false;
            }
        }

        return IWorkableMultiController.super.beforeWorking(recipe);
    }

    public boolean onWorking() {
        for (IMultiPart part : this.getParts()) {
            if (!part.onWorking(this)) {
                return false;
            }
        }

        return IWorkableMultiController.super.onWorking();
    }

    public void onWaiting() {
        for (IMultiPart part : this.getParts()) {
            part.onWaiting(this);
        }

        IWorkableMultiController.super.onWaiting();
    }

    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (!isWorkingAllowed) {
            for (IMultiPart part : this.getParts()) {
                part.onPaused(this);
            }
        }

        IWorkableMultiController.super.setWorkingEnabled(isWorkingAllowed);
    }
}
