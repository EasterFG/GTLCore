package org.gtlcore.gtlcore.api.machine.trait.AECraft;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;

public interface IMolecularAssemblerHandlerTrait extends IMolecularAssemblerHandler {

    ISubscription addChangedListener(Runnable listener);
}
