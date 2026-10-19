package org.NJ.hwamaihelper.client;
import net.fabricmc.api.ClientModInitializer;
import fi.dy.masa.malilib.event.InitializationHandler;
public class HwamaihelperClient implements ClientModInitializer {
 public void onInitializeClient() { DiscordPresence.initialize(); DiscordFeed.initialize(); ContainerPreview.initialize(); TeleportVisualEffect.initialize(); net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->client.execute(ChatImages::clear)); InitializationHandler.getInstance().registerInitializationHandler(new HwamaiInitHandler()); }
}
