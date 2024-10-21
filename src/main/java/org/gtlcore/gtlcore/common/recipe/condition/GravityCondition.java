package org.gtlcore.gtlcore.common.recipe.condition;

import org.gtlcore.gtlcore.common.data.GTLRecipeConditions;
import org.gtlcore.gtlcore.common.machine.multiblock.part.maintenance.IGravityPartMachine;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor
public class GravityCondition extends RecipeCondition {

    public static final Codec<GravityCondition> CODEC = RecordCodecBuilder
            .create(instance -> RecipeCondition.isReverse(instance)
                    .and(Codec.BOOL.fieldOf("gravity").forGetter(val -> val.zero))
                    .apply(instance, GravityCondition::new));

    public final static GravityCondition INSTANCE = new GravityCondition();

    private boolean zero = false;

    public GravityCondition(boolean zero) {
        this.zero = zero;
    }

    public GravityCondition(boolean isReverse, boolean zero) {
        super(isReverse);
        this.zero = zero;
    }

    @Override
    public RecipeConditionType<?> getType() {
        return GTLRecipeConditions.GRAVITY;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("gtlcore.condition." + (zero ? "zero_" : "") + "gravity");
    }

    @Override
    public boolean test(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        MetaMachine machine = recipeLogic.getMachine();
        if (machine instanceof MultiblockControllerMachine controllerMachine) {
            for (IMultiPart part : controllerMachine.self().getParts()) {
                if (part instanceof IGravityPartMachine gravityPart) {
                    return gravityPart.getCurrentGravity() == (zero ? 0 : 100);
                }
            }
        }
        Level level = Objects.requireNonNull(recipeLogic.getMachine().getLevel());
        return level.kjs$getDimension().toString().contains("_orbit") && zero;
    }

    @Override
    public RecipeCondition createTemplate() {
        return new GravityCondition();
    }

    @NotNull
    @Override
    public JsonObject serialize() {
        JsonObject config = super.serialize();
        config.addProperty("gravity", zero);
        return config;
    }

    @Override
    public RecipeCondition deserialize(@NotNull JsonObject config) {
        super.deserialize(config);
        zero = GsonHelper.getAsBoolean(config, "gravity", false);
        return this;
    }

    @Override
    public RecipeCondition fromNetwork(FriendlyByteBuf buf) {
        super.fromNetwork(buf);
        zero = buf.readBoolean();
        return this;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf) {
        super.toNetwork(buf);
        buf.writeBoolean(zero);
    }
}
