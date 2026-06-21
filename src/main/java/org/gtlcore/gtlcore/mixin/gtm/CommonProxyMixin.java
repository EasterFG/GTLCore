package org.gtlcore.gtlcore.mixin.gtm;

import org.gtlcore.gtlcore.api.recipe.ingredient.LongIngredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.*;
import com.gregtechceu.gtceu.common.CommonProxy;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CommonProxy.class)
public class CommonProxyMixin {

    /**
     * @author .
     * @reason .
     */
    @SubscribeEvent
    @Overwrite(remap = false)
    public void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CraftingHelper.register(SizedIngredient.TYPE, SizedIngredient.SERIALIZER);
            CraftingHelper.register(LongIngredient.TYPE, LongIngredient.SERIALIZER);
            CraftingHelper.register(IntCircuitIngredient.TYPE, IntCircuitIngredient.SERIALIZER);
            CraftingHelper.register(IntProviderIngredient.TYPE, IntProviderIngredient.SERIALIZER);
        });
    }
}
