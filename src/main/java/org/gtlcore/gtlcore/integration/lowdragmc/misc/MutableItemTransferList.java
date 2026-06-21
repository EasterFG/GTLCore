package org.gtlcore.gtlcore.integration.lowdragmc.misc;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;

/**
 * 可变的 ItemTransferList，但支持动态添加/删除IItemTransfer，提供统一的物品操作接口
 */
public class MutableItemTransferList implements IItemTransfer, ITagSerializable<CompoundTag>, IContentChangeAware {

    @Getter
    @Setter
    private Runnable onContentsChanged = () -> {};
    private final ObjectArrayList<@NotNull IItemTransfer> transfers = new ObjectArrayList<>();
    private volatile int totalSlots = 0;
    private volatile boolean slotsCacheDirty = true;
    @Getter
    protected Predicate<ItemStack> filter = item -> true;

    public MutableItemTransferList() {}

    public MutableItemTransferList(int initialCapacity) {
        transfers.ensureCapacity(initialCapacity);
    }

    public MutableItemTransferList(Set<IItemTransfer> initialTransfers) {
        if (initialTransfers != null && !initialTransfers.isEmpty()) {
            transfers.addAll(initialTransfers);
            invalidateSlotsCache();
        }
    }

    // ==================== IItemTransfer Change ====================

    /**
     * 添加一个IItemTransfer
     * 
     * @param transfer 要添加的传输对象
     * @return 是否成功添加
     */
    public boolean addTransfer(@Nullable IItemTransfer transfer) {
        if (transfer == null) return false;
        boolean added = transfers.add(transfer);
        if (added) {
            invalidateSlotsCache();
        }
        return added;
    }

    /**
     * 添加多个IItemTransfer
     * 
     * @param transfers 要添加的传输对象列表
     * @return 是否成功添加
     */
    public boolean addTransfers(@Nullable Set<IItemTransfer> transfers) {
        if (transfers == null) return false;
        boolean added = this.transfers.addAll(transfers);
        if (added) {
            invalidateSlotsCache();
        }
        return added;
    }

    /**
     * 在指定位置插入IItemTransfer
     * 
     * @param index    插入位置
     * @param transfer 要插入的传输对象
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public void addTransfer(int index, @NotNull IItemTransfer transfer) {
        transfers.add(index, transfer);
        invalidateSlotsCache();
    }

    /**
     * 移除指定的IItemTransfer
     * 
     * @param transfer 要移除的传输对象
     * @return 是否成功移除
     */
    public boolean removeTransfer(@Nullable IItemTransfer transfer) {
        boolean removed = transfers.remove(transfer);
        if (removed) {
            invalidateSlotsCache();
        }
        return removed;
    }

    /**
     * 移除指定位置的IItemTransfer
     * 
     * @param index 要移除的位置
     * @return 被移除的传输对象
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public IItemTransfer removeTransfer(int index) {
        IItemTransfer removed = transfers.remove(index);
        invalidateSlotsCache();
        return removed;
    }

    /**
     * 移除所有满足条件的IItemTransfer
     * 
     * @param filter 过滤条件
     * @return 是否有移除操作
     */
    public boolean removeTransfersIf(@NotNull Predicate<IItemTransfer> filter) {
        boolean removed = transfers.removeIf(filter);
        if (removed) {
            invalidateSlotsCache();
        }
        return removed;
    }

    /**
     * 清空所有IItemTransfer
     */
    public void clear() {
        if (!transfers.isEmpty()) {
            transfers.clear();
            totalSlots = 0;
            slotsCacheDirty = false;
        }
    }

    /**
     * 获取传输对象的数量
     * 
     * @return 传输对象数量
     */
    public int getTransferCount() {
        return transfers.size();
    }

    /**
     * 检查是否为空
     * 
     * @return 是否为空
     */
    public boolean isEmpty() {
        return transfers.isEmpty();
    }

    // ==================== IItemTransfer Interface ====================

