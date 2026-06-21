package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.machine.trait.AECraft.IMolecularAssemblerHandlerTrait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.List;

public abstract class NotifiableMAHandlerTrait extends MachineTrait implements IMolecularAssemblerHandlerTrait {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableMAHandlerTrait.class);
    protected List<Runnable> listeners = new ReferenceArrayList<>();

    public NotifiableMAHandlerTrait(MetaMachine machine) {
        super(machine);
    }

    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public ISubscription addChangedListener(Runnable listener) {
        this.listeners.add(listener);
        return () -> this.listeners.remove(listener);
    }

    public void notifyListeners() {
        this.listeners.forEach(Runnable::run);
    }
}
