package org.gtlcore.gtlcore.mixin.ae2.gui;

import org.gtlcore.gtlcore.client.gui.PatterEncodingTermMenuModify;

import com.gregtechceu.gtceu.common.data.GTItems;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.config.Actionable;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.ITerminalHost;
import appeng.core.definitions.AEItems;
import appeng.helpers.IMenuCraftingPacket;
import appeng.helpers.IPatternTerminalMenuHost;
import appeng.menu.me.common.MEStorageMenu;
import appeng.menu.me.items.PatternEncodingTermMenu;
import appeng.menu.slot.RestrictedInputSlot;
import appeng.util.ConfigInventory;
import com.google.common.math.LongMath;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author EasterFG on 2024/9/12
 */
@Mixin(value = PatternEncodingTermMenu.class, priority = 900)
public abstract class PatternEncodingTermMenuMixin extends MEStorageMenu implements IMenuCraftingPacket, PatterEncodingTermMenuModify {

    @Unique
    private static final ItemStack Pattern = AEItems.BLANK_PATTERN.stack();

    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedInputsInv;
    @Shadow(remap = false)
    @Final
    private ConfigInventory encodedOutputsInv;
    @Shadow(remap = false)
    @Final
    private RestrictedInputSlot blankPatternSlot;
    @Shadow(remap = false)
    @Final
    private RestrictedInputSlot encodedPatternSlot;

    @Shadow(remap = false)
    protected abstract @Nullable ItemStack encodePattern();

    @Shadow(remap = false)
    protected abstract boolean isPattern(ItemStack output);

    @Shadow(remap = false)
    protected abstract void clearPattern();

    public PatternEncodingTermMenuMixin(MenuType<?> menuType, int id, Inventory ip, ITerminalHost host) {
        super(menuType, id, ip, host);
    }

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/MenuType;ILnet/minecraft/world/entity/player/Inventory;Lappeng/helpers/IPatternTerminalMenuHost;Z)V",
            at = @At("TAIL"),
            remap = false)
    public void initHooks(MenuType<?> menuType, int id, Inventory ip, IPatternTerminalMenuHost host, boolean bindInventory, CallbackInfo ci) {
        this.registerClientAction("modifyPatter", Integer.class,
                this::gTLCore$modifyPatter);
    }

    /**
     * @author .
     * @reason 样板不足时自动填充(如果库存有)
     */
    @Overwrite(remap = false)
    public void encode() {
        if (isClientSide()) {
            sendClientAction("encode");
            return;
        }

        ItemStack encodedPattern = encodePattern();
        if (encodedPattern != null) {
            var encodeOutput = this.encodedPatternSlot.getItem();

            // first check the output slots, should either be null, or a pattern (encoded or otherwise)
            if (!encodeOutput.isEmpty() && !PatternDetailsHelper.isEncodedPattern(encodeOutput) && !AEItems.BLANK_PATTERN.isSameAs(encodeOutput)) {
                return;
            } // if nothing is there we should snag a new pattern.
            else if (encodeOutput.isEmpty()) {
                var blankPattern = this.blankPatternSlot.getItem();
                if (!isPattern(blankPattern)) {
                    return; // no blanks.
                }

                // remove one, and clear the input slot.
                blankPattern.shrink(1);
                if (blankPattern.getCount() <= 0) {
                    if (this.storage != null) {
                        long extract = this.storage.extract(AEItemKey.of(Pattern), 64, Actionable.SIMULATE, this.getActionSource());
                        if (extract > 0) {
                            extract = this.storage.extract(AEItemKey.of(Pattern), extract, Actionable.MODULATE, this.getActionSource());
                            this.blankPatternSlot.set(Pattern.copyWithCount((int) extract));
                        }
                    } else this.blankPatternSlot.set(ItemStack.EMPTY);
                }
            }

            this.encodedPatternSlot.set(encodedPattern);
        } else {
            clearPattern();
        }
    }

    @Redirect(method = "encodeProcessingPattern",
              at = @At(value = "INVOKE",
                       target = "Lappeng/util/ConfigInventory;getStack(I)Lappeng/api/stacks/GenericStack;",
                       ordinal = 0),
              remap = false)
    private GenericStack filterData(ConfigInventory instance, int slot) {
        var stack = instance.getStack(slot);
        if (stack != null && stack.what() instanceof AEItemKey aeItemKey) {
            if (aeItemKey.hasTag() &&
                    (aeItemKey.getItem() == GTItems.TOOL_DATA_STICK.asItem() ||
                            aeItemKey.getItem() == GTItems.TOOL_DATA_ORB.asItem() ||
                            aeItemKey.getItem() == GTItems.TOOL_DATA_MODULE.asItem()))
                return new GenericStack(AEItemKey.of(aeItemKey.getItem()), stack.amount());
        }
        return stack;
    }

    @Override
    public void gTLCore$modifyPatter(Integer data) {
        if (this.isClientSide()) {
            this.sendClientAction("modifyPatter", data);
        } else {
            // modify
            var output = gTLCore$valid(this.encodedOutputsInv, data);
            if (output == null) {
                return;
            }
            var input = gTLCore$valid(this.encodedInputsInv, data);
            if (input == null) {
                return;
            }
            for (int slot = 0; slot < output.length; ++slot) {
                if (output[slot] != null) {
                    this.encodedOutputsInv.setStack(slot, output[slot]);
                }
            }
            for (int slot = 0; slot < input.length; ++slot) {
                if (input[slot] != null) {
                    this.encodedInputsInv.setStack(slot, input[slot]);
                }
            }
        }
    }

    @Unique
    private GenericStack[] gTLCore$valid(ConfigInventory inv, int data) {
        // data 错误的被修改为正数, 在有多个多个材料时
        boolean flag = data > 0;
        if (!flag) {
            data = -data;
        }
        GenericStack[] result = new GenericStack[inv.size()];
        for (int slot = 0; slot < inv.size(); ++slot) {
            GenericStack stack = inv.getStack(slot);
            if (stack != null) {
                if (flag) {
                    long modify = LongMath.saturatedMultiply(data, stack.amount());
                    if (modify == Long.MAX_VALUE || modify == Long.MIN_VALUE) {
                        return null;
                    } else {
                        result[slot] = new GenericStack(stack.what(), modify);
                    }
                } else {
                    if (stack.amount() % data != 0) {
                        return null;
                    } else {
                        // 除尽
                        result[slot] = new GenericStack(stack.what(), stack.amount() / data);
                    }
                }
            }
        }
        return result;
    }
}
