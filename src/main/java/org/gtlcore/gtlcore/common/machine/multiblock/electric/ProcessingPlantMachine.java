package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.api.machine.multiblock.StorageMachine;
import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers;
import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.OverclockingLogic;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingPlantMachine extends StorageMachine {

    private static final Set<GTRecipeType> RECIPETYPES = Set.of(GTRecipeTypes.BENDER_RECIPES,
            GTRecipeTypes.COMPRESSOR_RECIPES, GTRecipeTypes.FORGE_HAMMER_RECIPES, GTRecipeTypes.CUTTER_RECIPES, GTRecipeTypes.LASER_ENGRAVER_RECIPES,
            GTRecipeTypes.EXTRUDER_RECIPES, GTRecipeTypes.LATHE_RECIPES, GTRecipeTypes.WIREMILL_RECIPES, GTRecipeTypes.FORMING_PRESS_RECIPES,
            GTRecipeTypes.POLARIZER_RECIPES, GTLRecipeTypes.CLUSTER_RECIPES, GTLRecipeTypes.ROLLING_RECIPES, GTRecipeTypes.ASSEMBLER_RECIPES,
            GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES, GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.SIFTER_RECIPES, GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.EXTRACTOR_RECIPES, GTLRecipeTypes.DEHYDRATOR_RECIPES,
            GTRecipeTypes.CHEMICAL_RECIPES, GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES);

    @Nullable
    private GTRecipeType[] recipeTypeCache = new GTRecipeType[] { GTRecipeTypes.DUMMY_RECIPES };

    private int machineTier = 0;

    public ProcessingPlantMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, 1);
    }

    @Override
    protected boolean filter(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            MachineDefinition definition = metaMachineItem.getDefinition();
            if (definition instanceof MultiblockMachineDefinition) {
                return false;
            }
            GTRecipeType recipeType = definition.getRecipeTypes()[0];
            return RECIPETYPES.contains(recipeType);
        }
        return false;
    }

    @Nullable
    public static GTRecipe processingPlantOverclock(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                                    @NotNull OCResult result) {
        if (machine instanceof WorkableElectricMultiblockMachine workableElectricMultiblockMachine) {
            GTRecipe recipe1 = GTLRecipeModifiers.reduction(machine, recipe, 0.9, 0.6);
            if (recipe1 != null) {
                recipe1 = GTRecipeModifiers.accurateParallel(machine, recipe1, 4 * workableElectricMultiblockMachine.getTier() - 1, false).getFirst();
                if (recipe1 != null) return RecipeHelper.applyOverclock(OverclockingLogic.NON_PERFECT_OVERCLOCK_SUBTICK, recipe1,
                        workableElectricMultiblockMachine.getOverclockVoltage(), params, result);
            }
        }
        return null;
    }

    @Nullable
    public MachineDefinition getMachineDefinition() {
        if (machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
            return metaMachineItem.getDefinition();
        }
        return null;
    }

    @Override
    public GTRecipeType[] getRecipeTypes() {
        return recipeTypeCache;
    }

    @NotNull
    @Override
    public GTRecipeType getRecipeType() {
        return getRecipeTypes()[getActiveRecipeType()];
    }

    @Override
    public long getMaxVoltage() {
        return GTValues.V[machineTier];
    }

    @Override
    public int getTier() {
        return machineTier;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            machineStorage.addChangedListener(this::onMachineChanged);
        }
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        onMachineChanged();
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        textList.add(Component.translatable("gtceu.multiblock.parallel", Component.literal(FormattingUtil.formatNumbers(getTier() == 0 ? 0 : 4 * (getTier() - 1))).withStyle(ChatFormatting.DARK_PURPLE)).withStyle(ChatFormatting.GRAY));
    }

    protected void onMachineChanged() {
        recipeTypeCache = new GTRecipeType[] { GTRecipeTypes.DUMMY_RECIPES };
        machineTier = 0;
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
            MachineDefinition definition = getMachineDefinition();
            if (definition != null) {
                machineTier = definition.getTier();
                recipeTypeCache = definition.getRecipeTypes();
            }
        }
        tier = machineTier;
    }
}