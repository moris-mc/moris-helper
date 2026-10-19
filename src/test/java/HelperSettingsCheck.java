import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;
import fi.dy.masa.malilib.config.ConfigUtils;
import com.google.gson.JsonObject;
public class HelperSettingsCheck {
 public static void main(String[] args){
  net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  var cfg=HwamaiMalilibConfig.INSTANCE;
  if(!HwamaiMalilibConfig.WHEEL_KEY.getKeybind().getKeys().equals(java.util.List.of(com.mojang.blaze3d.platform.InputConstants.KEY_LALT)))throw new AssertionError("Wheel default not SDL left Alt");
  HwamaiMalilibConfig.HELPER_ENABLED.setBooleanValue(false);HwamaiMalilibConfig.TELEPORT_EFFECT.setBooleanValue(false);HwamaiMalilibConfig.WHEEL_RADIUS.setIntegerValue(98);HwamaiMalilibConfig.WHEEL_X.setIntegerValue(120);HwamaiMalilibConfig.WHEEL_Y.setIntegerValue(-30);HwamaiMalilibConfig.WHEEL_KEY.getKeybind().setValueFromString("RIGHT_CONTROL");
  JsonObject root=new JsonObject();ConfigUtils.writeConfigBase(root,"General",HwamaiMalilibConfig.GENERAL_OPTIONS);ConfigUtils.writeConfigBase(root,"Wheel",java.util.List.of(HwamaiMalilibConfig.WHEEL_RADIUS,HwamaiMalilibConfig.WHEEL_X,HwamaiMalilibConfig.WHEEL_Y));ConfigUtils.writeConfigBase(root,"Hotkeys",HwamaiMalilibConfig.HOTKEY_OPTIONS);
  HwamaiMalilibConfig.HELPER_ENABLED.setBooleanValue(true);HwamaiMalilibConfig.WHEEL_RADIUS.setIntegerValue(76);HwamaiMalilibConfig.WHEEL_KEY.getKeybind().setValueFromString("LEFT_ALT");ConfigUtils.readConfigBase(root,"General",HwamaiMalilibConfig.GENERAL_OPTIONS);ConfigUtils.readConfigBase(root,"Wheel",java.util.List.of(HwamaiMalilibConfig.WHEEL_RADIUS,HwamaiMalilibConfig.WHEEL_X,HwamaiMalilibConfig.WHEEL_Y));ConfigUtils.readConfigBase(root,"Hotkeys",HwamaiMalilibConfig.HOTKEY_OPTIONS);
  if(HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()||HwamaiMalilibConfig.TELEPORT_EFFECT.getBooleanValue()||HwamaiMalilibConfig.WHEEL_RADIUS.getIntegerValue()!=98||HwamaiMalilibConfig.WHEEL_X.getIntegerValue()!=120||HwamaiMalilibConfig.WHEEL_Y.getIntegerValue()!=-30)throw new AssertionError("Settings not persistent");
  if(!HwamaiMalilibConfig.WHEEL_KEY.getKeybind().getKeys().equals(java.util.List.of(com.mojang.blaze3d.platform.InputConstants.KEY_RCONTROL)))throw new AssertionError("Custom wheel key lost");
  if(HwamaiMalilibConfig.OPEN_MENU.getKeybind().getKeys().size()!=2)throw new AssertionError("Master off removed settings shortcut");
  System.out.println("PASS: default SDL wheel key, persisted master/effect toggles, wheel dimensions/offsets/custom key, settings shortcut remains configured.");
 }
}
