package org.NJ.hwamaihelper.client;
import com.google.gson.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.NJ.hwamaihelper.config.HwamaiMalilibConfig;
/** Read-only HUD. No screen, inventory interaction or slot-click packets are created. */
public final class ContainerPreview {
 private static BlockPos target;private static String world="",title="",status="";private static long sequence,lastRequest,lastReply,started,nextSendAt;private static boolean awaiting;private static ItemStack[] items,pending;private static int[] counts,pendingCounts;
 public static void initialize(){net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(ContainerPreview::tick);net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(ContainerPreview::clear));}
 private static boolean active(Minecraft mc){return HwamaiMalilibConfig.HELPER_ENABLED.getBooleanValue()&&mc.player!=null&&mc.level!=null&&mc.player.isAlive()&&mc.gui.screen()==null&&!HwamaiMalilibConfig.CONTAINER_PREVIEW.getKeybind().getStringValue().isBlank();}
 private static boolean held(Minecraft mc){return active(mc)&&HwamaiMalilibConfig.CONTAINER_PREVIEW.getKeybind().isKeybindHeld();}
 public static void clear(){target=null;items=pending=null;counts=pendingCounts=null;status="";sequence++;lastRequest=0;lastReply=0;started=System.currentTimeMillis();awaiting=false;}
 private static void tick(Minecraft mc){
  if(!active(mc)||!(mc.hitResult instanceof BlockHitResult hit)||hit.getType()!=HitResult.Type.BLOCK){if(target!=null)clear();return;}
  BlockPos pos=hit.getBlockPos();var entity=mc.level.getBlockEntity(pos);
  if(!(entity instanceof net.minecraft.world.Container)&&!(entity instanceof net.minecraft.world.level.block.entity.EnderChestBlockEntity)){if(target!=null)clear();return;}
  String dimension=mc.level.dimension().identifier().toString();
  if(!pos.equals(target)||!dimension.equals(world)){clear();target=pos.immutable();world=dimension;title=mc.level.getBlockState(pos).getBlock().getName().getString();status="正在讀取…";}
  long now=System.currentTimeMillis();if(lastReply!=0&&now-lastReply>1500){items=null;counts=null;status="資料已過期，正在重新讀取…";}
  if(!PreviewTiming.mayRequest(now,started,lastRequest,awaiting,held(mc),nextSendAt))return;lastRequest=now;nextSendAt=now+200;
  if(!net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(DiscordFeed.Payload.TYPE)){status="伺服器尚未提供容器預覽";return;}
  if(lastReply==0&&now-started>6000)status="請更新伺服器容器預覽插件";
  sequence++;awaiting=true;pending=null;pendingCounts=null;
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new DiscordFeed.Payload("container|"+sequence+"|"+pos.getX()+"|"+pos.getY()+"|"+pos.getZ()));
 }
 public static void receive(JsonObject body){Minecraft mc=Minecraft.getInstance();try{
  if(!active(mc)||target==null||mc.level==null||!world.equals(mc.level.dimension().identifier().toString())||body.get("seq").getAsLong()!=sequence||!body.get("world").getAsString().equals(world))return;
  if(!(mc.hitResult instanceof BlockHitResult hit)||!hit.getBlockPos().equals(target))return;
  String op=body.get("op").getAsString();
  if(op.equals("containerWait")){awaiting=false;nextSendAt=System.currentTimeMillis()+Math.clamp(body.get("retryAfter").getAsInt(),30,500);lastRequest=0;return;}
  lastReply=System.currentTimeMillis();
  if(op.equals("containerError")){awaiting=false;items=pending=null;counts=pendingCounts=null;status=body.get("text").getAsString();return;}
  if(op.equals("containerBegin")){int size=body.get("size").getAsInt();if(size<1||size>54)throw new IllegalArgumentException("size");pending=new ItemStack[size];Arrays.fill(pending,ItemStack.EMPTY);pendingCounts=new int[size];return;}
  if(op.equals("containerItems")&&pending!=null){for(JsonElement element:body.getAsJsonArray("items")){JsonObject slot=element.getAsJsonObject();int index=slot.get("slot").getAsInt();if(index<0||index>=pending.length)continue;int count=slot.get("count").getAsInt();if(count<=0)continue;ItemStack item=PreviewItemDecoder.decode(slot,mc.level.registryAccess());pending[index]=item;pendingCounts[index]=Math.clamp(count,1,99999);}return;}
  if(op.equals("containerDone")&&pending!=null){awaiting=false;items=pending;counts=pendingCounts;pending=null;pendingCounts=null;status="僅供查看";}
 }catch(RuntimeException e){items=pending=null;counts=pendingCounts=null;status="無法讀取物品資料";}}
 public static void render(GuiGraphicsExtractor g){Minecraft mc=Minecraft.getInstance();if(target==null||!held(mc))return;int rows=items==null?1:(items.length+8)/9,w=174,h=rows*18+30,x=Math.min(g.guiWidth()-w-6,g.guiWidth()/2+16),y=Math.clamp(g.guiHeight()/2-h/2,6,Math.max(6,g.guiHeight()-h-6));g.nextStratum();g.fill(x,y,x+w,y+h,0xEF25212B);g.fill(x,y,x+w,y+2,0xFFF2A5C6);g.text(mc.font,mc.font.plainSubstrByWidth(title,w-12),x+6,y+6,0xFFFFFFFF);if(items==null){g.text(mc.font,mc.font.plainSubstrByWidth(status,w-12),x+6,y+25,0xFFDFB8CC);return;}
  for(int i=0;i<items.length;i++){int sx=x+6+i%9*18,sy=y+20+i/9*18;g.fill(sx,sy,sx+17,sy+17,0xFF15131A);if(!items[i].isEmpty()){g.item(items[i],sx,sy);if(counts[i]>1){String count=Integer.toString(counts[i]);g.text(mc.font,count,sx+17-mc.font.width(count),sy+9,0xFFFFFFFF,true);}}}
 }
 private ContainerPreview(){}
}
