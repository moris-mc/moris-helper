package org.NJ.hwamaihelper.client;
import java.net.URI;
import java.util.*;
import net.dimaskama.mcef.api.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
/** Bounded, silent Chromium playback for GIF providers that deliver MP4/WebM. */
public final class BrowserMovie {
 private static final Set<BrowserMovie> LIVE=new LinkedHashSet<>();
 private final URI uri;private MCEFBrowser browser;private boolean pending,installed;private int ticket;private long seen;private int width=192,height=96;private String error;
 public BrowserMovie(URI uri){if(!DiscordImageUrl.video(uri))throw new IllegalArgumentException("Not trusted video");this.uri=uri;}
 public int width(){return width;}public int height(){return height;}
 public static void trim(){long now=System.nanoTime();for(var movie:List.copyOf(LIVE))if(now-movie.seen>10_000_000_000L)movie.close();}
 private void start(){if(pending||browser!=null||error!=null)return;if(LIVE.size()>=4){BrowserMovie oldest=LIVE.stream().min(java.util.Comparator.comparingLong(movie->movie.seen)).orElseThrow();if(System.nanoTime()-oldest.seen<250_000_000L)return;oldest.close();}LIVE.add(this);pending=true;int key=++ticket;
  try{MCEFApi.initialize().getFuture().whenComplete((api,failure)->Minecraft.getInstance().execute(()->{if(key!=ticket)return;pending=false;if(failure!=null){error="動畫瀏覽器啟動失敗";return;}try{browser=api.createBrowser("about:blank",false);browser.resize(384,192);browser.setFocus(false);}catch(Throwable e){error="動畫瀏覽器無法建立";}}));}catch(Throwable e){pending=false;error="動畫瀏覽器啟動失敗";}
 }
 public boolean draw(GuiGraphicsExtractor g,int x,int y,int w,int h){seen=System.nanoTime();start();if(browser==null)return false;
  if(!installed&&!browser.getCefBrowser().isLoading()){String json=BrowserJson.ascii(new com.google.gson.Gson().toJson(uri.toASCIIString()));browser.getCefBrowser().executeJavaScript("""
   (()=>{document.documentElement.style.cssText='margin:0;background:transparent;overflow:hidden';document.body.style.cssText='margin:0;overflow:hidden;background:transparent';const v=document.createElement('video');v.muted=true;v.autoplay=true;v.loop=true;v.playsInline=true;v.style.cssText='width:100vw;height:100vh;object-fit:contain';v.onloadedmetadata=()=>{location.hash='moris-size='+v.videoWidth+'x'+v.videoHeight};v.onerror=()=>{location.hash='moris-video-error'};v.src=%s;document.body.replaceChildren(v);v.play().catch(()=>{});})();
   """.formatted(json),"about:blank",0);installed=true;}
  String url=browser.getCefBrowser().getURL();int marker=url.indexOf("#moris-size=");if(marker>=0){try{String[] size=url.substring(marker+12).split("x");int nw=Integer.parseInt(size[0]),nh=Integer.parseInt(size[1]);if(nw>0&&nh>0&&nw<=8192&&nh<=8192&&(nw!=width||nh!=height)){width=nw;height=nh;double scale=Math.min(384.0/nw,192.0/nh);browser.resize(Math.max(1,(int)(nw*scale)),Math.max(1,(int)(nh*scale)));ChatImages.changed();}}catch(RuntimeException ignored){}}
  if(url.contains("#moris-video-error")){error="此動畫影片格式無法播放";return false;}
  if(browser.getTextureView()==null)return false;g.blit(browser.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),x,y,x+w,y+h,0,1,0,1);return true;
 }
 public String status(){return error==null?"動畫載入中…":error;}
 public void close(){ticket++;pending=false;installed=false;if(browser!=null){browser.close();browser=null;}LIVE.remove(this);}
}
