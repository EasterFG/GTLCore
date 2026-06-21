package org.gtlcore.gtlcore.common.item;

import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.IAutoConfiguratioGravityPart;
import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.IAutoConfigurationMaintenanceHatch;
import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.IGravityPartMachine;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.item.component.IInteractionItem;
import com.gregtechceu.gtceu.api.item.tool.behavior.IToolBehavior;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.Objects;

import static com.gregtechceu.gtceu.api.item.tool.ToolHelper.getBehaviorsTag;

public class ConfigurationCopyBehavior implements IToolBehavior, IInteractionItem {

    public static final ConfigurationCopyBehavior INSTANCE = new ConfigurationCopyBehavior();

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        CompoundTag tags = getBehaviorsTag(stack);
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MetaMachineBlockEntity machineBlock) {
            MetaMachine metaMachine = machineBlock.getMetaMachine();
            if (metaMachine instanceof SimpleTieredMachine simpleTieredMachine) {
                if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
                    getSTMCfg(tags, simpleTieredMachine);
                    context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_copied"), true);
                } else {
                    if (tags.getBoolean("Configuration")) {
                        setSTMCfg(tags, simpleTieredMachine);
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_pasted"), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_not_found"), true);
                    }
                }
            } else if (metaMachine instanceof ItemBusPartMachine itemBusPartMachine) {
                if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
                    getBusCfg(tags, itemBusPartMachine);
                    context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_copied"), true);
                } else {
                    if (tags.getBoolean("Configuration")) {
                        setBusCfg(tags, itemBusPartMachine);
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_pasted"), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_not_found"), true);
                    }
                }
            } else if (metaMachine instanceof IAutoConfiguratioGravityPart acgp) {
                if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
                    tags.putBoolean("Configuration", true);
                    tags.putFloat("durationMultiplier", acgp.getDurationMultiplier());
                    tags.putInt("currentGravity", acgp.getCurrentGravity());
                    context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_copied"), true);
                } else {
                    if (tags.getBoolean("Configuration")) {
                        acgp.setDurationMultiplier(tags.getFloat("durationMultiplier"));
                        acgp.setCurrentGravity(tags.getInt("currentGravity"));
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_pasted"), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_not_found"), true);
                    }
                }
            } else if (metaMachine instanceof IAutoConfigurationMaintenanceHatch acmh) {
                if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
                    tags.putBoolean("Configuration", true);
                    tags.putFloat("durationMultiplier", acmh.getDurationMultiplier());
                    context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_copied"), true);
                } else {
                    if (tags.getBoolean("Configuration")) {
                        acmh.setDurationMultiplier(tags.getFloat("durationMultiplier"));
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_pasted"), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_not_found"), true);
                    }
                }
            } else if (metaMachine instanceof IGravityPartMachine gpm) {
                if (Objects.requireNonNull(context.getPlayer()).isShiftKeyDown()) {
                    tags.putBoolean("Configuration", true);
                    tags.putInt("currentGravity", gpm.getCurrentGravity());
                    context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_copied"), true);
                } else {
                    if (tags.getBoolean("Configuration")) {
                        gpm.setCurrentGravity(tags.getInt("currentGravity"));
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_pasted"), true);
                    } else {
                        context.getPlayer().displayClientMessage(Component.translatable("message.gtlcore.machine_data_not_found"), true);
                    }
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void getBusCfg(CompoundTag tags, ItemBusPartMachine metaMachine) {
        tags.putBoolean("Configuration", true);
        tags.putBoolean("isDistinct", metaMachine.isDistinct());
        tags.putBoolean("isWorkingEnabled", metaMachine.isWorkingEnabled());
        NotifiableItemStackHandler circuitInventory = metaMachine.getCircuitInventory();
        if (circuitInventory.handlerIO.support(IO.IN)) {
            int c = IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0));
            if (c > 0) {
                tags.putInt("circuit", c);
            }
        }
    }

    private void setBusCfg(CompoundTag tags, ItemBusPartMachine metaMachine) {
        metaMachine.setDistinct(tags.getBoolean("isDistinct"));
        metaMachine.setWorkingEnabled(tags.getBoolean("isWorkingEnabled"));
        int c = tags.getInt("circuit");
        NotifiableItemStackHandler circuitInventory = metaMachine.getCircuitInventory();
        if (c > 0 && circuitInventory.handlerIO.support(IO.IN)) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(c));
        }
    }

    private void getSTMCfg(CompoundTag tags, SimpleTieredMachine metaMachine) {
        tags.putBoolean("Configuration", true);
        boolean hasAutoOutputItem = metaMachine.hasAutoOutputItem();
        boolean hasAutoOutputFluid = metaMachine.hasAutoOutputFluid();
        tags.putBoolean("hasAutoOutputItem", hasAutoOutputItem);
        tags.putBoolean("hasAutoOutputFluid", hasAutoOutputFluid);
        if (hasAutoOutputItem) {
            tags.putString("outputFacingItems", metaMachine.getOutputFacingItems().toString());
        }
        if (hasAutoOutputFluid) {
            tags.putString("outputFacingFluids", metaMachine.getOutputFacingFluids().toString());
        }
        tags.putBoolean("allowInputFromOutputSideItems", metaMachine.isAllowInputFromOutputSideItems());
        tags.putBoolean("allowInputFromOutputSideFluids", metaMachine.isAllowInputFromOutputSideFluids());
        NotifiableItemStackHandler circuitInventory = metaMachine.getCircuitInventory();
        if (circuitInventory.handlerIO.support(IO.IN)) {
            int c = IntCircuitBehaviour.getCircuitConfiguration(circuitInventory.getStackInSlot(0));
            if (c > 0) {
                tags.putInt("circuit", c);
            }
        }
    }

    private void setSTMCfg(CompoundTag tags, SimpleTieredMachine metaMachine) {
        boolean hasAutoOutputItem = tags.getBoolean("hasAutoOutputItem");
        boolean hasAutoOutputFluid = tags.getBoolean("hasAutoOutputFluid");
        metaMachine.setAutoOutputItems(hasAutoOutputItem);
        metaMachine.setAutoOutputFluids(hasAutoOutputFluid);
        if (hasAutoOutputItem) {
            metaMachine.setOutputFacingItems(Direction.byName(tags.getString("outputFacingItems")));
        }
        if (hasAutoOutputFluid) {
            metaMachine.setOutputFacingFluids(Direction.byName(tags.getString("outputFacingFluids")));
        }
        metaMachine.setAllowInputFromOutputSideItems(tags.getBoolean("allowInputFromOutputSideItems"));
        metaMachine.setAllowInputFromOutputSideFluids(tags.getBoolean("allowInputFromOutputSideFluids"));
        int c = tags.getInt("circuit");
        NotifiableItemStackHandler circuitInventory = metaMachine.getCircuitInventory();
        if (c > 0 && circuitInventory.handlerIO.support(IO.IN)) {
            circuitInventory.setStackInSlot(0, IntCircuitBehaviour.stack(c));
        }
    }
}
