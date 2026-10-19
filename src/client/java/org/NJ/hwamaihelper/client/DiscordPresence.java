package org.NJ.hwamaihelper.client;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
/** Background RPC session, scoped to an active Moris server connection. */
public final class DiscordPresence {
 private static final ScheduledExecutorService WORKER=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"moris-discord-presence");t.setDaemon(true);return t;});
 private static final Object LOCK=new Object();
 private static final Object SESSION=new Object();
 private static volatile DiscordRpcTransport connection;
 private static volatile boolean desired,ready,stopped,failed;
 private static volatile long start,connectedAt,lastSent;
 private static volatile String status="等待連入森友",address="未連線",problem="無";
 private static volatile long acceptedAt;
 private static int ticks;
 private static long nextConnect;
 public static void initialize(){
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(DiscordPresence::tick);
  net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler,client)->desired=false);
  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client->stop());
  WORKER.scheduleWithFixedDelay(DiscordPresence::update,0,1,TimeUnit.SECONDS);
 }
 private static void tick(Minecraft mc){
  if(++ticks%20!=0)return;
  var server=mc.getCurrentServer();address=server==null?"未連線":server.ip;boolean active=mc.player!=null&&mc.getConnection()!=null&&server!=null&&PresenceProtocol.isMoris(server.ip);
  if(active&&!desired)start=System.currentTimeMillis()/1000;
  desired=active;
 }
 private static void update(){
  try{
   if(stopped)return;
   if(!desired){clearAndClose();status="等待連入 mcmoris.net";return;}
   long now=System.currentTimeMillis();
   if(failed){closeConnection();nextConnect=now+15000;failed=false;}
   if(connection==null){if(now<nextConnect)return;nextConnect=now+15000;connect();}
   if(!ready){if(now-connectedAt>15000){closeConnection();nextConnect=now+15000;}return;}
   if(now-lastSent>=15000){send(connection,1,PresenceProtocol.activity(ProcessHandle.current().pid(),start,UUID.randomUUID().toString(),true).getBytes(java.nio.charset.StandardCharsets.UTF_8));lastSent=now;}
  }catch(Exception e){problem=e.getClass().getSimpleName()+": "+e.getMessage();status="連線失敗，15 秒後重試";System.err.println("[MorisHelper] Discord RPC: "+problem);closeConnection();nextConnect=System.currentTimeMillis()+15000;}
 }
 private static void connect()throws IOException{
  status="尋找 Discord 桌面程式";DiscordRpcTransport opened=DiscordRpcTransport.connect();
  synchronized(SESSION){if(stopped||!desired){opened.close();return;}connection=opened;ready=false;failed=false;lastSent=0;connectedAt=System.currentTimeMillis();}
  send(opened,0,PresenceProtocol.handshake().getBytes(java.nio.charset.StandardCharsets.UTF_8));
  Thread reader=new Thread(()->read(opened),"moris-discord-reader");reader.setDaemon(true);reader.start();
 }
 private static void read(DiscordRpcTransport opened){
  try{
   while(!stopped&&connection==opened){
    var packet=PresenceProtocol.read(opened.input);
    if(stopped||connection!=opened)return;
    if(packet.opcode()==2)throw new EOFException();
    if(packet.opcode()==3){send(opened,4,packet.data());continue;}
    if(packet.opcode()!=1)continue;
    JsonObject frame=JsonParser.parseString(packet.text()).getAsJsonObject();
    String event=frame.has("evt")&&!frame.get("evt").isJsonNull()?frame.get("evt").getAsString():"";
    if(event.equals("READY")){ready=true;acceptedAt=0;status="已握手，等待活動確認";System.out.println("[MorisHelper] Discord Rich Presence connected.");}
    if(frame.has("cmd")&&frame.get("cmd").getAsString().equals("SET_ACTIVITY")&&event.isEmpty()){acceptedAt=System.currentTimeMillis();status="Discord 已接受活動內容";problem="無";System.out.println("[MorisHelper] Discord SET_ACTIVITY accepted.");}
    if(event.equals("ERROR")){problem=String.valueOf(frame.get("data"));status="Discord 拒絕活動";System.err.println("[MorisHelper] Discord rejected an RPC request: "+frame.get("data"));failed=true;return;}
   }
  }catch(Exception e){if(connection==opened){problem=e.toString();status="Discord 連線中斷";failed=true;}}
 }
 private static void send(DiscordRpcTransport opened,int opcode,byte[] data)throws IOException{
  synchronized(LOCK){if(connection!=opened)throw new EOFException();PresenceProtocol.write(opened.output,opcode,data);}
 }
 private static void clearAndClose(){
  if(connection==null)return;
  try{if(ready)send(connection,1,PresenceProtocol.activity(ProcessHandle.current().pid(),0,UUID.randomUUID().toString(),false).getBytes(java.nio.charset.StandardCharsets.UTF_8));}catch(Exception ignored){}finally{closeConnection();}
 }
 private static void closeConnection(){DiscordRpcTransport old;synchronized(SESSION){old=connection;connection=null;ready=false;lastSent=0;}if(old!=null)old.close();}
 public static void stop(){synchronized(SESSION){stopped=true;desired=false;}closeConnection();WORKER.shutdownNow();}
 public static String[] diagnostics(){return new String[]{"伺服器："+address,"Application ID："+PresenceProtocol.APPLICATION_ID,"狀態："+status,"最近錯誤："+problem,acceptedAt>0?"若仍未顯示，請確認 Discord 設定中的活動隱私允許分享活動。":"等待 Discord 回覆 SET_ACTIVITY 結果。"};}
 private DiscordPresence(){}
}
