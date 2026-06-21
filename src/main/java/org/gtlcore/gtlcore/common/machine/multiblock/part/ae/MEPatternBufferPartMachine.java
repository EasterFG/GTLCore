package org.gtlcore.gtlcore.common.machine.multiblock.part.ae;

import org.gtlcore.gtlcore.api.gui.MEPatternCatalystUIManager;
import org.gtlcore.gtlcore.api.machine.trait.*;
import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEPatternPartMachine;
import org.gtlcore.gtlcore.api.machine.trait.MEPart.IMEPatternTrait;
import org.gtlcore.gtlcore.common.data.GTLMachines;
import org.gtlcore.gtlcore.integration.ae2.AEUtils;
import org.gtlcore.gtlcore.integration.ae2.handler.PatternCircuitHandler;
import org.gtlcore.gtlcore.integration.ae2.handler.SlotCacheManager;
import org.gtlcore.gtlcore.integration.ae2.widget.AEPatternViewExtendSlotWidget;
import org.gtlcore.gtlcore.utils.GTLUtil;

import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.*;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.*;
import com.gregtechceu.gtceu.api.machine.feature.IInteractedMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AETextInputButtonWidget;
import com.gregtechceu.gtceu.utils.*;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.misc.*;
import com.lowdragmc.lowdraglib.side.fluid.*;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.LazyManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.server.ServerLifecycleHooks;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.ticking.*;
import appeng.api.stacks.*;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.crafting.pattern.ProcessingPatternItem;
import appeng.helpers.patternprovider.PatternContainer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.primitives.Ints;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystFluidStackHandler;
import com.hepdd.gtmthings.common.block.machine.trait.CatalystItemStackHandler;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

import static org.gtlcore.gtlcore.api.pattern.AdvancedBlockPattern.foundItem;

