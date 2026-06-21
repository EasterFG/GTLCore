package org.gtlcore.gtlcore.common.util;

import org.gtlcore.gtlcore.GTLCore;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = GTLCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class BlockStateWatcher {

    private static final Map<Level, Long2ObjectMap<List<Consumer<BlockState>>>> LEVEL_DATA = new ConcurrentHashMap<>();

    public static WatcherHandle addWatcher(Level level, BlockPos pos,
                                           Consumer<BlockState> callback) {
        if (level == null || pos == null || callback == null) {
            throw new IllegalArgumentException("Level, position and callback cannot be null");
        }

        long posLong = pos.asLong();

        LEVEL_DATA.computeIfAbsent(level, k -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(posLong, k -> new ObjectArrayList<>())
                .add(callback);

        return new WatcherHandle(level, posLong, callback);
    }

    public static void removeWatcher(WatcherHandle handle) {
        if (handle == null) return;

        Long2ObjectMap<List<Consumer<BlockState>>> watchers = LEVEL_DATA.get(handle.level);
        if (watchers != null) {
            List<Consumer<BlockState>> callbacks = watchers.get(handle.posLong);
            if (callbacks != null) {
                callbacks.remove(handle.callback);
                if (callbacks.isEmpty()) {
                    watchers.remove(handle.posLong);
                }
            }
            if (watchers.isEmpty()) {
                LEVEL_DATA.remove(handle.level);
            }
        }
    }

    public static void clearLevel(Level level) {
        LEVEL_DATA.remove(level);
    }

    public static void notifyWatchersInternal(ServerLevel level, BlockPos pos, BlockState newState) {
        Long2ObjectMap<List<Consumer<BlockState>>> watchers = LEVEL_DATA.get(level);
        if (watchers == null) return;

        List<Consumer<BlockState>> callbacks = watchers.get(pos.asLong());
        if (callbacks == null || callbacks.isEmpty()) return;

        for (Consumer<BlockState> callback : callbacks) {
            try {
                callback.accept(newState);
            } catch (Exception e) {
                GTLCore.LOGGER.error("Error in BlockStateWatcher callback at {}: {}",
                        pos, e.getMessage(), e);
            }
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            clearLevel(level);
        }
    }

    public static class WatcherHandle {

        private final Level level;
        private final long posLong;
        private final Consumer<BlockState> callback;

        WatcherHandle(Level level, long posLong, Consumer<BlockState> callback) {
            this.level = level;
            this.posLong = posLong;
            this.callback = callback;
        }

        public void remove() {
            BlockStateWatcher.removeWatcher(this);
        }
    }
}
