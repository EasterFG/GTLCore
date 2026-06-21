package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.common.util.BlockStateWatcher;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

public class BedrockDrillingRig extends WorkableElectricMultiblockMachine implements IMachineLife {

    protected BlockStateWatcher.WatcherHandle watcherHandle;
    protected BlockPos targetPos;
    protected boolean hasBedrockAtTarget;

    public BedrockDrillingRig(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        targetPos = getPos().offset(0, -9, 0);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (!isRemote()) unregisterBlockWatcher();
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (getLevel() instanceof ServerLevel level) {
            registerBlockWatcher(level);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        if (!isRemote()) unregisterBlockWatcher();
    }

    @Override
    public void onMachinePlaced(@Nullable LivingEntity player, ItemStack stack) {
        targetPos = getPos().offset(0, -9, 0);
    }

    @Override
    public void onMachineRemoved() {
        if (!isRemote()) unregisterBlockWatcher();
    }

    protected void registerBlockWatcher(ServerLevel level) {
        unregisterBlockWatcher();
        watcherHandle = BlockStateWatcher.addWatcher(getLevel(), targetPos, this::onBlockStateChanged);

        BlockState currentState = level.getBlockState(targetPos);
        hasBedrockAtTarget = currentState.getBlock().kjs$getId().equals("minecraft:bedrock");
        if (hasBedrockAtTarget) this.recipeLogic.updateTickSubscription();
    }

    protected void unregisterBlockWatcher() {
        if (watcherHandle != null) {
            watcherHandle.remove();
            watcherHandle = null;
        }
    }

    protected void onBlockStateChanged(BlockState newState) {
        hasBedrockAtTarget = newState != null && newState.getBlock().kjs$getId().equals("minecraft:bedrock");
        if (hasBedrockAtTarget) this.recipeLogic.updateTickSubscription();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return hasBedrockAtTarget;
    }

    @Override
    public void afterWorking() {
        super.afterWorking();
        Level level = getLevel();
        if (level != null && ThreadLocalRandom.current().nextInt(10) == 0) {
            level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
        }
    }
}
