package org.NJ.hwamaihelper.client;

import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Read-only feed carried by the authenticated Minecraft connection. All state is client-thread owned. */
public final class DiscordFeed {
 public record Payload(String json) implements CustomPacketPayload {
  public static final Type<Payload> TYPE=new Type<>(Identifier.parse("moris:discord_feed"));
  public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC=new StreamCodec<>(){
   public Payload decode(RegistryFriendlyByteBuf b){int n=b.readableBytes();if(n>30000)throw new IllegalArgumentException("Oversized feed frame");byte[] bytes=new byte[n];b.readBytes(bytes);return new Payload(new String(bytes,StandardCharsets.UTF_8));}
   public void encode(RegistryFriendlyByteBuf b,Payload p){byte[] bytes=p.json.getBytes(StandardCharsets.UTF_8);if(bytes.length>30000)throw new IllegalArgumentException("Oversized feed frame");b.writeBytes(bytes);}
  };
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 private static final Set<String> IDS=Set.of("1496456026893975552","1543903711808593941","1526988528674799729","1500375995457470485","1559943033963679744","1507814531903262863","1528619418123370647");
 private static final Map<String,Page> PAGES=new HashMap<>();
 private static AnnouncementReadState reads=new AnnouncementReadState(null);
 private record ReadRequest(String channel,boolean older){}
 private static final Deque<ReadRequest> REQUESTS=new ArrayDeque<>();
 private static String inFlight;
 private static long lastRequest;
 private static long lastSubscribe;private static boolean ready;
 public static final class Page {
  final String channel;Page(String channel){this.channel=channel;}
  final TreeMap<String,JsonObject> messages=new TreeMap<>(Comparator.comparingLong(Long::parseUnsignedLong));
  String status="尚未連接伺服器資料服務。";boolean more,loading,append;int unreadCache;long version,requested,unreadVersion=-1;
  public String json(){JsonObject o=new JsonObject();o.addProperty("status",status);o.addProperty("more",more);o.addProperty("loading",loading);JsonArray a=new JsonArray();messages.descendingMap().values().forEach(m->{JsonObject copy=m.deepCopy();copy.addProperty("unread",reads.unread(channel,m.get("id").getAsString(),AnnouncementReadState.fingerprint(m)));a.add(copy);});o.add("messages",a);return o.toString();}
 }
 public static void initialize(){
  DiscordBanner.initialize();
  reads=new AnnouncementReadState(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("moris-announcements.json"));
  PayloadTypeRegistry.serverboundPlay().register(Payload.TYPE,Payload.CODEC);PayloadTypeRegistry.clientboundPlay().register(Payload.TYPE,Payload.CODEC);
  ClientPlayNetworking.registerGlobalReceiver(Payload.TYPE,(payload,context)->context.client().execute(()->receive(payload.json)));
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(DiscordFeed::tick);
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((h,c)->c.execute(()->{PAGES.clear();REQUESTS.clear();inFlight=null;DiscordBanner.clear();ready=false;lastSubscribe=0;}));
 }
 private static boolean available(){return Minecraft.getInstance().getConnection()!=null&&ClientPlayNetworking.canSend(Payload.TYPE);}
 private static void tick(Minecraft mc){
  if(!available())return;long now=System.currentTimeMillis();
  if(!ready&&now-lastSubscribe>5000){lastSubscribe=now;ClientPlayNetworking.send(new Payload("subscribe"));}
  for(Page p:PAGES.values())if(p.loading&&p.channel.equals(inFlight)&&now-p.requested>25000){p.loading=false;p.status="讀取逾時，請重新整理。";p.version++;inFlight=null;}
  if(inFlight==null&&!REQUESTS.isEmpty()&&now-lastRequest>=1250){ReadRequest request=REQUESTS.removeFirst();Page p=page(request.channel);String before=request.older&&!p.messages.isEmpty()?p.messages.firstKey():"";inFlight=request.channel;lastRequest=now;p.requested=now;p.status="正在讀取 Discord 訊息…";p.version++;ClientPlayNetworking.send(new Payload("history|"+request.channel+"|"+before));}

 }
 public static Page page(String id){if(!IDS.contains(id))throw new IllegalArgumentException("Unknown channel");return PAGES.computeIfAbsent(id,Page::new);}
 public static int unreadCount(String id){if(!IDS.contains(id))return 0;Page p=PAGES.get(id);if(p==null)return Math.max(reads.unreadIds(id).size(),reads.pending(id)?1:0);if(p.unreadVersion!=p.version){Set<String> ids=reads.unreadIds(id);p.messages.forEach((mid,m)->{if(reads.unread(id,mid,AnnouncementReadState.fingerprint(m)))ids.add(mid);});p.unreadCache=ids.size();p.unreadVersion=p.version;}return Math.max(p.unreadCache,reads.pending(id)?1:0);}
 public static int totalUnread(){return IDS.stream().mapToInt(DiscordFeed::unreadCount).sum();}
 public static boolean unread(String id){return unreadCount(id)>0;}
 public static boolean anyUnread(){return totalUnread()>0;}
 public static Component unreadLabel(String label,int count){return count==0?Component.literal(label):Component.literal(label+" ").append(Component.literal(Integer.toString(count)).withStyle(net.minecraft.ChatFormatting.RED));}
 public static void markRead(String id,Collection<String> ids){Page p=page(id);Map<String,String> seen=new LinkedHashMap<>();for(String mid:ids){JsonObject message=p.messages.get(mid);if(message!=null)seen.put(mid,AnnouncementReadState.fingerprint(message));}reads.markRead(id,seen);p.version++;}
 public static void markAllRead(String id){markRead(id,new ArrayList<>(page(id).messages.keySet()));}
 public static void request(String id,boolean older){
  queue(id,older,true);
 }
 private static void queue(String id,boolean older,boolean priority){
  Page p=page(id);if(p.loading){if(priority&&!id.equals(inFlight)){ReadRequest found=REQUESTS.stream().filter(r->r.channel.equals(id)).findFirst().orElse(null);if(found!=null){REQUESTS.remove(found);REQUESTS.addFirst(found);}}return;}
  if(!available()){p.status="伺服器尚未安裝頻道串接更新，或目前分流未提供服務。";p.version++;return;}
  p.loading=true;p.status="等待讀取 Discord 訊息…";p.requested=System.currentTimeMillis();p.version++;ReadRequest request=new ReadRequest(id,older);if(priority)REQUESTS.addFirst(request);else REQUESTS.addLast(request);
 }
 private static void receive(String raw){
  try{JsonObject o=JsonParser.parseString(raw).getAsJsonObject();String op=o.get("op").getAsString();if(op.startsWith("teleport")){TeleportVisualEffect.receive(o);return;}if(op.startsWith("container")){ContainerPreview.receive(o);return;}if(op.equals("profileError")){if(Minecraft.getInstance().gui.screen() instanceof PlayerInspectScreen screen)screen.fail(o.get("text").getAsString());return;}if(op.equals("profile")){if(Minecraft.getInstance().gui.screen() instanceof PlayerInspectScreen screen)screen.accept(o);return;}if(op.equals("ready")){if(!ready){ready=true;IDS.forEach(id->queue(id,false,false));}return;}
   String id=o.get("channel").getAsString();if(!IDS.contains(id))return;Page p=page(id);
   switch(op){
    case "begin"->{p.append=o.get("append").getAsBoolean();if(!p.append)p.messages.clear();}
    case "message"->{JsonObject m=o.getAsJsonObject("message");String mid=m.get("id").getAsString();if(mid.matches("[0-9]{1,19}")){p.messages.put(mid,m);while(p.messages.size()>200)p.messages.pollLastEntry();}}
    case "done"->{p.more=o.get("more").getAsBoolean();p.loading=false;if(id.equals(inFlight))inFlight=null;if(!p.append){Map<String,String> latest=new LinkedHashMap<>();p.messages.descendingMap().entrySet().stream().limit(20).forEach(e->latest.put(e.getKey(),AnnouncementReadState.fingerprint(e.getValue())));reads.snapshot(id,latest);}p.status=p.messages.isEmpty()?"這個頻道目前沒有訊息。":"僅供閱讀 · 可點擊圖片查看原圖";}
    case "error"->{p.loading=false;if(id.equals(inFlight))inFlight=null;p.status=o.get("text").getAsString();}
    case "notice"->{if(!o.get("kind").getAsString().equals("delete")){reads.changed(id);DiscordBanner.offer(id,o.get("name").getAsString(),o.get("kind").getAsString().equals("edit")?"公告內容已更新":"有新的頻道訊息");}p.status="頻道內容有變動，請按重新整理。";queue(id,false,false);}
   }p.version++;
  }catch(RuntimeException e){System.err.println("[MorisHelper] Invalid Discord feed frame: "+e.getClass().getSimpleName());}
 }
 private DiscordFeed(){}
}
