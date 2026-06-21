package org.gtlcore.gtlcore.mixin.gtm;

import com.gregtechceu.gtceu.api.block.IMachineBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.pattern.MultiblockShapeInfo;
import com.gregtechceu.gtceu.client.renderer.MultiblockInWorldPreviewRenderer;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin(MultiblockInWorldPreviewRenderer.class)
public class MultiblockInWorldPreviewRendererMixin {

    @Shadow(remap = false)
    private static BlockPos LAST_POS;
    @Shadow(remap = false)
    private static int LAST_LAYER;
    @Shadow(remap = false)
    private static TrackedDummyWorld LEVEL;

    @Shadow(remap = false)
    private static BlockPos rotateByFrontAxis(BlockPos pos, Direction front, Rotation rotation) {
        return pos;
    }

    @Shadow(remap = false)
    private static void prepareBuffers(TrackedDummyWorld level, Collection<BlockPos> renderedBlocks, int duration) {}

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public static void showPreview(BlockPos pos, MultiblockControllerMachine controller, int duration) {
        if (!controller.getDefinition().isRenderWorldPreview()) return;
        Direction front = controller.getFrontFacing();
        Direction up = controller.getUpwardsFacing();
        MultiblockShapeInfo shapeInfo = controller.getDefinition().getMatchingShapes().get(0);

        Map<BlockPos, BlockInfo> blockMap = new Object2ObjectOpenHashMap<>();
        IMultiController controllerBase = null;
        LEVEL = new TrackedDummyWorld();

        var blocks = shapeInfo.getBlocks();
        BlockPos controllerPatternPos = null;
        var maxY = 0;
        // find the pos of controller
        l:
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            maxY = Math.max(maxY, aisle.length);
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    var info = column[z];
                    if (info == null) continue;
                    if (info.getBlockState().getBlock() instanceof IMachineBlock machineBlock &&
                            machineBlock.getDefinition() instanceof MultiblockMachineDefinition) {
                        controllerPatternPos = new BlockPos(x, y, z);
                        break l;
                    }
                }
            }
        }

        if (controllerPatternPos == null) return;

        if (LAST_POS != null && LAST_POS.equals(pos)) {
            LAST_LAYER++;
            if (LAST_LAYER >= maxY) LAST_LAYER = -1;
        } else LAST_LAYER = -1;
        LAST_POS = pos;

        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                if (LAST_LAYER != -1 && LAST_LAYER != y) continue;
                for (int z = 0; z < column.length; z++) {
                    var info = column[z];
                    if (info == null) continue;
                    var blockState = info.getBlockState();
                    var offset = new BlockPos(x, y, z).subtract(controllerPatternPos);

                    // rotation
                    offset = switch (front) {
                        case NORTH, UP, DOWN -> offset.rotate(Rotation.NONE);
                        case SOUTH -> offset.rotate(Rotation.CLOCKWISE_180);
                        case EAST -> offset.rotate(Rotation.COUNTERCLOCKWISE_90);
                        case WEST -> offset.rotate(Rotation.CLOCKWISE_90);
                    };

                    Rotation r = up == Direction.NORTH ? Rotation.NONE : up == Direction.EAST ? Rotation.CLOCKWISE_90 :
                            up == Direction.SOUTH ? Rotation.CLOCKWISE_180 :
                                    up == Direction.WEST ? Rotation.COUNTERCLOCKWISE_90 : Rotation.NONE;

                    offset = rotateByFrontAxis(offset, front, r);

                    if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
                        var rotationState = machineBlock.getRotationState();
                        if (rotationState != RotationState.NONE) {
                            var face = blockState.getValue(rotationState.property);
                            if (face.getAxis() != Direction.Axis.Y) {
                                face = switch (front) {
                                    case NORTH, UP, DOWN -> front;
                                    case SOUTH -> face.getOpposite();
                                    case WEST -> face.getCounterClockWise();
                                    case EAST -> face.getClockWise();
                                };
                            }
                            if (rotationState.test(face)) {
                                blockState = blockState.setValue(rotationState.property, face);
                            }
                        }
                    }

                    BlockPos realPos = pos.offset(offset);

                    if (info.getBlockEntity(realPos) instanceof IMachineBlockEntity holder &&
                            holder.getMetaMachine() instanceof IMultiController cont) {
                        holder.getSelf().setLevel(LEVEL);
                        controllerBase = cont;
                    } else {
                        blockMap.put(realPos, BlockInfo.fromBlockState(blockState));
                    }
                }
            }
        }

        LEVEL.addBlocks(blockMap);
        if (controllerBase != null) {
            LEVEL.setInnerBlockEntity(controllerBase.self().holder.getSelf());
        }

        prepareBuffers(LEVEL, blockMap.keySet(), duration);
    }
}
