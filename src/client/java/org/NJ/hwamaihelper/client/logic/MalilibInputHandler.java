package org.NJ.hwamaihelper.client.logic;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.hotkeys.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.NJ.hwamaihelper.client.NJMainScreen;
import org.NJ.hwamaihelper.client.screens.*;
import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;
public final class MalilibInputHandler implements IKeybindProvider {
 private static final MalilibInputHandler INSTANCE = new MalilibInputHandler();
 public static MalilibInputHandler getInstance() { return INSTANCE; }
 public void initialize() {
  HwamaiMalilibConfig.OPEN_MENU.getKeybind().setCallback((a,k)->open(0));
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(mc -> {
   boolean inspect=org.NJ.hwamaihelper.client.InspectHotkey.pressed(com.mojang.blaze3d.platform.InputConstants::isKeyDown);
   if(HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()&&inspect&&!inspectWasDown&&mc.player!=null&&mc.player.isAlive()){if(mc.gui.screen() instanceof org.NJ.hwamaihelper.client.PlayerInspectScreen view)view.onClose();else if(mc.gui.screen()==null&&mc.getCameraEntity()==mc.player)mc.gui.setScreen(new org.NJ.hwamaihelper.client.PlayerInspectScreen());}
   inspectWasDown=inspect;
   boolean down = altDown();
   if (down && !altWasDown) open(2);
   altWasDown = down;
  });
 }
 private static boolean altWasDown,inspectWasDown;
 public static boolean altDown() {
  var keys=HwamaiMalilibConfig.WHEEL_KEY.getKeybind().getKeys();return !keys.isEmpty()&&keys.stream().allMatch(fi.dy.masa.malilib.hotkeys.KeybindMulti::isKeyDown);
 }
 private static boolean open(int type) {
  Minecraft mc=Minecraft.getInstance();
  if(mc.player==null || mc.gui.screen()!=null) return false;
  if(type!=0&&!GameModeWheelScreen.canUse(mc))return false;
  GuiBase.openGui(switch(type) { case 0 -> new NJMainScreen(Component.literal("moris`Helper`")); default -> new GameModeWheelScreen(); });
  return true;
 }
 public void markRegistered() { InputEventHandler.getKeybindManager().updateUsedKeys(); }
 public void addKeysToMap(IKeybindManager m) { for(IHotkey k:HwamaiMalilibConfig.HOTKEY_OPTIONS) m.addKeybindToMap(k.getKeybind()); }
 public void addHotkeys(IKeybindManager m) { m.addHotkeysForCategory("senyouhelper","moris`Helper`",HwamaiMalilibConfig.HOTKEY_OPTIONS); }
}