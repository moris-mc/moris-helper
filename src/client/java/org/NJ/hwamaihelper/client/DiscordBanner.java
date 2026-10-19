package org.NJ.hwamaihelper.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
public final class DiscordBanner {
 private static final BannerTimeline MODEL=new BannerTimeline();
 private static final Identifier LOGO=Identifier.parse("senyouhelper:textures/gui/moris-logo.png");
 private static boolean enabled=true;
 private static java.nio.file.Path settings;
 public static void initialize(){settings=net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("moris-notifications.properties");if(java.nio.file.Files.isRegularFile(settings))try(var reader=java.nio.file.Files.newBufferedReader(settings)){var props=new java.util.Properties();props.load(reader);enabled=Boolean.parseBoolean(props.getProperty("enabled","true"));}catch(Exception e){System.err.println("[MorisHelper] Cannot load notification settings.");}}
 public static boolean enabled(){return enabled;}
 public static void toggle(){enabled=!enabled;if(!enabled)clear();if(settings!=null)try{java.nio.file.Files.createDirectories(settings.getParent());java.nio.file.Files.writeString(settings,"enabled="+enabled+"\n");}catch(Exception e){System.err.println("[MorisHelper] Cannot save notification settings.");}}
 private static void show(String channel,String name,String text){MODEL.offer(new BannerTimeline.Notice(channel,name,text),now());Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvent.createVariableRangeEvent(Identifier.parse("minecraft:announce.ui_two")),0.5F,0.2F));}
 public static void test(){show("1496456026893975552","測試通知","這是一則森友通知測試");}
 private static long now(){return System.nanoTime()/1_000_000;}
 public static void offer(String channel,String name,String text){if(enabled)show(channel,name,text);}
 public static void clear(){MODEL.clear();}
 private static int width(Minecraft mc){return Math.min(350,mc.getWindow().getGuiScaledWidth()-16);}
 private static int top(long time){return 6-(int)Math.round((1-MODEL.visibility(time))*38);}
 public static void render(GuiGraphicsExtractor g){
  Minecraft mc=Minecraft.getInstance();long time=now();var notice=MODEL.current(time);if(mc.player==null||notice==null)return;
  int w=width(mc),x=(g.guiWidth()-w)/2,y=top(time);if(w<70)return;
  double mx=mc.mouseHandler.getScaledXPos(mc.getWindow()),my=mc.mouseHandler.getScaledYPos(mc.getWindow());boolean hover=!mc.mouseHandler.isMouseGrabbed()&&mx>=x&&mx<x+w&&my>=y&&my<y+30;
  g.nextStratum();g.fill(x,y,x+w,y+30,hover?0xFA44303F:0xF5252028);g.fill(x,y,x+w,y+1,0xFFF2A5C6);g.fill(x,y+29,x+w,y+30,0xFFAA6D8B);
  var texture=mc.getTextureManager().getTexture(LOGO);g.blit(texture.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),x+5,y+4,x+27,y+26,0,1,0,1);
  String hint=mc.mouseHandler.isMouseGrabbed()?"按 T 點擊":"點擊開啟";
  int hintWidth=mc.font.width(hint);String title="Discord · "+notice.name()+" · "+notice.text();g.text(mc.font,mc.font.plainSubstrByWidth(title,Math.max(8,w-48-hintWidth)),x+33,y+10,0xFFFFFFFF,false);
  g.text(mc.font,hint,x+w-hintWidth-7,y+10,0xFFDFB8CC,false);
 }
 public static boolean click(double mx,double my){
  Minecraft mc=Minecraft.getInstance();long time=now();var notice=MODEL.current(time);if(notice==null||mc.player==null||mc.mouseHandler.isMouseGrabbed())return false;
  int w=width(mc),x=(mc.getWindow().getGuiScaledWidth()-w)/2,y=top(time);if(mx<x||mx>=x+w||my<Math.max(0,y)||my>=y+30)return false;
  MODEL.dismiss(time);mc.gui.setScreen(MorisWebScreen.discord(mc.gui.screen(),notice.channel(),notice.name()));return true;
 }
 private DiscordBanner(){}
}
