package org.gtlcore.gtlcore.api.machine.trait.MEPart;

public interface IMEFilterIOTrait extends IMEIOTrait {

    default boolean hasItemFilter() {
        return false;
    }

    default boolean hasFluidFilter() {
        return false;
    }
}
