package org.NJ.hwamaihelper.client;
import com.google.gson.JsonObject;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;
/** Cosmetic camera only. Actual teleport destination and success come from Moris. */
public final class TeleportVisualEffect {
 private static long id,started,phaseStarted;private static int phase,soundStage,returnSoundStage;private static CameraType previous;private static Vec3 origin,cameraOffset,goal;private static String originWorld,goalWorld;private static float originYaw,originPitch,playerYaw,yawOffset;private static boolean arrived,travel;private static Boolean sentPreference;
 private static long now(){return System.nanoTime()/1000000;}
 private static boolean enabled(){return HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()&&HwamaiMalilibConfig.TELEPORT_EFFECT.getBooleanValue();}
 public static boolean active(){return id!=0;}
 public static void initialize(){net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(TeleportVisualEffect::tick);net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(()->{stop();sentPreference=null;}));}
 private static boolean userScreen(Minecraft mc){return mc.gui.screen()!=null&&!(mc.gui.screen() instanceof net.minecraft.client.gui.screens.LevelLoadingScreen)&&!(mc.gui.screen() instanceof net.minecraft.client.gui.screens.ProgressScreen);}
 private static void tick(Minecraft mc){boolean wanted=enabled();if(mc.getConnection()!=null&&net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(DiscordFeed.Payload.TYPE)&&(sentPreference==null||sentPreference!=wanted)){net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new DiscordFeed.Payload("cinematic|"+(wanted?"on":"off")));sentPreference=wanted;}
  if(!active())return;if(!wanted||mc.player==null||!mc.player.isAlive()||mc.level==null||userScreen(mc)||mc.getCameraEntity()!=mc.player||now()-started>15000){stop();return;}
  if(arrived&&phase==0&&now()-started>=CinematicPath.DEPARTURE_MS&&mc.gui.screen()==null&&mc.level.dimension().identifier().toString().equals(goalWorld)&&mc.player.position().distanceToSqr(goal)<16){phaseStarted=now();travel=originWorld.equals(goalWorld)&&origin.distanceTo(goal)<Math.min(96,mc.options.getEffectiveRenderDistance()*10);phase=travel?1:2;if(phase==2)sound("minecraft:block.amethyst_block.chime",1.1F);}
  if(phase==0){int next=CinematicPath.ascentStage(now()-started);if(next>soundStage){soundStage=next;sound("minecraft:block.note_block.chime",0.8F+next*0.15F);}}
  if(phase==2){int next=CinematicPath.descentStage(now()-phaseStarted);if(next>returnSoundStage){returnSoundStage=next;sound("minecraft:block.note_block.chime",1.25F-next*0.15F);}}
  if(phase==1&&now()-phaseStarted>=CinematicPath.TRAVEL_MS){phase=2;phaseStarted=now();sound("minecraft:block.amethyst_block.chime",1.1F);}
  if(phase==2&&now()-phaseStarted>=CinematicPath.RETURN_MS)stop();
 }
 public static void receive(JsonObject body){Minecraft mc=Minecraft.getInstance();String op=body.get("op").getAsString();long eventId=body.get("id").getAsLong();if(op.equals("teleportStart")){
   if(!enabled()||mc.player==null||mc.level==null||mc.gui.screen()!=null||mc.getCameraEntity()!=mc.player)return;stop();var camera=mc.gameRenderer.mainCamera();previous=mc.options.getCameraType();origin=camera.position();originYaw=camera.yRot();originPitch=camera.xRot();playerYaw=mc.player.getYRot();yawOffset=net.minecraft.util.Mth.wrapDegrees(originYaw-playerYaw);cameraOffset=origin.subtract(mc.player.getEyePosition());originWorld=mc.level.dimension().identifier().toString();goalWorld=body.get("world").getAsString();goal=new Vec3(body.get("x").getAsDouble(),body.get("y").getAsDouble(),body.get("z").getAsDouble());started=phaseStarted=now();phase=0;soundStage=0;returnSoundStage=0;arrived=false;travel=false;id=eventId;mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);sound("minecraft:block.note_block.chime",0.8F);return;
  }if(eventId!=id)return;if(op.equals("teleportCancel")){stop();return;}if(op.equals("teleportArrive")){goalWorld=body.get("world").getAsString();goal=new Vec3(body.get("x").getAsDouble(),body.get("y").getAsDouble(),body.get("z").getAsDouble());arrived=true;}}
 private static void sound(String name,float pitch){var manager=Minecraft.getInstance().getSoundManager();Identifier sound=Identifier.parse(name);if(manager.getSoundEvent(sound)==null)sound=Identifier.parse("minecraft:block.note_block.pling");manager.play(new net.minecraft.client.resources.sounds.SimpleSoundInstance(sound,net.minecraft.sounds.SoundSource.MASTER,0.2F,pitch,net.minecraft.client.resources.sounds.SoundInstance.createUnseededRandom(),false,0,net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE,0,0,0,true));}
 public static void stop(){if(previous!=null){Minecraft.getInstance().options.setCameraType(previous);previous=null;}id=0;arrived=false;}
 public record Frame(Vec3 position,float yaw,float pitch){}
 public static Frame frame(float partial){if(!active())return null;Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return null;long elapsed=now()-phaseStarted;String current=mc.level.dimension().identifier().toString();
  if(phase==0){if(!current.equals(originWorld))return new Frame(mc.player.getEyePosition(partial).add(0,72,0),mc.player.getYRot(),85);double t=(now()-started)/(double)CinematicPath.DEPARTURE_MS;return new Frame(origin.add(0,CinematicPath.height(t),0),originYaw,originPitch+(85-originPitch)*(float)CinematicPath.ease(t));}
  Vec3 end=mc.player.getEyePosition(partial).add(cameraOffset.yRot((float)Math.toRadians(-(mc.player.getYRot()-playerYaw))));float endYaw=mc.player.getViewYRot(partial)+yawOffset,endPitch=previous==CameraType.THIRD_PERSON_FRONT?-mc.player.getViewXRot(partial):mc.player.getViewXRot(partial);
  if(phase==1){double t=CinematicPath.ease(elapsed/(double)CinematicPath.TRAVEL_MS);return new Frame(origin.add(0,72,0).lerp(end.add(0,72,0),t),originYaw+(float)(net.minecraft.util.Mth.wrapDegrees(endYaw-originYaw)*t),85);}
  double t=elapsed/(double)CinematicPath.RETURN_MS;return new Frame(end.add(0,CinematicPath.height(1-t),0),endYaw,85+(endPitch-85)*(float)CinematicPath.ease(t));
 }
 public static void render(GuiGraphicsExtractor g){if(!active())return;Minecraft mc=Minecraft.getInstance();double alpha=phase==0?CinematicPath.fadeOut(now()-started):phase==1?1-CinematicPath.ease((now()-phaseStarted)/500.0):travel?0:1-CinematicPath.ease((now()-phaseStarted)/600.0);g.nextStratum();int tint=(int)Math.round(48*CinematicPath.filter(phase,now()-phaseStarted));if(tint>0)g.fill(0,0,g.guiWidth(),g.guiHeight(),(tint<<24)|0x386D83);if(alpha>0)g.fill(0,0,g.guiWidth(),g.guiHeight(),((int)(Math.clamp(alpha,0,1)*255)<<24));Component hint=Component.literal("本功能為娛樂特效功能，若不喜歡可以開啟\"小幫手\"模組關閉此功能").withStyle(ChatFormatting.RED);var lines=mc.font.split(hint,Math.max(100,g.guiWidth()-24));int y=g.guiHeight()-48-lines.size()*10;for(var line:lines){g.centeredText(mc.font,line,g.guiWidth()/2,y,0xFFFF5555);y+=10;}}
 private TeleportVisualEffect(){}
}
