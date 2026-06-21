package org.gtlcore.gtlcore.api.machine.trait.MEStock;

import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.Nullable;

public interface IMESlot {

    void setOnConfigChanged(Runnable onConfigChanged);

    Runnable getOnConfigChanged();

    void setConfigWithoutNotify(@Nullable GenericStack config);
}
