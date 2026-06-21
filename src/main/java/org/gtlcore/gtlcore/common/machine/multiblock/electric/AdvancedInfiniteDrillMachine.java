package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.common.data.GTLMaterials;
import org.gtlcore.gtlcore.common.machine.multiblock.part.TemperatureSensorPartMachine;
import org.gtlcore.gtlcore.common.machine.trait.AdvancedInfiniteDrillLogic;
import org.gtlcore.gtlcore.utils.MachineUtil;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author EasterFG on 2024/10/26
 *         <p>
 *         1.2 * heat
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AdvancedInfiniteDrillMachine extends StorageMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            AdvancedInfiniteDrillMachine.class, StorageMachine.MANAGED_FIELD_HOLDER);

    private static final FluidStack DISTILLED_WATER = GTMaterials.DistilledWater.getFluid(20000);
    private static final FluidStack OXYGEN = GTMaterials.Oxygen.getFluid(FluidStorageKeys.LIQUID, 20000);
    private static final FluidStack HELIUM = GTMaterials.Helium.getFluid(FluidStorageKeys.LIQUID, 20000);
    private static final FluidStack NITROGEN = GTMaterials.Nitrogen.getFluid(20000);

    protected static final Map<Item, Integer> DRILLS = Map.of(
            ChemicalHelper.get(TagPrefix.toolHeadDrill, GTMaterials.Neutronium).getItem(), 50000,
            ChemicalHelper.get(TagPrefix.toolHeadDrill, GTLMaterials.Vibranium).getItem(), 100000);

    protected static final Map<Material, Integer> TEMPERATURE = Map.of(
            GTLMaterials.TitanSteel, 0,
            GTLMaterials.Adamantine, 1,
            GTLMaterials.NaquadriaticTaranium, 2,
            GTLMaterials.Starmetal, 3,
            GTLMaterials.Infinity, 4,
            GTLMaterials.Hypogen, 6,
            GTLMaterials.Eternity, 8);

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public static final double RUNNING_HEAT = 2000;

    public int maxHeat = -1;

    @Persisted
    @Getter
    protected int currentHeat = 300;

    @Persisted
    protected int process = 0;

    @Persisted
    protected boolean fast;

    protected ConditionalSubscriptionHandler heatSubs;

    private TemperatureSensorPartMachine sensorMachines;

    public AdvancedInfiniteDrillMachine(IMachineBlockEntity holder) {
        super(holder, 1);
        this.heatSubs = new ConditionalSubscriptionHandler(this, this::heatUpdate, this::isFormed);
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new AdvancedInfiniteDrillLogic(this, 5);
    }

    protected void heatUpdate() {
        if (!fast && getOffsetTimer() % 5 != 0) return;
        if (isEmpty() || maxHeat == -1) return;
        int heat = 0;

        if (getRecipeLogic().isWorking()) {
            if (process <= 0) {
                heat += (int) Math.floor(Math.abs(currentHeat - RUNNING_HEAT) / 2000D);
            }
        }

        if (getRecipeLogic().isWorking() || process > 0) {
            if (MachineUtil.inputFluid(this, DISTILLED_WATER)) {
                heat--;
            } else if (MachineUtil.inputFluid(this, OXYGEN)) {
                heat -= 2;
            } else if (MachineUtil.inputFluid(this, HELIUM)) {
                heat -= 4;
            } else if (MachineUtil.inputFluid(this, NITROGEN)) {
                heat -= 8;
            }
        }

        if (inputBlast()) {
            heat++;
        }

        this.currentHeat = Math.max(0, currentHeat + heat);

        if (currentHeat > maxHeat) {
            process += fast ? 1 : 5;
            if (process >= 300) {
                process = 0;
                currentHeat = 300;
                this.machineStorage.setStackInSlot(0, ItemStack.EMPTY);
                this.getRecipeLogic().interruptRecipe();
            }
        } else if (process > 0) {
            process--;
        }

        if (sensorMachines != null) {
            sensorMachines.update(currentHeat);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var type = getMultiblockState().getMatchContext().get("CoilType");
        if (type instanceof ICoilType coil) {
            Material material = coil.getMaterial();
            int rate = TEMPERATURE.getOrDefault(material, -1);
            if (rate < 0) {
                maxHeat = -1;
            } else {
                maxHeat = 8000 + (1000 * rate);
            }
        }
        List<IMultiPart> parts = getParts();
        for (IMultiPart part : parts) {
            if (part instanceof TemperatureSensorPartMachine temperatureSensorPartMachine) {
                sensorMachines = temperatureSensorPartMachine;
                break;
            }
        }
        this.heatSubs.initialize(getLevel());
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        sensorMachines = null;
    }

    @Override
    protected boolean filter(ItemStack itemStack) {
        return DRILLS.containsKey(itemStack.getItem());
    }

    public int getRate() {
        double heat = computeTempEfficiency();
        double volt = computeVoltageFactor();
        double overclocking = (maxHeat - 12000) / 1000D * 0.125;
        return (int) (getDrillHeadRate() * (1 + overclocking) * heat * volt);
    }

    private double computeTempEfficiency() {
        double normalizedTemp = (currentHeat - RUNNING_HEAT) / (maxHeat - RUNNING_HEAT);
        if (normalizedTemp < 0.4) {
            return 0.3 * Math.sqrt(normalizedTemp / 0.4);
        } else {
            double hyperFactor = (normalizedTemp - 0.4) / (1 - 0.4);
            return 0.3 + 0.7 * Math.pow(hyperFactor, 2.5);
        }
    }

    private double computeVoltageFactor() {
        double voltage = (GTUtil.getTierByVoltage(getMaxVoltage()) - GTValues.UHV) / 5.0;
        double clampedVoltage = Math.max(0, Math.min(voltage, 1.0));
        double tempRatio = (currentHeat - RUNNING_HEAT) / (maxHeat - RUNNING_HEAT);
        double decayFactor = 1.0 - 0.2 * tempRatio;
        return Math.pow(clampedVoltage, 0.6 * decayFactor) * (1.0 - 0.05 * (1.0 - tempRatio));
    }

    @Override
    public AdvancedInfiniteDrillLogic getRecipeLogic() {
        return (AdvancedInfiniteDrillLogic) super.getRecipeLogic();
    }

    @Override
    public void onWaiting() {
        super.onWaiting();
        getRecipeLogic().resetRecipeLogic();
    }

    @Override
    public void addDisplayText(List<Component> tooltips) {
        if (isFormed()) {
            MultiblockDisplayText.builder(tooltips, true)
                    .setWorkingStatus(isWorkingEnabled(), isActive())
                    .addIdlingLine(false)
                    .addEnergyUsageLine(energyContainer);
            if (isEmpty()) {
                tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.not_fluid_head")
                        .withStyle(ChatFormatting.RED));
            } else {
                if (maxHeat == -1) {
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.no_coil")
                            .withStyle(ChatFormatting.RED));
                } else {
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.range",
                            Component.literal("5x5").withStyle(ChatFormatting.GOLD)));
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.heat", maxHeat, RUNNING_HEAT));
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.current_heat", currentHeat));
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.fast",
                            ComponentPanelWidget.withButton(Component.translatable(fast ? "gui.gtlcore.status_on" : "gui.gtlcore.status_off"),
                                    "fast_mode")));
                    tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.process",
                            FormattingUtil.formatNumber2Places(process / 300F * 100))
                            .append("%"));
                    var fluids = getRecipeLogic().getVeinFluids();
                    if (!fluids.isEmpty()) {
                        fluids.forEach((fluid, produced) -> {
                            Component fluidInfo = fluid.getFluidType().getDescription().copy()
                                    .withStyle(ChatFormatting.GREEN);
                            Component amountInfo = Component.literal(FormattingUtil.formatNumbers(
                                    produced * getRate()) +
                                    " mB/s").withStyle(ChatFormatting.BLUE);
                            tooltips.add(Component.translatable("gtceu.machine.advanced_infinite_driller.drilled_fluid",
                                    fluidInfo, amountInfo));
                        });
                    } else {
                        Component noFluid = Component.translatable("gtceu.multiblock.fluid_rig.no_fluid_in_area")
                                .withStyle(ChatFormatting.RED);
                        tooltips.add(Component.translatable("gtceu.multiblock.fluid_rig.drilled_fluid", noFluid)
                                .withStyle(ChatFormatting.GRAY));
                    }
                }
            }
        } else {
            Component tooltip = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                    .withStyle(ChatFormatting.GRAY);
            tooltips.add(Component.translatable("gtceu.multiblock.invalid_structure")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))));
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (componentData.equals("fast_mode")) {
            fast = !fast;
        }
    }

    public int getDrillHeadRate() {
        ItemStack itemStack = getMachineStorageItem();
        if (!itemStack.isEmpty()) {
            Item item = itemStack.getItem();
            return DRILLS.getOrDefault(item, 0);
        }
        return 0;
    }

    protected boolean inputBlast() {
        return MachineUtil.inputFluid(this, GTMaterials.Blaze.getFluid(getFluidConsume()));
    }

    protected long getFluidConsume() {
        return (long) Math.pow(currentHeat, 1.3);
    }

    public boolean canRunnable() {
        return currentHeat >= RUNNING_HEAT;
    }
}