    @Override
    public int getSlots() {
        if (slotsCacheDirty) {
            updateSlotsCache();
        }
        return totalSlots;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        if (transfers.isEmpty()) {
            return ItemStack.EMPTY;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        return pair != null ? pair.transfer.getStackInSlot(pair.localSlot) : ItemStack.EMPTY;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (transfers.isEmpty()) {
            return;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        if (pair != null) {
            pair.transfer.setStackInSlot(pair.localSlot, stack);
        }
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (transfers.isEmpty() || stack.isEmpty() || !filter.test(stack)) {
            return stack;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        return pair != null ?
                pair.transfer.insertItem(pair.localSlot, stack, simulate, notifyChanges) :
                stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (transfers.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        return pair != null ?
                pair.transfer.extractItem(pair.localSlot, amount, simulate, notifyChanges) :
                ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (transfers.isEmpty()) {
            return 0;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        return pair != null ? pair.transfer.getSlotLimit(pair.localSlot) : 0;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        if (transfers.isEmpty() || !filter.test(stack)) {
            return false;
        }

        TransferSlotPair pair = findTransferForSlot(slot);
        return pair != null && pair.transfer.isItemValid(pair.localSlot, stack);
    }

    @Override
    public void onContentsChanged() {
        for (IItemTransfer transfer : transfers) {
            transfer.onContentsChanged();
        }
    }

    @SuppressWarnings("all")
    @NotNull
    @Override
    public Object createSnapshot() {
        return transfers.stream().map(IItemTransfer::createSnapshot).toArray(Object[]::new);
    }

    @SuppressWarnings("all")
    @Override
    public void restoreFromSnapshot(Object snapshot) {
        if (snapshot instanceof Object[] array && array.length == transfers.size()) {
            for (int i = 0; i < array.length; i++) {
                IItemTransfer transfer = transfers.get(i);
                if (array[i] != null) {
                    transfer.restoreFromSnapshot(array[i]);
                }
            }
        }
    }

    // ==================== Filter ====================

    /**
     * 设置物品过滤器
     * 
     * @param filter 过滤器函数
     */
    public void setFilter(@NotNull Predicate<ItemStack> filter) {
        this.filter = filter;
    }

    // ==================== Utils ====================

    private void updateSlotsCache() {
        int slots = 0;
        for (IItemTransfer transfer : transfers) {
            slots += transfer.getSlots();
        }
        totalSlots = slots;
        slotsCacheDirty = false;
    }

    private void invalidateSlotsCache() {
        slotsCacheDirty = true;
    }

    /**
     * 查找指定槽位对应的传输对象和本地槽位
     * 
     * @param globalSlot 全局槽位索引
     * @return 传输对象和本地槽位的配对，如果未找到返回null
     */
    @Nullable
    private TransferSlotPair findTransferForSlot(int globalSlot) {
        int index = 0;
        for (IItemTransfer transfer : transfers) {
            if (globalSlot - index < transfer.getSlots()) {
                return new TransferSlotPair(transfer, globalSlot - index);
            }
            index += transfer.getSlots();
        }
        return null;
    }

    protected record TransferSlotPair(IItemTransfer transfer, int localSlot) {}

    // ==================== Factory ====================

    /**
     * 创建空的ItemTransferList
     * 
     * @return 空的列表实例
     */
    public static MutableItemTransferList empty() {
        return new MutableItemTransferList();
    }

    /**
     * 从单个传输对象创建列表
     * 
     * @param transfer 传输对象
     * @return 包含单个传输对象的列表
     */
    public static MutableItemTransferList of(@NotNull IItemTransfer transfer) {
        MutableItemTransferList list = new MutableItemTransferList(1);
        list.addTransfer(transfer);
        return list;
    }

    /**
     * 从多个传输对象创建列表
     * 
     * @param transfers 传输对象数组
     * @return 包含所有传输对象的列表
     */
    public static MutableItemTransferList of(@NotNull IItemTransfer... transfers) {
        MutableItemTransferList list = new MutableItemTransferList(transfers.length);
        for (IItemTransfer transfer : transfers) {
            list.addTransfer(transfer);
        }
        return list;
    }

    // ==================== ITagSerializable ====================

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var list = new ListTag();
        for (var transfer : transfers) {
            if (transfer instanceof ItemStackTransfer serializable) {
                list.add(serializable.serializeNBT());
            } else {
                LDLib.LOGGER.warn("[ItemTransferList] internal container doesn't support serialization");
            }
        }
        tag.put("slots", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        var list = nbt.getList("slots", Tag.TAG_COMPOUND);
        transfers.clear();
        for (Tag tag : list) {
            if (tag instanceof CompoundTag compoundTag) {
                var itemStackTransfer = new ItemStackTransfer();
                itemStackTransfer.deserializeNBT(compoundTag);
                this.addTransfer(itemStackTransfer);
            } else {
                LDLib.LOGGER.warn("[ItemTransferList] internal container doesn't support serialization");
            }
        }
    }

    // ==================== Object ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MutableItemTransferList other)) return false;
        return transfers.equals(other.transfers);
    }

    @Override
    public int hashCode() {
        return transfers.hashCode();
    }
}
