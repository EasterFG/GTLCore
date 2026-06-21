package org.gtlcore.gtlcore.mixin.ae2.ticking;

import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import appeng.crafting.CraftingCalculation;
import appeng.hooks.ticking.TickHandler;
import appeng.me.Grid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TickHandler.class)
public abstract class TickHandlerMixin {

    @Shadow(remap = false)
    public Iterable<Grid> getGridList() {
        throw new AssertionError();
    }

    @Shadow(remap = false)
    private void readyBlockEntities(ServerLevel level) {
        throw new AssertionError();
    }

    /**
     * @author Dragons
     * @reason 删去simulateCraftingJobs调用
     */
    @Overwrite(remap = false)
    private void onServerLevelTickEnd(ServerLevel level) {
        this.readyBlockEntities(level);

        // tick networks
        for (var g : this.getGridList()) {
            try {
                g.onLevelEndTick(level);
            } catch (Throwable t) {
                CrashReport crashReport = CrashReport.forThrowable(t, "Ticking grid on end of level tick");
                g.fillCrashReportCategory(crashReport.addCategory("Grid being ticked"));
                level.fillReportDetails(crashReport);
                throw new ReportedException(crashReport);
            }
        }
    }

    /**
     * @author Dragons
     * @reason 禁用
     */
    @Overwrite(remap = false)
    public void registerCraftingSimulation(Level level, CraftingCalculation craftingCalculation) {
        throw new AssertionError();
    }

    /**
     * @author Dragons
     * @reason 禁用
     */
    @Overwrite(remap = false)
    private void simulateCraftingJobs(LevelAccessor level) {
        throw new AssertionError();
    }
}
