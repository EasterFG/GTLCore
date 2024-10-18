package org.gtlcore.gtlcore.data.recipe;

import org.gtlcore.gtlcore.common.data.GTLBlocks;
import org.gtlcore.gtlcore.common.data.GTLItems;
import org.gtlcore.gtlcore.common.data.GTLMachines;
import org.gtlcore.gtlcore.common.data.GTLMaterials;
import org.gtlcore.gtlcore.common.data.machines.*;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.UnificationEntry;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.tag.TagUtil;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.FinishedRecipe;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.data.CustomItems;
import com.hepdd.gtmthings.data.WirelessMachines;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.CONVEYOR_MODULE_EV;
import static com.gregtechceu.gtceu.common.data.GTItems.COVER_ITEM_DETECTOR_ADVANCED;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.ASSEMBLER_RECIPES;
import static com.gregtechceu.gtceu.data.recipe.CraftingComponent.*;
import static com.gregtechceu.gtceu.data.recipe.misc.MetaTileEntityLoader.registerMachineRecipe;

public class MachineRecipe {

    public static void init(Consumer<FinishedRecipe> provider) {
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_uev", GTBlocks.MACHINE_CASING_UEV.asStack(),
                "PPP",
                "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTLMaterials.Quantanium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_uiv", GTBlocks.MACHINE_CASING_UIV.asStack(),
                "PPP",
                "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTLMaterials.Adamantium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_uxv", GTBlocks.MACHINE_CASING_UXV.asStack(),
                "PPP",
                "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTLMaterials.Vibranium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_opv", GTBlocks.MACHINE_CASING_OpV.asStack(),
                "PPP",
                "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTLMaterials.Draconium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "casing_max", GTBlocks.MACHINE_CASING_MAX.asStack(),
                "PPP",
                "PwP", "PPP", 'P', new UnificationEntry(TagPrefix.plate, GTLMaterials.Chaos));
        ASSEMBLER_RECIPES.recipeBuilder("casing_uev").EUt(16).inputItems(plate, GTLMaterials.Quantanium, 8)
                .outputItems(GTBlocks.MACHINE_CASING_UEV.asStack()).circuitMeta(8).duration(50)
                .save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_uiv").EUt(16).inputItems(plate, GTLMaterials.Adamantium, 8)
                .outputItems(GTBlocks.MACHINE_CASING_UIV.asStack()).circuitMeta(8).duration(50)
                .save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_uxv").EUt(16).inputItems(plate, GTLMaterials.Vibranium, 8)
                .outputItems(GTBlocks.MACHINE_CASING_UXV.asStack()).circuitMeta(8).duration(50)
                .save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_opv").EUt(16).inputItems(plate, GTLMaterials.Draconium, 8)
                .outputItems(GTBlocks.MACHINE_CASING_OpV.asStack()).circuitMeta(8).duration(50)
                .save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("casing_max").EUt(16).inputItems(plate, GTLMaterials.Chaos, 8)
                .outputItems(GTBlocks.MACHINE_CASING_MAX.asStack()).circuitMeta(8).duration(50)
                .save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("rotor_hatch_a")
                .inputItems(GTMachines.ITEM_IMPORT_BUS[EV], 1)
                .inputItems(COVER_ITEM_DETECTOR_ADVANCED)
                .inputItems(pipeLargeRestrictive, SterlingSilver, 4)
                .inputItems(CONVEYOR_MODULE_EV, 16)
                .inputFluids(Polyethylene.getFluid(L))
                .outputItems(GTLMachines.ROTOR_HATCH)
                .duration(400).EUt(480).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hull_uhv").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_UHV)
                .inputItems(cableGtSingle, Europium, 2)
                .inputFluids(GTLMaterials.Polyetheretherketone.getFluid(L * 2))
                .outputItems(GTMachines.HULL[9]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("hull_uev").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_UEV.asStack())
                .inputItems(cableGtSingle, GTLMaterials.Mithril, 2)
                .inputFluids(GTLMaterials.Polyetheretherketone.getFluid(L * 2))
                .outputItems(GTMachines.HULL[10]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_uiv").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_UIV.asStack())
                .inputItems(cableGtSingle, GTMaterials.Neutronium, 2)
                .inputFluids(GTLMaterials.Zylon.getFluid(L * 2))
                .outputItems(GTMachines.HULL[11]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_uxv").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_UXV.asStack())
                .inputItems(cableGtSingle, GTLMaterials.Taranium, 2)
                .inputFluids(GTLMaterials.Zylon.getFluid(L * 2))
                .outputItems(GTMachines.HULL[12]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_opv").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_OpV.asStack())
                .inputItems(cableGtSingle, GTLMaterials.Crystalmatrix, 2)
                .inputFluids(GTLMaterials.FullerenePolymerMatrixPulp.getFluid(L * 2))
                .outputItems(GTMachines.HULL[13]).save(provider);
        ASSEMBLER_RECIPES.recipeBuilder("hull_max").duration(50).EUt(16)
                .inputItems(GTBlocks.MACHINE_CASING_MAX.asStack())
                .inputItems(cableGtSingle, GTLMaterials.CosmicNeutronium, 2)
                .inputFluids(GTLMaterials.Radox.getFluid(L * 2))
                .outputItems(GTMachines.HULL[14]).save(provider);

        var multiHatchMaterials = new Material[] {
                GTMaterials.Neutronium, GTLMaterials.Enderium, GTLMaterials.Enderium,
                GTLMaterials.HeavyQuarkDegenerateMatter,
                GTLMaterials.HeavyQuarkDegenerateMatter,
        };
        for (int i = 0; i < multiHatchMaterials.length; i++) {
            var tier = GTMachines.MULTI_HATCH_TIERS[i + 6];
            var tierName = VN[tier].toLowerCase();

            var material = multiHatchMaterials[i];

            var importHatch = GTMachines.FLUID_IMPORT_HATCH[tier];
            var exportHatch = GTMachines.FLUID_EXPORT_HATCH[tier];

            var importHatch4x = GTMachines.FLUID_IMPORT_HATCH_4X[tier];
            var exportHatch4x = GTMachines.FLUID_EXPORT_HATCH_4X[tier];
            var importHatch9x = GTMachines.FLUID_IMPORT_HATCH_9X[tier];
            var exportHatch9x = GTMachines.FLUID_EXPORT_HATCH_9X[tier];

            VanillaRecipeHelper.addShapedRecipe(
                    provider, true, "fluid_import_hatch_4x_" + tierName,
                    importHatch4x.asStack(), "P", "M",
                    'M', importHatch.asStack(),
                    'P', new UnificationEntry(TagPrefix.pipeQuadrupleFluid, material));
            VanillaRecipeHelper.addShapedRecipe(
                    provider, true, "fluid_export_hatch_4x_" + tierName,
                    exportHatch4x.asStack(), "M", "P",
                    'M', exportHatch.asStack(),
                    'P', new UnificationEntry(TagPrefix.pipeQuadrupleFluid, material));
            VanillaRecipeHelper.addShapedRecipe(
                    provider, true, "fluid_import_hatch_9x_" + tierName,
                    importHatch9x.asStack(), "P", "M",
                    'M', importHatch.asStack(),
                    'P', new UnificationEntry(TagPrefix.pipeNonupleFluid, material));
            VanillaRecipeHelper.addShapedRecipe(
                    provider, true, "fluid_export_hatch_9x_" + tierName,
                    exportHatch9x.asStack(), "M", "P",
                    'M', exportHatch.asStack(),
                    'P', new UnificationEntry(TagPrefix.pipeNonupleFluid, material));
        }
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_uhv", GTMachines.ROTOR_HOLDER[UHV].asStack(),
                "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.UHV].asStack(), 'G',
                new UnificationEntry(TagPrefix.gear, GTLMaterials.Orichalcum), 'S',
                new UnificationEntry(TagPrefix.gearSmall, GTMaterials.Neutronium));
        VanillaRecipeHelper.addShapedRecipe(provider, true, "rotor_holder_uev", GTMachines.ROTOR_HOLDER[UEV].asStack(),
                "SGS", "GHG", "SGS", 'H', GTMachines.HULL[GTValues.UEV].asStack(), 'G',
                new UnificationEntry(TagPrefix.gear, GTLMaterials.AstralTitanium), 'S',
                new UnificationEntry(TagPrefix.gearSmall, GTLMaterials.Quantanium));
        registerMachineRecipe(provider, ArrayUtils.subarray(GTMachines.TRANSFORMER, GTValues.UHV, GTValues.MAX),
                "WCC",
                "TH ", "WCC", 'W', POWER_COMPONENT, 'C', CABLE, 'T', CABLE_TIER_UP, 'H', HULL);
        registerMachineRecipe(provider,
                ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_2A, GTValues.UHV, GTValues.MAX),
                "WCC", "TH ", "WCC",
                'W', POWER_COMPONENT, 'C', CABLE_DOUBLE, 'T', CABLE_TIER_UP_DOUBLE, 'H', HULL);
        registerMachineRecipe(provider,
                ArrayUtils.subarray(GTMachines.HI_AMP_TRANSFORMER_4A, GTValues.UHV, GTValues.MAX),
                "WCC", "TH ", "WCC",
                'W', POWER_COMPONENT, 'C', CABLE_QUAD, 'T', CABLE_TIER_UP_QUAD, 'H', HULL);
        registerMachineRecipe(provider, GTLMachines.DEHYDRATOR, "WCW", "AMA", "PRP", 'M', HULL, 'P', PLATE, 'C',
                CIRCUIT, 'W', WIRE_QUAD, 'R', ROBOT_ARM, 'A', CABLE_QUAD);
        registerMachineRecipe(provider, GTLMachines.LIGHTNING_PROCESSOR, "WEW", "AMA", "WSW", 'M', HULL, 'E',
                EMITTER, 'W', WIRE_HEX, 'S', SENSOR, 'A', CABLE_TIER_UP);

        ASSEMBLER_RECIPES.recipeBuilder("zpm_fluid_drilling_rig")
                .inputItems(GTMachines.HULL[UV])
                .inputItems(frameGt, Ruridit, 4)
                .inputItems(CustomTags.UV_CIRCUITS, 4)
                .inputItems(GTItems.ELECTRIC_MOTOR_UV, 4)
                .inputItems(GTItems.ELECTRIC_PUMP_UV, 4)
                .inputItems(gear, Neutronium, 4)
                .circuitMeta(2)
                .outputItems(AdvancedMultiBlockMachine.FLUID_DRILLING_RIG[ZPM])
                .duration(400).EUt(VA[UV]).save(provider);

        ASSEMBLER_RECIPES.recipeBuilder("wood_distillation")
                .inputItems(MultiBlockMachineA.LARGE_PYROLYSE_OVEN, 2)
                .inputItems(GCyMMachines.LARGE_DISTILLERY, 4)
                .inputItems(CustomTags.LuV_CIRCUITS, 16)
                .inputItems(GTItems.EMITTER_LuV, 4)
                .inputItems(pipeHugeFluid, StainlessSteel, 8)
                .inputItems(GTItems.ELECTRIC_PUMP_IV, 8)
                .inputItems(WatertightSteel, 16)
                .inputItems(plateDouble, StainlessSteel, 32)
                .inputFluids(SolderingAlloy.getFluid(1296))
                .outputItems(MultiBlockMachineB.WOOD_DISTILLATION)
                .duration(400).EUt(VA[LuV])
                .save(provider);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "generator_array",
                GeneratorMachine.GENERATOR_ARRAY.asStack(),
                "ABA", "BCB", "ABA", 'A', new UnificationEntry(plate, Steel),
                'B', CustomTags.LV_CIRCUITS, 'C', GTItems.EMITTER_LV.asStack());

