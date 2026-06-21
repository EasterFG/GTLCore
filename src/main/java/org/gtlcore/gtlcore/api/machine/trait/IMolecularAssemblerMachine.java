package org.gtlcore.gtlcore.api.machine.trait;

import org.gtlcore.gtlcore.api.machine.trait.AECraft.IMolecularAssemblerHandler;

import org.jetbrains.annotations.Nullable;

public interface IMolecularAssemblerMachine {

    @Nullable
    IMolecularAssemblerHandler getMAHandler();
}
