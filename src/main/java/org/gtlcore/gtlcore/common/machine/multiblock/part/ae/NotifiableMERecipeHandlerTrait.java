package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.machine.trait.IMERecipeHandlerTrait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import com.lowdragmc.lowdraglib.syncdata.ISubscription;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.List;
import java.util.function.Predicate;

public abstract class NotifiableMERecipeHandlerTrait<T extends Predicate<S>, S> extends MachineTrait implements IMERecipeHandlerTrait<T, S> {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(NotifiableMERecipeHandlerTrait.class);
    protected List<Runnable> listeners = new ReferenceArrayList<>();

    public NotifiableMERecipeHandlerTrait(MetaMachine machine) {
        super(machine);
    }

    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public ISubscription addChangedListener(Runnable listener) {
        this.listeners.add(listener);
        return () -> this.listeners.remove(listener);
    }

    public ISubscription addBufferChangedListener(Runnable mainListener) {
        this.listeners.add(0, mainListener);
        return () -> this.listeners.remove(mainListener);
    }

    public void notifyListeners() {
        this.listeners.forEach(Runnable::run);
    }
}
