import org.NJ.hwamaihelper.client.BrowserJson;
import com.google.gson.*;
public class BrowserJsonCheck {
 public static void main(String[] args){
  String text="森友 😀 🔶 🉑 👨‍👩‍👧‍👦 𓆩𓆪 \"\\\n";JsonObject object=new JsonObject();object.addProperty("text",text);
  String ascii=BrowserJson.ascii(object.toString());if(ascii.chars().anyMatch(c->c>=128))throw new AssertionError("Non-ASCII crosses JNI source boundary");
  if(!JsonParser.parseString(ascii).getAsJsonObject().get("text").getAsString().equals(text))throw new AssertionError("Unicode damaged");
  if(!ascii.contains("\\ud83d")||!ascii.contains("\\u68ee"))throw new AssertionError("Missing surrogate/BMP escapes");
  System.out.println("PASS: supplementary emoji, ZWJ families, supplementary symbols, Chinese, quotes and line breaks round-trip through ASCII JavaScript source.");
 }
}
