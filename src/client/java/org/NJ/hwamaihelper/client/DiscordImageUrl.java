package org.NJ.hwamaihelper.client;
import java.net.URI;
import java.util.Locale;
public final class DiscordImageUrl {
 private static boolean secure(URI uri){return uri!=null&&"https".equalsIgnoreCase(uri.getScheme())&&uri.getHost()!=null&&uri.getUserInfo()==null&&(uri.getPort()==-1||uri.getPort()==443);}
 public static boolean share(URI uri){if(!secure(uri))return false;String h=uri.getHost().toLowerCase(Locale.ROOT),p=uri.getPath();return ((h.equals("klipy.com")||h.equals("www.klipy.com")||h.equals("giphy.com")||h.equals("www.giphy.com"))&&p.startsWith("/gifs/"))||((h.equals("tenor.com")||h.equals("www.tenor.com"))&&p.startsWith("/view/"));}
 public static boolean accepted(URI uri){return share(uri)||direct(uri)||video(uri);}
 public static boolean emoji(URI uri){return secure(uri)&&((uri.getHost().equalsIgnoreCase("cdn.discordapp.com")&&uri.getPath().matches("/emojis/[0-9]+\\.(?:png|gif|webp)"))||(uri.getHost().equalsIgnoreCase("cdn.jsdelivr.net")&&uri.getPath().startsWith("/gh/jdecked/twemoji@17.0.3/assets/72x72/")&&uri.getPath().endsWith(".png")));}
 public static boolean video(URI uri){return secure(uri)&&trusted(uri.getHost().toLowerCase(Locale.ROOT))&&uri.getPath().toLowerCase(Locale.ROOT).matches(".*\\.(?:mp4|webm)");}
 private static boolean trusted(String host){return host.equals("cdn.discordapp.com")||host.equals("media.discordapp.net")||host.matches("images-ext-[0-9]+\\.discordapp\\.net")||host.matches("static[0-9]*\\.klipy\\.com")||host.equals("media.tenor.com")||host.equals("c.tenor.com")||host.matches("(?:i|media[0-9]*)\\.giphy\\.com");}
 public static boolean direct(URI uri){
  if(!secure(uri))return false;String host=uri.getHost().toLowerCase(Locale.ROOT),path=uri.getPath().toLowerCase(Locale.ROOT);
  return (trusted(host)||emoji(uri))&&path.matches(".*\\.(?:png|jpe?g|gif|webp|avif)");
 }
 private DiscordImageUrl(){}
}
