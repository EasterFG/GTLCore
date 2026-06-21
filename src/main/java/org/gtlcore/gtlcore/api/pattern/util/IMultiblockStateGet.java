package org.gtlcore.gtlcore.api.pattern.util;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;

import net.minecraft.core.BlockPos;

public interface IMultiblockStateGet {

    void cleanState();

    boolean updateState(BlockPos posIn, TraceabilityPredicate predicate);
}
