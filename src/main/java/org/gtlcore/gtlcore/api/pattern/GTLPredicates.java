package org.gtlcore.gtlcore.api.pattern;

import org.gtlcore.gtlcore.api.pattern.util.IValueContainer;
import org.gtlcore.gtlcore.api.pattern.util.SimpleValueContainer;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.ITieredMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.MultiblockState;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.predicates.PredicateBlocks;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import lombok.NonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;

public class GTLPredicates {

    public static TraceabilityPredicate tierCasings(Int2ObjectMap<Supplier<?>> map, String tierType) {
        BlockInfo[] blockInfos = new BlockInfo[map.size()];
        int index = 0;

        for (var entry = map.values().iterator(); entry.hasNext(); ++index) {
            var blockSupplier = entry.next();
            var block = (Block) blockSupplier.get();
            blockInfos[index] = BlockInfo.fromBlockState(block.defaultBlockState());
        }

        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (var entry : map.int2ObjectEntrySet()) {
                if (blockState.is((Block) entry.getValue().get())) {
                    var stats = entry.getIntKey();
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut(tierType, stats);
                    if (!currentCoil.equals(stats)) {
                        blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.tier"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> blockInfos).addTooltips(Component.translatable("gtceu.multiblock.pattern.error.tier"));
    }

    public static TraceabilityPredicate countBlock(String name, Block... blocks) {
        TraceabilityPredicate inner = blocks(blocks);
        Predicate<MultiblockState> predicate = state -> {
            if (inner.test(state)) {
                IValueContainer<?> currentContainer = state.getMatchContext().getOrPut(name + "Value",
                        new SimpleValueContainer<>(0, (integer, block, tierType) -> ++integer));
                currentContainer.operate(state.getBlockState().getBlock(), null);
                return true;
            }
            return false;
        };
        BlockInfo[] candidates = inner.common.stream()
                .map(p -> p.candidates)
                .filter(Objects::nonNull)
                .map(Supplier::get)
                .flatMap(Arrays::stream)
                .toArray(BlockInfo[]::new);
        return new TraceabilityPredicate(new SimplePredicate(predicate, () -> candidates));
    }

    public static TraceabilityPredicate RotorBlock() {
        return new TraceabilityPredicate(
                new PredicateBlocks(
                        PartAbility.ROTOR_HOLDER.getAllBlocks().toArray(Block[]::new)) {

                    @Override
                    public boolean test(MultiblockState blockWorldState) {
                        if (super.test(blockWorldState)) {
                            var level = blockWorldState.getWorld();
                            var pos = blockWorldState.getPos();
                            var machine = MetaMachine.getMachine(level, pos);
                            if (machine instanceof ITieredMachine tieredMachine) {
                                int tier = blockWorldState
                                        .getMatchContext()
                                        .getOrPut("ReinforcedRotor", tieredMachine.getTier());
                                if (tier != tieredMachine.getTier()) {
                                    return false;
                                }
                                return level
                                        .getBlockState(pos.relative(machine.getFrontFacing()))
                                        .isAir();
                            }
                        }
                        return false;
                    }
                });
    }

    // group1 - group2
    public static TraceabilityPredicate diffAbilities(@NonNull Collection<PartAbility> group1, @NonNull Collection<PartAbility> group2) {
        return blocks(diffBlocks(group1, group2).toArray(Block[]::new));
    }

    private static ObjectSet<Block> unionBlocks(@NonNull Collection<PartAbility> group) {
        if (group.isEmpty()) return ObjectSets.emptySet();
        ObjectSet<Block> out = new ObjectOpenHashSet<>();
        for (PartAbility ability : group) {
            out.addAll(ability.getAllBlocks());
        }
        return out;
    }

    private static ObjectSet<Block> diffBlocks(Collection<PartAbility> group1,
                                               Collection<PartAbility> group2) {
        ObjectSet<Block> g1 = unionBlocks(group1);
        g1.removeAll(unionBlocks(group2));
        return g1;
    }
}
