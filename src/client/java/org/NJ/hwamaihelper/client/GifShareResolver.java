package org.NJ.hwamaihelper.client;
import java.net.URI;
import java.util.*;
import java.util.regex.*;
/** Reads public OpenGraph metadata only; never executes page scripts or sends credentials. */
public final class GifShareResolver {
 private static final Pattern META=Pattern.compile("<meta\\b[^>]{0,8192}>",Pattern.CASE_INSENSITIVE);
 private static final Pattern ATTR=Pattern.compile("([\\w:-]+)\\s*=\\s*([\"'])(.*?)\\2",Pattern.DOTALL);
 public static URI image(URI page,String html){
  if(!DiscordImageUrl.share(page))throw new IllegalArgumentException("Unsupported share page");
  URI best=null;int rank=-1;var tags=META.matcher(html);while(tags.find()){
   Map<String,String> attrs=new HashMap<>();var match=ATTR.matcher(tags.group());while(match.find())attrs.put(match.group(1).toLowerCase(Locale.ROOT),match.group(3));
   String type=attrs.getOrDefault("property",attrs.getOrDefault("name",""));if(!type.equalsIgnoreCase("og:image")&&!type.equalsIgnoreCase("og:image:secure_url")&&!type.equalsIgnoreCase("twitter:image")&&!type.equalsIgnoreCase("og:video")&&!type.equalsIgnoreCase("og:video:secure_url")&&!type.equalsIgnoreCase("twitter:player:stream"))continue;
   String content=attrs.get("content");if(content==null)continue;
   try{URI result=page.resolve(content.replace("&amp;","&").replace("&#38;","&"));if(DiscordImageUrl.direct(result)||DiscordImageUrl.video(result)){int score=result.getPath().endsWith(".mp4")?4:result.getPath().endsWith(".gif")?3:DiscordImageUrl.video(result)?2:1;if(score>rank){best=result;rank=score;}}}catch(IllegalArgumentException ignored){}
  }
  if(best!=null)return best;
  throw new IllegalArgumentException("Share page has no supported image preview");
 }
 private GifShareResolver(){}
}
