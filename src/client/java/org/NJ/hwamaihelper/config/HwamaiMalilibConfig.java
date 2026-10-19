package org.NJ.hwamaihelper.config;
import com.google.gson.*;
import fi.dy.masa.malilib.config.*;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.hotkeys.KeybindSettings;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.*;
import java.io.*;
import java.util.*;
public final class HwamaiMalilibConfig implements IConfigHandler {
 public static final HwamaiMalilibConfig INSTANCE = new HwamaiMalilibConfig();
 private static Path path(){return FabricLoader.getInstance().getConfigDir().resolve("senyouhelper.json");}
 public static final ConfigHotkey OPEN_MENU = new ConfigHotkey("開啟moris`Helper`", "X,F", KeybindSettings.RELEASE);
 public static final ConfigHotkey CONTAINER_PREVIEW = new ConfigHotkey("容器預覽", "RIGHT_ALT", KeybindSettings.PRESS_ALLOWEXTRA);
 public static final ConfigHotkey WHEEL_KEY = new ConfigHotkey("開啟模式輪盤", "LEFT_ALT", KeybindSettings.PRESS_ALLOWEXTRA);
 public static final ConfigBoolean HELPER_ENABLED=new ConfigBoolean("啟用小幫手",true,"關閉功能快捷鍵與傳送特效，X+F 仍可開啟設定；聊天欄按鈕保留。");
 public static final ConfigBoolean TELEPORT_EFFECT=new ConfigBoolean("GrandTeleport 傳送特效",true,"分段拉高與拉近鏡頭；原生音效音量 0.2。");
 public static final ConfigInteger WHEEL_RADIUS=new ConfigInteger("輪盤半徑",76,48,110);
 public static final ConfigInteger WHEEL_X=new ConfigInteger("輪盤水平偏移",0,-600,600);
 public static final ConfigInteger WHEEL_Y=new ConfigInteger("輪盤垂直偏移",0,-400,400);
 public static final List<ConfigHotkey> HOTKEY_OPTIONS = List.of(OPEN_MENU,CONTAINER_PREVIEW,WHEEL_KEY);
 public static final List<IConfigBase> GENERAL_OPTIONS=List.of(HELPER_ENABLED,TELEPORT_EFFECT);
 public static final List<IConfigBase> WHEEL_OPTIONS=List.of(WHEEL_KEY,WHEEL_RADIUS,WHEEL_X,WHEEL_Y);

 public void load() {
  Path PATH=path();
  if (!Files.isRegularFile(PATH)) { save(); return; }
  try (Reader r=Files.newBufferedReader(PATH)) {
   JsonObject root=JsonParser.parseReader(r).getAsJsonObject();
 
   ConfigUtils.readConfigBase(root,"Hotkeys",HOTKEY_OPTIONS);
   ConfigUtils.readConfigBase(root,"General",GENERAL_OPTIONS);ConfigUtils.readConfigBase(root,"Wheel",List.of(WHEEL_RADIUS,WHEEL_X,WHEEL_Y));
  } catch(Exception e) { System.err.println("[SenyouHelper] 無法讀取設定: " + e); }
 }
 public void save() {
  Path PATH=path();
  JsonObject root=new JsonObject();

  ConfigUtils.writeConfigBase(root,"Hotkeys",HOTKEY_OPTIONS);
  ConfigUtils.writeConfigBase(root,"General",GENERAL_OPTIONS);ConfigUtils.writeConfigBase(root,"Wheel",List.of(WHEEL_RADIUS,WHEEL_X,WHEEL_Y));
  try { Files.createDirectories(PATH.getParent()); try(Writer w=Files.newBufferedWriter(PATH)) { new GsonBuilder().setPrettyPrinting().create().toJson(root,w); } }
  catch(IOException e) { System.err.println("[SenyouHelper] 無法儲存設定: " + e); }
 }
 public void onConfigsChanged() { save(); }
}