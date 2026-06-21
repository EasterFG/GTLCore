package org.gtlcore.gtlcore.client.gui.widget;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IExtendedClickData {

    void setUUID(@Nullable UUID uuid);

    @Nullable
    UUID getUUID();
}
