package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;
import org.gtlcore.gtlcore.integration.ae2.AEUtils;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;

import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.IntStream;

public class MEPatternBufferRecipeHandlerTrait extends MachineTrait {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferPartMachine.class);

    @Getter
    protected final MEItemInputHandler meItemHandler;

    @Getter
    protected final MEFluidHandler meFluidHandler;

    public MEPatternBufferRecipeHandlerTrait(MEPatternBufferPartMachine ioBuffer, IO io) {
        super(ioBuffer);
        meItemHandler = new MEItemInputHandler(ioBuffer, io);
        meFluidHandler = new MEFluidHandler(ioBuffer, io);
    }

    @Override
    public MEPatternBufferPartMachine getMachine() {
        return (MEPatternBufferPartMachine) super.getMachine();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {}

    private boolean handleItemInner(GTRecipe recipe, Object2LongMap<Ingredient> left, int circuit, boolean simulate, int trySlot) {
        var internalSlot = getMachine().getInternalInventory()[trySlot];
        if (internalSlot.isItemActive(simulate)) {
            if (simulate) {
                if (!internalSlot.testCatalystItemInternal(recipe)) return false;
            }
            return internalSlot.handleItemInternal(left, circuit, simulate);
        } else return left.isEmpty() && circuit < 0;
    }

    private boolean handleFluidInner(GTRecipe recipe, Object2LongMap<FluidIngredient> left, boolean simulate, int trySlot) {
        var internalSlot = getMachine().getInternalInventory()[trySlot];
        if (internalSlot.isFluidActive(simulate)) {
            if (simulate) {
                if (!internalSlot.testCatalystFluidInternal(recipe)) return false;
            }
            return internalSlot.handleFluidInternal(left, simulate);
        } else return left.isEmpty();
    }

    private Set<Integer> getActiveSlots(MEPatternBufferPartMachine.InternalSlot[] slots) {
        final Set<Integer> activeSlots = new IntOpenHashSet();
        for (int i = 0; i < slots.length; i++) {
            if (slots[i].isActive()) activeSlots.add(i);
        }
        return activeSlots;
    }

    private int[] getActiveAndUnCachedSlots(MEPatternBufferPartMachine.InternalSlot[] slots) {
        final var machine = getMachine();
        return IntStream.range(0, slots.length)
                .filter(i -> slots[i].isActive() && !machine.cacheRecipe[i])
                .toArray();
    }

    public class MEItemInputHandler extends NotifiableMERecipeHandlerTrait<Ingredient, ItemStack> {

        @Getter
        private final IO io;

        @Getter
        @Setter
        private Object2LongMap<Ingredient> preparedMEHandleContents = new Object2LongOpenHashMap<>();

        @Setter
        private int preparedCircuitConfig = -1;

        public MEItemInputHandler(MEPatternBufferPartMachine machine, IO io) {
            super(machine);
            this.io = io;
        }

        public MEPatternBufferPartMachine getMachine() {
            return (MEPatternBufferPartMachine) this.machine;
        }

        @Override
        public RecipeCapability<Ingredient> getCapability() {
            return ItemRecipeCapability.CAP;
        }

        @Override
        public Set<Integer> getActiveSlots() {
            return MEPatternBufferRecipeHandlerTrait.this.getActiveSlots(getMachine().getInternalInventory());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Int2ObjectMap<List<ItemStack>> getActiveAndUnCachedSlotsLimitContentsMap() {
            var map = new Int2ObjectArrayMap<List<ItemStack>>();
            var machine = getMachine();
            var shared = (List<ItemStack>) (Object) machine.getSharedCatalystInventory().getContents();
            for (int slot : MEPatternBufferRecipeHandlerTrait.this.getActiveAndUnCachedSlots(getMachine().getInternalInventory())) {
                var inputs = machine.getInternalInventory()[slot].getLimitItemStackInput();

                // 通过统一方法获取该槽位的电路（可能来自样板或总成配置）
                ItemStack circuitForRecipe = machine.getCircuitForRecipe(slot);
                if (!circuitForRecipe.isEmpty()) {
                    inputs.add(circuitForRecipe);
                }

                inputs.addAll(shared);
                map.put(slot, inputs);
            }
            return map;
        }

        @Override
        public Object2LongMap<ItemStack> getStackMapFromFirstAvailableSlot(IntCollection slots) {
            final var inventory = getMachine().getInternalInventory();
            for (int slot : slots) {
                if (!inventory[slot].isActive()) continue;
                Object2LongOpenHashMap<ItemStack> map = new Object2LongOpenHashMap<>();
                for (var entry : Object2LongMaps.fastIterable(inventory[slot].getItemStackInputMap())) {
                    map.addTo(entry.getKey(), entry.getLongValue());
                }
                return map;
            }
            return Object2LongMaps.emptyMap();
        }

        @Override
        public boolean meHandleRecipeInner(GTRecipe recipe, Object2LongMap<Ingredient> left, boolean simulate, int trySlot) {
            return handleItemInner(recipe, left, preparedCircuitConfig, simulate, trySlot);
        }

        @Override
        public void prepareMEHandleContents(GTRecipe recipe, List<Ingredient> left, boolean simulate) {
            preparedCircuitConfig = -1;
            if (simulate) {
                // 处理总成配置的电路
                getMachine().getSharedCircuitInventory().handleRecipeInner(IO.IN, recipe, left, null, true);

                // 处理共享库存
                getMachine().getSharedCatalystInventory().handleRecipeInner(IO.IN, recipe, left, null, true);

                // simulate时left会包含Circuit
                setPreparedMEHandleContents(AEUtils.ingredientsMapWithOutCircuit(left, this::setPreparedCircuitConfig));
            } else {
                setPreparedMEHandleContents(AEUtils.ingredientsMap(left));
            }
        }

        @Override
        public List<Ingredient> meHandleRecipeOutputInner(List<Ingredient> left, boolean simulate) {
            if (simulate) return List.of();
            final var buffer = getMachine().buffer;
            for (Ingredient ingredient : left) {
                if (ingredient instanceof IntProviderIngredient intProvider) {
                    intProvider.setItemStacks(null);
                    intProvider.setSampledCount(null);
                }

                ItemStack[] items = ingredient.getItems();
                if (items.length != 0) {
                    ItemStack output = items[0];
                    if (!output.isEmpty()) {
                        buffer.addTo(AEItemKey.of(output), ingredient instanceof LongIngredient longIngredient ? longIngredient.getActualAmount() : output.getCount());
                    }
                }
            }
            return List.of();
        }
    }

    public class MEFluidHandler extends NotifiableMERecipeHandlerTrait<FluidIngredient, FluidStack> {

        @Getter
        private final IO io;

        @Getter
        @Setter
        private Object2LongMap<FluidIngredient> preparedMEHandleContents = new Object2LongOpenHashMap<>();

        public MEFluidHandler(MEPatternBufferPartMachine machine, IO io) {
            super(machine);
            this.io = io;
        }

        public MEPatternBufferPartMachine getMachine() {
            return (MEPatternBufferPartMachine) this.machine;
        }

        @Override
        public RecipeCapability<FluidIngredient> getCapability() {
            return FluidRecipeCapability.CAP;
        }

        @Override
        public Set<Integer> getActiveSlots() {
            return MEPatternBufferRecipeHandlerTrait.this.getActiveSlots(getMachine().getInternalInventory());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Int2ObjectMap<List<FluidStack>> getActiveAndUnCachedSlotsLimitContentsMap() {
            var map = new Int2ObjectArrayMap<List<FluidStack>>();
            var machine = getMachine();
            var shared = (List<FluidStack>) (Object) machine.getSharedCatalystTank().getContents();
            for (int slot : MEPatternBufferRecipeHandlerTrait.this.getActiveAndUnCachedSlots(getMachine().getInternalInventory())) {
                var inputs = machine.getInternalInventory()[slot].getLimitFluidStackInput();
                inputs.addAll(shared);
                map.put(slot, inputs);
            }
            return map;
        }

        @Override
        public Object2LongMap<FluidStack> getStackMapFromFirstAvailableSlot(IntCollection slots) {
            final var inventory = getMachine().getInternalInventory();
            for (int slot : slots) {
                if (!inventory[slot].isActive()) continue;
                Object2LongOpenHashMap<FluidStack> map = new Object2LongOpenHashMap<>();
                for (var entry : Object2LongMaps.fastIterable(inventory[slot].getFluidStackInputMap())) {
                    map.addTo(entry.getKey(), entry.getLongValue());
                }
                return map;
            }
            return Object2LongMaps.emptyMap();
        }

        @Override
        public boolean meHandleRecipeInner(GTRecipe recipe, Object2LongMap<FluidIngredient> left, boolean simulate, int trySlot) {
            return handleFluidInner(recipe, left, simulate, trySlot);
        }

        @Override
        public void prepareMEHandleContents(GTRecipe recipe, List<FluidIngredient> left, boolean simulate) {
            if (simulate) {
                getMachine().getSharedCatalystTank().handleRecipeInner(IO.IN, recipe, left, null, true);
            }
            setPreparedMEHandleContents(AEUtils.fluidIngredientsMap(left));
        }

        @Override
        public List<FluidIngredient> meHandleRecipeOutputInner(List<FluidIngredient> left, boolean simulate) {
            if (simulate) return List.of();
            final var buffer = getMachine().buffer;
            for (FluidIngredient fluidIngredient : left) {
                if (!fluidIngredient.isEmpty()) {
                    FluidStack[] fluids = fluidIngredient.getStacks();
                    if (fluids.length != 0) {
                        FluidStack output = fluids[0];
                        buffer.addTo(AEFluidKey.of(output.getFluid()), output.getAmount());
                    }
                }
            }
            return List.of();
        }
    }
}
