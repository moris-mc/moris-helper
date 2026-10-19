import java.lang.reflect.*;
import com.google.gson.*;
/** Exercises real feed state transitions without starting Minecraft or connecting to Discord. */
public class DiscordFeedCheck {
 public static void main(String[] args)throws Exception{
  Class<?> feed=Class.forName("org.NJ.hwamaihelper.client.DiscordFeed");
  Method receive=feed.getDeclaredMethod("receive",String.class);receive.setAccessible(true);
  String id="1496456026893975552";
  java.util.function.Consumer<String> push=s->{try{receive.invoke(null,s);}catch(Exception e){throw new RuntimeException(e);}};
  push.accept("{\"op\":\"begin\",\"channel\":\""+id+"\",\"append\":false}");
  for(String mid:new String[]{"1543903711808593941","1496456026893975552"})push.accept("{\"op\":\"message\",\"channel\":\""+id+"\",\"message\":{\"id\":\""+mid+"\",\"text\":\"中文 __底線__\"}}");
  push.accept("{\"op\":\"done\",\"channel\":\""+id+"\",\"more\":true}");
  Object page=feed.getMethod("page",String.class).invoke(null,id);Method json=page.getClass().getMethod("json");
  JsonObject state=JsonParser.parseString((String)json.invoke(page)).getAsJsonObject();
  if(state.getAsJsonArray("messages").size()!=2||!state.get("more").getAsBoolean())throw new AssertionError("history lost");
  if(!state.getAsJsonArray("messages").get(0).getAsJsonObject().get("id").getAsString().equals("1543903711808593941"))throw new AssertionError("incorrect ordering");
  Method count=feed.getMethod("unreadCount",String.class);
  if(!count.invoke(null,id).equals(2))throw new AssertionError("Unread count must be two");
  feed.getMethod("markRead",String.class,java.util.Collection.class).invoke(null,id,java.util.List.of("1543903711808593941"));
  if(!count.invoke(null,id).equals(1))throw new AssertionError("Single read must decrement count");
  feed.getMethod("markAllRead",String.class).invoke(null,id);
  if(!count.invoke(null,id).equals(0))throw new AssertionError("All read must clear count");
  push.accept("{\"op\":\"begin\",\"channel\":\""+id+"\",\"append\":true}");
  push.accept("{\"op\":\"error\",\"channel\":\""+id+"\",\"text\":\"權限不足\"}");
  state=JsonParser.parseString((String)json.invoke(page)).getAsJsonObject();
  if(state.get("loading").getAsBoolean()||!state.get("status").getAsString().equals("權限不足")||state.getAsJsonArray("messages").size()!=2)throw new AssertionError("error state");
  push.accept("{\"op\":\"begin\",\"channel\":\""+id+"\",\"append\":false}");
  push.accept("{\"op\":\"done\",\"channel\":\""+id+"\",\"more\":false}");
  if(JsonParser.parseString((String)json.invoke(page)).getAsJsonObject().getAsJsonArray("messages").size()!=0)throw new AssertionError("stale messages");
  try{feed.getMethod("page",String.class).invoke(null,"unlisted");throw new AssertionError("accepted unlisted channel");}catch(InvocationTargetException expected){if(!(expected.getCause() instanceof IllegalArgumentException))throw expected;}
  System.out.println("PASS: real feed history ordering, older-page retention, explicit errors, empty refresh, channel whitelist.");
 }
}
