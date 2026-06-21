package org.gtlcore.gtlcore.mixin.gtm.recipe;

import org.gtlcore.gtlcore.common.data.GTLMachines;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;
import com.gregtechceu.gtceu.data.recipe.misc.MachineRecipeLoader;

import net.minecraft.data.recipes.FinishedRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Locale;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.GTValues.*;
import static com.gregtechceu.gtceu.common.data.GTMachines.*;

@Mixin(MachineRecipeLoader.class)
public class MachineRecipeLoaderMixin {

    /**
     * @author .
     * @reason .
     */
    @Overwrite(remap = false)
    private static void registerHatchConversion(Consumer<FinishedRecipe> provider) {
        for (int i = 0; i < FLUID_IMPORT_HATCH.length; i++) {
            if (FLUID_IMPORT_HATCH[i] != null && FLUID_EXPORT_HATCH[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider,
                        "fluid_hatch_output_to_input_" + FLUID_IMPORT_HATCH[i].getTier(),
                        FLUID_IMPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_EXPORT_HATCH[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider,
                        "fluid_hatch_input_to_output_" + FLUID_EXPORT_HATCH[i].getTier(),
                        FLUID_EXPORT_HATCH[i].asStack(),
                        "d", "B", 'B', FLUID_IMPORT_HATCH[i].asStack());
            }
        }
        for (int i = 0; i < ITEM_IMPORT_BUS.length; i++) {
            if (ITEM_IMPORT_BUS[i] != null && ITEM_EXPORT_BUS[i] != null) {

                VanillaRecipeHelper.addShapedRecipe(provider,
                        "item_bus_output_to_input_" + ITEM_IMPORT_BUS[i].getTier(), ITEM_IMPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_EXPORT_BUS[i].asStack());
                VanillaRecipeHelper.addShapedRecipe(provider,
                        "item_bus_input_to_output_" + ITEM_EXPORT_BUS[i].getTier(), ITEM_EXPORT_BUS[i].asStack(),
                        "d", "B", 'B', ITEM_IMPORT_BUS[i].asStack());
            }
        }

        for (int tier : GTValues.tiersBetween(EV, MAX)) {
            var tierName = VN[tier].toLowerCase(Locale.ROOT);

            var importHatch4x = FLUID_IMPORT_HATCH_4X[tier];
            var exportHatch4x = FLUID_EXPORT_HATCH_4X[tier];
            var importHatch9x = FLUID_IMPORT_HATCH_9X[tier];
            var exportHatch9x = FLUID_EXPORT_HATCH_9X[tier];

            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_4x_output_to_input_" + tierName,
                    importHatch4x.asStack(), "d", "B",
                    'B', exportHatch4x.asStack());
            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_4x_input_to_output_" + tierName,
                    exportHatch4x.asStack(), "d", "B",
                    'B', importHatch4x.asStack());

            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_9x_output_to_input_" + tierName,
                    importHatch9x.asStack(), "d", "B",
                    'B', exportHatch9x.asStack());
            VanillaRecipeHelper.addShapedRecipe(
                    provider, "fluid_hatch_9x_input_to_output_" + tierName,
                    exportHatch9x.asStack(), "d", "B",
                    'B', importHatch9x.asStack());
        }

        for (int tier : GTValues.tiersBetween(LuV, MAX)) {
            var tierName = VN[tier].toLowerCase(Locale.ROOT);

            var inputBuffer = DUAL_IMPORT_HATCH[tier];
            var outputBuffer = DUAL_EXPORT_HATCH[tier];

            VanillaRecipeHelper.addShapedRecipe(
                    provider,
                    "dual_hatch_output_to_input_" + tierName,
                    inputBuffer.asStack(),
                    "d",
                    "B",
                    'B',
                    outputBuffer.asStack());
            VanillaRecipeHelper.addShapedRecipe(
                    provider,
                    "dual_hatch_input_to_output_" + tierName,
                    outputBuffer.asStack(),
                    "d",
                    "B",
                    'B',
                    inputBuffer.asStack());
        }

        // Steam
        VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_output_to_input", STEAM_EXPORT_BUS.asStack(),
                "d", "B", 'B', STEAM_IMPORT_BUS.asStack());
        VanillaRecipeHelper.addShapedRecipe(provider, "steam_bus_input_to_output", STEAM_IMPORT_BUS.asStack(),
                "d", "B", 'B', STEAM_EXPORT_BUS.asStack());

        if (GTCEu.isAE2Loaded()) {
            VanillaRecipeHelper.addShapedRecipe(provider, "me_fluid_hatch_output_to_input",
                    GTLMachines.GTAEMachines.FLUID_IMPORT_HATCH_ME.asStack(), "d", "B", 'B',
                    GTLMachines.GTAEMachines.FLUID_EXPORT_HATCH_ME.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_fluid_hatch_input_to_output",
                    GTLMachines.GTAEMachines.FLUID_EXPORT_HATCH_ME.asStack(), "d", "B", 'B',
                    GTLMachines.GTAEMachines.FLUID_IMPORT_HATCH_ME.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_item_bus_output_to_input",
                    GTLMachines.GTAEMachines.ITEM_IMPORT_BUS_ME.asStack(), "d", "B", 'B',
                    GTLMachines.GTAEMachines.ITEM_EXPORT_BUS_ME.asStack());
            VanillaRecipeHelper.addShapedRecipe(provider, "me_item_bus_input_to_output",
                    GTLMachines.GTAEMachines.ITEM_EXPORT_BUS_ME.asStack(), "d", "B", 'B',
                    GTLMachines.GTAEMachines.ITEM_IMPORT_BUS_ME.asStack());
        }
    }
}
