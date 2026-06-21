package org.gtlcore.gtlcore.mixin.mc;

import org.gtlcore.gtlcore.common.util.BlockStateWatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {

    @Final
    @Shadow
    Level level;

    @Inject(method = "setBlockState",
            at = @At(value = "INVOKE",
                     opcode = Opcodes.GETFIELD,
                     target = "Lnet/minecraft/world/level/block/state/BlockState;onPlace(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"))
    private void notifyBlockStateWatchers(BlockPos pos, BlockState state, boolean isMoving,
                                          CallbackInfoReturnable<BlockState> cir) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> BlockStateWatcher.notifyWatchersInternal(serverLevel, pos, state));
        }
    }
}
