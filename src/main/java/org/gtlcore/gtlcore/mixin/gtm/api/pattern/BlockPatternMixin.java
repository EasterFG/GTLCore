package org.gtlcore.gtlcore.mixin.gtm.api.pattern;

import org.gtlcore.gtlcore.api.pattern.util.IMultiblockStateGet;

import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.pattern.*;
import com.gregtechceu.gtceu.api.pattern.error.*;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;
import org.spongepowered.asm.mixin.*;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 思路参考自gto
 * &#064;line <a href="https://github.com/GregTech-Odyssey/GTOCore">...</a>
 */

@Mixin(BlockPattern.class)
public abstract class BlockPatternMixin {

    @Mutable
    @Shadow(remap = false)
    @Final
    protected final TraceabilityPredicate[][][] blockMatches;
    @Mutable
    @Shadow(remap = false)
    @Final
    protected final int fingerLength;
    @Mutable
    @Shadow(remap = false)
    @Final
    protected final int thumbLength;
    @Mutable
    @Shadow(remap = false)
    @Final
    protected final int palmLength;
    @Mutable
    @Shadow(remap = false)
    @Final
    public final int[][] aisleRepetitions;
    @Mutable
    @Shadow(remap = false)
    @Final
    public final RelativeDirection[] structureDir;
    @Mutable
    @Shadow(remap = false)
    @Final
    protected final int[] centerOffset;

    protected BlockPatternMixin(TraceabilityPredicate[][][] blockMatches, int fingerLength, int thumbLength, int palmLength, int[][] aisleRepetitions, RelativeDirection[] structureDir, int[] centerOffset) {
        this.blockMatches = blockMatches;
        this.fingerLength = fingerLength;
        this.thumbLength = thumbLength;
        this.palmLength = palmLength;
        this.aisleRepetitions = aisleRepetitions;
        this.structureDir = structureDir;
        this.centerOffset = centerOffset;
    }

    @Shadow(remap = false)
    protected abstract BlockPos setActualRelativeOffset(int x, int y, int z, Direction facing, Direction upwardsFacing, boolean isFlipped);

