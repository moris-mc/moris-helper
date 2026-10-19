import org.NJ.hwamaihelper.client.PresenceProtocol;
import java.io.*;
import com.google.gson.*;
public final class PresenceCheck {
 public static void main(String[] args)throws Exception{
  for(String s:new String[]{"mcmoris.net","mcmoris.net:25565","PLAY.MCMORIS.NET","mcmoris.net."})if(!PresenceProtocol.isMoris(s))throw new AssertionError(s);
  for(String s:new String[]{"mcmoris.net.evil.example","notmcmoris.net","127.0.0.1","", "example.org:25565"})if(PresenceProtocol.isMoris(s))throw new AssertionError(s);
  String activity=PresenceProtocol.activity(123,456,"test-nonce",true);
  var json=JsonParser.parseString(activity).getAsJsonObject();var argsData=json.getAsJsonObject("args");var presence=argsData.getAsJsonObject("activity");
  if(!presence.get("details").getAsString().equals("我在森友伺服器｜mcmoris.net")||!presence.get("state").getAsString().equals("等你一起來玩ouo!!"))throw new AssertionError("exact text");
  if(!presence.getAsJsonObject("assets").get("large_image").getAsString().equals(PresenceProtocol.LOGO)||presence.getAsJsonObject("timestamps").get("start").getAsLong()!=456)throw new AssertionError("logo/timestamp");
  if(!JsonParser.parseString(PresenceProtocol.handshake()).getAsJsonObject().get("client_id").getAsString().equals("1527675374874529823"))throw new AssertionError("app ID");
  if(!JsonParser.parseString(PresenceProtocol.activity(123,0,"test",false)).getAsJsonObject().getAsJsonObject("args").get("activity").isJsonNull())throw new AssertionError("clear presence");
  ByteArrayOutputStream bytes=new ByteArrayOutputStream();PresenceProtocol.write(bytes,1,activity);
  byte[] frame=bytes.toByteArray();if(frame[0]!=1||frame[1]!=0||frame[2]!=0||frame[3]!=0)throw new AssertionError("little-endian opcode");
  var decoded=PresenceProtocol.read(new ByteArrayInputStream(frame));if(decoded.opcode()!=1||!decoded.text().equals(activity))throw new AssertionError("UTF-8 round trip");
  bytes.reset();PresenceProtocol.write(bytes,3,new byte[]{1,2,3});var ping=PresenceProtocol.read(new ByteArrayInputStream(bytes.toByteArray()));bytes.reset();PresenceProtocol.write(bytes,4,ping.data());if(PresenceProtocol.read(new ByteArrayInputStream(bytes.toByteArray())).opcode()!=4)throw new AssertionError("ping pong");
  try{PresenceProtocol.read(new ByteArrayInputStream(new byte[]{1,0,0,0,-1,-1,-1,-1}));throw new AssertionError("invalid frame length");}catch(IOException expected){}
  try{PresenceProtocol.read(new ByteArrayInputStream(new byte[]{1,0,0,0,2,0,0,0,1}));throw new AssertionError("truncated frame");}catch(IOException expected){}
  System.out.println("PASS: Moris domain gating, exact application ID/text/logo, clearing, UTF-8 little-endian RPC packets, PING/PONG and invalid packet rejection.");
 }
}