        registerMachineRecipe(provider, GTMachines.FLUID_IMPORT_HATCH, " G", " M", 'M', HULL, 'G', GLASS);
        registerMachineRecipe(provider, GTMachines.FLUID_EXPORT_HATCH, " M", " G", 'M', HULL, 'G', GLASS);

        registerMachineRecipe(provider, GTMachines.ITEM_IMPORT_BUS, " C", " M", 'M', HULL, 'C',
                TagUtil.createItemTag("chests/wooden"));
        registerMachineRecipe(provider, GTMachines.ITEM_EXPORT_BUS, " M", " C", 'M', HULL, 'C',
                TagUtil.createItemTag("chests/wooden"));

        registerMachineRecipe(provider, GTMachines.DUAL_IMPORT_HATCH, "PG", "CM", 'P', PIPE_NONUPLE, 'M', HULL,
                'G', GLASS, 'C', CraftingComponentAddition.BUFFER);
        registerMachineRecipe(provider, GTMachines.DUAL_EXPORT_HATCH, "MG", "CP", 'P', PIPE_NONUPLE, 'M', HULL,
                'G', GLASS, 'C', CraftingComponentAddition.BUFFER);

        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_uev",
                GTLBlocks.HERMETIC_CASING_UEV.asStack(), "PPP", "PFP", "PPP", 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Quantanium), 'F',
                new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_uiv",
                GTLBlocks.HERMETIC_CASING_UIV.asStack(), "PPP", "PFP", "PPP", 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Adamantium), 'F',
                new UnificationEntry(TagPrefix.pipeLargeFluid, GTMaterials.Neutronium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_uxv",
                GTLBlocks.HERMETIC_CASING_UXV.asStack(), "PPP", "PFP", "PPP", 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Vibranium), 'F',
                new UnificationEntry(TagPrefix.pipeLargeFluid, GTLMaterials.Enderium));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "hermetic_casing_opv",
                GTLBlocks.HERMETIC_CASING_OpV.asStack(), "PPP", "PFP", "PPP", 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Draconium), 'F',
                new UnificationEntry(TagPrefix.pipeLargeFluid,
                        GTLMaterials.HeavyQuarkDegenerateMatter));

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uev",
                GTMachines.QUANTUM_TANK[UEV].asStack(),
                "CGC", "PHP", "CUC", 'C', CustomTags.UEV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Quantanium), 'U',
                GTItems.ELECTRIC_PUMP_UHV.asStack(),
                'G', GTItems.FIELD_GENERATOR_UV.asStack(), 'H',
                GTLBlocks.HERMETIC_CASING_UEV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uiv",
                GTMachines.QUANTUM_TANK[UIV].asStack(),
                "CGC", "PHP", "CUC", 'C', CustomTags.UIV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Adamantium), 'U',
                GTItems.ELECTRIC_PUMP_UEV.asStack(),
                'G', GTItems.FIELD_GENERATOR_UHV.asStack(), 'H',
                GTLBlocks.HERMETIC_CASING_UIV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_uxv",
                GTMachines.QUANTUM_TANK[UXV].asStack(),
                "CGC", "PHP", "CUC", 'C', CustomTags.UXV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Vibranium), 'U',
                GTItems.ELECTRIC_PUMP_UIV.asStack(),
                'G', GTItems.FIELD_GENERATOR_UEV.asStack(), 'H',
                GTLBlocks.HERMETIC_CASING_UXV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_tank_opv",
                GTMachines.QUANTUM_TANK[OpV].asStack(),
                "CGC", "PHP", "CUC", 'C', CustomTags.OpV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Draconium), 'U',
                GTItems.ELECTRIC_PUMP_UXV.asStack(),
                'G', GTItems.FIELD_GENERATOR_UIV.asStack(), 'H',
                GTLBlocks.HERMETIC_CASING_OpV.asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uev",
                GTMachines.QUANTUM_CHEST[UEV].asStack(), "CPC", "PHP", "CFC", 'C',
                CustomTags.UEV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Quantanium), 'F',
                GTItems.FIELD_GENERATOR_UV.asStack(), 'H', GTMachines.HULL[10].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uiv",
                GTMachines.QUANTUM_CHEST[UIV].asStack(), "CPC", "PHP", "CFC", 'C',
                CustomTags.UIV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Adamantium), 'F',
                GTItems.FIELD_GENERATOR_UHV.asStack(), 'H', GTMachines.HULL[11].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_uxv",
                GTMachines.QUANTUM_CHEST[UXV].asStack(), "CPC", "PHP", "CFC", 'C',
                CustomTags.UXV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Vibranium), 'F',
                GTItems.FIELD_GENERATOR_UEV.asStack(), 'H', GTMachines.HULL[12].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "quantum_chest_opv",
                GTMachines.QUANTUM_CHEST[OpV].asStack(), "CPC", "PHP", "CFC", 'C',
                CustomTags.OpV_CIRCUITS, 'P',
                new UnificationEntry(TagPrefix.plate, GTLMaterials.Draconium), 'F',
                GTItems.FIELD_GENERATOR_UIV.asStack(), 'H', GTMachines.HULL[13].asStack());

        VanillaRecipeHelper.addShapedRecipe(provider, true, "large_block_conversion_room",
                AdvancedMultiBlockMachine.LARGE_BLOCK_CONVERSION_ROOM.asStack(), "SES", "EHE", "SES",
                'S', GTItems.SENSOR_ZPM.asStack(), 'E', GTItems.EMITTER_ZPM.asStack(), 'H',
                AdvancedMultiBlockMachine.BLOCK_CONVERSION_ROOM.asStack());

        List<ItemEntry<ComponentItem>> WIRELESS_ENERGY_RECEIVE_COVER = List.of(
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV,
                GTLItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX);

        List<ItemEntry<ComponentItem>> WIRELESS_ENERGY_RECEIVE_COVER_4A = List.of(
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_MV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_HV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_EV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_IV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_LUV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_ZPM_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UHV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UEV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UIV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_UXV_4A,
                CustomItems.WIRELESS_ENERGY_RECEIVE_COVER_OPV_4A,
                GTLItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX_4A);

        for (int tier : GTValues.tiersBetween(GTValues.LV, GTValues.HV)) {
            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[tier].toLowerCase() + "_4a"))
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH_4A[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER.get(tier - 1).asStack(2))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);

            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[tier].toLowerCase() + "_16a"))
                    .inputItems(GTMachines.ENERGY_INPUT_HATCH_16A[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1).asStack(2))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);

            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[tier].toLowerCase() + "_4a"))
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_4A[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER.get(tier - 1).asStack(2))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);

            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[tier].toLowerCase() + "_16a"))
                    .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_16A[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1).asStack(2))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
        }

        for (int tier : GTValues.tiersBetween(GTValues.EV, GTValues.MAX)) {
            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[tier].toLowerCase() + "_64a"))
                    .inputItems(GTMachines.SUBSTATION_ENERGY_INPUT_HATCH[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1).asStack(4))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(GTLMachines.WIRELESS_ENERGY_INPUT_HATCH_64A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);

            ASSEMBLER_RECIPES
                    .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[tier].toLowerCase() + "_64a"))
                    .inputItems(GTMachines.SUBSTATION_ENERGY_OUTPUT_HATCH[tier].asStack())
                    .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(tier - 1).asStack(4))
                    .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                    .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                    .outputItems(GTLMachines.WIRELESS_ENERGY_OUTPUT_HATCH_64A[tier].asStack())
                    .duration(200)
                    .EUt(GTValues.VA[tier])
                    .save(provider);
        }

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings
                        .id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase()))
                .inputItems(GTMachines.ENERGY_INPUT_HATCH[MAX].asStack())
                .inputItems(GTLItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings
                        .id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase()))
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH[MAX].asStack())
                .inputItems(GTLItems.WIRELESS_ENERGY_RECEIVE_COVER_MAX.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack())
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase() + "_4a"))
                .inputItems(GTMachines.ENERGY_INPUT_HATCH_4A[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER.get(MAX - 1).asStack(2))
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase() + "_16a"))
                .inputItems(GTMachines.ENERGY_INPUT_HATCH_16A[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(2))
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_16A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase() + "_4a"))
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_4A[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER.get(MAX - 1).asStack(2))
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase() + "_16a"))
                .inputItems(GTMachines.ENERGY_OUTPUT_HATCH_16A[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(2))
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_16A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase() + "_256a"))
                .inputItems(GTMachines.LASER_INPUT_HATCH_256[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(4))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_256A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase() + "_1024a"))
                .inputItems(GTMachines.LASER_INPUT_HATCH_1024[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(8))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_1024A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_input_hatch_" + GTValues.VN[MAX].toLowerCase() + "_4096a"))
                .inputItems(GTMachines.LASER_INPUT_HATCH_4096[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(16))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_INPUT_HATCH_4096A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase() + "_256a"))
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_256[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(4))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_256A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase() + "_1024a"))
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_1024[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(8))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_1024A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);

        ASSEMBLER_RECIPES
                .recipeBuilder(GTMThings.id("wireless_energy_output_hatch_" + GTValues.VN[MAX].toLowerCase() + "_4096a"))
                .inputItems(GTMachines.LASER_OUTPUT_HATCH_4096[MAX].asStack())
                .inputItems(WIRELESS_ENERGY_RECEIVE_COVER_4A.get(MAX - 1).asStack(16))
                .inputItems(GTMachines.ACTIVE_TRANSFORMER.asStack())
                .inputItems(GTBlocks.SUPERCONDUCTING_COIL.asStack())
                .inputItems(GTItems.COVER_ENERGY_DETECTOR_ADVANCED.asStack(1))
                .inputFluids(GTMaterials.SolderingAlloy.getFluid(144))
                .outputItems(WirelessMachines.WIRELESS_ENERGY_OUTPUT_HATCH_4096A[MAX].asStack())
                .duration(200)
                .EUt(GTValues.VA[MAX])
                .save(provider);
    }
}
