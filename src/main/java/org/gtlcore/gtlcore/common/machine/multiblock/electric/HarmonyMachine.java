package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;
import com.hepdd.gtmthings.utils.TeamUtil;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;
import org.gtlcore.gtlcore.utils.MachineIO;
import org.gtlcore.gtlcore.utils.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HarmonyMachine extends NoEnergyMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            HarmonyMachine.class, NoEnergyMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int oc = 0;
    @Persisted
    private long hydrogen = 0, helium = 0;
    @Persisted
    private UUID userid;

    protected ConditionalSubscriptionHandler StartupSubs;

    public HarmonyMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.StartupSubs = new ConditionalSubscriptionHandler(this, this::StartupUpdate, this::isFormed);
    }

    protected void StartupUpdate() {
        if (getOffsetTimer() % 10 == 0 && !this.onWorking()) {
            if (MachineIO.inputFluid(this, Registries.getFluidStack("gtceu:hydrogen", 1000000))) {
                hydrogen += 1000000;
            }
            if (MachineIO.inputFluid(this, Registries.getFluidStack("gtceu:helium", 1000000))) {
                helium += 1000000;
            }
            if (MachineIO.notConsumableCircuit(this, 4)) {
                oc = 4;
            }
            if (MachineIO.notConsumableCircuit(this, 3)) {
                oc = 3;
            }
            if (MachineIO.notConsumableCircuit(this, 2)) {
                oc = 2;
            }
            if (MachineIO.notConsumableCircuit(this, 1)) {
                oc = 1;
            }
        }
    }

    private long getStartupEnergy() {
        return oc == 0 ? 0 : (long) (5277655810867200L * Math.pow(8, oc - 1));
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        StartupSubs.initialize(getLevel());
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof HarmonyMachine harmonyMachine && harmonyMachine.userid != null && harmonyMachine.hydrogen >= 1024000000 && harmonyMachine.helium >= 1024000000 && harmonyMachine.oc > 0) {
            harmonyMachine.hydrogen -= 1024000000;
            harmonyMachine.helium -= 1024000000;
            if (WirelessEnergyManager.addEUToGlobalEnergyMap(harmonyMachine.userid, -harmonyMachine.getStartupEnergy(), machine)) {
                GTRecipe recipe1 = recipe.copy();
                recipe1.duration = (int) (4800 / Math.pow(2, harmonyMachine.oc));
                return recipe1;
            }
        }
        return null;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.userid==null || !this.userid.equals(player.getUUID())) {
            this.userid = player.getUUID();
        }
        return true;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (this.isFormed()) {
            if (userid != null) {
                textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.0",
                        TeamUtil.GetName(getLevel(), userid)));
                textList.add(Component.translatable("gtmthings.machine.wireless_energy_monitor.tooltip.1",
                        FormattingUtil.formatNumbers(WirelessEnergyManager.getUserEU(userid))));
            }
            textList.add(Component.literal("启动耗能：" + FormattingUtil.formatNumbers(getStartupEnergy()) + "EU"));
            textList.add(Component.literal("氢储量：" + FormattingUtil.formatNumbers(hydrogen) + "mb"));
            textList.add(Component.literal("氦储量：" + FormattingUtil.formatNumbers(helium) + "mb"));
        }
    }
}