public class MEPatternBufferPartMachine extends MEIOPartMachine implements IInteractedMachine, ICraftingProvider, PatternContainer, IMEPatternPartMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            MEPatternBufferPartMachine.class, MEIOPartMachine.MANAGED_FIELD_HOLDER);

    private final InternalInventory internalPatternInventory = new InternalInventory() {

        @Override
        public int size() {
            return maxPatternCount;
        }

        @Override
        public ItemStack getStackInSlot(int slotIndex) {
            return patternInventory.getStackInSlot(slotIndex);
        }

        @Override
        public void setItemDirect(int slotIndex, ItemStack stack) {
            patternInventory.setStackInSlot(slotIndex, stack);
            patternInventory.onContentsChanged(slotIndex);
            onPatternChange(slotIndex);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot <= maxPatternCount && AEUtils.PROCESS_FILTER.apply(stack);
        }
    };

    // ========================================
    // Info
    // ========================================

    @DescSynced
    @Persisted
    @Setter
    @Getter
    protected String customName = "";
    protected final int maxPatternCount;
    private final boolean[] hasPatternArray;
    @DescSynced
    protected final boolean[] cacheRecipe;
    private boolean needPatternSync;

    // ========================================
    // Inventory
    // ========================================

    @Getter
    @Persisted
    private final ItemStackTransfer patternInventory;

    @Getter
    @Persisted(key = "shareInventory")
    protected final CatalystItemStackHandler sharedCatalystInventory;

    @Getter
    @Persisted(key = "shareTank")
    protected final CatalystFluidStackHandler sharedCatalystTank;

    @Getter
    @Persisted(key = "mePatternCircuitInventory")
    protected final NotifiableItemStackHandler sharedCircuitInventory;

    @Persisted
    protected final ItemStackTransfer[] catalystItems;

    @Persisted
    @LazyManaged
    protected final FluidTransferList[] catalystFluids;

    @Getter
    @Persisted
    protected final InternalSlot[] internalInventory;

    @Getter
    protected final Object2LongOpenHashMap<AEKey> buffer;

    // ========================================
    // Handlers
    // ========================================

    /** Pattern circuit handler for managing circuit logic */
    @Getter
    protected final PatternCircuitHandler circuitHandler;

    /** Recipe handler trait for ME Pattern Buffer */
    protected final MEPatternBufferRecipeHandlerTrait recipeHandler;

    // ========================================
    // Cache Map
    // ========================================

    protected final Int2ReferenceMap<ObjectSet<@NotNull GTRecipe>> recipeMultipleCacheMap;
    protected final byte[] cacheRecipeCount;
    private final BiMap<@NotNull IPatternDetails, Integer> patternSlotMap;
    private final Int2ObjectMap<IPatternDetails> slot2PatternMap;
    protected IntConsumer removeSlotFromMap = i -> {};

    // ========================================
    // Proxy
    // ========================================

    @Persisted
    private final ObjectOpenHashSet<BlockPos> proxies;
    private final Set<MEPatternBufferProxyPartMachine> proxyMachines;

    public MEPatternBufferPartMachine(IMachineBlockEntity holder, int maxPatternCount, IO io) {
        super(holder, io);

        // Initialize UI configuration
        this.maxPatternCount = maxPatternCount;

        // Initialize arrays with calculated size
        this.hasPatternArray = new boolean[maxPatternCount];
        this.cacheRecipe = new boolean[maxPatternCount];
        this.internalInventory = new InternalSlot[maxPatternCount];
        this.catalystItems = new ItemStackTransfer[maxPatternCount];
        this.catalystFluids = new FluidTransferList[maxPatternCount];
        this.cacheRecipeCount = new byte[maxPatternCount];

        // Initialize pattern cache
        this.patternSlotMap = HashBiMap.create();
        this.slot2PatternMap = new Int2ObjectOpenHashMap<>();
        this.recipeMultipleCacheMap = new Int2ReferenceOpenHashMap<>();

        // Initialize Proxy
        this.proxies = new ObjectOpenHashSet<>();
        this.proxyMachines = new ReferenceOpenHashSet<>();

        // Initialize inventories
        this.buffer = new Object2LongOpenHashMap<>();
        this.patternInventory = new ItemStackTransfer(maxPatternCount);
        this.patternInventory.setFilter(AEUtils.PROCESS_FILTER);
        Arrays.setAll(internalInventory, InternalSlot::new);
        Arrays.setAll(catalystItems, i -> {
            var transfer = new ItemStackTransfer(9);
            transfer.setFilter(stack -> !(stack.getItem() instanceof ProcessingPatternItem));
            return transfer;
        });
        Arrays.setAll(catalystFluids, i -> new FluidTransferList(Stream.generate(() -> (IFluidTransfer) new FluidStorage(16 * FluidHelper.getBucket()))
                .limit(9)
                .toList()));
        Arrays.fill(cacheRecipeCount, (byte) 1);

        // Initialize handlers
        this.sharedCircuitInventory = new NotifiableCircuitItemStackHandler(this);
        this.circuitHandler = new PatternCircuitHandler((NotifiableCircuitItemStackHandler) sharedCircuitInventory);
        this.sharedCatalystInventory = new CatalystItemStackHandler(this, 9, IO.IN, IO.NONE);
        this.sharedCatalystTank = new CatalystFluidStackHandler(this, 9, 16 * FluidHelper.getBucket(), IO.IN, IO.NONE);
        this.recipeHandler = new MEPatternBufferRecipeHandlerTrait(this, io);

        // Initialize AE2 Service
        getMainNode().addService(ICraftingProvider.class, this);
        if (io == IO.BOTH) {
            getMainNode().addService(IGridTickable.class, new Ticker());
        }
    }

    // ========================================
    // LIFECYCLE & NETWORK MANAGEMENT
    // ========================================

    @Nullable
    protected TickableSubscription updateSubs;

    @Override
    public void onLoad() {
        super.onLoad();
        if (getLevel() instanceof ServerLevel serverLevel) {
            serverLevel.getServer().execute(() -> {
                for (int i = 0; i < patternInventory.getSlots(); i++) {
                    var pattern = patternInventory.getStackInSlot(i);
                    var realPattern = getRealPattern(i, pattern);
                    if (realPattern != null) {
                        this.slot2PatternMap.put(i, realPattern);
                        hasPatternArray[i] = true;
                    }
                }
                reCalculatePatternSlotMap();
                needPatternSync = true;
            });
            for (int i = 0; i < maxPatternCount; i++) {
                final int index = i;
                catalystItems[index].setOnContentsChanged(() -> reCalculateCatalystItemMap(index));
                for (IFluidTransfer transfer : catalystFluids[index].transfers) {
                    if (transfer instanceof FluidStorage storage) {
                        storage.setOnContentsChanged(() -> reCalculateCatalystFluidMap(index));
                    }
                }
            }
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.@NotNull State reason) {
        super.onMainNodeStateChanged(reason);
        this.updateSubscription();
    }

    protected void updateSubscription() {
        if (getMainNode().isOnline()) {
            updateSubs = subscribeServerTick(updateSubs, this::update);
        } else if (updateSubs != null) {
            updateSubs.unsubscribe();
            updateSubs = null;
        }
    }

    protected void update() {
        if (needPatternSync) {
            ICraftingProvider.requestUpdate(getMainNode());
            this.needPatternSync = false;
        }
    }

    protected void onPatternChange(int index) {
        if (isRemote()) return;

        var internalInv = internalInventory[index];
        var newPattern = patternInventory.getStackInSlot(index);
        var newPatternDetailsWithOutCircuit = getRealPattern(index, newPattern);
        var oldPatternDetails = slot2PatternMap.get(index);

        // Update pattern mapping and tracking
        if (newPatternDetailsWithOutCircuit != null) {
            slot2PatternMap.put(index, newPatternDetailsWithOutCircuit);
            hasPatternArray[index] = true;
        } else {
            slot2PatternMap.remove(index);
            hasPatternArray[index] = false;
        }

        // remove old pattern cache
        if (oldPatternDetails != null && !oldPatternDetails.equals(newPatternDetailsWithOutCircuit)) {
            internalInv.cacheManager.clearAllCaches();
            removeSlotFromGTRecipeCache(index);
            refundSlot(internalInv);
            AEUtils.reFunds(buffer, getMainNode().getGrid(), actionSource);
        }

        reCalculatePatternSlotMap();
        needPatternSync = true;
    }

    @Override
    public void onMachineRemoved() {
        clearInventory(patternInventory);
        clearInventory(sharedCatalystInventory);
        for (ItemStackTransfer catalystItem : catalystItems) {
            clearInventory(catalystItem);
        }
        for (MEPatternBufferProxyPartMachine proxy : this.getProxies()) {
            proxy.setBuffer(null);
        }
    }

    private void reCalculateCatalystItemMap(int slot) {
        final var itemCatalystInventory = internalInventory[slot].itemCatalystInventory;
        itemCatalystInventory.clear();
        var catalystItem = catalystItems[slot];
        for (int i = 0; i < catalystItem.getSlots(); i++) {
            ItemStack stack = catalystItem.getStackInSlot(i);
            if (!stack.isEmpty()) {
                itemCatalystInventory.mergeLong(AEItemKey.of(stack), stack.getCount(), Long::sum);
            }
        }
        internalInventory[slot].onContentsChanged.run();
    }

    private void reCalculateCatalystFluidMap(int slot) {
        final var fluidCatalystInventory = internalInventory[slot].fluidCatalystInventory;
        fluidCatalystInventory.clear();
        var catalystFluid = catalystFluids[slot];
        for (int i = 0; i < catalystFluid.getTanks(); i++) {
            FluidStack stack = catalystFluid.getFluidInTank(i);
            if (!stack.isEmpty()) {
                fluidCatalystInventory.mergeLong(AEFluidKey.of(stack.getFluid()), stack.getAmount(), Long::sum);
            }
        }
        internalInventory[slot].onContentsChanged.run();
    }

    protected void reCalculatePatternSlotMap() {
        patternSlotMap.clear();
        for (var entry : Int2ObjectMaps.fastIterable(slot2PatternMap)) {
            int slot = entry.getIntKey();
            var pattern = entry.getValue();
            if (pattern != null) {
                if (cacheRecipe[slot]) patternSlotMap.forcePut(pattern, slot);
                else patternSlotMap.putIfAbsent(pattern, slot);
            }
        }
    }

    protected void removeSlotFromGTRecipeCache(int slot) {
        cacheRecipe[slot] = false;
        recipeMultipleCacheMap.remove(slot);
        removeSlotFromMap.accept(slot);
        for (MEPatternBufferProxyPartMachine proxy : this.getProxies()) {
            proxy.removeSlotFromMap.accept(slot);
        }
    }

    // ========================================
    // Persist
    // ========================================

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (!recipeMultipleCacheMap.isEmpty()) {
            final CompoundTag recipeCacheTag = new CompoundTag();
            for (var entry : Int2ReferenceMaps.fastIterable(recipeMultipleCacheMap)) {
                var recipeSet = entry.getValue();
                if (recipeSet.isEmpty()) continue;

                final ListTag list = new ListTag();
                for (GTRecipe recipe : recipeSet) {
                    list.add(GTLUtil.serializeNBT(recipe));
                }

                recipeCacheTag.put(Integer.toString(entry.getIntKey()), list);
            }
            tag.put("recipeMultipleCacheIdMap", recipeCacheTag);
        }

        tag.putByteArray("cacheRecipeCount", cacheRecipeCount);

        ListTag bufferTag = AEUtils.createListTag(AEKey::toTagGeneric, buffer);
        if (!bufferTag.isEmpty()) tag.put("buffer", bufferTag);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);

        var byteArray = tag.getByteArray("cacheRecipeCount");
        System.arraycopy(byteArray, 0, cacheRecipeCount, 0, byteArray.length);

        recipeMultipleCacheMap.clear();
        var recipeManager = ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        if (tag.contains("recipeMultipleCacheIdMap")) {
            CompoundTag recipeCacheTag = tag.getCompound("recipeMultipleCacheIdMap");
            for (String key : recipeCacheTag.getAllKeys()) {
                final int slotIndex = Integer.parseInt(key);
                final ListTag recipeTags = recipeCacheTag.getList(key, Tag.TAG_COMPOUND);
                for (Tag recipeTag : recipeTags) {
                    GTRecipe recipe = GTLUtil.deserializeNBT(recipeTag);
                    if (recipe != null && slotIndex >= 0 && slotIndex < maxPatternCount) {
                        var real = recipe.recipeType.getRecipe(recipeManager, recipe.id);
                        if (real != null) {
                            var set = recipeMultipleCacheMap.computeIfAbsent(slotIndex, integer -> new ObjectArraySet<>());
                            set.add(real);
                            if (set.size() >= cacheRecipeCount[slotIndex]) cacheRecipe[slotIndex] = true;
                        }
                    }
                }
            }
        } else if (tag.contains("gtRecipeCache")) {
            CompoundTag oldRecipeCacheTag = tag.getCompound("gtRecipeCache");
            for (String key : oldRecipeCacheTag.getAllKeys()) {
                int slotIndex = Integer.parseInt(key);
                Tag recipeTag = oldRecipeCacheTag.get(key);
                GTRecipe recipe = GTLUtil.deserializeNBT(recipeTag);
                if (recipe != null && slotIndex >= 0 && slotIndex < maxPatternCount) {
                    var real = recipe.recipeType.getRecipe(recipeManager, recipe.id);
                    if (real != null) {
                        var set = recipeMultipleCacheMap.computeIfAbsent(slotIndex, integer -> new ObjectArraySet<>());
                        set.add(real);
                        if (set.size() >= cacheRecipeCount[slotIndex]) cacheRecipe[slotIndex] = true;
                    }
                }
            }
        } // Compatibility

        ListTag bufferTag = tag.getList("buffer", Tag.TAG_COMPOUND);
        AEUtils.loadInventory(bufferTag, AEKey::fromTagGeneric, buffer);
    }

    public void copyFromTag(CompoundTag tag, ServerPlayer serverPlayer) {
        this.setCustomName(tag.getString("name"));
        var list = tag.getList("patterns", Tag.TAG_COMPOUND);

        int listIndex = 0;
        for (int index = 0; index < internalPatternInventory.size() && listIndex < list.size(); index++) {
            if (!internalPatternInventory.getStackInSlot(index).isEmpty()) {
                continue;
            }

            var result = foundItem(serverPlayer, List.of(AEItems.BLANK_PATTERN.stack()), AEItems.BLANK_PATTERN.stack()::is);
            if (result.getA() == null) break;

            CompoundTag patternData = list.getCompound(listIndex);
            var patternTag = patternData.getCompound("pattern");
            var sourceCacheCount = patternData.getByte("cacheCount");
            if (sourceCacheCount <= 0) break;

            internalPatternInventory.setItemDirect(index, ItemStack.of(patternTag));
            this.cacheRecipeCount[index] = sourceCacheCount;
            var handler = result.getB();
            if (handler != null) handler.extractItem(result.getC(), 1, false);

            listIndex++;
        }
    }

    public CompoundTag copyToTag(CompoundTag tags) {
        var tag = new CompoundTag();
        tag.putString("name", customName);

        var listPattern = new ListTag();
        for (int slotIndex : patternSlotMap.values()) {
            ItemStack stack = internalPatternInventory.getStackInSlot(slotIndex);
            if (!stack.isEmpty()) {
                CompoundTag patternData = new CompoundTag();
                patternData.put("pattern", stack.serializeNBT());
                patternData.putByte("cacheCount", cacheRecipeCount[slotIndex]);
                listPattern.add(patternData);
            }
        }
        tag.put("patterns", listPattern);

        tags.put("tag", tag);
        return tags;
    }

    @Override
    @NotNull
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // ========================================
    // PROXY MANAGEMENT SYSTEM
    // ========================================

    public void addProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.add(proxy.getPos());
        proxyMachines.add(proxy);
    }

    public void removeProxy(MEPatternBufferProxyPartMachine proxy) {
        proxies.remove(proxy.getPos());
        proxyMachines.remove(proxy);
    }

    public Set<MEPatternBufferProxyPartMachine> getProxies() {
        if (proxyMachines.size() != proxies.size()) {
            proxyMachines.clear();
            for (var pos : proxies) {
                if (MetaMachine.getMachine(Objects.requireNonNull(getLevel()), pos) instanceof MEPatternBufferProxyPartMachine proxy) {
                    proxyMachines.add(proxy);
                }
            }
        }
        return Collections.unmodifiableSet(proxyMachines);
    }

    // ========================================
    // REFUND SYSTEM
    // ========================================

    private void refundAll(ClickData clickData) {
        if (!clickData.isRemote) {
            // Move all slot contents to pending refund
            Arrays.stream(internalInventory)
                    .filter(InternalSlot::isActive)
                    .forEach(this::refundSlot);

            // Immediately try to process pending refunds
            AEUtils.reFunds(buffer, getMainNode().getGrid(), actionSource);
        }
    }

    public void refundSlot(InternalSlot slot) {
        // Move all item contents to pending refund
        for (var it = slot.itemInventory.object2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            long amount = entry.getLongValue();
            if (amount > 0) {
                buffer.addTo(entry.getKey(), amount);
                it.remove();
            }
        }

        // Move all fluid contents to pending refund
        for (var it = slot.fluidInventory.object2LongEntrySet().fastIterator(); it.hasNext();) {
            var entry = it.next();
            long amount = entry.getLongValue();
            if (amount > 0) {
                buffer.addTo(entry.getKey(), amount);
                it.remove();
            }
        }
    }

    // ========================================
    // DATA STICK INTERACTION
    // ========================================

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player,
                                   InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return InteractionResult.PASS;

        if (stack.is(GTItems.TOOL_DATA_STICK.asItem())) {
            if (!world.isClientSide) {
                // Check if it's research data - if so, pass to avoid conflicts
                var researchData = ResearchManager.readResearchId(stack);
                if (researchData != null) {
                    return InteractionResult.PASS;
                }

                // Store this pattern buffer's position in the data stick
                stack.getOrCreateTag().putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
                player.sendSystemMessage(Component.translatable("gtceu.machine.me.import_copy_settings"));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }

        return InteractionResult.PASS;
    }

    // ========================================
    // GUI SYSTEM
    // ========================================

    @Override
    public void attachConfigurators(ConfiguratorPanel configuratorPanel) {
        // Refund all button
        configuratorPanel.attachConfigurators(new ButtonConfigurator(
                new GuiTextureGroup(GuiTextures.BUTTON, GuiTextures.REFUND_OVERLAY), this::refundAll)
                .setTooltips(List.of(Component.translatable("gui.gtceu.refund_all.desc"))));

        // Circuit configurator
        configuratorPanel.attachConfigurators(new CircuitFancyConfigurator(sharedCircuitInventory.storage));

        // Share inventory configurator
        configuratorPanel.attachConfigurators(new FancyInvConfigurator(
                sharedCatalystInventory.storage, Component.translatable("gui.gtceu.share_inventory.title"))
                .setTooltips(List.of(
                        Component.translatable("gui.gtceu.share_inventory.desc.0"),
                        Component.translatable("gui.gtceu.share_inventory.desc.1"))));

        // Share tank configurator
        configuratorPanel.attachConfigurators(new FancyTankConfigurator(
                sharedCatalystTank.getStorages(), Component.translatable("gui.gtceu.share_tank.title"))
                .setTooltips(List.of(
                        Component.translatable("gui.gtceu.share_tank.desc.0"),
                        Component.translatable("gui.gtceu.share_inventory.desc.1"))));
    }

    @Override
    public @NotNull Widget createUIWidget() {
        int rowSize = 9;
        int colSize = maxPatternCount / rowSize;
        var group = new WidgetGroup(0, 0, 18 * rowSize + 16, 18 * colSize + 16);

        // ME Network status indicator
        group.addWidget(new LabelWidget(8, 2,
                () -> this.isOnline ? "gtceu.gui.me_network.online" : "gtceu.gui.me_network.offline"));

        // Custom name input widget
        group.addWidget(new AETextInputButtonWidget(18 * rowSize + 8 - 70, 2, 70, 10)
                .setText(customName)
                .setOnConfirm(this::setCustomName)
                .setButtonTooltips(Component.translatable("gui.gtceu.rename.desc")));

        final var catalystUIManager = new MEPatternCatalystUIManager(group.getSizeWidth() + 4, catalystItems, catalystFluids, cacheRecipeCount, this::removeSlotFromGTRecipeCache);
        group.waitToAdded(catalystUIManager);

        int index = 0;
        for (int y = 0; y < colSize; ++y) {
            for (int x = 0; x < rowSize; ++x) {
                int finalI = index;

                var slot = new AEPatternViewExtendSlotWidget(patternInventory, index++, x * 18 + 8, y * 18 + 14)
                        .setOnMiddleClick(() -> catalystUIManager.toggleFor(finalI))
                        .setOnPatternSlotChanged(() -> this.onPatternChange(finalI))
                        .setOccupiedTexture(GuiTextures.SLOT)
                        .setItemHook(stack -> {
                            if (!stack.isEmpty() && stack.getItem() instanceof EncodedPatternItem iep) {
                                final ItemStack out = iep.getOutput(stack);
                                if (!out.isEmpty()) return out;
                            }
                            return stack;
                        })
                        .setOnAddedTooltips((s, l) -> {
                            if (cacheRecipe[finalI])
                                l.add(Component.translatable("gtceu.machine.pattern.recipe.cache"));
                        })
                        .setBackground(GuiTextures.SLOT, GuiTextures.PATTERN_OVERLAY);
                group.addWidget(slot);
            }
        }
        return group;
    }

    // ========================================
    // CIRCUIT HANDLING
    // ========================================

    private IPatternDetails getRealPattern(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            var internalSlot = internalInventory[slot];
            return circuitHandler.processPatternWithCircuit(
                    stack, internalSlot.cacheManager::setCircuitCache, getLevel());
        }
        return null;
    }

    /**
     * 获取用于配方的电路
     *
     * @param slotIndex 槽位索引
     * @return 电路ItemStack，可能为空
     */
    public ItemStack getCircuitForRecipe(int slotIndex) {
        return circuitHandler.getCircuitForRecipe(internalInventory[slotIndex].cacheManager.getCircuitStack());
    }

    // ========================================
    // AE2 CRAFTING
    // ========================================

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return patternSlotMap.keySet().stream().toList();
    }

    @Override
    public boolean pushPattern(IPatternDetails patternDetails, KeyCounter[] inputHolder) {
        if (!getMainNode().isActive() || !patternSlotMap.containsKey(patternDetails)) {
            return false;
        }

        var slotIndex = patternSlotMap.get(patternDetails);
        if (slotIndex != null && slotIndex >= 0) {
            internalInventory[slotIndex].pushPattern(inputHolder);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBusy() {
        return false;
    }

    // ========================================
    // PATTERN CONTAINER IMPLEMENTATION
    // ========================================

    @Override
    public @Nullable IGrid getGrid() {
        return getMainNode().getGrid();
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return internalPatternInventory;
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        List<IMultiController> controllers = getControllers();

        // Handle multiblock controller grouping
        if (!controllers.isEmpty()) {
            IMultiController controller = controllers.get(0);
            MultiblockMachineDefinition controllerDefinition = controller.self().getDefinition();

            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                ItemStack circuitStack = sharedCircuitInventory.storage.getStackInSlot(0);
                int circuitConfiguration = circuitStack.isEmpty() ? -1 :
                        IntCircuitBehaviour.getCircuitConfiguration(circuitStack);

                Component groupName = circuitConfiguration != -1 ?
                        Component.translatable(controllerDefinition.getDescriptionId())
                                .append(" - " + circuitConfiguration) :
                        Component.translatable(controllerDefinition.getDescriptionId());

                return new PatternContainerGroup(
                        AEItemKey.of(controllerDefinition.asStack()), groupName, Collections.emptyList());
            }
        } else {
            if (!customName.isEmpty()) {
                return new PatternContainerGroup(
                        AEItemKey.of(GTLMachines.GTAEMachines.ME_EXTEND_PATTERN_BUFFER.getItem()),
                        Component.literal(customName),
                        Collections.emptyList());
            } else {
                return new PatternContainerGroup(
                        AEItemKey.of(GTLMachines.GTAEMachines.ME_EXTEND_PATTERN_BUFFER.getItem()),
                        GTLMachines.GTAEMachines.ME_EXTEND_PATTERN_BUFFER.get().getDefinition().getItem().getDescription(),
                        Collections.emptyList());
            }
        }
    }

    // ========================================
    // IMEPatternPartMachine
    // ========================================

    @Override
    protected @NotNull IMEPatternTrait createMETrait() {
        return new MEPatternTrait(this);
    }

    @Override
    public @NotNull IMEPatternTrait getMETrait() {
        return (IMEPatternTrait) meTrait;
    }

    @Override
    public Pair<IMERecipeHandlerTrait<Ingredient, ItemStack>, IMERecipeHandlerTrait<FluidIngredient, FluidStack>> getMERecipeHandlerTraits() {
        return Pair.of(recipeHandler.meItemHandler, recipeHandler.meFluidHandler);
    }

    /**
     * Internal Slot: Pattern-specific inventory management
     * Each slot represents a single pattern's ingredient storage with separate item and fluid inventories.
     * Provides optimized handling for recipe matching
     * and ingredient consumption with efficient serialization.
     * Features:
     * - Separate inventories per pattern
     * - Efficient recipe matching with early exit conditions
     */
    public class InternalSlot implements ITagSerializable<CompoundTag>, IContentChangeAware {

        @Getter
        @Setter
        protected Runnable onContentsChanged = () -> {
            recipeHandler.getMeFluidHandler().notifyListeners();
            recipeHandler.getMeItemHandler().notifyListeners();
        };

        @Getter
        private final Object2LongOpenHashMap<AEItemKey> itemInventory = new Object2LongOpenHashMap<>();

        @Getter
        private final Object2LongOpenHashMap<AEFluidKey> fluidInventory = new Object2LongOpenHashMap<>();

        private final Object2LongMap<AEItemKey> itemCatalystInventory = new Object2LongArrayMap<>();

        private final Object2LongMap<AEFluidKey> fluidCatalystInventory = new Object2LongArrayMap<>();

        @Persisted
        @Getter
        private final SlotCacheManager cacheManager = new SlotCacheManager();

        @Getter
        private final int slotIndex;

        public InternalSlot(int slotIndex) {
            this.slotIndex = slotIndex;
            itemInventory.defaultReturnValue(0);
            fluidInventory.defaultReturnValue(0);
            itemCatalystInventory.defaultReturnValue(0);
            fluidCatalystInventory.defaultReturnValue(0);
        }

        public boolean isActive() {
            return hasPatternArray[slotIndex] && (!itemInventory.isEmpty() || !fluidInventory.isEmpty());
        }

        public boolean isItemActive(boolean simulate) {
            return hasPatternArray[slotIndex] && simulate ? (!itemInventory.isEmpty() || !sharedCatalystInventory.isEmpty() || !circuitHandler.getCircuitForRecipe(cacheManager.getCircuitStack()).isEmpty() || !itemCatalystInventory.isEmpty()) : !itemInventory.isEmpty();
        }

        public boolean isFluidActive(boolean simulate) {
            return hasPatternArray[slotIndex] && simulate ? !fluidInventory.isEmpty() || !sharedCatalystTank.isEmpty() || !fluidCatalystInventory.isEmpty() : !fluidInventory.isEmpty();
        }

        private void add(AEKey what, long amount) {
            if (amount <= 0L) return;
            if (what instanceof AEItemKey itemKey) {
                itemInventory.addTo(itemKey, amount);
            } else if (what instanceof AEFluidKey fluidKey) {
                fluidInventory.addTo(fluidKey, amount);
            }
        }

        public Object2LongMap<ItemStack> getItemStackInputMap() {
            var itemInputMap = new Object2LongOpenCustomHashMap<>(ItemStackHashStrategy.comparingAllButCount());
            for (Object2LongMap.Entry<AEItemKey> entry : Object2LongMaps.fastIterable(itemInventory)) {
                AEItemKey key = entry.getKey();
                long amount = entry.getLongValue();
                if (amount <= 0) continue;

                ItemStack stack = key.toStack(1);
                itemInputMap.addTo(stack, amount);
            }
            return itemInputMap;
        }

        public Object2LongMap<FluidStack> getFluidStackInputMap() {
            var fluidInputMap = new Object2LongOpenCustomHashMap<>(FluidStackHashStrategy.comparingAllButAmount());
            for (Object2LongMap.Entry<AEFluidKey> entry : Object2LongMaps.fastIterable(fluidInventory)) {
                AEFluidKey key = entry.getKey();
                long amount = entry.getLongValue();
                if (amount <= 0) continue;

                FluidStack stack = FluidStack.create(key.getFluid(), 1);
                fluidInputMap.addTo(stack, amount);
            }
            return fluidInputMap;
        }

        public ObjectList<ItemStack> getLimitItemStackInput() {
            var limitInput = new ObjectArrayList<ItemStack>(itemInventory.size());
            for (var it = Object2LongMaps.fastIterator(itemInventory); it.hasNext();) {
                var entry = it.next();
                long amount = entry.getLongValue();
                if (amount <= 0) {
                    it.remove();
                    continue;
                }
                limitInput.add(entry.getKey().toStack(Ints.saturatedCast(amount)));
            }
            for (Object2LongMap.Entry<AEItemKey> entry : Object2LongMaps.fastIterable(itemCatalystInventory)) {
                limitInput.add(entry.getKey().toStack(Ints.saturatedCast(entry.getLongValue())));
            }
            return limitInput;
        }

        public ObjectList<FluidStack> getLimitFluidStackInput() {
            var limitInput = new ObjectArrayList<FluidStack>(fluidInventory.size());
            for (var it = Object2LongMaps.fastIterator(fluidInventory); it.hasNext();) {
                var entry = it.next();
                long amount = entry.getLongValue();
                if (amount <= 0) {
                    it.remove();
                    continue;
                }
                limitInput.add(FluidStack.create(entry.getKey().getFluid(), amount));
            }
            for (Object2LongMap.Entry<AEFluidKey> entry : Object2LongMaps.fastIterable(fluidCatalystInventory)) {
                limitInput.add(FluidStack.create(entry.getKey().getFluid(), entry.getLongValue()));
            }
            return limitInput;
        }

        public void pushPattern(KeyCounter[] inputHolder) {
            AEUtils.pushInputsToMEPatternBufferInventory(inputHolder, this::add);
            onContentsChanged.run();
        }

        public boolean testCatalystItemInternal(GTRecipe recipe) {
            for (Content content : recipe.getInputContents(ItemRecipeCapability.CAP)) {
                if (content.chance <= 0) continue;
                var ingredient = (Ingredient) content.getContent();
                for (ItemStack item : ingredient.getItems()) {
                    AEItemKey key = AEItemKey.of(item);
                    if (itemCatalystInventory.containsKey(key)) return false;
                }
            }
            return true;
        }

        public boolean testCatalystFluidInternal(GTRecipe recipe) {
            for (Content content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
                if (content.chance <= 0) continue;
                var fluidIngredient = (FluidIngredient) content.getContent();
                for (FluidStack stack : fluidIngredient.getStacks()) {
                    AEFluidKey key = AEFluidKey.of(stack.getFluid());
                    if (fluidCatalystInventory.containsKey(key)) return false;
                }
            }
            return true;
        }

        public boolean handleItemInternal(Object2LongMap<Ingredient> left, int leftCircuit, boolean simulate) {
            if (left.isEmpty() && leftCircuit < 0) return true;

            if (simulate && leftCircuit > 0 && !(leftCircuit == cacheManager.getCircuitCache())) {
                return false;
            }

            for (var it = Object2LongMaps.fastIterator(left); it.hasNext();) {
                var entry = it.next();
                var ingredient = entry.getKey();
                long needAmount = entry.getLongValue();
                if (needAmount <= 0) {
                    it.remove();
                    continue;
                }

                AEItemKey bestMatch = simulate ? cacheManager.getBestItemMatchSimulate(ingredient, itemInventory, itemCatalystInventory, needAmount) :
                        cacheManager.getBestItemMatch(ingredient, itemInventory, needAmount);
                if (bestMatch == null) {
                    return false;
                }
            }

            if (!simulate) {
                for (var it = Object2LongMaps.fastIterator(left); it.hasNext();) {
                    var entry = it.next();
                    var ingredient = entry.getKey();
                    long needAmount = entry.getLongValue();

                    var bestMatch = cacheManager.getBestItemMatch(ingredient, itemInventory, needAmount);
                    if (bestMatch != null) {
                        long amount = itemInventory.getLong(bestMatch);
                        long except = amount - needAmount;
                        if (except <= 0) {
                            itemInventory.removeLong(bestMatch);
                        } else {
                            itemInventory.put(bestMatch, except);
                        }
                        it.remove();
                    }
                }
            }

            return true;
        }

        public boolean handleFluidInternal(Object2LongMap<FluidIngredient> left, boolean simulate) {
            if (left.isEmpty()) return true;

            for (var it = Object2LongMaps.fastIterator(left); it.hasNext();) {
                var entry = it.next();
                var ingredient = entry.getKey();
                long needAmount = entry.getLongValue();
                if (needAmount <= 0) {
                    it.remove();
                    continue;
                }

                AEFluidKey bestMatch = simulate ? cacheManager.getBestFluidMatchSimulate(ingredient, fluidInventory, fluidCatalystInventory, needAmount) :
                        cacheManager.getBestFluidMatch(ingredient, fluidInventory, needAmount);
                if (bestMatch == null) {
                    return false;
                }
            }

            if (!simulate) {
                for (var it = Object2LongMaps.fastIterator(left); it.hasNext();) {
                    var entry = it.next();
                    var ingredient = entry.getKey();
                    long needAmount = entry.getLongValue();

                    AEFluidKey bestMatch = cacheManager.getBestFluidMatch(ingredient, fluidInventory, needAmount);
                    if (bestMatch != null) {
                        long amount = fluidInventory.getLong(bestMatch);
                        long except = amount - needAmount;
                        if (except <= 0) {
                            fluidInventory.removeLong(bestMatch);
                        } else {
                            fluidInventory.put(bestMatch, except);
                        }
                        it.remove();
                    }
                }
            }

            return true;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();

            ListTag itemsTag = AEUtils.createListTag(AEItemKey::toTag, itemInventory);
            if (!itemsTag.isEmpty()) tag.put("inventory", itemsTag);

            ListTag itemCatalystTag = AEUtils.createListTag(AEItemKey::toTag, itemCatalystInventory);
            if (!itemCatalystTag.isEmpty()) tag.put("catalystInventory", itemCatalystTag);

            ListTag fluidsTag = AEUtils.createListTag(AEFluidKey::toTag, fluidInventory);
            if (!fluidsTag.isEmpty()) tag.put("fluidInventory", fluidsTag);

            ListTag fluidCatalystTag = AEUtils.createListTag(AEFluidKey::toTag, fluidCatalystInventory);
            if (!fluidCatalystTag.isEmpty()) tag.put("catalystFluidInventory", fluidCatalystTag);

            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            itemInventory.clear();
            itemCatalystInventory.clear();
            fluidInventory.clear();
            fluidCatalystInventory.clear();

            ListTag items = tag.getList("inventory", Tag.TAG_COMPOUND);
            AEUtils.loadInventory(items, AEItemKey::fromTag, itemInventory);

            ListTag catalystItems = tag.getList("catalystInventory", Tag.TAG_COMPOUND);
            AEUtils.loadInventory(catalystItems, AEItemKey::fromTag, itemCatalystInventory);

            ListTag fluids = tag.getList("fluidInventory", Tag.TAG_COMPOUND);
            AEUtils.loadInventory(fluids, AEFluidKey::fromTag, fluidInventory);

            ListTag catalystFluids = tag.getList("catalystFluidInventory", Tag.TAG_COMPOUND);
            AEUtils.loadInventory(catalystFluids, AEFluidKey::fromTag, fluidCatalystInventory);
        }
    }

    protected class Ticker implements IGridTickable {

        @Override
        public TickingRequest getTickingRequest(IGridNode node) {
            return new TickingRequest(MEExtendedOutputPartMachineBase.MIN_FREQUENCY, MEExtendedOutputPartMachineBase.MAX_FREQUENCY, false, true);
        }

        @Override
        public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
            if (!getMainNode().isActive()) {
                return TickRateModulation.SLEEP;
            }

            if (buffer.isEmpty()) {
                if (ticksSinceLastCall >= MEExtendedOutputPartMachineBase.MAX_FREQUENCY) {
                    isSleeping = true;
                    return TickRateModulation.SLEEP;
                } else return TickRateModulation.SLOWER;
            } else return AEUtils.reFunds(buffer, getMainNode().getGrid(), actionSource) ? TickRateModulation.URGENT : TickRateModulation.SLOWER;
        }
    }

    protected class MEPatternTrait extends MEIOTrait implements IMEPatternTrait {

        public MEPatternTrait(MEPatternBufferPartMachine machine) {
            super(machine);
        }

        @Override
        public MEPatternBufferPartMachine getMachine() {
            return (MEPatternBufferPartMachine) machine;
        }

        @Override
        public @NotNull ObjectSet<@NotNull GTRecipe> getCachedGTRecipe() {
            ObjectSet<GTRecipe> recipes = new ObjectOpenHashSet<>();
            for (var it = Int2ReferenceMaps.fastIterator(recipeMultipleCacheMap); it.hasNext();) {
                var entry = it.next();
                var recipeSet = entry.getValue();
                int slot = entry.getIntKey();
                if (recipeSet.isEmpty()) it.remove();
                else if (cacheRecipe[slot] && internalInventory[slot].isActive()) recipes.addAll(recipeSet);
            }
            return recipes;
        }

        @Override
        public void setSlotCacheRecipe(int index, GTRecipe recipe) {
            if (recipe != null && recipe.recipeType != GTRecipeTypes.DUMMY_RECIPES) {
                var set = recipeMultipleCacheMap.computeIfAbsent(index, integer -> new ObjectArraySet<>());
                if (set.add(recipe)) cacheRecipe[index] = set.size() >= cacheRecipeCount[index];
            }
        }

        @Override
        public @NotNull Int2ReferenceMap<ObjectSet<@NotNull GTRecipe>> getSlot2RecipesCache() {
            return recipeMultipleCacheMap;
        }

        @Override
        public void setOnPatternChange(IntConsumer removeMapOnSlot) {
            removeSlotFromMap = removeMapOnSlot;
        }

        @Override
        public boolean hasCacheInSlot(int slot) {
            return cacheRecipe[slot];
        }
    }
}
