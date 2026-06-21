package org.gtlcore.gtlcore.integration.ae2.async;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.*;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.*;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class AEWriteService implements AutoCloseable {

    public static final AEWriteService INSTANCE = new AEWriteService();
    public static int THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
    public static int TIME_OUT = 10;

    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            1, 1, 30, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(4096),
            r -> {
                Thread t = new Thread(r, "AE-Writer");
                t.setDaemon(true);
                t.setPriority(Math.max(Thread.MIN_PRIORITY, Math.min(Thread.MAX_PRIORITY, THREAD_PRIORITY)));
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    public void submitIngredientLeft(WeakReference<AEAccumulator> accRef, List<Ingredient> left) {
        if (left == null || left.isEmpty()) return;
        executor.execute(() -> {
            var acc = accRef.get();
            if (acc == null) return;
            for (Ingredient ingredient : left) {
                if (ingredient instanceof IntProviderIngredient intProvider) {
                    intProvider.setItemStacks(null);
                    intProvider.setSampledCount(null);
                }

                ItemStack[] items = ingredient.getItems();
                if (items.length != 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty()) {
                        acc.add(AEItemKey.of(output), ingredient instanceof LongIngredient longIngredient ? longIngredient.getActualAmount() : output.getCount());
                    }
                }
            }
        });
    }

    public void submitFluidIngredientLeft(WeakReference<AEAccumulator> accRef, List<FluidIngredient> left) {
        if (left == null || left.isEmpty()) return;
        executor.execute(() -> {
            var acc = accRef.get();
            if (acc == null) return;
            for (FluidIngredient fluidIngredient : left) {
                if (!fluidIngredient.isEmpty()) {
                    FluidStack[] fluids = fluidIngredient.getStacks();
                    if (fluids.length != 0) {
                        FluidStack output = fluids[0];
                        acc.add(AEFluidKey.of(output.getFluid()), output.getAmount());
                    }
                }
            }
        });
    }

    public void prepareDrainedData(WeakReference<AEAccumulator> accRef,
                                   AtomicReference<Object2LongOpenHashMap<AEKey>> targetRef,
                                   AtomicBoolean drainRequested) {
        executor.execute(() -> {
            var acc = accRef.get();
            if (acc == null) {
                drainRequested.set(false);
                return;
            }

            if (targetRef.get() != null) {
                drainRequested.set(false);
                return;
            }

            Object2LongOpenHashMap<AEKey> drainedData = new Object2LongOpenHashMap<>();
            acc.drainTo(drainedData);

            if (!drainedData.isEmpty()) {
                if (!targetRef.compareAndSet(null, drainedData)) {
                    drainedData.object2LongEntrySet().fastForEach(e -> acc.add(e.getKey(), e.getLongValue()));
                }
            }

            drainRequested.set(false);
        });
    }

    public void shutDownGracefully() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(TIME_OUT, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

    @Override
    public void close() {
        shutDownGracefully();
    }
}
