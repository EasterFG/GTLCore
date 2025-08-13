package org.gtlcore.gtlcore.data.recipe.processing;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;

import net.minecraft.data.recipes.FinishedRecipe;

import java.util.function.Consumer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.lens;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;
import static com.gregtechceu.gtceu.common.data.GTRecipeTypes.*;
import static org.gtlcore.gtlcore.api.data.tag.GTLTagPrefix.nanoswarm;
import static org.gtlcore.gtlcore.common.data.GTLMaterials.*;
import static org.gtlcore.gtlcore.common.data.GTLRecipeTypes.*;

public class Lanthanidetreatment {

    public static void init(Consumer<FinishedRecipe> provider) {
        DIGESTION_TREATMENT.recipeBuilder("monazite_rare_earth_turbid_liquid1")
                .inputItems(dust, Monazite, 2)
                .inputFluids(NitricAcid.getFluid(700))
                .outputItems(dust, SiliconDioxide, 1)
                .outputFluids(MonaziteRareEarthTurbidLiquid.getFluid(400))
                .duration(200)
                .EUt(1920)
                .blastFurnaceTemp(1440)
                .save(provider);

        DISSOLUTION_TREATMENT.recipeBuilder("diluted_monazite_slurry2")
                .inputItems(dust, Saltpeter, 9)
                .inputFluids(MonaziteRareEarthTurbidLiquid.getFluid(9000))
                .inputFluids(Water.getFluid(90000))
                .outputItems(dust, ThoritePowder, 9)
                .outputItems(dust, Monazite, 2)
                .outputFluids(DilutedMonaziteSlurry.getFluid(100000))
                .duration(200)
                .EUt(480)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("diluted_monazite_slurry3")
                .inputFluids(DilutedMonaziteSlurry.getFluid(1000))
                .chancedOutput(dust, MonaziteSulfatePowder, 1, 9000, 0)
                .chancedOutput(dust, SiliconDioxide, 1, 7500, 0)
                .chancedOutput(dust, Rutile, 1, 2000, 0)
                .chancedOutput(dust, RedZirconPowder, 1, 500, 0)
                .chancedOutput(dust, Ilmenite, 1, 2000, 0)
                .duration(400)
                .EUt(120)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("red_zircon_powder4")
                .inputItems(dust, RedZirconPowder, 4)
                .outputItems(dust, Zircon, 3)
                .outputItems(dust, GraniteRed, 2)
                .duration(320)
                .EUt(90)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("monazite_sulfate_powder5")
                .inputItems(dust, MonaziteSulfatePowder, 1)
                .inputFluids(Water.getFluid(6000))
                .outputFluids(DilutedMonaziteSulfateSolution.getFluid(7000))
                .duration(240)
                .EUt(120)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("diluted_monazite_sulfate_solution6")
                .inputFluids(DilutedMonaziteSulfateSolution.getFluid(9000))
                .inputFluids(AmmoniumNitrateSolution.getFluid(1800))
                .outputItems(dust, AcidicMonazitePowder, 3)
                .duration(160)
                .EUt(480)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("acidic_monazite_powder7")
                .inputItems(dust, AcidicMonazitePowder, 1)
                .chancedOutput(dust, MonaziteRareEarthFilterResiduePowder, 1, 9000, 0)
                .chancedOutput(dust, ThoriumPhosphateFilterCakePowder, 1, 7000, 0)
                .duration(300)
                .EUt(256)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("thorium_phosphate_filter_cake_powder8")
                .inputItems(dust, ThoriumPhosphateFilterCakePowder, 1)
                .outputItems(dust, ThoriumPhosphateRefinedPowder, 1)
                .duration(100)
                .EUt(120)
                .blastFurnaceTemp(1200)
                .save(provider);

        THERMAL_CENTRIFUGE_RECIPES.recipeBuilder("thorium_phosphate_refined_powder9")
                .inputItems(dust, ThoriumPhosphateRefinedPowder, 1)
                .outputItems(dust, Monazite, 1)
                .outputItems(dust, Phosphate, 1)
                .duration(200)
                .EUt(240)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("monazite_rare_earth_filter_residue_powder10")
                .inputItems(dust, MonaziteRareEarthFilterResiduePowder, 1)
                .inputFluids(AmmoniumNitrateSolution.getFluid(320))
                .outputItems(dust, NeutralizedMonaziteRareEarthFilterResiduePowder, 1)
                .duration(120)
                .EUt(240)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("neutralized_monazite_rare_earth_filter_residue_powder11")
                .inputItems(dust, NeutralizedMonaziteRareEarthFilterResiduePowder, 1)
                .chancedOutput(dust, ConcentratedMonaziteRareEarthHydroxidePowder, 1, 9000, 0)
                .chancedOutput(dust, UraniumFilterResiduePowder, 1, 5000, 0)
                .chancedOutput(dust, UraniumFilterResiduePowder, 1, 4000, 0)
                .duration(160)
                .EUt(480)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("uranium_filter_residue_powder12")
                .inputItems(dust, UraniumFilterResiduePowder, 1)
                .inputFluids(HydrofluoricAcid.getFluid(100))
                .outputItems(dust, NeutralizedUraniumFilterResiduePowder, 1)
                .duration(180)
                .EUt(120)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("neutralized_uranium_filter_residue_powder13")
                .inputItems(dust, NeutralizedUraniumFilterResiduePowder, 1)
                .chancedOutput(dust, Uranium235, 1, 4500, 0)
                .chancedOutput(dust, Uranium235, 1, 4000, 0)
                .chancedOutput(dust, Uranium235, 1, 3000, 0)
                .chancedOutput(dust, Uranium235, 1, 3000, 0)
                .chancedOutput(dust, Uranium235, 1, 2000, 0)
                .duration(240)
                .EUt(30)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("concentrated_monazite_rare_earth_hydroxide_powder14")
                .inputItems(dust, ConcentratedMonaziteRareEarthHydroxidePowder, 1)
                .outputItems(dust, DriedConcentratedNitricMonaziteRareEarthPowder, 1)
                .duration(300)
                .EUt(120)
                .blastFurnaceTemp(1200)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("dried_concentrated_nitric_monazite_rare_earth_powder15")
                .inputItems(dust, DriedConcentratedNitricMonaziteRareEarthPowder, 1)
                .inputFluids(NitricAcid.getFluid(500))
                .outputFluids(ConcentratedNitrideMonaziteRareEarthSolution.getFluid(500))
                .duration(240)
                .EUt(480)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("concentrated_nitride_monazite_rare_earth_solution16")
                .inputItems(dust, CeriumRichMixturePowder, 3)
                .inputFluids(ConcentratedNitrideMonaziteRareEarthSolution.getFluid(1000))
                .outputFluids(NitricLeachateFromMonazite.getFluid(2000))
                .duration(240)
                .EUt(120)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("concentrated_nitride_monazite_rare_earth_solution17")
                .inputFluids(Water.getFluid(1000))
                .inputFluids(ConcentratedNitrideMonaziteRareEarthSolution.getFluid(1000))
                .outputFluids(NitricLeachateFromMonazite.getFluid(1000))
                .duration(200)
                .EUt(120)
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("cerium_rich_mixture_powder18")
                .inputItems(dust, Bastnasite, 6)
                .outputItems(dust, CeriumRichMixturePowder, 1)
                .outputItems(dust, Carbon, 1)
                .outputFluids(Oxygen.getFluid(3000))
                .outputFluids(Fluorine.getFluid(1000))
                .duration(120)
                .EUt(90)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("cerium_chloride_powder19")
                .inputItems(dust, CeriumRichMixturePowder, 15)
                .inputFluids(HydrofluoricAcid.getFluid(750))
                .outputItems(dust, CeriumChloridePowder, 1)
                .outputItems(dust, Monazite, 1)
                .outputFluids(Water.getFluid(750))
                .duration(300)
                .EUt(480)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("cerium_oxalate_powder20")
                .inputItems(dust, CeriumChloridePowder, 15)
                .inputFluids(OxalicAcid.getFluid(3000))
                .outputItems(dust, CeriumOxalatePowder, 1)
                .outputFluids(HydrochloricAcid.getFluid(6000))
                .duration(300)
                .EUt(480)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("oxalic_acid21")
                .notConsumable(dust, VanadiumPentoxidePowder, 1)
                .inputFluids(Oxygen.getFluid(40000))
                .inputFluids(Ethanol.getFluid(9000))
                .outputFluids(OxalicAcid.getFluid(9000))
                .outputFluids(Water.getFluid(20000))
                .duration(400)
                .EUt(240)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("oxalic_acid22")
                .notConsumable(dust, VanadiumPentoxidePowder, 1)
                .inputFluids(Oxygen.getFluid(27000))
                .inputFluids(Methanol.getFluid(9000))
                .inputFluids(CarbonMonoxide.getFluid(9000))
                .outputFluids(OxalicAcid.getFluid(9000))
                .outputFluids(Water.getFluid(20000))
                .duration(400)
                .EUt(240)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("oxalic_acid23")
                .notConsumable(dust, VanadiumPentoxidePowder, 1)
                .inputItems(dust, Sugar, 24)
                .inputFluids(NitricAcid.getFluid(6000))
                .outputFluids(OxalicAcid.getFluid(3000))
                .outputFluids(NitricOxide.getFluid(6000))
                .duration(300)
                .EUt(120)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("vanadium_pentoxide_powder24")
                .inputItems(dust, Vanadium, 2)
                .inputFluids(Oxygen.getFluid(5000))
                .outputItems(dust, VanadiumPentoxidePowder, 7)
                .duration(200)
                .EUt(120)
                .blastFurnaceTemp(2500)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("cerium_oxalate_powder25")
                .inputItems(dust, CeriumOxalatePowder, 5)
                .inputItems(dust, Carbon, 3)
                .outputItems(dust, CeriumOxide, 5)
                .outputFluids(CarbonMonoxide.getFluid(9000))
                .duration(200)
                .EUt(480)
                .blastFurnaceTemp(800)
                .save(provider);

        DIGESTION_TREATMENT.recipeBuilder("concentrated_cerium_chloride_solution26")
                .inputItems(dust, CeriumRichMixturePowder, 1)
                .inputFluids(Chlorine.getFluid(10000))
                .outputItems(dust, SiliconDioxide, 1)
                .outputFluids(ConcentratedCeriumChlorideSolution.getFluid(1000))
                .duration(40)
                .EUt(122880)
                .blastFurnaceTemp(3820)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("concentrated_nitric_leachate_from_monazite27")
                .inputFluids(NitricLeachateFromMonazite.getFluid(1000))
                .chancedOutput(dust, CeriumOxide, 1, 1000, 100)
                .outputFluids(ConcentratedNitricLeachateFromMonazite.getFluid(1000))
                .duration(200)
                .EUt(240)
                .save(provider);

        VACUUM_RECIPES.recipeBuilder("cooling_concentrated_nitric_monazite_rare_earth_powder28")
                .inputFluids(ConcentratedNitricLeachateFromMonazite.getFluid(1000))
                .outputItems(dust, CoolingConcentratedNitricMonaziteRareEarthPowder, 1)
                .duration(100)
                .EUt(240)
                .save(provider);

        ELECTROMAGNETIC_SEPARATOR_RECIPES.recipeBuilder("monazite_rare_earth_precipitate_powder29")
                .inputItems(dust, CoolingConcentratedNitricMonaziteRareEarthPowder, 1)
                .chancedOutput(dust, MonaziteRareEarthPrecipitatePowder, 1, 9000, 0)
                .chancedOutput(dust, EuropiumOxide, 1, 500, 0)
                .duration(100)
                .EUt(240)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("heterogeneous_halide_monazite_rare_earth_mixture_powder30")
                .inputItems(dust, MonaziteRareEarthPrecipitatePowder, 1)
                .inputFluids(Chlorine.getFluid(1000))
                .outputItems(dust, HeterogeneousHalideMonaziteRareEarthMixturePowder, 1)
                .duration(250)
                .EUt(480)
                .blastFurnaceTemp(1200)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("saturated_monazite_rare_earth_powder30")
                .inputItems(dust, HeterogeneousHalideMonaziteRareEarthMixturePowder, 1)
                .inputItems(dust, SamariumRefinedPowder, 2)
                .inputFluids(Acetone.getFluid(1000))
                .outputItems(dust, SaturatedMonaziteRareEarthPowder, 3)
                .duration(200)
                .EUt(240)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("saturated_monazite_rare_earth_powder32")
                .inputItems(dust, HeterogeneousHalideMonaziteRareEarthMixturePowder, 1)
                .inputItems(dust, Salt, 1)
                .inputFluids(Acetone.getFluid(1000))
                .outputItems(dust, SaturatedMonaziteRareEarthPowder, 2)
                .duration(200)
                .EUt(240)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("samarium_precipitate_powder33")
                .inputItems(dust, SaturatedMonaziteRareEarthPowder, 4)
                .outputItems(dust, RareEarth, 5)
                .outputItems(dust, SamariumPrecipitatePowder, 3)
                .outputFluids(Chloromethane.getFluid(400))
                .duration(160)
                .EUt(1920)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("samarium_precipitate_powder34")
                .inputItems(dust, SamariumPrecipitatePowder, 3)
                .outputItems(dust, Samarium, 2)
                .chancedOutput(dust, Gadolinium, 1, 2000, 0)
                .duration(160)
                .EUt(1920)
                .save(provider);

        DIGESTION_TREATMENT.recipeBuilder("samarium_rrare_eearth_turbid_liquid35")
                .inputItems(dust, SamariumRefinedPowder, 16)
                .inputFluids(NitricAcid.getFluid(200))
                .outputItems(dust, ThoriumPhosphateRefinedPowder, 1)
                .outputFluids(SamariumRrareEearthTurbidLiquid.getFluid(800))
                .duration(200)
                .EUt(1920)
                .blastFurnaceTemp(2680)
                .save(provider);

        DISSOLUTION_TREATMENT.recipeBuilder("samarium_rrare_eearth_turbid_liquid36")
                .inputFluids(SamariumRrareEearthTurbidLiquid.getFluid(1000))
                .inputFluids(NitricAcid.getFluid(1000))
                .chancedOutput(dust, CeriumRichMixturePowder, 1, 6000, 0)
                .chancedOutput(dust, CeriumOxide, 1, 8000, 0)
                .outputFluids(SamariumRareEarthSlurry.getFluid(2000))
                .duration(80)
                .EUt(1920)
                .save(provider);

        DISSOLUTION_TREATMENT.recipeBuilder("samarium_rare_earth_slurry37")
                .inputFluids(SamariumRareEarthSlurry.getFluid(1000))
                .inputFluids(Water.getFluid(9000))
                .chancedOutput(dust, NeodymiumRareEarthConcentratePowder, 1, 9000, 0)
                .chancedOutput(dust, NeodymiumRareEarthConcentratePowder, 1, 6000, 0)
                .outputFluids(SamariumRareEarthDilutedSolution.getFluid(10000))
                .duration(120)
                .EUt(1920)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("lanthanum_chloride38")
                .inputItems(dust, NeodymiumRareEarthConcentratePowder, 1)
                .inputFluids(HydrochloricAcid.getFluid(2000))
                .outputItems(dust, LanthanumChloride, 1)
                .outputItems(dust, NeodymiumOxide, 1)
                .duration(450)
                .EUt(120)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("samarium_oxalate_with_impurities39")
                .inputFluids(SamariumRareEarthDilutedSolution.getFluid(2000))
                .inputFluids(OxalicAcid.getFluid(3000))
                .outputItems(dust, SamariumOxalateWithImpurities, 5)
                .chancedOutput(dust, PhosphorusFreeSamariumConcentratePowder, 3, 1000, 0)
                .outputFluids(SamariumRrareEearthTurbidLiquid.getFluid(50))
                .duration(280)
                .EUt(480)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("Samarium40")
                .inputItems(dust, PhosphorusFreeSamariumConcentratePowder, 1)
                .chancedOutput(dust, Samarium, 1, 9000, 0)
                .chancedOutput(dust, ThoritePowder, 2, 8000, 0)
                .duration(100)
                .EUt(1920)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("samarium_chloride_concentrate_solution41")
                .inputItems(dust, SamariumOxalateWithImpurities, 5)
                .inputFluids(HydrochloricAcid.getFluid(6000))
                .outputItems(dust, SamariumChlorideWithImpurities, 8)
                .outputFluids(CarbonMonoxide.getFluid(6000))
                .duration(100)
                .EUt(960)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("samarium_chloride_sodium_chloride_mixture_powder42")
                .inputItems(dust, Salt, 1)
                .inputItems(dust, SamariumChlorideWithImpurities, 2)
                .outputItems(dust, SamariumChlorideSodiumChlorideMixturePowder, 3)
                .duration(80)
                .EUt(30)
                .save(provider);

        ELECTROLYZER_RECIPES.recipeBuilder("Samarium43")
                .inputItems(dust, SamariumChlorideSodiumChlorideMixturePowder, 6)
                .outputItems(dust, Samarium, 1)
                .outputItems(dust, Sodium, 1)
                .outputFluids(Chlorine.getFluid(4000))
                .duration(20)
                .EUt(7680)
                .save(provider);

        DIGESTION_TREATMENT.recipeBuilder("monazite_rare_earth_turbid_liquid_output44")
                .inputItems(dust, SamariumRefinedPowder, 1)
                .inputFluids(Chlorine.getFluid(10000))
                .outputItems(dust, SiliconDioxide, 1)
                .outputFluids(SamariumChlorideConcentrateSolution.getFluid(1000))
                .duration(40)
                .EUt(122880)
                .blastFurnaceTemp(3820)
                .save(provider);

        DISTILLERY_RECIPES.recipeBuilder("lanthanum_chloride_with_impurities45")
                .inputItems(dust, Lanthanum, 9)
                .inputFluids(SamariumChlorideWithImpurities.getFluid(5184))
                .outputItems(dust, LanthanumChlorideWithImpurities, 36)
                .outputFluids(Samarium.getFluid(2304))
                .duration(100)
                .EUt(122880)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("lanthanum_chloride_with_impurities46")
                .inputItems(dust, LanthanumChlorideWithImpurities, 36)
                .outputItems(dust, LanthanumChloride, 36)
                .duration(100)
                .EUt(1920)
                .save(provider);

        /* 氟碳镧铈线 */
        DIGESTION_TREATMENT.recipeBuilder("fluoro_carbon_lanthanide_cerium_solution47")
                .inputItems(dust, Bastnasite, 2)
                .inputFluids(NitricAcid.getFluid(700))
                .outputItems(dust, SiliconDioxide, 1)
                .outputFluids(FluoroCarbonLanthanideCeriumSolution.getFluid(400))
                .duration(200)
                .EUt(1920)
                .blastFurnaceTemp(1440)
                .save(provider);

        CRACKING_RECIPES.recipeBuilder("steam_cracked_fluoro_carbon_lanthanide_slurry48")
                .inputFluids(FluoroCarbonLanthanideCeriumSolution.getFluid(1000))
                .inputFluids(Steam.getFluid(1000))
                .outputFluids(SteamCrackedFluoroCarbonLanthanideSlurry.getFluid(2000))
                .duration(300)
                .EUt(480)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("fluoro_carbon_lanthanide_cerium_solution49")
                .inputFluids(SteamCrackedFluoroCarbonLanthanideSlurry.getFluid(1000))
                .inputFluids(NitricAcid.getFluid(700))
                .outputFluids(ModulatedFluoroCarbonLanthanideSlurry.getFluid(1320))
                .duration(200)
                .EUt(120)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("sodium_fluorosilicate50")
                .inputItems(dust, Salt, 4)
                .inputFluids(FluorosilicicAcid.getFluid(1000))
                .outputFluids(HydrochloricAcid.getFluid(2000))
                .outputFluids(SodiumFluorosilicate.getFluid(1000))
                .duration(300)
                .EUt(480)
                .save(provider);

        DISSOLUTION_TREATMENT.recipeBuilder("diluted_fluoro_carbon_lanthanide_slurry51")
                .inputItems(dust, Saltpeter, 1)
                .inputFluids(ModulatedFluoroCarbonLanthanideSlurry.getFluid(1000))
                .inputFluids(Water.getFluid(10000))
                .outputItems(dust, Stone, 1)
                .outputFluids(DilutedFluoroCarbonLanthanideSlurry.getFluid(10000))
                .duration(160)
                .EUt(1920)
                .save(provider);

        SIFTER_RECIPES.recipeBuilder("filtered_fluoro_carbon_lanthanide_slurry52")
                .inputFluids(DilutedFluoroCarbonLanthanideSlurry.getFluid(1000))
                .chancedOutput(dust, SiliconDioxide, 1, 9000, 0)
                .chancedOutput(dust, Rutile, 1, 7500, 0)
                .chancedOutput(dust, RedZirconPowder, 1, 1000, 0)
                .chancedOutput(dust, Ilmenite, 1, 500, 0)
                .outputFluids(FilteredFluoroCarbonLanthanideSlurry.getFluid(400))
                .duration(200)
                .EUt(240)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("fluoro_carbon_lanthanide_cerium_oxide_powder53")
                .inputFluids(FilteredFluoroCarbonLanthanideSlurry.getFluid(1000))
                .outputItems(dust, FluoroCarbonLanthanideCeriumOxidePowder, 1)
                .duration(240)
                .EUt(480)
                .blastFurnaceTemp(1400)
                .save(provider);

        CHEMICAL_BATH_RECIPES.recipeBuilder("acid_leached_fluoro_carbon_lanthanide_cerium_oxide_powder54")
                .inputItems(dust, FluoroCarbonLanthanideCeriumOxidePowder, 1)
                .inputFluids(HydrochloricAcid.getFluid(500))
                .outputItems(dust, AcidLeachedFluoroCarbonLanthanideCeriumOxidePowder, 1)
                .duration(200)
                .EUt(30)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("calcined_rare_earth_oxide_powder55")
                .inputItems(dust, AcidLeachedFluoroCarbonLanthanideCeriumOxidePowder, 1)
                .inputFluids(Oxygen.getFluid(1000))
                .outputItems(dust, CalcinedRareEarthOxidePowder, 1)
                .outputFluids(Fluorine.getFluid(13))
                .duration(240)
                .EUt(120)
                .blastFurnaceTemp(1200)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("wet_rare_earth_oxide_powder56")
                .inputItems(dust, CalcinedRareEarthOxidePowder, 1)
                .inputFluids(Water.getFluid(200))
                .outputItems(dust, WetRareEarthOxidePowder, 1)
                .duration(100)
                .EUt(30)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("cerium_oxide_rare_earth_oxide_powder57")
                .inputItems(dust, WetRareEarthOxidePowder, 1)
                .inputFluids(Fluorine.getFluid(4000))
                .outputItems(dust, CeriumOxideRareEarthOxidePowder, 1)
                .outputFluids(HydrofluoricAcid.getFluid(4000))
                .duration(300)
                .EUt(480)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("fluoro_carbon_lanthanide_cerium_rare_earth_oxide_powder58")
                .inputItems(dust, CeriumOxideRareEarthOxidePowder, 1)
                .outputItems(dust, FluoroCarbonLanthanideCeriumRareEarthOxidePowder, 1)
                .outputItems(dust, CeriumOxide, 1)
                .duration(320)
                .EUt(480)
                .save(provider);

        MIXER_RECIPES.recipeBuilder("nitrided_fluoro_carbon_lanthanide_cerium_rare_earth_oxide_solution59")
                .inputItems(dust, FluoroCarbonLanthanideCeriumRareEarthOxidePowder, 1)
                .inputFluids(NitricAcid.getFluid(400))
                .outputFluids(NitridedFluoroCarbonLanthanideCeriumRareEarthOxideSolution.getFluid(1000))
                .duration(150)
                .EUt(480)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("fluoro_carbon_lanthanide_cerium_rare_earth_suspension60")
                .inputFluids(Acetone.getFluid(1000))
                .inputFluids(NitridedFluoroCarbonLanthanideCeriumRareEarthOxideSolution.getFluid(1000))
                .outputFluids(FluoroCarbonLanthanideCeriumRareEarthSuspension.getFluid(1000))
                .duration(350)
                .EUt(480)
                .save(provider);

        CENTRIFUGE_RECIPES.recipeBuilder("samarium_rare_earth_concentrate_powder61")
                .inputFluids(FluoroCarbonLanthanideCeriumRareEarthSuspension.getFluid(1000))
                .chancedOutput(dust, NeodymiumRareEarthConcentratePowder, 1, 8000, 0)
                .chancedOutput(dust, SamariumRareEarthConcentratePowder, 1, 5000, 0)
                .outputFluids(Acetone.getFluid(450))
                .duration(450)
                .EUt(120)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("fluorinated_samarium_concentrate_powder62")
                .inputItems(dust, SamariumRareEarthConcentratePowder, 1)
                .inputFluids(HydrofluoricAcid.getFluid(2000))
                .outputItems(dust, RareEarth, 4)
                .outputItems(dust, FluorinatedSamariumConcentratePowder, 1)
                .duration(300)
                .EUt(480)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("samarium_terbium_mixture_powder63")
                .inputItems(dust, FluorinatedSamariumConcentratePowder, 4)
                .inputItems(dust, Calcium, 4)
                .outputItems(dust, Holmium, 1)
                .outputItems(dust, SamariumTerbiumMixturePowder, 4)
                .outputItems(dust, Fluorite, 12)
                .duration(200)
                .EUt(1920)
                .blastFurnaceTemp(1200)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("nitrided_samarium_terbium_mixture_powder64")
                .inputItems(dust, SamariumTerbiumMixturePowder, 1)
                .inputItems(dust, AmmoniumChloride, 9)
                .outputItems(dust, NitridedSamariumTerbiumMixturePowder, 1)
                .duration(300)
                .EUt(480)
                .save(provider);

        CHEMICAL_RECIPES.recipeBuilder("terbium_nitrate_powder65")
                .inputItems(dust, NitridedSamariumTerbiumMixturePowder, 4)
                .inputItems(dust, Copper, 1)
                .outputItems(dust, TerbiumNitratePowder, 2)
                .outputItems(dust, SamariumPrecipitatePowder, 2)
                .duration(320)
                .EUt(1920)
                .save(provider);

        RARE_EARTH_CENTRIFUGAL_RECIPES.recipeBuilder("concentrated_cerium_chloride_solution66")
                .inputFluids(ConcentratedCeriumChlorideSolution.getFluid(1000))
                .outputItems(dust, CeriumOxide, 4)
                .outputFluids(ConcentratedRareEarthChlorideSolution.getFluid(1000))
                .duration(20)
                .EUt(491520)
                .save(provider);

        RARE_EARTH_CENTRIFUGAL_RECIPES.recipeBuilder("samarium_chloride_concentrate_solution67")
                .inputFluids(SamariumChlorideConcentrateSolution.getFluid(1000))
                .outputItems(dust, SamariumOxide, 4)
                .outputFluids(ConcentratedRareEarthChlorideSolution.getFluid(1000))
                .duration(20)
                .EUt(491520)
                .save(provider);

        RARE_EARTH_CENTRIFUGAL_RECIPES.recipeBuilder("samarium_chloride_concentrate_solution69")
                .inputFluids(EnrichedRareEarthChlorideSolution.getFluid(1000))
                .outputFluids(DilutedRareEarthChlorideSolution.getFluid(1000))
                .outputItems(dustSmall, LanthanumOxide, 1)
                .outputItems(dustSmall, PraseodymiumOxide, 1)
                .outputItems(dustSmall, NeodymiumOxide, 1)
                .outputItems(dustSmall, CeriumOxide, 1)
                .outputItems(dustSmall, EuropiumOxide, 1)
                .outputItems(dustSmall, GadoliniumOxide, 1)
                .outputItems(dustSmall, SamariumOxide, 1)
                .outputItems(dustSmall, TerbiumOxide, 1)
                .outputItems(dustSmall, DysprosiumOxide, 1)
                .outputItems(dustSmall, HolmiumOxide, 1)
                .outputItems(dustSmall, ErbiumOxide, 1)
                .outputItems(dustSmall, ThuliumOxide, 1)
                .outputItems(dustSmall, YtterbiumOxide, 1)
                .outputItems(dustSmall, LutetiumOxide, 1)
                .outputItems(dustSmall, ScandiumOxide, 1)
                .outputItems(dustSmall, YttriumOxide, 1)
                .outputItems(dustSmall, PromethiumOxide, 1)
                .duration(20)
                .EUt(491520)
                .save(provider);

        RARE_EARTH_CENTRIFUGAL_RECIPES.recipeBuilder("samarium_chloride_concentrate_solution70")
                .inputFluids(DilutedRareEarthChlorideSolution.getFluid(1000))
                .outputFluids(RedMud.getFluid(1000))
                .outputItems(dustSmall, LanthanumOxide, 1)
                .outputItems(dustSmall, PraseodymiumOxide, 1)
                .outputItems(dustSmall, NeodymiumOxide, 1)
                .outputItems(dustSmall, CeriumOxide, 1)
                .outputItems(dustSmall, EuropiumOxide, 1)
                .outputItems(dustSmall, GadoliniumOxide, 1)
                .outputItems(dustSmall, SamariumOxide, 1)
                .outputItems(dustSmall, TerbiumOxide, 1)
                .outputItems(dustSmall, DysprosiumOxide, 1)
                .outputItems(dustSmall, HolmiumOxide, 1)
                .outputItems(dustSmall, ErbiumOxide, 1)
                .outputItems(dustSmall, ThuliumOxide, 1)
                .outputItems(dustSmall, YtterbiumOxide, 1)
                .outputItems(dustSmall, LutetiumOxide, 1)
                .outputItems(dustSmall, ScandiumOxide, 1)
                .outputItems(dustSmall, YttriumOxide, 1)
                .outputItems(dustSmall, PromethiumOxide, 1)
                .duration(20)
                .EUt(491520)
                .save(provider);

        BLAST_RECIPES.recipeBuilder("samarium_chloride_concentrate_solution71")
                .inputItems(dust, PromethiumOxide, 10)
                .inputItems(dust, Carbon, 3)
                .outputItems(dust, Promethium, 4)
                .outputFluids(CarbonDioxide.getFluid(3000))
                .duration(200)
                .EUt(480)
                .blastFurnaceTemp(2500)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("make_p507_1")
                .notConsumable(plate, Copper, 1)
                .inputFluids(SeedOil.getFluid(3000))
                .inputFluids(Hydrogen.getFluid(8000))
                .outputFluids(EthylHexanol.getFluid(1000))
                .duration(400)
                .EUt(480)
                .save(provider);

        LARGE_CHEMICAL_RECIPES.recipeBuilder("make_p507_2")
                .inputItems(dust, Sodium, 2)
                .inputFluids(EthylHexanol.getFluid(2000))
                .inputFluids(PhosphoricAcid.getFluid(2000))
                .inputFluids(Ethanol.getFluid(2000))
                .outputFluids(P507.getFluid(1000))
                .duration(1200)
                .EUt(1920)
                .save(provider);

        Material[] rareEarthElements = {
                Lanthanum,
                Cerium,
                Praseodymium,
                Neodymium,
                Promethium,
                Samarium,
                Europium,
                Gadolinium,
                Terbium,
                Dysprosium,
                Holmium,
                Erbium,
                Thulium,
                Ytterbium,
                Lutetium,
                Scandium,
                Yttrium
        };

        Material[] extractionNanoResins = {
                LanthanumExtractionNanoResin,
                CeriumExtractionNanoResin,
                PraseodymiumExtractionNanoResin,
                NeodymiumExtractionNanoResin,
                PromethiumExtractionNanoResin,
                SamariumExtractionNanoResin,
                EuropiumExtractionNanoResin,
                GadoliniumExtractionNanoResin,
                TerbiumExtractionNanoResin,
                DysprosiumExtractionNanoResin,
                HolmiumExtractionNanoResin,
                ErbiumExtractionNanoResin,
                ThuliumExtractionNanoResin,
                YtterbiumExtractionNanoResin,
                LutetiumExtractionNanoResin,
                ScandiumExtractionNanoResin,
                YttriumExtractionNanoResin
        };

        Material[] extractedNanoResins = {
                LanthanumExtractedNanoResin,
                CeriumExtractedNanoResin,
                PraseodymiumExtractedNanoResin,
                NeodymiumExtractedNanoResin,
                PromethiumExtractedNanoResin,
                SamariumExtractedNanoResin,
                EuropiumExtractedNanoResin,
                GadoliniumExtractedNanoResin,
                TerbiumExtractedNanoResin,
                DysprosiumExtractedNanoResin,
                HolmiumExtractedNanoResin,
                ErbiumExtractedNanoResin,
                ThuliumExtractedNanoResin,
                YtterbiumExtractedNanoResin,
                LutetiumExtractedNanoResin,
                ScandiumExtractedNanoResin,
                YttriumExtractedNanoResin
        };

        for (int i = 0; i < rareEarthElements.length; i++) {
            Material rareEarthElement = rareEarthElements[i];
            Material nanoResin = extractionNanoResins[i];
            Material extractedNanoResin = extractedNanoResins[i];

            LASER_ENGRAVER_RECIPES.recipeBuilder("make_extraction_nano_resin_" + i)
                    .inputItems(nanoswarm, Carbon, 1)
                    .notConsumable(lens, NetherStar, 1)
                    .inputFluids(rareEarthElement.getFluid(4000))
                    .inputFluids(P507.getFluid(4000))
                    .outputFluids(nanoResin.getFluid(1000))
                    .duration(1200)
                    .EUt(491520)
                    .addData("special", true)
                    .save(provider);

            LARGE_CHEMICAL_RECIPES.recipeBuilder("extraction_nano_resin_a_" + i)
                    .inputFluids(nanoResin.getFluid(1000))
                    .inputFluids(ConcentratedRareEarthChlorideSolution.getFluid(1000))
                    .outputFluids(EnrichedRareEarthChlorideSolution.getFluid(1000))
                    .outputFluids(extractedNanoResin.getFluid(1000))
                    .duration(20)
                    .EUt(491520)
                    .save(provider);

            LARGE_CHEMICAL_RECIPES.recipeBuilder("extraction_nano_resin_b_" + i)
                    .inputFluids(nanoResin.getFluid(1000))
                    .inputFluids(EnrichedRareEarthChlorideSolution.getFluid(1000))
                    .outputFluids(DilutedRareEarthChlorideSolution.getFluid(1000))
                    .outputFluids(extractedNanoResin.getFluid(1000))
                    .duration(80)
                    .EUt(491520)
                    .save(provider);

            LARGE_CHEMICAL_RECIPES.recipeBuilder("extraction_nano_resin_c_" + i)
                    .inputFluids(nanoResin.getFluid(1000))
                    .inputFluids(DilutedRareEarthChlorideSolution.getFluid(1000))
                    .outputFluids(extractedNanoResin.getFluid(1000))
                    .duration(320)
                    .EUt(491520)
                    .save(provider);

            ELECTROLYZER_RECIPES.recipeBuilder("break_down_extracted_nano_resin_" + i)
                    .inputFluids(extractedNanoResin.getFluid(1000))
                    .outputItems(dust, rareEarthElement, 1)
                    .outputFluids(nanoResin.getFluid(1000))
                    .outputFluids(Chlorine.getFluid(3000))
                    .duration(100)
                    .EUt(122880)
                    .save(provider);
        }
    }
}
