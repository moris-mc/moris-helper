import org.NJ.hwamaihelper.client.*;
import net.minecraft.network.chat.*;
import java.net.URI;
public class ChatEmojiCheck {
 public static void main(String[] args){net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  URI gif=URI.create("https://cdn.discordapp.com/emojis/123.gif?size=48");
  var message=Component.literal("【DC】 Name ").append(Component.literal(":wave:").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(gif)))).append(Component.literal(" 😀👨‍👩‍👧‍👦🇹🇼"));
  var transformed=ChatEmoji.decorate(message);long count=transformed.getString().codePoints().filter(cp->cp==0x3000).count();if(count!=4)throw new AssertionError("Emoji grouping: "+transformed.getString());
  if(!ChatImages.urls(message).isEmpty())throw new AssertionError("Inline emoji reserved attachment rows");
  if(!ChatEmoji.decorate(Component.literal("ordinary player 😀")).getString().equals("ordinary player 😀"))throw new AssertionError("Unrelated chat changed");
  System.out.println("PASS: custom animated emoji, Unicode/ZWJ/flag grouping, zero attachment rows and ordinary chat preservation.");
 }
}