    @Shadow(remap = false)
    protected abstract void resetFacing(BlockPos pos, BlockState blockState, Direction facing, BiFunction<BlockPos, Direction, Boolean> checker, Consumer<BlockState> consumer);

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public boolean checkPatternAt(MultiblockState worldState, BlockPos centerPos, Direction frontFacing, Direction upwardsFacing, boolean isFlipped, boolean savePredicate) {
        boolean findFirstAisle = false;
        int minZ = -centerOffset[4];
        if (worldState instanceof IMultiblockStateGet stateGet) stateGet.cleanState();
        PatternMatchContext matchContext = worldState.getMatchContext();
        Map<SimplePredicate, Integer> globalCount = worldState.getGlobalCount();
        Map<SimplePredicate, Integer> layerCount = worldState.getLayerCount();
        // Checking aisles
        for (int c = 0, z = minZ++, r; c < this.fingerLength; c++) {
            // Checking repeatable slices
            loop:
            for (r = 0; (findFirstAisle ? r < aisleRepetitions[c][1] : z <= -centerOffset[3]); r++) {
                // Checking single slice
                layerCount.clear();

                for (int b = 0, y = -centerOffset[1]; b < this.thumbLength; b++, y++) {
                    for (int a = 0, x = -centerOffset[0]; a < this.palmLength; a++, x++) {
                        worldState.setError(null);
                        TraceabilityPredicate predicate = this.blockMatches[c][b][a];
                        if (predicate.isAny()) continue;
                        BlockPos pos = setActualRelativeOffset(x, y, z, frontFacing, upwardsFacing, isFlipped).offset(centerPos.getX(),
                                centerPos.getY(), centerPos.getZ());
                        if (worldState instanceof IMultiblockStateGet stateGet && !stateGet.updateState(pos, predicate)) {
                            return false;
                        }
                        if (predicate.addCache()) {
                            worldState.addPosCache(pos);
                            if (savePredicate) {
                                matchContext.getOrCreate("predicates", Object2ObjectOpenHashMap::new).put(pos, predicate);
                            }
                        }
                        boolean canPartShared = true;
                        if (worldState.getTileEntity() instanceof IMachineBlockEntity machineBlockEntity &&
                                machineBlockEntity.getMetaMachine() instanceof IMultiPart part) { // add detected parts
                            if (!predicate.isAny()) {
                                if (part.isFormed() && !part.canShared() &&
                                        !part.hasController(worldState.controllerPos)) { // check part can be shared
                                    canPartShared = false;
                                    worldState.setError(new PatternStringError("multiblocked.pattern.error.share"));
                                } else {
                                    matchContext.getOrCreate("parts", ObjectOpenHashSet::new).add(part);
                                }
                            }
                        }
                        if (worldState.getBlockState().getBlock() instanceof ActiveBlock) {
                            matchContext.getOrCreate("vaBlocks", LongOpenHashSet::new)
                                    .add(worldState.getPos().asLong());
                        }
                        if (!predicate.test(worldState) || !canPartShared) { // matching failed
                            if (findFirstAisle) {
                                if (r < aisleRepetitions[c][0]) {// retreat to see if the first aisle can start later
                                    r = c = 0;
                                    z = minZ++;
                                    matchContext.reset();
                                    findFirstAisle = false;
                                }
                            } else {
                                z++;// continue searching for the first aisle
                            }
                            continue loop;
                        }
                        matchContext.getOrCreate("ioMap", Long2ObjectOpenHashMap::new).put(worldState.getPos().asLong(),
                                worldState.io);
                    }
                }
                findFirstAisle = true;
                z++;

                // Check layer-local matcher predicate
                for (var entry : layerCount.entrySet()) {
                    if (entry.getValue() < entry.getKey().minLayerCount) {
                        worldState.setError(new SinglePredicateError(entry.getKey(), 3));
                        return false;
                    }
                }
            }
            // Repetitions out of range
            if (r < aisleRepetitions[c][0] || worldState.hasError() || !findFirstAisle) {
                if (!worldState.hasError()) {
                    worldState.setError(new PatternError());
                }
                return false;
            }
        }

        // Check count matches amount
        for (var entry : globalCount.entrySet()) {
            if (entry.getValue() < entry.getKey().minCount) {
                worldState.setError(new SinglePredicateError(entry.getKey(), 1));
                return false;
            }
        }

        worldState.setError(null);
        worldState.setNeededFlip(isFlipped);
        return true;
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    public BlockInfo[][][] getPreview(int[] repetition) {
        Object2IntOpenHashMap<SimplePredicate> cacheGlobal = new Object2IntOpenHashMap<>();
        Long2ObjectOpenHashMap<BlockInfo> blocks = new Long2ObjectOpenHashMap<>();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (int l = 0, x = 0; l < this.fingerLength; l++) {
            for (int r = 0; r < repetition[l]; r++) {
                Object2IntOpenHashMap<SimplePredicate> cacheLayer = new Object2IntOpenHashMap<>();
                for (int y = 0; y < this.thumbLength; y++) {
                    for (int z = 0; z < this.palmLength; z++) {
                        TraceabilityPredicate predicate = this.blockMatches[l][y][z];
                        if (predicate.isAny()) continue;
                        boolean find = false;
                        BlockInfo[] infos = null;
                        for (SimplePredicate limit : predicate.limited) {
                            if (limit.minLayerCount > 0) {
                                if (cacheLayer.getInt(limit) < limit.minLayerCount) {
                                    cacheLayer.addTo(limit, 1);
                                } else continue;
                                if (cacheGlobal.getInt(limit) < limit.previewCount) {
                                    cacheGlobal.addTo(limit, 1);
                                } else continue;
                            } else continue;
                            infos = limit.candidates == null ? null : limit.candidates.get();
                            find = true;
                            break;
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.minCount == -1 && limit.previewCount == -1) continue;
                                if (cacheGlobal.getInt(limit) < limit.previewCount) {
                                    cacheGlobal.addTo(limit, 1);
                                } else if (limit.minCount > 0) {
                                    if (cacheGlobal.getInt(limit) < limit.minCount) {
                                        cacheGlobal.addTo(limit, 1);
                                    } else continue;
                                } else continue;
                                infos = limit.candidates == null ? null : limit.candidates.get();
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            for (SimplePredicate common : predicate.common) {
                                if (common.previewCount > 0) {
                                    if (cacheGlobal.getInt(common) < common.previewCount) {
                                        cacheGlobal.addTo(common, 1);
                                    } else continue;
                                } else continue;
                                infos = common.candidates == null ? null : common.candidates.get();
                                find = true;
                                break;
                            }
                        }
                        if (!find) {
                            for (SimplePredicate common : predicate.common) {
                                if (common.previewCount == -1) {
                                    infos = common.candidates == null ? null : common.candidates.get();
                                    find = true;
                                    break;
                                }
                            }
                        }
                        if (!find) {
                            for (SimplePredicate limit : predicate.limited) {
                                if (limit.previewCount != -1) continue;
                                if (limit.maxCount != -1 || limit.maxLayerCount != -1) {
                                    if (cacheGlobal.getOrDefault(limit, 0) < limit.maxCount) {
                                        cacheGlobal.addTo(limit, 1);
                                    } else if (cacheLayer.getOrDefault(limit, 0) < limit.maxLayerCount) {
                                        cacheLayer.addTo(limit, 1);
                                    } else continue;
                                }
                                infos = limit.candidates == null ? null : limit.candidates.get();
                                break;
                            }
                        }
                        BlockInfo info = infos == null || infos.length == 0 ? BlockInfo.EMPTY : infos[0];
                        BlockPos pos = setActualRelativeOffset(z, y, x, Direction.NORTH, Direction.UP, false);

                        blocks.put(pos.asLong(), info);
                        minX = Math.min(pos.getX(), minX);
                        minY = Math.min(pos.getY(), minY);
                        minZ = Math.min(pos.getZ(), minZ);
                        maxX = Math.max(pos.getX(), maxX);
                        maxY = Math.max(pos.getY(), maxY);
                        maxZ = Math.max(pos.getZ(), maxZ);
                    }
                }
                x++;
            }
        }

        BlockInfo[][][] result = (BlockInfo[][][]) Array.newInstance(BlockInfo.class, new int[] { maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1 });
        int finalMinX = minX;
        int finalMinY = minY;
        int finalMinZ = minZ;
        blocks.long2ObjectEntrySet().fastForEach(entry -> {
            var blockPos = entry.getLongKey();
            var pos = BlockPos.of(blockPos);
            var info = entry.getValue();
            resetFacing(pos, info.getBlockState(), null, (p, f) -> {
                BlockInfo blockInfo = blocks.get(p.relative(f).asLong());
                if (blockInfo == null || blockInfo.getBlockState().getBlock() == Blocks.AIR) {
                    if (blocks.get(blockPos).getBlockState().getBlock() instanceof MetaMachineBlock machineBlock) {
                        if (machineBlock.newBlockEntity(BlockPos.ZERO,
                                machineBlock.defaultBlockState()) instanceof IMachineBlockEntity machineBlockEntity) {
                            var machine = machineBlockEntity.getMetaMachine();
                            if (machine instanceof IMultiController) return false;
                            else return machine.isFacingValid(f);
                        }
                    }
                    return true;
                }
                return false;
            }, info::setBlockState);
            result[pos.getX() - finalMinX][pos.getY() - finalMinY][pos.getZ() - finalMinZ] = info;
        });
        return result;
    }
}
