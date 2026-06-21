package org.gtlcore.gtlcore.mixin.ae2.crafting;

import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingCalculation;
import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingTreeNode;
import org.gtlcore.gtlcore.integration.ae2.crafting.ICraftingTreeProcess;

import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.networking.crafting.ICraftingService;
import appeng.api.stacks.*;
import appeng.crafting.*;
import appeng.crafting.execution.CraftingCpuHelper;
import appeng.crafting.execution.InputTemplate;
import appeng.crafting.inv.ChildCraftingSimulationState;
import appeng.crafting.inv.CraftingSimulationState;
import appeng.crafting.inv.ICraftingInventory;
import appeng.crafting.pattern.AEProcessingPattern;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("AddedMixinMembersNamePattern")
@Mixin(CraftingTreeNode.class)
public class CraftingTreeNodeMixin implements ICraftingTreeNode {

    @Unique
    private IPatternDetails patternDetails;

    @Shadow(remap = false)
    @Final
    @Mutable
    final IPatternDetails.@Nullable IInput parentInput;
    @Shadow(remap = false)
    @Final
    @Mutable
    private final Level level;
    @Shadow(remap = false)
    @Final
    @Mutable
    private final AEKey what;
    @Shadow(remap = false)
    @Final
    private appeng.crafting.CraftingCalculation job;
    @Shadow(remap = false)
    private ArrayList<CraftingTreeProcess> nodes;
    @Shadow(remap = false)
    @Final
    private long amount;
    @Shadow(remap = false)
    @Final
    private boolean canEmit;

