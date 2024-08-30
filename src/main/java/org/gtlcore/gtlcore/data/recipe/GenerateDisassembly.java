package org.gtlcore.gtlcore.data.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

import static org.gtlcore.gtlcore.common.data.GTLRecipeTypes.DISASSEMBLY;
import static org.gtlcore.gtlcore.common.data.GTLRecipes.DISASSEMBLY_RECORD;

public class GenerateDisassembly {

    private static final String[] inputItem = { "suprachronal_", "_circuit", "_processor", "_assembly",
            "_computer", "_mainframe", "circuit_resonatic_" };

    private static final String[] outputItem = { "_cable", "_frame", "_fence", "_electric_motor",
            "_electric_pump", "_conveyor_module", "_electric_piston", "_robot_arm", "_field_generator",
            "_emitter", "_sensor", "smd_", "_lamp", "_crate", "_drum", "_machine_casing",
            "ae2:blank_pattern", "gtceu:carbon_nanoswarm" };

    private static boolean isExcludeItems(String id, String[] excludeItems) {
        for (String pattern : excludeItems) {
            if (id.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    public static void generateDisassembly(GTRecipeBuilder r, Consumer<FinishedRecipe> p) {
        ItemStack outputs = ItemRecipeCapability.CAP
                .of(r.output.get(ItemRecipeCapability.CAP).get(0).getContent()).getItems()[0];
        String id = outputs.kjs$getId();
        boolean cal = r.recipeType == GTRecipeTypes.get("circuit_assembly_line");
        if (isExcludeItems(id, outputItem)) return;
        GTRecipeBuilder builder = DISASSEMBLY.recipeBuilder(new ResourceLocation(id))
                .inputItems(outputs)
                .duration(1)
                .EUt(4 * EURecipeCapability.CAP
                        .of(r.tickInput.get(EURecipeCapability.CAP).get(0).getContent()));
        if (DISASSEMBLY_RECORD.remove(id) && !cal) {
            DISASSEMBLY_RECORD.add(id);
        } else {
            DISASSEMBLY_RECORD.add(id);
            List<Content> itemList = r.input.getOrDefault(ItemRecipeCapability.CAP, null);
            List<Content> fluidList = r.input.getOrDefault(FluidRecipeCapability.CAP, null);
            if (itemList != null) {
                itemList.forEach(content -> {
                    ItemStack item = ItemRecipeCapability.CAP
                            .of(content.getContent()).getItems()[0];
                    if (content.chance == ChanceLogic.getMaxChancedValue() && !item.isEmpty() && !item.hasTag()) {
                        if (cal || !isExcludeItems(item.kjs$getId(), inputItem)) {
                            builder.outputItems(item);
                        }
                    }
                });
            }
            if (fluidList != null) {
                fluidList.forEach(content -> {
                    FluidIngredient fluid = FluidRecipeCapability.CAP
                            .of(content.getContent());
                    if (content.chance == ChanceLogic.getMaxChancedValue() && !fluid.isEmpty()) {
                        builder.outputFluids(fluid);
                    }
                });
            }
        }
        builder.save(p);
    }
}
