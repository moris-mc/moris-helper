package org.NJ.hwamaihelper.client;
import java.net.URI;
import java.util.*;
import java.util.regex.*;
import net.minecraft.network.chat.*;
import net.minecraft.client.gui.*;
import net.minecraft.util.FormattedCharSequence;
/** Reserve one unbreakable glyph for each icon, keeping the original emoji hover/click. */
public final class ChatEmoji {
 private static final Pattern UNICODE=Pattern.compile("(?:[\\x{1F300}-\\x{1FAFF}\\x{2600}-\\x{27BF}][\\x{FE0F}\\x{FE0E}]?[\\x{1F3FB}-\\x{1F3FF}]?(?:\\x{200D}[\\x{1F300}-\\x{1FAFF}\\x{2600}-\\x{27BF}][\\x{FE0F}\\x{FE0E}]?[\\x{1F3FB}-\\x{1F3FF}]?)*|[\\x{1F1E6}-\\x{1F1FF}]{2}|[0-9#*]\\x{FE0F}?\\x{20E3})");
 public static URI uri(Style style){if(style.getClickEvent() instanceof ClickEvent.OpenUrl link&&DiscordImageUrl.emoji(link.uri()))return link.uri();return null;}
 public static Component decorate(Component source){var result=Component.empty();boolean discord=source.getString().contains("【DC】");
  source.visit((style,text)->{URI custom=uri(style);if(custom!=null){result.append(Component.literal("\u3000").withStyle(style));return Optional.empty();}
   if(!discord){result.append(Component.literal(text).withStyle(style));return Optional.empty();}
   var matches=UNICODE.matcher(text);int end=0;while(matches.find()){
    result.append(Component.literal(text.substring(end,matches.start())).withStyle(style));String emoji=matches.group();boolean zwj=emoji.indexOf('\u200d')>=0;
    String code=emoji.codePoints().filter(cp->zwj||cp!=0xfe0f).mapToObj(Integer::toHexString).collect(java.util.stream.Collectors.joining("-"));URI target=URI.create("https://cdn.jsdelivr.net/gh/jdecked/twemoji@17.0.3/assets/72x72/"+code+".png");
    result.append(Component.literal("\u3000").withStyle(style.withClickEvent(new ClickEvent.OpenUrl(target)).withHoverEvent(new HoverEvent.ShowText(Component.literal(emoji)))));end=matches.end();
   }result.append(Component.literal(text.substring(end)).withStyle(style));return Optional.empty();
  },Style.EMPTY);return result;
 }
 public static void draw(GuiGraphicsExtractor g,Font font,FormattedCharSequence line,int y){float[] x={0};line.accept((index,style,cp)->{var glyph=FormattedCharSequence.forward(new String(Character.toChars(cp)),style);int advance=font.width(glyph);URI uri=uri(style);if(cp==0x3000&&uri!=null)ChatImages.drawIcon(g,uri,(int)x[0],y,Math.max(1,advance));x[0]+=advance;return true;});}
 private ChatEmoji(){}
}
