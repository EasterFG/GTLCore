package org.gtlcore.gtlcore.mixin.gtm.gui;

import org.gtlcore.gtlcore.api.gui.ExtendPatternPreviewWidget;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.pattern.*;

import com.lowdragmc.lowdraglib.utils.*;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget.locateNextRegion;
import static org.gtlcore.gtlcore.api.gui.ExtendPatternPreviewWidget.gatherBlockDrops;

@Mixin(PatternPreviewWidget.class)
public class PatternPreviewWidgetMixin {

    @Shadow(remap = false)
    private static TrackedDummyWorld LEVEL;

    @Shadow(remap = false)
    private void loadControllerFormed(Collection<BlockPos> poses, IMultiController controllerBase) {}

    @ModifyConstant(method = "setPage", remap = false, constant = @Constant(intValue = 18, ordinal = 0))
    private int modifyContainer(int constant) {
        return 36;
    }

    @Inject(method = "initializePattern", at = @At("HEAD"), remap = false, cancellable = true)
    private void initializePattern(MultiblockShapeInfo shapeInfo, HashSet<ItemStackKey> blockDrops, CallbackInfoReturnable cir) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<BlockPos, BlockInfo> blockMap = new Object2ObjectOpenHashMap<>();
        IMultiController controllerBase = null;
        BlockPos multiPos = locateNextRegion(500);

        BlockInfo[][][] blocks = shapeInfo.getBlocks();
        for (int x = 0; x < blocks.length; x++) {
            BlockInfo[][] aisle = blocks[x];
            for (int y = 0; y < aisle.length; y++) {
                BlockInfo[] column = aisle[y];
                for (int z = 0; z < column.length; z++) {
                    var block = column[z];
                    if (block == null) continue;
                    BlockState blockState = block.getBlockState();
                    BlockPos pos = multiPos.offset(x, y, z);
                    if (column[z].getBlockEntity(pos) instanceof IMachineBlockEntity holder &&
                            holder.getMetaMachine() instanceof IMultiController controller) {
                        holder.getSelf().setLevel(LEVEL);
                        controllerBase = controller;
                    }
                    blockMap.put(pos, BlockInfo.fromBlockState(blockState));
                }
            }
        }

        LEVEL.addBlocks(blockMap);
        if (controllerBase != null) {
            LEVEL.setInnerBlockEntity(controllerBase.self().holder.getSelf());
        }

        Map<ItemStackKey, ExtendPatternPreviewWidget.PartInfo> parts = gatherBlockDrops(blockMap, LEVEL);
        blockDrops.addAll(parts.keySet());

        Map<BlockPos, TraceabilityPredicate> predicateMap = new Object2ObjectOpenHashMap<>();
        if (controllerBase != null) {
            loadControllerFormed(predicateMap.keySet(), controllerBase);
            predicateMap = controllerBase.getMultiblockState().getMatchContext().get("predicates");
        }

        if (controllerBase == null) cir.setReturnValue(null);

        Class<?> MEclass = Class.forName("com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget$MBPattern");
        var con = MEclass.getConstructor(Map.class, List.class, Map.class, IMultiController.class);
        con.setAccessible(true);
        cir.setReturnValue(con.newInstance(blockMap, parts.values().stream().sorted((one, two) -> {
            if (one.isController) return -1;
            if (two.isController) return +1;
            if (one.isTile && !two.isTile) return -1;
            if (two.isTile && !one.isTile) return +1;
            if (one.blockId != two.blockId) return two.blockId - one.blockId;
            return two.amount - one.amount;
        }).map(ExtendPatternPreviewWidget.PartInfo::getItemStack)
                .filter(list -> !list.isEmpty()).collect(Collectors.toList()),
                predicateMap,
                controllerBase));
    }
}
