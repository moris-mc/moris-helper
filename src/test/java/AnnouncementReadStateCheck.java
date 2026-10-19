import org.NJ.hwamaihelper.client.AnnouncementReadState;
import com.google.gson.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
public class AnnouncementReadStateCheck {
 public static void main(String[] args)throws Exception{
  Path directory=Path.of("build/read-state-check");Files.createDirectories(directory);Path file=directory.resolve("state-"+UUID.randomUUID()+".json");
  var state=new AnnouncementReadState(file);JsonObject message=JsonParser.parseString("{\"id\":\"1\",\"text\":\"更新公告\",\"attachments\":[{\"name\":\"a.png\",\"url\":\"https://cdn.discordapp.com/a.png?hm=one\"}]}").getAsJsonObject();
  String hash=AnnouncementReadState.fingerprint(message);state.snapshot("news",Map.of("1",hash));if(!state.unread("news"))throw new AssertionError("Fresh announcement not unread");
  state.markRead("news",Map.of("1",hash));if(state.unread("news"))throw new AssertionError("Read receipt ignored");
  message.getAsJsonArray("attachments").get(0).getAsJsonObject().addProperty("url","https://cdn.discordapp.com/a.png?hm=two");if(!AnnouncementReadState.fingerprint(message).equals(hash))throw new AssertionError("CDN signature created false unread");
  state.changed("news");state.markRead("news",Map.of("1",hash));if(!state.unread("news"))throw new AssertionError("Stale loaded page cleared a new update");
  message.addProperty("text","https://example.test/?內容=已編輯");String edited=AnnouncementReadState.fingerprint(message);state.snapshot("news",Map.of("1",edited));if(!state.unread("news"))throw new AssertionError("Edit did not become unread");state.markRead("news",Map.of("1",edited));
  state.saveAsync().get(3,TimeUnit.SECONDS);var loaded=new AnnouncementReadState(file);if(loaded.unread("news")||loaded.unread("news","1",edited))throw new AssertionError("Read state not persistent");
  message.addProperty("text","https://example.test/?內容=另一個更新");if(AnnouncementReadState.fingerprint(message).equals(edited))throw new AssertionError("Text after URL query was ignored");
  String saved=Files.readString(file);if(saved.contains("更新公告")||saved.contains("cdn.discordapp.com"))throw new AssertionError("Stored announcement bodies");
  loaded.changed("news");loaded.saveAsync().get(3,TimeUnit.SECONDS);if(!new AnnouncementReadState(file).unread("news"))throw new AssertionError("Pending update not persistent");
  System.out.println("PASS: persistent read receipts, edit detection, stable CDN signatures, stale-page protection, content hashing and pending changes.");
 }
}
