package org.NJ.hwamaihelper.client;

import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.interfaces.IInitializationHandler;
import fi.dy.masa.malilib.registry.Registry;
import fi.dy.masa.malilib.util.data.ModInfo;
import net.minecraft.network.chat.Component;
import org.NJ.hwamaihelper.client.logic.MalilibInputHandler;
import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;

public final class HwamaiInitHandler implements IInitializationHandler {
    public static final String MOD_ID = "senyouhelper";

    @Override
    public void registerModHandlers() {
        ConfigManager.getInstance().registerConfigHandler(MOD_ID, HwamaiMalilibConfig.INSTANCE);

        MalilibInputHandler inputHandler = MalilibInputHandler.getInstance();
        inputHandler.initialize();
        InputEventHandler.getKeybindManager().registerKeybindProvider(inputHandler);
        inputHandler.markRegistered();

        Registry.CONFIG_SCREEN.registerConfigScreenFactory(new ModInfo(
                MOD_ID,
                "SenyouHelper",
                () -> new NJMainScreen(Component.literal("moris`Helper`"))));
    }
}
