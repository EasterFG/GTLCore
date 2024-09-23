package org.gtlcore.gtlcore.common.machine.multiblock.generator;

import org.gtlcore.gtlcore.common.data.GTLRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.MetaMachineItem;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import com.hepdd.gtmthings.api.misc.WirelessEnergyManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorArrayMachine extends WorkableElectricMultiblockMachine implements IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GeneratorArrayMachine.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @DescSynced
    public final NotifiableItemStackHandler machineStorage;
    @Nullable
    private GTRecipeType[] recipeTypeCache;

    @Persisted
    private UUID userid;

    @Persisted
    private boolean isw;

    @Persisted
    private long eut = 0;

    public GeneratorArrayMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
        this.machineStorage = createMachineStorage(args);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableItemStackHandler createMachineStorage(Object... args) {
        var storage = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.NONE, slots -> new ItemStackTransfer(1) {

            @Override
            public int getSlotLimit(int slot) {
                return 16;
            }
        });
        storage.setFilter(this::isMachineStack);
        return storage;
    }

    protected boolean isMachineStack(ItemStack itemStack) {
        if (itemStack.getItem() instanceof MetaMachineItem metaMachineItem) {
            MachineDefinition definition = metaMachineItem.getDefinition();

            if (definition instanceof MultiblockMachineDefinition) {
                return false;
            }

            var recipeTypes = definition.getRecipeTypes();
            if (recipeTypes == null) {
                return false;
            }
            for (GTRecipeType type : recipeTypes) {
                if (type == GTRecipeTypes.STEAM_TURBINE_FUELS || type == GTRecipeTypes.GAS_TURBINE_FUELS || type == GTRecipeTypes.COMBUSTION_GENERATOR_FUELS || type == GTLRecipeTypes.SEMI_FLUID_GENERATOR_FUELS || type == GTLRecipeTypes.ROCKET_ENGINE_FUELS || type == GTLRecipeTypes.NAQUADAH_REACTOR) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public MachineDefinition getMachineDefinition() {
        if (machineStorage.storage.getStackInSlot(0).getItem() instanceof MetaMachineItem metaMachineItem) {
            return metaMachineItem.getDefinition();
        }
        return null;
    }

    @Override
    @NotNull
    public GTRecipeType[] getRecipeTypes() {
        if (recipeTypeCache == null) {
            var definition = getMachineDefinition();
            recipeTypeCache = definition == null ? null : definition.getRecipeTypes();
        }
        if (recipeTypeCache == null) {
            recipeTypeCache = new GTRecipeType[] { GTRecipeTypes.DUMMY_RECIPES };
        }
        return recipeTypeCache;
    }

    @NotNull
    @Override
    public GTRecipeType getRecipeType() {
        return getRecipeTypes()[getActiveRecipeType()];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) {
            machineStorage.addChangedListener(this::onMachineChanged);
        }
    }

    protected void onMachineChanged() {
        recipeTypeCache = null;
        if (isFormed) {
            if (getRecipeLogic().getLastRecipe() != null) {
                getRecipeLogic().markLastRecipeDirty();
            }
            getRecipeLogic().updateTickSubscription();
        }
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(machineStorage.storage);
    }

    //////////////////////////////////////
    // ******* Recipe Logic *******//
    //////////////////////////////////////

    @Override
    public int getTier() {
        MachineDefinition machineDefinition = getMachineDefinition();
        return machineDefinition == null ? 0 : machineDefinition.getTier();
    }

    @Override
    public int getOverclockTier() {
        MachineDefinition machineDefinition = getMachineDefinition();
        return machineDefinition == null ? getDefinition().getTier() : machineDefinition.getTier();
    }

    @Override
    public int getMinOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public int getMaxOverclockTier() {
        return getOverclockTier();
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (this.userid == null || !this.userid.equals(player.getUUID())) {
            this.userid = player.getUUID();
        }
        return true;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (this.isw) {
            if (eut > 0) {
                WirelessEnergyManager.addEUToGlobalEnergyMap(userid, eut, this);
            } else {
                return false;
            }
        }
        return value;
    }

    @Override
    public void afterWorking() {
        eut = 0;
        super.afterWorking();
    }

    @Override
    public boolean dampingWhenWaiting() {
        return false;
    }

    public static int getAmperage(int tier) {
        return Math.max(1, 2 * (5 - tier));
    }

    public static int getEfficiency(int tier) {
        return (105 - 5 * tier);
    }

    @Nullable
    public static GTRecipe recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params,
                                          @NotNull OCResult result) {
        if (machine instanceof GeneratorArrayMachine generatorArrayMachine) {
            int a = generatorArrayMachine.machineStorage.storage.getStackInSlot(0).getCount();
            if (a > 0) {
                long EUt = RecipeHelper.getOutputEUt(recipe);
                if (EUt > 0) {
                    int maxParallel = (int) (GTValues.V[generatorArrayMachine.getOverclockTier()] * a * 2 * getAmperage(generatorArrayMachine.getTier()) / EUt);
                    int multipliers = 0;
                    for (RecipeCapability<?> cap : recipe.inputs.keySet()) {
                        if (cap instanceof FluidRecipeCapability fluidRecipeCapability) {
                            multipliers += fluidRecipeCapability.getMaxParallelRatio(generatorArrayMachine, recipe, maxParallel);
                        }
                    }
                    GTRecipe paraRecipe = recipe.copy(ContentModifier.multiplier(multipliers), false);
                    paraRecipe.duration = paraRecipe.duration * (105 - 5 * generatorArrayMachine.getTier()) / 100;
                    if (generatorArrayMachine.isw) {
                        generatorArrayMachine.eut = RecipeHelper.getOutputEUt(paraRecipe);
                        paraRecipe.tickOutputs.remove(EURecipeCapability.CAP);
                    }
                    return paraRecipe;
                }
            }
        }
        return null;
    }

    //////////////////////////////////////
    // ******** Gui ********//
    //////////////////////////////////////

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        textList.add(Component.translatable("gtceu.machine.generator_array.wireless")
                .append(ComponentPanelWidget.withButton(Component.literal("[")
                        .append(this.isw ?
                                Component.translatable("gtceu.machine.on") :
                                Component.translatable("gtceu.machine.off"))
                        .append(Component.literal("]")), "wireless_switch")));
        if (isActive() && this.isw) {
            GTRecipe r = getRecipeLogic().getLastRecipe();
            if (r != null) {
                textList.add(Component.translatable("gtceu.recipe.eu_inverted", FormattingUtil.formatNumbers(eut)));
            }
        }
    }

    @Override
    public void handleDisplayClick(String componentData, ClickData clickData) {
        if (!clickData.isRemote) {
            if (componentData.equals("wireless_switch")) {
                this.isw = !this.isw;
            }
        }
    }

    @Override
    public Widget createUIWidget() {
        var widget = super.createUIWidget();
        if (widget instanceof WidgetGroup group) {
            var size = group.getSize();
            group.addWidget(new SlotWidget(machineStorage.storage, 0, size.width - 30, size.height - 30, true, true)
                    .setBackground(GuiTextures.SLOT));
        }
        return widget;
    }
}
