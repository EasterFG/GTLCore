package org.gtlcore.gtlcore.api.recipe.ingredient;

import org.gtlcore.gtlcore.mixin.gtm.recipe.Ingredient.IntProviderIngredientAccessor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.core.mixins.*;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import com.google.common.primitives.Ints;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class LongIngredient extends SizedIngredient {

    public static final ResourceLocation TYPE = GTCEu.id("Long");

    @Getter
    protected long actualAmount;
    private int hashCode = 0;
    private boolean changed = true;
    @Getter
    private final boolean isEmpty;
    private final Value value;

    protected LongIngredient(Ingredient inner, long actualAmount) {
        super(inner, Ints.saturatedCast(actualAmount));
        this.actualAmount = actualAmount;
        this.isEmpty = inner.isEmpty();
        if (isEmpty || inner.getClass() != Ingredient.class) {
            this.value = null;
        } else {
            var values = ((IngredientAccessor) inner).getValues();
            this.value = values.length == 1 ? values[0] : null;
        }
    }

    protected LongIngredient(@NotNull TagKey<Item> tag, long actualAmount) {
        this(Ingredient.of(tag), actualAmount);
    }

    protected LongIngredient(ItemStack itemStack, long actualAmount) {
        this(itemStack.hasTag() ? NBTIngredient.createNBTIngredient(itemStack) : Ingredient.of(itemStack), actualAmount);
    }

    public static LongIngredient create(Ingredient inner, long amount) {
        return new LongIngredient(inner, amount);
    }

    public static LongIngredient create(Ingredient inner) {
        return new LongIngredient(inner, 1);
    }

    public static Ingredient copy(Ingredient ingredient) {
        if (ingredient instanceof LongIngredient longIngredient) {
            var copy = LongIngredient.create(longIngredient.inner, longIngredient.actualAmount);
            copy.hashCode = longIngredient.hashCode;
            return copy;
        } else if (ingredient instanceof SizedIngredient sizedIngredient) {
            if (sizedIngredient.getInner() instanceof IntProviderIngredient intProviderIngredient) {
                return copy(intProviderIngredient);
            }
            return LongIngredient.create(sizedIngredient.getInner(), (long) sizedIngredient.getAmount());
        } else if (ingredient instanceof IntCircuitIngredient circuit) {
            return circuit.copy();
        } else if (ingredient instanceof IntProviderIngredient intProviderIngredient) {
            IntProviderIngredient copied = new IntProviderIngredient(intProviderIngredient.getInner(), intProviderIngredient.getCountProvider());
            final var accessor = (IntProviderIngredientAccessor) intProviderIngredient;
            if (accessor.getItemStack() != null) {
                copied.setItemStacks(Arrays.stream(accessor.getItemStack()).map(ItemStack::copy)
                        .toArray(ItemStack[]::new));
            }
            if (accessor.getSampledCount() != -1) {
                copied.setSampledCount(accessor.getSampledCount());
            }
            return copied;
        } else {
            return create(ingredient);
        }
    }

    @Override
    @NotNull
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return SERIALIZER;
    }

    public static SizedIngredient fromJson(JsonObject json) {
        return SERIALIZER.parse(json);
    }

    @Override
    public @NotNull JsonElement toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("type", TYPE.toString());
        json.addProperty("actualAmount", this.actualAmount);
        json.add("ingredient", this.inner.toJson());
        return json;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null) return false;
        if (this.isEmpty) return stack.isEmpty();

        if (this.value instanceof TagValueAccessor tagValue) {
            return stack.is(tagValue.getTag());
        } else if (this.value instanceof ItemValueAccessor itemValue) {
            return ItemStack.isSameItem(stack, itemValue.getItem());
        }
        return this.inner.test(stack);
    }

    @Override
    public ItemStack @NotNull [] getItems() {
        if (getInner() instanceof IntProviderIngredient intProviderIngredient) {
            return intProviderIngredient.getItems();
        }
        if (changed || itemStacks == null) {
            if (isEmpty) return new ItemStack[0];
            var items = new ObjectArrayList<ItemStack>(inner.getItems().length);
            for (ItemStack item : this.inner.getItems()) {
                items.add(item.copyWithCount(Ints.saturatedCast(actualAmount)));
            }
            itemStacks = items.toArray(new ItemStack[0]);
            changed = false;
        }
        return itemStacks;
    }

    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = Objects.hash(this.actualAmount, Objects.hashCode(this.inner));
        }
        return this.hashCode;
    }

    public void setActualAmount(long actualAmount) {
        this.actualAmount = actualAmount;
        this.changed = true;
    }

    public static final IIngredientSerializer<LongIngredient> SERIALIZER = new IIngredientSerializer<>() {

        @Override
        public @NotNull LongIngredient parse(FriendlyByteBuf buffer) {
            long amount = buffer.readVarLong();
            return new LongIngredient(Ingredient.fromNetwork(buffer), amount);
        }

        @Override
        public @NotNull LongIngredient parse(JsonObject json) {
            long amount = json.get("actualAmount").getAsLong();
            Ingredient inner = Ingredient.fromJson(json.get("ingredient"));
            return new LongIngredient(inner, amount);
        }

        @Override
        public void write(FriendlyByteBuf buffer, LongIngredient ingredient) {
            buffer.writeVarLong(ingredient.getActualAmount());
            ingredient.inner.toNetwork(buffer);
        }
    };
}
