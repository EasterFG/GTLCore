package org.gtlcore.gtlcore.mixin.gtm.registry;

import org.gtlcore.gtlcore.common.data.GTLMachines;
import org.gtlcore.gtlcore.common.data.machines.GCyMMachines;
import org.gtlcore.gtlcore.common.machine.multiblock.generator.GTLLargeCombustionEngineMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.generator.GeneratorArrayMachine;
import org.gtlcore.gtlcore.common.machine.multiblock.generator.MegaTurbineMachine;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IRotorHolderMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.predicates.SimplePredicate;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.MachineBuilder;
import com.gregtechceu.gtceu.client.renderer.machine.RotorHolderMachineRenderer;
import com.gregtechceu.gtceu.client.renderer.machine.SimpleGeneratorMachineRenderer;
import com.gregtechceu.gtceu.common.data.*;
import com.gregtechceu.gtceu.common.data.machines.GTResearchMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.*;
import com.gregtechceu.gtceu.common.machine.storage.BufferMachine;
import com.gregtechceu.gtceu.data.lang.LangHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.ModLoader;

import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.pattern.Predicates.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.workableTiered;
import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.utils.FormattingUtil.toEnglishName;

@Mixin(GTMachines.class)
public class GTMachinesMixin {

    @Inject(method = "init", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void init(CallbackInfo ci) {
        GCyMMachines.init();
        GTResearchMachines.init();
        ModLoader.get().postEvent(new GTCEuAPI.RegisterEvent<>(GTRegistries.MACHINES, MachineDefinition.class));
        GTRegistries.MACHINES.freeze();
        ci.cancel();
    }

    @Unique
    private static MachineDefinition[] gTLCore$registerTieredMachines(String name,
                                                                      BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory,
                                                                      BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder,
                                                                      int... tiers) {
        MachineDefinition[] definitions = new MachineDefinition[GTValues.TIER_COUNT];
        for (int tier : tiers) {
            var register = REGISTRATE
                    .machine(GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + name,
                            holder -> factory.apply(holder, tier))
                    .tier(tier);
            definitions[tier] = builder.apply(tier, register);
        }
        return definitions;
    }

    @Inject(method = "registerTieredMachines", at = @At("HEAD"), remap = false, cancellable = true)
    private static void registerTieredMachines(String name, BiFunction<IMachineBlockEntity, Integer, MetaMachine> factory, BiFunction<Integer, MachineBuilder<MachineDefinition>, MachineDefinition> builder, int[] tiers, CallbackInfoReturnable<MachineDefinition[]> cir) {
        switch (name) {
            case "input_bus":
                cir.setReturnValue(gTLCore$registerTieredMachines("input_bus",
                        (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.IN),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " Input Bus")
                                .rotationState(RotationState.ALL)
                                .abilities(
                                        tier == 0 ? new PartAbility[] { PartAbility.IMPORT_ITEMS, PartAbility.STEAM_IMPORT_ITEMS } :
                                                new PartAbility[] { PartAbility.IMPORT_ITEMS })
                                .overlayTieredHullRenderer("item_bus.import")
                                .tooltips(Component.translatable("gtceu.machine.item_bus.import.tooltip"),
                                        Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                                (1 + tier) * (1 + tier)))
                                .compassNode("item_bus")
                                .register(),
                        ALL_TIERS));
                break;
            case "output_bus":
                cir.setReturnValue(gTLCore$registerTieredMachines("output_bus",
                        (holder, tier) -> new ItemBusPartMachine(holder, tier, IO.OUT),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " Output Bus")
                                .rotationState(RotationState.ALL)
                                .abilities(
                                        tier == 0 ? new PartAbility[] { PartAbility.EXPORT_ITEMS, PartAbility.STEAM_EXPORT_ITEMS } :
                                                new PartAbility[] { PartAbility.EXPORT_ITEMS })
                                .overlayTieredHullRenderer("item_bus.export")
                                .tooltips(Component.translatable("gtceu.machine.item_bus.export.tooltip"),
                                        Component.translatable("gtceu.universal.tooltip.item_storage_capacity",
                                                (1 + tier) * (1 + tier)))
                                .compassNode("item_bus")
                                .register(),
                        ALL_TIERS));
                break;
            case "energy_input_hatch_4a":
                cir.setReturnValue(gTLCore$registerTieredMachines("energy_input_hatch_4a",
                        (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 4),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " 4A Energy Hatch")
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.INPUT_ENERGY)
                                .tooltips(Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                                .overlayTieredHullRenderer("energy_hatch.input_4a")
                                .compassNode("energy_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "energy_output_hatch_4a":
                cir.setReturnValue(gTLCore$registerTieredMachines("energy_output_hatch_4a",
                        (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 4),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " 4A Dynamo Hatch")
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.OUTPUT_ENERGY)
                                .tooltips(Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                                .overlayTieredHullRenderer("energy_hatch.output_4a")
                                .compassNode("energy_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "energy_input_hatch_16a":
                cir.setReturnValue(gTLCore$registerTieredMachines("energy_input_hatch_16a",
                        (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.IN, 16),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " 16A Energy Hatch")
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.INPUT_ENERGY)
                                .tooltips(Component.translatable("gtceu.machine.energy_hatch.input_hi_amp.tooltip"))
                                .overlayTieredHullRenderer("energy_hatch.input_16a")
                                .compassNode("energy_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "energy_output_hatch_16a":
                cir.setReturnValue(gTLCore$registerTieredMachines("energy_output_hatch_16a",
                        (holder, tier) -> new EnergyHatchPartMachine(holder, tier, IO.OUT, 16),
                        (tier, builder1) -> builder1
                                .langValue(VNF[tier] + " 16A Dynamo Hatch")
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.OUTPUT_ENERGY)
                                .tooltips(Component.translatable("gtceu.machine.energy_hatch.output_hi_amp.tooltip"))
                                .overlayTieredHullRenderer("energy_hatch.output_16a")
                                .compassNode("energy_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "dual_input_hatch":
                cir.setReturnValue(gTLCore$registerTieredMachines(
                        "dual_input_hatch",
                        (holder, tier) -> new DualHatchPartMachine(holder, tier, IO.IN),
                        (tier, builder1) -> builder1
                                .langValue("%s Dual Input Hatch".formatted(VNF[tier]))
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.IMPORT_ITEMS)
                                .overlayTieredHullRenderer("dual_hatch.import")
                                .tooltips(
                                        Component.translatable("gtceu.machine.buffer.import.tooltip"),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.item_storage_capacity",
                                                (1 + tier) * (1 + tier)),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                                1 + tier,
                                                FluidHatchPartMachine.getTankCapacity(
                                                        DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)),
                                        Component.translatable("gtceu.universal.enabled"))
                                .tooltipBuilder(GTLMachines.GTL_MODIFY)
                                .compassNode("dual_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "dual_output_hatch":
                cir.setReturnValue(gTLCore$registerTieredMachines(
                        "dual_output_hatch",
                        (holder, tier) -> new DualHatchPartMachine(holder, tier, IO.OUT),
                        (tier, builder1) -> builder1
                                .langValue("%s Dual Output Hatch".formatted(VNF[tier]))
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.EXPORT_ITEMS)
                                .overlayTieredHullRenderer("dual_hatch.export")
                                .tooltips(
                                        Component.translatable("gtceu.machine.buffer.export.tooltip"),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.item_storage_capacity",
                                                (1 + tier) * (1 + tier)),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                                1 + tier,
                                                FluidHatchPartMachine.getTankCapacity(
                                                        DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)),
                                        Component.translatable("gtceu.universal.enabled"))
                                .tooltipBuilder(GTLMachines.GTL_MODIFY)
                                .compassNode("dual_hatch")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "buffer":
                cir.setReturnValue(gTLCore$registerTieredMachines("buffer",
                        BufferMachine::new,
                        (tier, builder1) -> builder1
                                .langValue("%s Buffer %s".formatted(VLVH[tier], VLVT[tier]))
                                .rotationState(RotationState.NONE)
                                .tieredHullRenderer(GTCEu.id("block/machine/buffer"))
                                .tooltips(
                                        Component.translatable("gtceu.machine.buffer.tooltip"),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.item_storage_capacity",
                                                BufferMachine.getInventorySize(tier)),
                                        Component.translatable(
                                                "gtceu.universal.tooltip.fluid_storage_capacity_mult",
                                                BufferMachine.getTankSize(tier), FluidHatchPartMachine.getTankCapacity(
                                                        DualHatchPartMachine.INITIAL_TANK_CAPACITY, tier)))
                                .tooltipBuilder(GTLMachines.GTL_MODIFY)
                                .compassNode("buffer")
                                .register(),
                        GTValues.tiersBetween(LV, GTCEuAPI.isHighTier() ? MAX : UHV)));
                break;
            case "rotor_holder":
                cir.setReturnValue(gTLCore$registerTieredMachines("rotor_holder",
                        RotorHolderPartMachine::new,
                        (tier, builder1) -> builder1
                                .langValue("%s Rotor Holder".formatted(VNF[tier]))
                                .rotationState(RotationState.ALL)
                                .abilities(PartAbility.ROTOR_HOLDER)
                                .renderer(() -> new RotorHolderMachineRenderer(tier))
                                .tooltips(LangHandler.getFromMultiLang("gtceu.machine.rotor_holder.tooltip", 0),
                                        LangHandler.getFromMultiLang("gtceu.machine.rotor_holder.tooltip", 1),
                                        Component.translatable("gtceu.universal.disabled"))
                                .compassNode("rotor_holder")
                                .register(),
                        HV, EV, IV, LuV, ZPM, UV, UHV, UEV));
        }
    }

    @Inject(method = "registerLaserHatch", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void registerLaserHatch(IO io, int amperage, PartAbility ability, CallbackInfoReturnable<MachineDefinition[]> cir) {
        String name = io == IO.IN ? "target" : "source";
        cir.setReturnValue(gTLCore$registerTieredMachines(amperage + "a_laser_" + name + "_hatch",
                (holder, tier) -> new LaserHatchPartMachine(holder, io, tier, amperage), (tier, builder) -> builder
                        .langValue(VNF[tier] + " " + FormattingUtil.formatNumbers(amperage) + "A Laser " +
                                FormattingUtil.toEnglishName(name) + " Hatch")
                        .rotationState(RotationState.ALL)
                        .tooltips(Component.translatable("gtceu.machine.laser_hatch." + name + ".tooltip"),
                                Component.translatable("gtceu.machine.laser_hatch.both.tooltip"),
                                Component.translatable("gtceu.universal.disabled"))
                        .abilities(ability)
                        .overlayTieredHullRenderer("laser_hatch." + name)
                        .register(),
                GTValues.tiersBetween(IV, GTCEuAPI.isHighTier() ? MAX : UHV)));
    }

    @Inject(method = "registerSimpleGenerator", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void registerSimpleGenerator(String name, GTRecipeType recipeType, Int2LongFunction tankScalingFunction, float hazardStrengthPerOperation, int[] tiers, CallbackInfoReturnable<MachineDefinition[]> cir) {
        cir.setReturnValue(gTLCore$registerTieredMachines(name,
                (holder, tier) -> new SimpleGeneratorMachine(holder, tier, hazardStrengthPerOperation * tier,
                        tankScalingFunction),
                (tier, builder) -> builder
                        .langValue("%s %s Generator %s".formatted(VLVH[tier], toEnglishName(name), VLVT[tier]))
                        .editableUI(SimpleGeneratorMachine.EDITABLE_UI_CREATOR.apply(GTCEu.id(name), recipeType))
                        .rotationState(RotationState.ALL)
                        .recipeType(recipeType)
                        .recipeModifier(SimpleGeneratorMachine::recipeModifier, true)
                        .addOutputLimit(ItemRecipeCapability.CAP, 0)
                        .addOutputLimit(FluidRecipeCapability.CAP, 0)
                        .renderer(() -> new SimpleGeneratorMachineRenderer(tier, GTCEu.id("block/generators/" + name)))
                        .tooltips(Component.translatable("gtceu.machine.efficiency.tooltip",
                                GeneratorArrayMachine.getEfficiency(recipeType, tier)).append("%"))
                        .tooltips(Component.translatable("gtceu.universal.tooltip.ampere_out",
                                GeneratorArrayMachine.getAmperage(tier)))
                        .tooltips(workableTiered(tier, GTValues.V[tier],
                                GTValues.V[tier] * 64 * GeneratorArrayMachine.getAmperage(tier), recipeType,
                                tankScalingFunction.apply(tier), false))
                        .tooltipBuilder(GTLMachines.GTL_MODIFY)
                        .compassNode(name)
                        .register(),
                tiers));
    }

    @Inject(method = "registerLargeCombustionEngine", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void registerLargeCombustionEngine(String name, int tier, Supplier<? extends Block> casing, Supplier<? extends Block> gear, Supplier<? extends Block> intake, ResourceLocation casingTexture, ResourceLocation overlayModel, CallbackInfoReturnable<MultiblockMachineDefinition> cir) {
        cir.setReturnValue(REGISTRATE.multiblock(name, holder -> new GTLLargeCombustionEngineMachine(holder, tier))
                .rotationState(RotationState.ALL)
                .recipeType(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS)
                .generator(true)
                .recipeModifier(GTLLargeCombustionEngineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("XXX", "XDX", "XXX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("XCX", "CGC", "XCX")
                        .aisle("AAA", "AYA", "AAA")
                        .where('X', blocks(casing.get()))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()).setMinGlobalLimited(3)
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .where('D',
                                ability(PartAbility.OUTPUT_ENERGY,
                                        Stream.of(ULV, LV, MV, HV, EV, IV, LuV, ZPM, UV, UHV).filter(t -> t >= tier)
                                                .mapToInt(Integer::intValue).toArray())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                GTValues.VN[tier])))
                        .where('A',
                                blocks(intake.get())
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_1")))
                        .where('Y', controller(blocks(definition.getBlock())))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingRenderer(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier] * 4),
                        Component.translatable("gtceu.universal.tooltip.uses_per_hour_lubricant",
                                FluidHelper.getBucket()),
                        tier > EV ?
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_extreme",
                                        V[tier] * 16) :
                                Component.translatable("gtceu.machine.large_combustion_engine.tooltip.boost_regular",
                                        V[tier] * 12))
                .tooltipBuilder(GTLMachines.GTL_MODIFY)
                .compassSections(GTCompassSections.TIER[EV])
                .compassNode("large_combustion")
                .register());
    }

    @Inject(method = "registerLargeTurbine", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static void registerLargeTurbine(String name, int tier, GTRecipeType recipeType, Supplier<? extends Block> casing, Supplier<? extends Block> gear, ResourceLocation casingTexture, ResourceLocation overlayModel, CallbackInfoReturnable<MultiblockMachineDefinition> cir) {
        int finalAm = switch (name) {
            case "rocket_large_turbine" -> 12;
            case "supercritical_steam_turbine" -> 16;
            default -> 8;
        };
        cir.setReturnValue(REGISTRATE.multiblock(name, holder -> new MegaTurbineMachine(holder, tier, finalAm))
                .rotationState(RotationState.ALL)
                .recipeType(recipeType)
                .generator(true)
                .recipeModifier(MegaTurbineMachine::recipeModifier, true)
                .appearanceBlock(casing)
                .pattern(definition -> FactoryBlockPattern.start()
                        .aisle("CCCC", "CHHC", "CCCC")
                        .aisle("CHHC", "RGGR", "CHHC")
                        .aisle("CCCC", "CSHC", "CCCC")
                        .where('S', controller(blocks(definition.getBlock())))
                        .where('G', blocks(gear.get()))
                        .where('C', blocks(casing.get()))
                        .where('R',
                                new TraceabilityPredicate(
                                        new SimplePredicate(
                                                state -> MetaMachine.getMachine(state.getWorld(),
                                                        state.getPos()) instanceof IRotorHolderMachine rotorHolder &&
                                                        state.getWorld()
                                                                .getBlockState(state.getPos()
                                                                        .relative(rotorHolder.self().getFrontFacing()))
                                                                .isAir(),
                                                () -> PartAbility.ROTOR_HOLDER.getAllBlocks().stream()
                                                        .map(BlockInfo::fromBlock).toArray(BlockInfo[]::new)))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.clear_amount_3"))
                                        .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.limited.1",
                                                VN[tier]))
                                        .setExactLimit(1)
                                        .or(abilities(PartAbility.OUTPUT_ENERGY)).setExactLimit(1))
                        .where('H', blocks(casing.get())
                                .or(autoAbilities(definition.getRecipeTypes(), false, false, true, true, true, true))
                                .or(autoAbilities(true, true, false)))
                        .build())
                .recoveryItems(
                        () -> new ItemLike[] { GTItems.MATERIAL_ITEMS.get(TagPrefix.dustTiny, GTMaterials.Ash).get() })
                .workableCasingRenderer(casingTexture, overlayModel)
                .tooltips(
                        Component.translatable("gtceu.universal.tooltip.base_production_eut", V[tier] * finalAm),
                        Component.translatable("gtceu.multiblock.turbine.efficiency_tooltip", VNF[tier]))
                .tooltipBuilder(GTLMachines.GTL_MODIFY)
                .compassSections(GTCompassSections.TIER[HV])
                .compassNode("large_turbine")
                .register());
    }
}
