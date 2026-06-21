package org.gtlcore.gtlcore.forge;

import org.gtlcore.gtlcore.GTLCore;
import org.gtlcore.gtlcore.integration.ae2.async.AEWriteService;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GTLCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.DEDICATED_SERVER)
public class ForgeServerEventListener {

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent e) {
        AEWriteService.INSTANCE.shutDownGracefully();
    }
}
