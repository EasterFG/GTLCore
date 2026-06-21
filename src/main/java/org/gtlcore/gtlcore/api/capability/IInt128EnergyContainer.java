package org.gtlcore.gtlcore.api.capability;

import org.gtlcore.gtlcore.utils.datastructure.Int128;

public interface IInt128EnergyContainer {

    /**
     * @return input eu/s
     */
    default Int128 getInt128InputPerSec() {
        return Int128.ZERO();
    }

    /**
     * @return output eu/s
     */
    default Int128 getInt128OutputPerSec() {
        return Int128.ZERO();
    }

    default void addEnergyPerSec(long energy) {}

    default Int128 getInt128EnergyStored() {
        return Int128.ZERO();
    }

    default Int128 getInt128EnergyCapacity() {
        return Int128.ZERO();
    }
}
