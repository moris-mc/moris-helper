package org.NJ.hwamaihelper.client.screens;
import fi.dy.masa.malilib.gui.GuiBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;
public class GameModeWheelScreen extends GuiBase {
 private int selected=-1;
 private static final String[] LABELS={"創造","生存","旁觀者","隱形模式"};
 private static final String[] COMMANDS={"gm c","gm s","gm sp","gm v"};
 private static final Item[] ICONS={Items.GRASS_BLOCK,Items.DIAMOND_SWORD,Items.ENDER_EYE,Items.GLASS};
 public GameModeWheelScreen() { setTitle("遊戲模式"); }
 public static boolean canUse(net.minecraft.client.Minecraft mc){return HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()&&mc.player!=null&&mc.player.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_MODERATOR);}

 public boolean isPauseScreen() { return false; }
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
  if(!canUse(net.minecraft.client.Minecraft.getInstance())){selected=-1;return;}
  super.extractRenderState(g,mx,my,delta);
  var layout=org.NJ.hwamaihelper.client.WheelLayout.create(width,height,HwamaiMalilibConfig.WHEEL_RADIUS.getIntegerValue(),HwamaiMalilibConfig.WHEEL_X.getIntegerValue(),HwamaiMalilibConfig.WHEEL_Y.getIntegerValue());int cx=layout.x(),cy=layout.y(),radius=layout.radius(),n=4; double sector=360.0/n;
  double dx=mx-cx,dy=my-cy;
  selected=layout.selection(mx,my);
  for(int y=-radius;y<=radius;y++) { int w=(int)Math.sqrt(radius*radius-y*y); g.fill(cx-w,cy+y,cx+w,cy+y+1,0xCC202020); }
  for(int i=0;i<n;i++) {
   double a=Math.toRadians(-90+i*sector); int x=cx+(int)(radius*0.64*Math.cos(a)),y=cy+(int)(radius*0.64*Math.sin(a));
   if(selected==i) g.fill(x-25,y-21,x+25,y+22,0x88777777);
   g.item(new ItemStack(ICONS[i]),x-8,y-16);
   g.centeredText(font,Component.literal(LABELS[i]),x,y+5,selected==i?0xFFFFFFFF:0xFFBBBBBB);
  }
  g.centeredText(font,Component.literal("放開按鍵套用 · 中央／圈外取消"),cx,cy+radius+12,0xFFFFFFFF);
 }
 public void tick() {
  super.tick();
  if(!canUse(minecraft)){selected=-1;onClose();return;}
  if(!org.NJ.hwamaihelper.client.logic.MalilibInputHandler.altDown()) {
   if(selected>=0 && selected<COMMANDS.length && canUse(minecraft)) minecraft.player.connection.sendCommand(COMMANDS[selected]);
   onClose();
  }
 }
}

