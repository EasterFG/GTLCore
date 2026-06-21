package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEIOPartMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEIOTrait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyUIProvider;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.integration.ae2.machine.feature.IGridConnectedMachine;
import com.gregtechceu.gtceu.integration.ae2.machine.trait.GridNodeHolder;

import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.security.IActionSource;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public abstract class MEIOPartMachine extends MultiblockPartMachine implements IMachineLife, IGridConnectedMachine, IMEIOPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MEIOPartMachine.class, MultiblockPartMachine.MANAGED_FIELD_HOLDER);

    @Persisted
    @Getter
    protected final GridNodeHolder nodeHolder;

    @Getter
    @Setter
    @DescSynced
    protected boolean isOnline;

    protected final IMEIOTrait meTrait;

    protected final IActionSource actionSource;

    protected final IO io;

    protected boolean isSleeping = true;

    public MEIOPartMachine(IMachineBlockEntity holder, IO io) {
        super(holder);
        this.nodeHolder = createNodeHolder();
        this.actionSource = IActionSource.ofMachine(nodeHolder.getMainNode()::getNode);
        this.io = io;
        this.meTrait = createMETrait();
    }

    protected @NotNull IMEIOTrait createMETrait() {
        return new MEIOTrait(this);
    }

    @Override
    public @NotNull IMEIOTrait getMETrait() {
        return meTrait;
    }

    @Nullable
    @Override
    public IFancyUIProvider.@Nullable PageGroupingData getPageGroupingData() {
        return switch (this.io) {
            case IN -> new PageGroupingData("gtceu.multiblock.page_switcher.io.import", 1);
            case OUT -> new PageGroupingData("gtceu.multiblock.page_switcher.io.export", 2);
            case BOTH -> new PageGroupingData("gtceu.multiblock.page_switcher.io.both", 3);
            case NONE -> null;
        };
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        if (tag.getCompound("ForgeData").getBoolean("isAllFacing")) {
            getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
        }
    }

    @Override
    public IManagedGridNode getMainNode() {
        return nodeHolder.getMainNode();
    }

    @Override
    public void onRotated(@NotNull Direction oldFacing, @NotNull Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        this.getMainNode().setExposedOnSides(EnumSet.of(newFacing));
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected GridNodeHolder createNodeHolder() {
        return new GridNodeHolder(this);
    }

    public class MEIOTrait extends MachineTrait implements IMEIOTrait {

        protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
                MEIOPartMachine.class);

        public MEIOTrait(MEIOPartMachine machine) {
            super(machine);
        }

        @Override
        public MEIOPartMachine getMachine() {
            return (MEIOPartMachine) machine;
        }

        @Override
        public void notifySelfIO() {
            if (isSleeping) {
                if (getMainNode().isActive()) {
                    getMainNode().ifPresent((grid, node) -> {
                        grid.getTickManager().wakeDevice(node);
                        isSleeping = false;
                    });
                }
            }
        }

        @Override
        public IO getIO() {
            return io;
        }

        @Override
        public ManagedFieldHolder getFieldHolder() {
            return MANAGED_FIELD_HOLDER;
        }
    }
}
