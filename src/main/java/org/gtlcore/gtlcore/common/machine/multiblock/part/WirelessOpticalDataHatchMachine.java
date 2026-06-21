package org.gtlcore.gtlcore.common.machine.multiblock.part;

import org.gtlcore.gtlcore.api.capability.BindCapability;
import org.gtlcore.gtlcore.api.recipe.RecipeResult;

import com.gregtechceu.gtceu.api.capability.IDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IOpticalDataAccessHatch;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;

import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author EasterFG on 2024/10/27
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WirelessOpticalDataHatchMachine extends MultiblockPartMachine implements IOpticalDataAccessHatch, IInteractedMachine, BindCapability {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            WirelessOpticalDataHatchMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final boolean transmitter;

    @Getter
    @Setter
    @Persisted
    private BlockPos transmitterPos;
    @Getter
    @Setter
    @Persisted
    private BlockPos receiverPos;

    public WirelessOpticalDataHatchMachine(IMachineBlockEntity holder, boolean transmitter) {
        super(holder);
        this.transmitter = transmitter;
    }

    @Override
    public boolean isTransmitter() {
        return transmitter;
    }

    @Override
    public boolean isRecipeAvailable(GTRecipe recipe, Collection<IDataAccessHatch> seen) {
        seen.add(this);
        if (!getControllers().isEmpty()) {
            if (isTransmitter()) {
                IMultiController controller = getControllers().get(0);
                if (!(controller instanceof IWorkable workable) || !workable.isActive()) return false;
                List<IDataAccessHatch> dataAccesses = new ArrayList<>();
                List<IDataAccessHatch> transmitters = new ArrayList<>();
                for (var part : controller.getParts()) {
                    Block block = part.self().getBlockState().getBlock();
                    if (part instanceof IDataAccessHatch hatch && PartAbility.DATA_ACCESS.isApplicable(block)) {
                        dataAccesses.add(hatch);
                    }
                    if (part instanceof IDataAccessHatch hatch && PartAbility.OPTICAL_DATA_RECEPTION.isApplicable(block)) {
                        transmitters.add(hatch);
                    }
                }
                return isRecipeAvailable(dataAccesses, seen, recipe) || isRecipeAvailable(transmitters, seen, recipe);
            } else {
                Level level = getLevel();
                if (level == null || transmitterPos == null) {
                    RecipeResult.of((IRecipeLogicMachine) getControllers().get(0), RecipeResult.fail(Component.translatable("gtceu.machine.wireless_data_receiver_hatch.unbind")));
                    return false;
                }
                if (MetaMachine.getMachine(level, transmitterPos) instanceof WirelessOpticalDataHatchMachine machine) {
                    if (machine.isRecipeAvailable(recipe, seen)) return true;
                    else RecipeResult.of((IRecipeLogicMachine) getControllers().get(0), RecipeResult.FAIL_NO_FIND_RESEARCHED);
                }
            }
        }
        return false;
    }

    private static boolean isRecipeAvailable(@NotNull Iterable<? extends IDataAccessHatch> hatches, @NotNull Collection<IDataAccessHatch> seen, @NotNull GTRecipe recipe) {
        for (IDataAccessHatch hatch : hatches) {
            if (hatch.isCreative()) return true;
            if (!seen.contains(hatch) && hatch.isRecipeAvailable(recipe, seen)) {
                return true;
            }
        }
        return false;
    }

    private CompoundTag createPos(BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("x", pos.getX());
        posTag.putInt("y", pos.getY());
        posTag.putInt("z", pos.getZ());
        return posTag;
    }

    private BlockPos getPos(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack is = player.getItemInHand(hand);
        if (is.isEmpty()) return InteractionResult.PASS;
        if (is.is(GTItems.TOOL_DATA_STICK.asItem())) {
            Level level = getLevel();
            if (level == null) return InteractionResult.PASS;
            if (transmitter) {
                var tag = is.getTag();
                if (this.transmitterPos == null) this.transmitterPos = pos;
                if (tag != null) {
                    tag.put("transmitterPos", createPos(pos));
                    var bindPos = (CompoundTag) tag.get("receiverPos");
                    if (bindPos != null) {
                        BlockPos recPos = getPos(bindPos);
                        if (MetaMachine.getMachine(level, recPos) instanceof WirelessOpticalDataHatchMachine wod && !wod.transmitter) {
                            wod.setTransmitterPos(this.transmitterPos);
                            this.receiverPos = pos;
                            tag.remove("transmitterPos");
                            tag.remove("receiverPos");
                            if (level.isClientSide()) {
                                player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_hatch.bind"));
                            }
                        }
                    } else {
                        if (level.isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_transmitter_hatch.to_bind"));
                        }
                    }
                    is.setTag(tag);
                } else {
                    tag = new CompoundTag();
                    tag.put("transmitterPos", createPos(transmitterPos));
                    is.setTag(tag);
                    if (level.isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_transmitter_hatch.to_bind"));
                    }
                }
                return InteractionResult.SUCCESS;
            } else {
                if (this.receiverPos == null) this.receiverPos = pos;
                var tag = is.getTag();
                if (tag != null) {
                    tag.put("receiverPos", createPos(pos));
                    var bindPos = (CompoundTag) tag.get("transmitterPos");
                    if (bindPos != null) {
                        BlockPos tranPos = new BlockPos(bindPos.getInt("x"), bindPos.getInt("y"), bindPos.getInt("z"));
                        if (MetaMachine.getMachine(level, tranPos) instanceof WirelessOpticalDataHatchMachine wod && wod.transmitter) {
                            wod.setReceiverPos(this.receiverPos);
                            this.transmitterPos = tranPos;
                            tag.remove("transmitterPos");
                            tag.remove("receiverPos");
                            if (level.isClientSide()) {
                                player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_hatch.bind"));
                            }
                        }
                    } else {
                        if (level.isClientSide()) {
                            player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_receiver_hatch.to_bind"));
                        }
                    }
                    is.setTag(tag);
                } else {
                    tag = new CompoundTag();
                    tag.put("receiverPos", createPos(this.receiverPos));
                    is.setTag(tag);
                    if (level.isClientSide()) {
                        player.sendSystemMessage(Component.translatable("gtceu.machine.wireless_data_receiver_hatch.to_bind"));
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean canShared() {
        return false;
    }

    @Override
    public GTRecipe modifyRecipe(GTRecipe recipe) {
        return IOpticalDataAccessHatch.super.modifyRecipe(recipe);
    }

    @Override
    public boolean bind() {
        return this.transmitterPos != null || this.receiverPos != null;
    }

    @Override
    public String pos() {
        if (this.isTransmitter() && this.receiverPos != null) {
            return this.receiverPos.toShortString();
        } else if (!this.isTransmitter() && this.transmitterPos != null) {
            return this.transmitterPos.toShortString();
        }
        return "";
    }
}
