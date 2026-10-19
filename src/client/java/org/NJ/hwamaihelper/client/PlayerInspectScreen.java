package org.NJ.hwamaihelper.client;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import com.google.gson.*;
/** Self inspection moves only the local render camera, never the player's entity. */
public final class PlayerInspectScreen extends Screen {
 private CameraType previous;private long opened,closing;private double closingFrom;private Vec3 originOffset;private float originYaw,originPitch,bodyYaw;private net.minecraft.client.multiplayer.ClientLevel world;
 private String[] values=new String[5];private String status="正在取得伺服器資料…";private long requested,firstRequested;private int attempts;
 public PlayerInspectScreen(){super(Component.literal("玩家檢視"));}
 protected void init(){if(previous==null){if(minecraft.player==null)return;world=minecraft.level;previous=minecraft.options.getCameraType();Camera camera=minecraft.gameRenderer.mainCamera();originOffset=camera.position().subtract(minecraft.player.position());originYaw=camera.yRot();originPitch=camera.xRot();bodyYaw=minecraft.player.getYRot();opened=System.nanoTime();minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);values[0]=minecraft.player.getGameProfile().name();request();}}
 private void request(){requested=System.currentTimeMillis();if(attempts++==0)firstRequested=requested;if(!net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(DiscordFeed.Payload.TYPE)){status="伺服器尚未提供玩家資訊串接。";return;}net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new DiscordFeed.Payload("profile|self"));}
 public void fail(String message){status=message;for(int i=1;i<values.length;i++)if(values[i]==null)values[i]="無法取得";}
 public void accept(JsonObject o){try{if(minecraft.player==null||!minecraft.player.getUUID().toString().equals(o.get("uuid").getAsString()))return;values[0]=o.get("name").getAsString();values[1]=o.get("nickname").getAsString();values[2]=o.has("balance")&&!o.get("balance").isJsonNull()?String.format(java.util.Locale.TAIWAN,"%,.2f",o.get("balance").getAsDouble()):"無法取得";long seconds=Math.max(0,o.get("seconds").getAsLong());values[3]=(seconds/3600)+" 小時 "+(seconds%3600/60)+" 分鐘";long first=o.get("firstJoin").getAsLong();values[4]=first>0?java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(java.time.ZoneId.of("Asia/Taipei")).format(java.time.Instant.ofEpochMilli(first)):"無法取得";status="伺服器資料 · 僅供本人查看";}catch(RuntimeException e){status="伺服器資料格式不正確。";}}
 public double progress(){return closing==0?InspectTransition.opening(System.nanoTime()-opened):InspectTransition.closing(closingFrom,System.nanoTime()-closing);}
 public record Pose(Vec3 position,float yaw,float pitch){}
 public Pose camera(float partial){if(minecraft.player==null||originOffset==null||minecraft.level!=world)return null;var player=minecraft.player;Vec3 base=player.getPosition(partial),eye=base.add(0,player.getEyeHeight(),0);double angle=Math.toRadians(bodyYaw);Vec3 forward=new Vec3(-Math.sin(angle),0,Math.cos(angle)),right=new Vec3(Math.cos(angle),0,Math.sin(angle));Vec3 destination=base.add(0,1.65,0).add(forward.scale(4.5)).add(right.scale(0.6));Vec3 aim=base.add(0,1.0,0);Vec3 direction=aim.subtract(destination);float yaw=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z)),pitch=(float)Math.toDegrees(Math.atan2(-direction.y,Math.hypot(direction.x,direction.z)));double t=progress();Vec3 position=base.add(originOffset).lerp(destination,t); // Keep the viewing path outside solid blocks.
 double distance=position.distanceTo(eye);if(distance>0.01){double allowed=distance;for(double x:new double[]{-0.1,0.1})for(double y:new double[]{-0.1,0.1})for(double z:new double[]{-0.1,0.1}){Vec3 offset=new Vec3(x,y,z);var hit=world.clip(new ClipContext(eye.add(offset),position.add(offset),ClipContext.Block.VISUAL,ClipContext.Fluid.NONE,player));if(hit.getType()!=HitResult.Type.MISS)allowed=Math.min(allowed,Math.max(0,hit.getLocation().distanceTo(eye.add(offset))-0.15));}position=eye.lerp(position,allowed/distance);}
 // A positive look-yaw offset puts the actor on the LEFT in the front-facing view.
 direction=aim.subtract(position);yaw=(float)(Math.toDegrees(Math.atan2(-direction.x,direction.z))+InspectFraming.yawOffset(minecraft.gameRenderer.mainCamera().getFov(),(double)minecraft.getWindow().getWidth()/Math.max(1,minecraft.getWindow().getHeight())));pitch=(float)Math.toDegrees(Math.atan2(-direction.y,Math.hypot(direction.x,direction.z)));
 return new Pose(position,originYaw+(float)(net.minecraft.util.Mth.wrapDegrees(yaw-originYaw)*t),originPitch+(pitch-originPitch)*(float)t);}
 public void tick(){if(!org.NJ.hwamaihelper.config.HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()||minecraft.player==null||minecraft.level!=world||!minecraft.player.isAlive()){minecraft.gui.setScreen(null);return;}if(closing!=0&&System.nanoTime()-closing>=500_000_000L)minecraft.gui.setScreen(null);if(status.startsWith("正在")){long now=System.currentTimeMillis();if(now-firstRequested>12000)fail("伺服器未回傳資料，請更新玩家檢視插件。");else if(attempts<3&&now-requested>3500)request();}}
 public void onClose(){if(closing==0){closingFrom=progress();closing=System.nanoTime();}}
 public void removed(){if(previous!=null)minecraft.options.setCameraType(previous);}
 public boolean isPauseScreen(){return false;}
 public void extractBackground(GuiGraphicsExtractor g,int mx,int my,float delta){}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){int w=Math.min(232,width/2-16),x=width/2+6,y=Math.max(12,height/2-100);g.fill(x,y,x+w,y+194,0xDD25212B);g.fill(x,y,x+w,y+2,0xFFF2A5C6);g.text(font,"玩家資訊",x+10,y+10,0xFFF2A5C6);String[] labels={"玩家 ID","玩家暱稱","遊戲餘額","上線總時數","第一次加入"};int row=y+30;for(int i=0;i<labels.length;i++){g.text(font,labels[i]+"：",x+10,row,0xFFB7A9B8);row+=11;g.text(font,font.plainSubstrByWidth(values[i]==null?"等待伺服器資料":values[i],w-20),x+10,row,0xFFFFFFFF);row+=18;}g.text(font,font.plainSubstrByWidth(status,w-20),x+10,y+177,0xFFB7A9B8);g.text(font,"Esc / Shift + G 返回",x,height-22,0xFFFFFFFF);}
}
