package org.gtlcore.gtlcore.mixin.gtm;

import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoWrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.util.thread.BlockableEventLoop;

import mezz.jei.api.registration.IRecipeRegistration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

import static com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory.RECIPE_TYPE;

/**
 * @author EasterFG on 2024/10/28
 */
@Mixin(MultiblockInfoCategory.class)
public class MultiblockInfoCategoryMixin {

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true, remap = false)
    private static void registerRecipes(IRecipeRegistration registry, @NotNull CallbackInfo ci) {
        BlockableEventLoop<?> executor = Minecraft.getInstance();
        CompletableFuture.supplyAsync(() -> GTRegistries.MACHINES.values().stream()
                .filter(MultiblockMachineDefinition.class::isInstance)
                .map(MultiblockMachineDefinition.class::cast)
                .filter(MultiblockMachineDefinition::isRenderXEIPreview)
                .map(MultiblockInfoWrapper::new)
                .toList(), executor).thenAcceptAsync(recipes -> {
                    registry.addRecipes(RECIPE_TYPE, recipes);
                }, executor);
        ci.cancel();
    }
}
