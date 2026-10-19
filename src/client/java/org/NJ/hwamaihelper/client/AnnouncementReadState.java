package org.NJ.hwamaihelper.client;
import com.google.gson.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Local read receipts contain IDs and content hashes, never cached announcement bodies. */
public final class AnnouncementReadState {
 private static final ExecutorService IO=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"moris-read-state");t.setDaemon(true);return t;});
 private final Path file;
 private final Map<String,LinkedHashMap<String,String>> seen=new LinkedHashMap<>(),latest=new LinkedHashMap<>();
 private final Set<String> pending=new HashSet<>();
 public AnnouncementReadState(Path file){this.file=file;if(file!=null&&Files.isRegularFile(file))try{
  JsonObject root=JsonParser.parseString(Files.readString(file)).getAsJsonObject();readMaps(root,"seen",seen);readMaps(root,"latest",latest);
  if(root.has("pending"))for(JsonElement e:root.getAsJsonArray("pending"))pending.add(e.getAsString());
 }catch(Exception e){System.err.println("[MorisHelper] Cannot load announcement read state: "+e.getClass().getSimpleName());}}
 private static void readMaps(JsonObject root,String name,Map<String,LinkedHashMap<String,String>> output){if(!root.has(name))return;for(var channel:root.getAsJsonObject(name).entrySet()){var map=new LinkedHashMap<String,String>();for(var entry:channel.getValue().getAsJsonObject().entrySet())map.put(entry.getKey(),entry.getValue().getAsString());output.put(channel.getKey(),map);}}
 public Set<String> unreadIds(String channel){Set<String> ids=new HashSet<>();latest.getOrDefault(channel,new LinkedHashMap<>()).forEach((id,hash)->{if(unread(channel,id,hash))ids.add(id);});return ids;}
 public boolean pending(String channel){return pending.contains(channel);}
 public boolean unread(String channel){return pending.contains(channel)||latest.getOrDefault(channel,new LinkedHashMap<>()).entrySet().stream().anyMatch(e->unread(channel,e.getKey(),e.getValue()));}
 public boolean unread(String channel,String id,String fingerprint){return !fingerprint.equals(seen.getOrDefault(channel,new LinkedHashMap<>()).get(id));}
 public void snapshot(String channel,Map<String,String> messages){latest.put(channel,new LinkedHashMap<>(messages));pending.remove(channel);saveAsync();}
 public void changed(String channel){pending.add(channel);saveAsync();}
 public void markRead(String channel,Map<String,String> messages){var saved=seen.computeIfAbsent(channel,k->new LinkedHashMap<>());messages.forEach((id,hash)->{saved.remove(id);saved.put(id,hash);});while(saved.size()>512)saved.remove(saved.keySet().iterator().next());saveAsync();}
 public CompletableFuture<Void> saveAsync(){if(file==null)return CompletableFuture.completedFuture(null);JsonObject root=new JsonObject();Gson gson=new Gson();root.add("seen",gson.toJsonTree(seen));root.add("latest",gson.toJsonTree(latest));root.add("pending",gson.toJsonTree(pending));String text=root.toString();return CompletableFuture.runAsync(()->{try{
  Files.createDirectories(file.getParent());Path temporary=file.resolveSibling(file.getFileName()+".tmp");Files.writeString(temporary,text,StandardCharsets.UTF_8);
  try{Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException ignored){Files.move(temporary,file,StandardCopyOption.REPLACE_EXISTING);}
 }catch(Exception e){System.err.println("[MorisHelper] Cannot save announcement read state: "+e.getClass().getSimpleName());}},IO);}
 public static String fingerprint(JsonObject message){JsonObject stable=new JsonObject();for(String key:List.of("id","text","embeds","attachments"))if(message.has(key))stable.add(key,key.equals("text")?message.get(key).deepCopy():normalize(message.get(key)));try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(stable.toString().getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
 private static JsonElement normalize(JsonElement element){
  if(element.isJsonObject()){JsonObject copy=new JsonObject();element.getAsJsonObject().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e->{JsonElement v=e.getValue();if(Set.of("url","image","thumbnail").contains(e.getKey())&&v.isJsonPrimitive()&&v.getAsJsonPrimitive().isString()){String value=v.getAsString();int query=value.indexOf('?');copy.addProperty(e.getKey(),query>=0&&value.startsWith("https://")?value.substring(0,query):value);}else copy.add(e.getKey(),normalize(v));});return copy;}
  if(element.isJsonArray()){JsonArray copy=new JsonArray();element.getAsJsonArray().forEach(e->copy.add(normalize(e)));return copy;}
  return element.deepCopy();
 }
}
