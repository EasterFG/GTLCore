package org.gtlcore.gtlcore.common.machine.multiblock.electric;

import org.gtlcore.gtlcore.api.machine.multiblock.IModularMachineModule;
import org.gtlcore.gtlcore.common.data.GTLRecipeModifiers;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SuprachronalAssemblyLineModuleMachine extends WorkableElectricMultiblockMachine
                                                   implements IModularMachineModule<SuprachronalAssemblyLineMachine, SuprachronalAssemblyLineModuleMachine>, IMachineLife {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            SuprachronalAssemblyLineModuleMachine.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Nullable
    @Getter
    @Setter
    private BlockPos hostPosition;

    @Nullable
    @Getter
    @Setter
    private SuprachronalAssemblyLineMachine host;

    public SuprachronalAssemblyLineModuleMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public @NotNull Class<SuprachronalAssemblyLineMachine> getHostType() {
        return SuprachronalAssemblyLineMachine.class;
    }

    @Override
    public BlockPos[] getHostScanPositions() {
        final BlockPos pos = getPos();
        return new BlockPos[] {
                pos.offset(3, 0, 0),
                pos.offset(-3, 0, 0),
                pos.offset(0, 0, 3),
                pos.offset(0, 0, -3)
        };
    }

    @Override
    public void onConnected(@NotNull SuprachronalAssemblyLineMachine host) {
        getRecipeLogic().updateTickSubscription();
    }

    public int getParallel() {
        if (host != null) {
            return GTLRecipeModifiers.getHatchParallel(host);
        }
        return 0;
    }

    @Override
    public boolean onWorking() {
        boolean value = super.onWorking();
        if (getOffsetTimer() % 20 == 0) {
            if (!isConnectedToHost()) {
                getRecipeLogic().setProgress(0);
            }
        }
        return value;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (!isConnectedToHost()) {
            getRecipeLogic().interruptRecipe();
            return false;
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        if (!findAndConnectToHost()) {
            removeFromHost(this.host);
        }
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        removeFromHost(this.host);
    }

    @Override
    public void onPartUnload() {
        super.onPartUnload();
        removeFromHost(this.host);
    }

    @Override
    public void onMachineRemoved() {
        removeFromHost(this.host);
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (!this.isFormed) return;
        textList.add(Component.translatable("gtceu.multiblock.parallel",
                Component.literal(FormattingUtil.formatNumbers(getParallel()))
                        .withStyle(ChatFormatting.DARK_PURPLE))
                .withStyle(ChatFormatting.GRAY));
        textList.add(Component.translatable(
                isConnectedToHost() ? "tooltip.gtlcore.module_installed" : "tooltip.gtlcore.module_not_installed"));
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
