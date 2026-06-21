package org.gtlcore.gtlcore.mixin.ae2.crafting;

import org.gtlcore.gtlcore.integration.ae2.AEUtils;
import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingCalculation;
import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingTreeNode;

import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.KeyCounter;
import appeng.core.AELog;
import appeng.crafting.CraftBranchFailure;
import appeng.crafting.CraftingCalculation;
import appeng.crafting.CraftingTreeNode;
import appeng.crafting.inv.CraftingSimulationState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(CraftingCalculation.class)
public abstract class CraftingCalculationMixin implements ICraftingCalculation {

    @Unique
    private final AtomicBoolean gTLCore$done = new AtomicBoolean(false);

    @Shadow(remap = false)
    private void logCraftingJob(ICraftingPlan plan) {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private ICraftingPlan computePlan() throws InterruptedException {
        throw new AssertionError();
    }

    /**
     * @author Dragons
     * @reason 优化性能
     */
    @Overwrite(remap = false)
    public ICraftingPlan run() {
        try {
            var plan = computePlan();
            this.logCraftingJob(plan);
            return plan;
        } catch (Exception ex) {
            AELog.info(ex, "Exception during async crafting calculation.");
            throw new RuntimeException(ex);
        } finally {
            this.finish();
        }
    }

    /**
     * @author Dragons
     * @reason 优化性能
     */
    @Overwrite(remap = false)
    private void finish() {
        gTLCore$done.set(true);
    }

    /**
     * @author Dragons
     * @reason 优化性能
     */
    @Overwrite(remap = false)
    public boolean simulateFor(int micros) {
        return !this.gTLCore$done.get();
    }

    /**
     * @author Dragons
     * @reason 优化性能
     */
    @Overwrite(remap = false)
    public void handlePausing() throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
    }

    @Redirect(
              method = "runCraftAttempt",
              at = @At(
                       value = "INVOKE",
                       target = "Lappeng/crafting/CraftingTreeNode;request(Lappeng/crafting/inv/CraftingSimulationState;JLappeng/api/stacks/KeyCounter;)V"),
              remap = false)
    private void redirectTreeRequest(
                                     CraftingTreeNode tree,
                                     CraftingSimulationState craftingInventory,
                                     long amount,
                                     KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        switch (AEUtils.CALCULATION_MODE) {
            case ULTRA_FAST -> ((ICraftingTreeNode) tree).ultraFastRequest(craftingInventory, amount, containerItems);
            case FAST -> ((ICraftingTreeNode) tree).fastRequest(craftingInventory, amount, containerItems);
            case LEGACY -> ((ICraftingTreeNode) tree).legacyRequest(craftingInventory, amount, containerItems);
        }
    }
}