    public CraftingTreeNodeMixin(IPatternDetails.@Nullable IInput parentInput, Level level, AEKey what) {
        this.parentInput = parentInput;
        this.level = level;
        this.what = what;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void CraftingTreeNode(ICraftingService cc, appeng.crafting.CraftingCalculation job, AEKey what, long amount, CraftingTreeProcess par, int slot, CallbackInfo ci) {
        this.patternDetails = slot == -1 ? null : ((ICraftingTreeProcess) par).getDetails();
    }

    @Shadow(remap = false)
    private void addContainerItems(AEKey template, long multiplier, @Nullable KeyCounter outputList) {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private void buildChildPatterns() {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    void request(CraftingSimulationState inv, long requestedAmount, @Nullable KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        throw new AssertionError();
    }

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private Iterable<InputTemplate> getValidItemTemplates(ICraftingInventory inv) {
        if (this.parentInput == null)
            return List.of(new InputTemplate(what, 1));
        else if (this.patternDetails instanceof AEProcessingPattern) {
            GenericStack stack = this.parentInput.getPossibleInputs()[0];
            return List.of(new InputTemplate(stack.what(), stack.amount()));
        }
        return CraftingCpuHelper.getValidItemTemplates(inv, this.parentInput, level);
    }

    @Override
    @Unique
    public void legacyRequest(CraftingSimulationState inv, long requestedAmount,
                              @Nullable KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        request(inv, requestedAmount, containerItems);
    }

    @Override
    @Unique
    public void fastRequest(CraftingSimulationState inv, long requestedAmount,
                            @Nullable KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        ((ICraftingCalculation) this.job).handlePausing();

        inv.addStackBytes(what, amount, requestedAmount);

        for (var template : getValidItemTemplates(inv)) {
            long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);

            if (extracted > 0) {
                requestedAmount -= extracted;
                addContainerItems(template.key(), extracted, containerItems);

                if (requestedAmount == 0) {
                    return;
                }
            }
        }

        addContainerItems(what, requestedAmount, containerItems);

        if (this.canEmit) {
            inv.emitItems(this.what, this.amount * requestedAmount);
            return;
        }

        buildChildPatterns();
        long totalRequestedItems = requestedAmount * this.amount;
        if (this.nodes.size() == 1) {
            final ICraftingTreeProcess pro = (ICraftingTreeProcess) (this.nodes.get(0));
            var craftedPerPattern = pro.getOutputCountTest(this.what);

            while (pro.getPossible() && totalRequestedItems > 0) {
                long times;
                if (pro.limitsQuantityTest()) {
                    times = 1;
                } else {
                    times = (totalRequestedItems + craftedPerPattern - 1) / craftedPerPattern;
                }
                pro.fastRequest(inv, times);

                var available = inv.extract(this.what, totalRequestedItems, Actionable.MODULATE);
                if (available != 0) {
                    totalRequestedItems -= available;

                    if (totalRequestedItems <= 0) {
                        return;
                    }
                } else {
                    var pattern = pro.getDetails().getDefinition();
                    String outputs = Stream.of(pro.getDetails().getOutputs())
                            .map(GenericStack::toString)
                            .collect(Collectors.joining(", "));
                    String errorMessage = """
                            Unexpected error in the crafting calculation: can't find created items.
                            This is an AE2 bug, please report it, with the following important information:

                            - Found none of %s. Remaining request: %d of %d*%d.
                            - Tried crafting %d times the pattern %s with tag %s.
                            - Pattern outputs: %s.
                            """.formatted(what, totalRequestedItems, requestedAmount, amount, times, pattern,
                            pattern.getTag(), outputs);
                    throw new UnsupportedOperationException(errorMessage);
                }
            }
        } else if (this.nodes.size() > 1) {
            // Multiple branches: distribute load evenly across all branches
            // This optimization strategy divides the request equally among available patterns
            // and continues trying all patterns in subsequent iterations

            while (totalRequestedItems > 0) {
                int processCount = this.nodes.size();
                long baseAmount = totalRequestedItems / processCount;
                long remainder = totalRequestedItems % processCount;
                boolean anySucceeded = false;

                for (int i = 0; i < this.nodes.size(); i++) {
                    ICraftingTreeProcess pro = (ICraftingTreeProcess) (this.nodes.get(i));

                    if (!pro.getPossible())
                        continue;

                    long targetAmount = baseAmount + (i < remainder ? 1 : 0);
                    if (targetAmount <= 0) continue;

                    try {
                        var craftedPerPattern = pro.getOutputCountTest(this.what);
                        long times = pro.limitsQuantityTest() ? 1 : (targetAmount + craftedPerPattern - 1) / craftedPerPattern;

                        if (times > 0) {
                            final ChildCraftingSimulationState child = new ChildCraftingSimulationState(inv);
                            pro.fastRequest(child, times);

                            var available = child.extract(this.what, targetAmount, Actionable.MODULATE);

                            if (available != 0) {
                                child.applyDiff(inv);
                                anySucceeded = true;

                                totalRequestedItems -= available;

                                if (totalRequestedItems <= 0) {
                                    return;
                                }
                            } else {
                                pro.setPossible(false);
                            }
                        }
                    } catch (CraftBranchFailure fail) {
                        // This process failed this iteration, but might succeed later
                    }
                }

                // If no process succeeded in this iteration, we're done trying
                if (!anySucceeded) {
                    break;
                }
            }
        }

        if (this.job.isSimulation()) {
            this.job.getMissingItems().add(this.what, totalRequestedItems);
        } else {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
    }

    @Override
    @Unique
    public void ultraFastRequest(CraftingSimulationState inv, long requestedAmount,
                                 @Nullable KeyCounter containerItems) throws CraftBranchFailure, InterruptedException {
        ((ICraftingCalculation) this.job).handlePausing();

        inv.addStackBytes(what, amount, requestedAmount);

        for (var template : getValidItemTemplates(inv)) {
            long extracted = CraftingCpuHelper.extractTemplates(inv, template, requestedAmount);

            if (extracted > 0) {
                requestedAmount -= extracted;
                addContainerItems(template.key(), extracted, containerItems);

                if (requestedAmount == 0) {
                    return;
                }
            }
        }

        addContainerItems(what, requestedAmount, containerItems);

        if (this.canEmit) {
            inv.emitItems(this.what, this.amount * requestedAmount);
            return;
        }

        buildChildPatterns();
        long totalRequestedItems = requestedAmount * this.amount;
        if (this.nodes.size() == 1) {
            final ICraftingTreeProcess pro = (ICraftingTreeProcess) (this.nodes.get(0));
            var craftedPerPattern = pro.getOutputCountTest(this.what);

            while (pro.getPossible() && totalRequestedItems > 0) {
                long times;
                if (pro.limitsQuantityTest()) {
                    times = 1;
                } else {
                    times = (totalRequestedItems + craftedPerPattern - 1) / craftedPerPattern;
                }
                pro.ultraFastRequest(inv, times);

                var available = inv.extract(this.what, totalRequestedItems, Actionable.MODULATE);
                if (available != 0) {
                    totalRequestedItems -= available;

                    if (totalRequestedItems <= 0) {
                        return;
                    }
                } else {
                    var pattern = pro.getDetails().getDefinition();
                    String outputs = Stream.of(pro.getDetails().getOutputs())
                            .map(GenericStack::toString)
                            .collect(Collectors.joining(", "));
                    String errorMessage = """
                            Unexpected error in the crafting calculation: can't find created items.
                            This is an AE2 bug, please report it, with the following important information:

                            - Found none of %s. Remaining request: %d of %d*%d.
                            - Tried crafting %d times the pattern %s with tag %s.
                            - Pattern outputs: %s.
                            """.formatted(what, totalRequestedItems, requestedAmount, amount, times, pattern,
                            pattern.getTag(), outputs);
                    throw new UnsupportedOperationException(errorMessage);
                }
            }
        } else if (this.nodes.size() > 1) {
            // Multiple branches: try maximum value for each node only once
            // This optimization strategy attempts the full remaining request on each pattern once

            for (CraftingTreeProcess node : this.nodes) {
                ICraftingTreeProcess pro = (ICraftingTreeProcess) node;

                try {
                    var craftedPerPattern = pro.getOutputCountTest(this.what);
                    long times = pro.limitsQuantityTest() ? 1 : (totalRequestedItems + craftedPerPattern - 1) / craftedPerPattern;

                    if (times > 0) {
                        final ChildCraftingSimulationState child = new ChildCraftingSimulationState(inv);
                        pro.ultraFastRequest(child, times);

                        var available = child.extract(this.what, totalRequestedItems, Actionable.MODULATE);

                        if (available != 0) {
                            child.applyDiff(inv);

                            totalRequestedItems -= available;

                            if (totalRequestedItems <= 0) {
                                return;
                            }
                        }
                    }
                } catch (CraftBranchFailure fail) {
                    // This process failed, move to next node
                }
            }
        }

        if (this.job.isSimulation()) {
            this.job.getMissingItems().add(this.what, totalRequestedItems);
        } else {
            throw new CraftBranchFailure(this.what, totalRequestedItems);
        }
    }
}
