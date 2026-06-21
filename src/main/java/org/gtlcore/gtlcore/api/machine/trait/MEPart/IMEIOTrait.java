package org.gtlcore.gtlcore.api.machine.trait.MEPart;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

public interface IMEIOTrait {

    IO getIO();

    void notifySelfIO();
}
