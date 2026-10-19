package org.NJ.hwamaihelper.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.*;
public final class ChatImages {
 private static long revision;
 public static long revision(){return revision;}
 public static void changed(){revision++;}
 public static void tick(){BrowserMovie.trim();}
 private static final int LIMIT=64,MAX_BYTES=20*1024*1024;
 private static final ExecutorService IO=Executors.newFixedThreadPool(2,r->{Thread t=new Thread(r,"moris-images");t.setDaemon(true);return t;});
 private static final HttpClient HTTP=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).followRedirects(HttpClient.Redirect.NEVER).build();
 private static final LinkedHashMap<URI,Image> CACHE=new LinkedHashMap<>();
 private static int serial,generation;
 public static final class Image { Identifier id;DynamicTexture texture;String state="圖片載入中…";int width,height,frame;DiscordImageData.Animation animation;long started;URI resolved;BrowserMovie movie; }
 public static List<URI> urls(Component message){
  LinkedHashSet<URI> urls=new LinkedHashSet<>();
  message.visit((style,text)->{if(style.getClickEvent() instanceof ClickEvent.OpenUrl link&&DiscordImageUrl.accepted(link.uri())&&!DiscordImageUrl.emoji(link.uri())&&urls.size()<3)urls.add(link.uri());return Optional.empty();},Style.EMPTY);
  return List.copyOf(urls);
 }
 public static int rows(URI uri,int maxWidth,int lineHeight){Image image=image(uri);int w=image.movie!=null?image.movie.width():image.width,h=image.movie!=null?image.movie.height():image.height;int height=image.texture==null&&image.movie==null?14:MediaLayout.height(w,h,maxWidth);return MediaLayout.rows(height,lineHeight);}
 public static Component decorate(Component message,int width,int lineHeight){
  List<URI> urls=urls(message);var result=ChatEmoji.decorate(message);if(urls.isEmpty())return result;
  int rows=0;for(URI uri:urls)rows+=rows(uri,Math.min(192,width-8),lineHeight);
  return result.copy().append(Component.literal(("\n\u00a0").repeat(rows)).withStyle(Style.EMPTY));
 }
 public static Image image(URI uri){
  Image existing=CACHE.get(uri);if(existing!=null)return existing;
  while(CACHE.size()>=LIMIT){var it=CACHE.entrySet().iterator();var old=it.next().getValue();it.remove();release(old);}
  Image result=new Image();CACHE.put(uri,result);int ticket=generation;
  CompletableFuture.supplyAsync(()->{try{URI source=resolve(uri);result.resolved=source;return source.getPath().toLowerCase(java.util.Locale.ROOT).endsWith(".webm")?null:download(source);}catch(Exception e){throw new CompletionException(e);}},IO).whenComplete((pixels,error)->Minecraft.getInstance().execute(()->{
   if(ticket!=generation||CACHE.get(uri)!=result)return;
   if(!DiscordImageUrl.emoji(uri))changed();
   if(error!=null){Throwable cause=error;while(cause.getCause()!=null)cause=cause.getCause();result.state=cause instanceof Unavailable?"圖片連結失效，請重新傳送附件":DiscordImageUrl.share(uri)?"等待 Discord 預覽（可點開查看）":"圖片無法載入（可點原連結）";System.err.println("[MorisHelper] Image load: "+cause.getMessage());return;}
   if(pixels==null){result.movie=new BrowserMovie(result.resolved);result.width=192;result.height=96;return;}
   long budget=(long)pixels.frames().size()*pixels.frames().getFirst().getWidth()*pixels.frames().getFirst().getHeight();
   for(var cached:CACHE.values())if(cached.animation!=null)budget+=(long)cached.width*cached.height*cached.animation.frames().size();
   var entries=CACHE.entrySet().iterator();while(budget>16000000&&entries.hasNext()){var old=entries.next().getValue();if(old==result)continue;if(old.animation!=null)budget-=(long)old.width*old.height*old.animation.frames().size();entries.remove();release(old);}
   result.animation=pixels;result.started=System.nanoTime();BufferedImage first=pixels.frames().getFirst();
   NativeImage image=new NativeImage(first.getWidth(),first.getHeight(),false);
   for(int y=0;y<first.getHeight();y++)for(int x=0;x<first.getWidth();x++)image.setPixel(x,y,first.getRGB(x,y));
   result.width=first.getWidth();result.height=first.getHeight();
   result.id=Identifier.fromNamespaceAndPath("senyouhelper","chat_image/"+(++serial));
   result.texture=new DynamicTexture(()->"Moris Discord image",image);
   Minecraft.getInstance().getTextureManager().register(result.id,result.texture);result.state="";
  }));return result;
 }
 private static final class Unavailable extends IOException {Unavailable(int status){super("Image HTTP "+status);}}
 public static URI viewUrl(URI original){Image image=CACHE.get(original);return image!=null&&image.resolved!=null?image.resolved:original;}
 private static URI resolve(URI uri)throws Exception {
  if(!DiscordImageUrl.share(uri))return uri;
  for(int redirects=0;redirects<3;redirects++){
   var response=HTTP.send(HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(10)).header("Accept","text/html").header("User-Agent","MorisHelper/1.13.1").GET().build(),HttpResponse.BodyHandlers.ofInputStream());
   try(var input=response.body()){
    if(response.statusCode()>=300&&response.statusCode()<400){URI next=uri.resolve(response.headers().firstValue("Location").orElseThrow());if(!DiscordImageUrl.share(next))throw new IOException("Unsupported share redirect");uri=next;continue;}
    if(response.statusCode()!=200)throw new IOException("Share page HTTP "+response.statusCode());
    byte[] html=input.readNBytes(1024*1024+1);if(html.length>1024*1024)throw new IOException("Share page too large");return GifShareResolver.image(uri,new String(html,java.nio.charset.StandardCharsets.UTF_8));
   }
  }throw new IOException("Too many share redirects");
 }
 private static DiscordImageData.Animation download(URI uri)throws Exception {
  if(!DiscordImageUrl.direct(uri)&&!DiscordImageUrl.video(uri))throw new IOException("unsupported image URL");
  HttpRequest request=HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(12)).header("Accept","image/png,image/jpeg,image/gif,video/mp4").GET().build();
  var response=HTTP.send(request,HttpResponse.BodyHandlers.ofInputStream());
  byte[] bytes;
  try(InputStream in=response.body()){
   if(response.statusCode()==404||response.statusCode()==403||response.statusCode()==410)throw new Unavailable(response.statusCode());if(response.statusCode()!=200)throw new IOException("HTTP "+response.statusCode());
   String type=response.headers().firstValue("Content-Type").orElse("");if(!type.startsWith("image/")&&!(DiscordImageUrl.video(uri)&&type.startsWith("video/")))throw new IOException("not image");
   bytes=in.readNBytes(MAX_BYTES+1);if(bytes.length>MAX_BYTES)throw new IOException("image too large");
  }
  return DiscordImageUrl.video(uri)?VideoFrames.decode(bytes):DiscordImageData.animation(bytes);
 }

 public static void drawIcon(GuiGraphicsExtractor g,URI uri,int x,int y,int advance){Image image=image(uri);if(image.texture!=null)draw(g,uri,x,y,advance,9);else g.text(Minecraft.getInstance().font,Component.literal("?"),x,y,0xFFBBBBBB);}
 public static int[] draw(GuiGraphicsExtractor g,URI uri,int x,int y,int maxWidth,int maxHeight){
  Image image=image(uri);
  if(image.movie!=null){int w=image.movie.width(),h=image.movie.height();double ratio=Math.min(1,Math.min((double)maxWidth/w,(double)maxHeight/h));int dw=Math.max(1,(int)(w*ratio)),dh=Math.max(1,(int)(h*ratio));if(!image.movie.draw(g,x,y,dw,dh))g.text(Minecraft.getInstance().font,Component.literal(image.movie.status()),x,y,0xFFCCCCCC);return new int[]{dw,dh};}
  if(image.texture==null){g.text(Minecraft.getInstance().font,Component.literal(image.state),x,y+3,0xFFCCCCCC);return new int[]{Math.min(maxWidth,180),14};}
  if(image.animation.frames().size()>1){
   long total=0;for(int delay:image.animation.delays())total+=delay;
   long elapsed=((System.nanoTime()-image.started)/1000000)%total;int frame=0;
   while(frame<image.animation.delays().length-1&&elapsed>=image.animation.delays()[frame])elapsed-=image.animation.delays()[frame++];
   if(frame!=image.frame){image.frame=frame;var pixels=image.animation.frames().get(frame);var nativePixels=image.texture.getPixels();
    for(int py=0;py<image.height;py++)for(int px=0;px<image.width;px++)nativePixels.setPixel(px,py,pixels.getRGB(px,py));image.texture.upload();}
  }
  double ratio=Math.min(1,Math.min((double)maxWidth/image.width,(double)maxHeight/image.height));int w=Math.max(1,(int)(image.width*ratio)),h=Math.max(1,(int)(image.height*ratio));
  g.blit(image.texture.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),x,y,x+w,y+h,0,1,0,1);return new int[]{w,h};
 }
 private static void release(Image image){if(image.movie!=null)image.movie.close();if(image.id!=null)Minecraft.getInstance().getTextureManager().release(image.id);}
 public static void clear(){ImageHitBoxes.clear();generation++;CACHE.values().forEach(ChatImages::release);CACHE.clear();changed();}
 private ChatImages(){}
}
