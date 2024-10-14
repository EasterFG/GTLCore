package org.gtlcore.gtlcore.common.machine.multiblock.noenergy;

import org.gtlcore.gtlcore.api.machine.multiblock.NoEnergyMultiblockMachine;
import org.gtlcore.gtlcore.api.pattern.util.IValueContainer;
import org.gtlcore.gtlcore.common.machine.multiblock.part.NeutronAcceleratorPartMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.part.NeutronSensorPartMachine;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.IParallelHatch;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NeutronActivatorMachine extends NoEnergyMultiblockMachine {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            NeutronActivatorMachine.class, NoEnergyMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    private int height = 0;

    @Getter
    @Persisted
    @DescSynced
    private int eV;

    @Persisted
    private boolean isWorking;

    protected ConditionalSubscriptionHandler neutronEnergySubs;
    protected ConditionalSubscriptionHandler moderateSubs;
    protected ConditionalSubscriptionHandler absorptionSubs;

    @Persisted
    private Set<NeutronSensorPartMachine> sensorMachines;
    @Persisted
    private Set<ItemBusPartMachine> busMachines;
    @Persisted
    private Set<NeutronAcceleratorPartMachine> acceleratorMachines;

    public NeutronActivatorMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.neutronEnergySubs = new ConditionalSubscriptionHandler(this, this::neutronEnergyUpdate, this::isFormed);
        this.moderateSubs = new ConditionalSubscriptionHandler(this, this::moderateUpdate, () -> eV > 0);
        this.absorptionSubs = new ConditionalSubscriptionHandler(this, this::absorptionUpdate, () -> eV > 0);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        IValueContainer<?> container = getMultiblockState().getMatchContext()
                .getOrCreate("SpeedPipeValue", IValueContainer::noop);
        if (container.getValue() instanceof Integer integer) {
            height = integer;
        }
        Map<Long, IO> ioMap = getMultiblockState().getMatchContext()
                .getOrCreate("ioMap", Long2ObjectMaps::emptyMap);
        for (IMultiPart part : getParts()) {
            if (part instanceof ItemBusPartMachine itemBusPartMachine) {
                busMachines = Objects.requireNonNullElseGet(busMachines, HashSet::new);
                busMachines.add(itemBusPartMachine);
            }
            if (part instanceof NeutronSensorPartMachine neutronSensorMachine) {
                sensorMachines = Objects.requireNonNullElseGet(sensorMachines, HashSet::new);
                sensorMachines.add(neutronSensorMachine);
            }
            if (part instanceof NeutronAcceleratorPartMachine neutronAccelerator) {
                acceleratorMachines = Objects.requireNonNullElseGet(acceleratorMachines, HashSet::new);
                acceleratorMachines.add(neutronAccelerator);
            }
            IO io = ioMap.getOrDefault(part.self().getPos().asLong(), IO.BOTH);
            if (io == IO.NONE) continue;
            for (var handler : part.getRecipeHandlers()) {
                var handlerIO = handler.getHandlerIO();
                if (io != IO.BOTH && handlerIO != IO.BOTH && io != handlerIO) continue;
                if (handler.getCapability() == EURecipeCapability.CAP && handler instanceof IEnergyContainer) {
                    traitSubscriptions.add(handler.addChangedListener(neutronEnergySubs::updateSubscription));
                    traitSubscriptions.add(handler.addChangedListener(moderateSubs::updateSubscription));
                }
                if (handler.getCapability() == ItemRecipeCapability.CAP && handler instanceof IItemTransfer) {
                    if (handlerIO == IO.IN || handlerIO == IO.BOTH) {
                        traitSubscriptions.add(handler.addChangedListener(absorptionSubs::updateSubscription));
                    }
                }
            }
        }
        neutronEnergySubs.initialize(getLevel());
    }

    @Override
    public void onLoad() {
        super.onLoad();
        moderateSubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        height = 0;
        sensorMachines = null;
        busMachines = null;
        acceleratorMachines = null;
    }

    private double getEfficiencyFactor() {
        return Math.pow(0.95, Math.max(height - 4, 0));
    }

    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof NeutronActivatorMachine nMachine &&
                (nMachine.eV > recipe.data.getInt("ev_min") * 1000000 &&
                        nMachine.eV < recipe.data.getInt("ev_max") * 1000000)) {
            GTRecipe recipe1 = GTRecipeModifiers.accurateParallel(machine, recipe, nMachine.getParallel(), false)
                    .getFirst();
            recipe1.duration = (int) Math.round(Math.max(recipe1.duration * nMachine.getEfficiencyFactor(), 1));
            return recipe1;
        }
        return null;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        return eV >= recipe.data.getInt("evt") * 1000 * Math.pow(getParallel(), 2);
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (getRecipeLogic().getLastRecipe() != null) {
            int evt = (int) (getRecipeLogic().getLastRecipe().data.getInt("evt") *
                    1000 * Math.max(1, Math.pow(getParallel(), 2) * getEfficiencyFactor()));
            if (eV < evt) {
                getRecipeLogic().interruptRecipe();
                return false;
            } else {
                eV -= evt;
            }
        }
        return value;
    }

    protected void neutronEnergyUpdate() {
        if (acceleratorMachines == null) return;
        boolean anyWorking = false;
        for (var accelerator : acceleratorMachines) {
            long increase = accelerator.consumeEnergy();
            if (increase > 0) {
                anyWorking = true;
                this.eV += (int) Math.round(Math.max(increase * getEfficiencyFactor(), 1));
            }
        }
        this.isWorking = anyWorking;
        if (this.eV > 1200000000) this.eV = 0;
        if (!isWorking) neutronEnergySubs.unsubscribe();
    }

    protected void moderateUpdate() {
        if (!isWorking && getOffsetTimer() % 20 == 0) {
            this.eV = Math.max(eV - 72 * 1000, 0);
        }
        if (this.eV < 0) this.eV = 0;
        if (!isFormed() || sensorMachines == null) return;
        sensorMachines.forEach(sensor -> sensor.update(eV));
    }

    protected void absorptionUpdate() {
        if (busMachines == null || eV <= 0) return;
        boolean hasSlower = false;
        for (var bus : busMachines) {
            var inv = bus.getInventory();
            var io = inv.getHandlerIO();
            if (io == IO.IN || io == IO.BOTH) {
                for (int i = 0; i < inv.getSlots(); i++) {
                    var dustBeryllium = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Beryllium).getItem();
                    var dustGraphite = ChemicalHelper.get(TagPrefix.dust, GTMaterials.Graphite).getItem();
                    var stack = inv.getStackInSlot(i);
                    if (stack.is(dustBeryllium) || stack.is(dustGraphite)) {
                        hasSlower = true;
                        int consume = Math.min(Math.max(eV / (10 * 1000000), 1), stack.getCount());
                        inv.extractItemInternal(i, consume, false);
                        this.eV -= 10 * 1000000 * consume;
                    }
                }
            }
        }
        if (!hasSlower) absorptionSubs.unsubscribe();
    }

    private int getParallel() {
        int numParallels = 1;
        Optional<IParallelHatch> optional = this.getParts().stream().filter(IParallelHatch.class::isInstance)
                .map(IParallelHatch.class::cast).findAny();
        if (optional.isPresent()) {
            IParallelHatch parallelHatch = optional.get();
            numParallels = parallelHatch.getCurrentParallel();
        }
        return numParallels;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {
            int numParallels = getParallel();
            textList.add(Component.translatable("gtceu.multiblock.parallel",
                    Component.literal(FormattingUtil.formatNumbers(numParallels)).withStyle(ChatFormatting.DARK_PURPLE))
                    .withStyle(ChatFormatting.GRAY));
            textList.add(Component.translatable("gtceu.machine.neutron_activator.ev", processNumber(eV)));
            textList.add(Component.translatable("gtceu.machine.neutron_activator.efficiency",
                    FormattingUtil.formatNumbers(Math.max(1, Math.pow(numParallels, 2) * getEfficiencyFactor()))));
            textList.add(Component.translatable("gtceu.machine.neutron_activator.height", height));
            textList.add(Component.translatable("gtceu.machine.neutron_activator.time",
                    FormattingUtil.formatNumbers(getEfficiencyFactor() * 100)).append("%"));
        }
    }

    private String processNumber(int num) {
        float num2;
        num2 = ((float) num) / 1000F;
        if (num2 <= 0) {
            return String.format("%d", num);
        }
        if (num2 < 1000.0) {
            return String.format("%.1fK", num2);
        }
        num2 /= 1000F;
        return String.format("%.1fM", num2);
    }
}
