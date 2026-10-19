package org.NJ.hwamaihelper.client;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import net.dimaskama.mcef.api.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2f;
import java.net.URI;
/** Real Chromium content in a centered Minecraft window. */
public final class MorisWebScreen extends Screen {
 private final Screen parent;
 private final boolean imageViewer;
 private String feedChannel;
 private long feedVersion=Long.MIN_VALUE;
 private int feedScrollY;
 private Button olderButton,markReadButton;
 private final java.util.Map<Button,String> feedButtons=new java.util.LinkedHashMap<>(),feedNames=new java.util.LinkedHashMap<>();
 private static final String FEED_SCRIPT=readFeedScript();
 private static String readFeedScript(){try(var in=MorisWebScreen.class.getResourceAsStream("/assets/senyouhelper/discord-feed.js")){return new String(java.util.Objects.requireNonNull(in).readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}}
 private boolean draggingWindow;
 private double dragX,dragY;
 private final String chatDraft;
 private String url,category;
 private MCEFBrowser browser;
 private MCEFApi.Initialization initialization;
 private BrowserViewport viewport;
 private int left,top,panelWidth,panelHeight,generation,ticks;
 private boolean requested,browserFocus;
 private volatile String error;
 private String preedit="";
 private final java.util.Set<Integer> editingKeys=new java.util.HashSet<>();
 public MorisWebScreen(Screen parent,String url,String category) {
  super(Component.literal("森友官網"));this.parent=parent;this.url=url;this.category=category;this.imageViewer=category.equals("圖片");
  chatDraft=parent instanceof ChatScreen?parent.children().stream().filter(e->e instanceof EditBox).map(e->((EditBox)e).getValue()).findFirst().orElse(""):null;
 }
 public static MorisWebScreen discord(net.minecraft.client.gui.screens.Screen parent,String id,String name){MorisWebScreen s=new MorisWebScreen(parent,"about:blank",name);s.feedChannel=id;DiscordFeed.request(id,false);return s;}
 private void switchFeed(String id,String name){
  feedChannel=id;category=name;feedVersion=Long.MIN_VALUE;feedScrollY=0;
  if(browser!=null)browser.getCefBrowser().executeJavaScript("window.scrollTo(0,0);",currentUrl(),0);
  DiscordFeed.request(id,false);rebuildWidgets();
 }
 public boolean isPauseScreen(){return false;}
 protected void init() {
  panelWidth=imageViewer?Math.min(600,Math.max(220,(int)(width*.74))):Math.min(960,width-20);panelHeight=imageViewer?Math.min(420,(int)(height*.76)):Math.min(640,height-20);
  left=(width-panelWidth)/2;top=(height-panelHeight)/2;
  int scale=Math.max(1,(int)minecraft.getWindow().getGuiScale());
  int sidebar=feedChannel!=null?92:4;
  viewport=new BrowserViewport(left+sidebar,top+29,panelWidth-sidebar-4,panelHeight-46,scale);
  if(feedChannel!=null){
   feedButtons.clear();feedNames.clear();
   String[] channelNames={"公告","活動公告","更新公告","不重要公告","提醒與警告","贊助小森友","贊助感謝"};
   String[] channelIds={"1496456026893975552","1543903711808593941","1526988528674799729","1500375995457470485","1559943033963679744","1507814531903262863","1528619418123370647"};
   for(int i=0;i<channelNames.length;i++){String id=channelIds[i],name=channelNames[i];var button=addRenderableWidget(new MouseOnlyButton(left+5,top+32+i*23,82,20,name,b->switchFeed(id,name)));button.active=!id.equals(feedChannel);feedButtons.put(button,id);feedNames.put(button,name);}
   int right=left+panelWidth;
   markReadButton=addRenderableWidget(Button.builder(Component.literal("全已讀"),b->DiscordFeed.markAllRead(feedChannel)).bounds(right-172,top+5,38,18).build());markReadButton.setTooltip(Tooltip.create(Component.literal("將目前頻道已載入的公告標為已讀")));
   addRenderableWidget(Button.builder(Component.literal("重整"),b->DiscordFeed.request(feedChannel,false)).bounds(right-130,top+5,38,18).build());
   olderButton=addRenderableWidget(Button.builder(Component.literal("較舊"),b->DiscordFeed.request(feedChannel,true)).bounds(right-88,top+5,38,18).build());
   addRenderableWidget(Button.builder(Component.literal("關閉"),b->onClose()).bounds(right-46,top+5,40,18).build());
  }else if(imageViewer){
   String[] labels={"符合","1:1","－","＋","×"};String[] commands={"fit()","original()","zoom(0.8)","zoom(1.25)",""};
   int x=left+panelWidth-142;
   for(int i=0;i<labels.length;i++){
    int index=i,w=i<2?32:22;
    addRenderableWidget(Button.builder(Component.literal(labels[i]),b->{if(index==4)onClose();else imageCommand(commands[index]);}).bounds(x,top+5,w,18).build());x+=w+2;
   }
  } else {
  int x=left+5;
  String[] names={"公告","更新","Wiki"};
  for(int i=0;i<3;i++) {
   String group=names[i],target=i==2?"https://www.mcmoris.net/wiki":"https://www.mcmoris.net/news";
   addRenderableWidget(Button.builder(Component.literal(group),b->navigate(target,group)).bounds(x,top+5,38,18).build());x+=40;
  }
  addRenderableWidget(Button.builder(Component.literal("返回"),b->{if(browser!=null&&browser.getCefBrowser().canGoBack())browser.getCefBrowser().goBack();}).bounds(x,top+5,32,18).build());x+=34;
  addRenderableWidget(Button.builder(Component.literal("重整"),b->{if(browser!=null)browser.getCefBrowser().reload();}).bounds(x,top+5,32,18).build());
  addRenderableWidget(Button.builder(Component.literal("網站"),b->ConfirmLinkScreen.confirmLinkNow(this,URI.create(currentUrl()))).bounds(left+panelWidth-73,top+5,32,18).build());
  addRenderableWidget(Button.builder(Component.literal("關閉"),b->onClose()).bounds(left+panelWidth-39,top+5,34,18).build());
  }
  if(browser!=null)resizeBrowser();else startBrowser();
 }
 private String currentUrl(){return browser==null?url:browser.getCefBrowser().getURL();}
 private void startBrowser() {
  if(requested)return;requested=true;int ticket=++generation;
  try {
   initialization=MCEFApi.initialize();
   initialization.getFuture().whenComplete((api,failure)->minecraft.execute(()->{
    if(ticket!=generation||minecraft.gui.screen()!=this)return;
    if(failure!=null){error="瀏覽器核心載入失敗，請查看 latest.log。";return;}
    try {browser=api.createBrowser(url,false);feedVersion=Long.MIN_VALUE;resizeBrowser();browser.setFocus(false);}
    catch(Throwable e){error="無法建立瀏覽器，請查看 latest.log。";System.err.println("[MorisHelper] "+e);}
   }));
  } catch(Throwable e){error="瀏覽器初始化失敗，請查看 latest.log。";System.err.println("[MorisHelper] "+e);}
 }
 private void resizeBrowser(){browser.resize(viewport.pixelWidth(),viewport.pixelHeight());}
 private void imageCommand(String command){if(browser!=null)browser.getCefBrowser().executeJavaScript("window.morisImage?."+command+";",currentUrl(),0);}
 private void moveImageWindow(double mx,double my){
  int nextX=Math.max(0,Math.min(width-panelWidth,(int)(mx-dragX))),nextY=Math.max(0,Math.min(height-panelHeight,(int)(my-dragY)));
  int dx=nextX-left,dy=nextY-top;left=nextX;top=nextY;
  for(var child:children())if(child instanceof AbstractWidget widget){widget.setX(widget.getX()+dx);widget.setY(widget.getY()+dy);}
  viewport=new BrowserViewport(left+4,top+29,panelWidth-8,panelHeight-46,viewport.scale());
 }
 private void navigate(String target,String group){url=target;category=group;if(browser!=null)browser.getCefBrowser().loadURL(target);}
 public void tick(){
  if(feedChannel!=null){
   for(var entry:feedButtons.entrySet())entry.getKey().setMessage(DiscordFeed.unreadLabel(feedNames.get(entry.getKey()),DiscordFeed.unreadCount(entry.getValue())));
   if(browser!=null&&currentUrl().contains("#moris-read=")){
    String marker=currentUrl().substring(currentUrl().indexOf("#moris-read=")+12);int split=marker.indexOf('&');String id=split<0?marker:marker.substring(0,split);
    if(id.matches("[0-9]{1,19}")){DiscordFeed.markRead(feedChannel,java.util.List.of(id));browser.getCefBrowser().executeJavaScript("try{history.replaceState(null,'','about:blank');}catch(e){location.hash='';}",currentUrl(),0);}
   }
   if(browser!=null&&currentUrl().contains("#moris-open=")){
    String marker=currentUrl().substring(currentUrl().indexOf("#moris-open=")+12);int split=marker.lastIndexOf("&scroll=");
    try{String target=java.net.URLDecoder.decode(split<0?marker:marker.substring(0,split),java.nio.charset.StandardCharsets.UTF_8);java.net.URI uri=java.net.URI.create(target);
     if(split>=0)feedScrollY=Math.max(0,Integer.parseInt(marker.substring(split+8)));
     if(uri.getScheme().equals("https")&&(uri.getHost().equals("mcmoris.net")||uri.getHost().equals("www.mcmoris.net"))&&uri.getUserInfo()==null&&(uri.getPort()==-1||uri.getPort()==443)){
      minecraft.gui.setScreen(new MorisWebScreen(this,target,"官網"));return;
     }
    }catch(RuntimeException ignored){}
    browser.getCefBrowser().loadURL("about:blank");feedVersion=Long.MIN_VALUE;
   }
   var page=DiscordFeed.page(feedChannel);if(markReadButton!=null)markReadButton.active=!page.loading&&!page.messages.isEmpty();if(olderButton!=null)olderButton.active=page.more&&!page.loading;
   if(browser!=null&&++ticks%10==0&&!browser.getCefBrowser().isLoading()){
    if(feedVersion!=page.version){
     String json=BrowserJson.ascii(page.json());
     browser.getCefBrowser().executeJavaScript(FEED_SCRIPT+";if(window.morisFeed){window.morisFeed.render("+json+");if(!window.morisFeedRestored){window.scrollTo(0,"+feedScrollY+");window.morisFeedRestored=true;}}",currentUrl(),0);feedVersion=page.version;
    }

   }
   return;
  }
  if(browser!=null&&++ticks%(imageViewer?5:20)==0) {
   if(imageViewer){browser.getCefBrowser().executeJavaScript(ImageViewerScript.INSTALL,currentUrl(),0);return;}
   // Keep category filtering working after the site's client-side navigation.
   String group=category.equals("更新")?"更新":"公告";
   browser.getCefBrowser().executeJavaScript("""
    (()=>{if(location.hostname!=='www.mcmoris.net')return;
      let s=document.getElementById('moris-helper-style');
      if(!s){s=document.createElement('style');s.id='moris-helper-style';document.head.appendChild(s);}
      s.textContent='.site-header,.mobile-navigation,.scroll-progress{display:none!important} body{margin:0!important}';
      if(location.pathname==='/news'||location.pathname==='/news/'){
        document.querySelectorAll('.news-category-section').forEach(e=>{
          e.style.display=e.classList.contains('news-wiki-tutorials')||!e.querySelector('header')?.textContent.includes('%s')?'none':'';
        });
      }
    })();
    """.formatted(group),browser.getCefBrowser().getURL(),0);
  }
 }
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  if(!imageViewer)g.fill(0,0,width,height,0x88000000);
  g.fill(left,top,left+panelWidth,top+panelHeight,0xF0201720);
  g.fill(left,top+25,left+panelWidth,top+27,0xFFE5A2C3);
  g.fill(viewport.x(),viewport.y(),viewport.x()+viewport.width(),viewport.y()+viewport.height(),0xFF20242A);
  if(browser!=null&&browser.getTextureView()!=null){
   g.blit(browser.getTextureView(),RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR),viewport.x(),viewport.y(),viewport.x()+viewport.width(),viewport.y()+viewport.height(),0,1,0,1);
   if(viewport.contains(mx,my))g.requestCursor(browser.getCursorType());
  } else {
   String status=error;
   if(status==null){status="正在準備網頁瀏覽器…";if(initialization!=null){status=switch(initialization.getStage()){
    case DOWNLOADING->"首次使用：下載瀏覽器核心中…";case EXTRACTING,INSTALL->"解壓縮／安裝瀏覽器核心中…";case INITIALIZING->"啟動瀏覽器中…";default->"等待網頁畫面…";};float p=initialization.getPercentage();if(p>=0)status+=" "+(int)p+"%";}}
   g.centeredText(font,Component.literal(status),width/2,height/2,0xFFFFFFFF);
  }
  if(feedChannel!=null)g.text(font,Component.literal(category),left+7,top+10,0xFFFFFFFF);
  if(imageViewer)g.text(font,Component.literal("圖片"),left+7,top+10,0xFFFFFFFF);
  String hint=feedChannel!=null?"Discord 頻道 · 僅供閱讀 · Esc 返回":imageViewer?"滾輪縮放 · 拖曳圖片／標題 · Esc 返回":"森友官網 · "+category+" · 滾輪捲動 · Esc 返回";
  g.text(font,Component.literal(font.plainSubstrByWidth(hint,panelWidth-12)),left+6,top+panelHeight-12,0xFFBBBBBB);
  if(!preedit.isEmpty()) {
   String text=font.plainSubstrByWidth("輸入中："+preedit,panelWidth-20);
   g.fill(left+4,top+panelHeight-31,left+panelWidth-4,top+panelHeight-15,0xFF4C3045);
   g.text(font,Component.literal(text),left+8,top+panelHeight-27,0xFFFFFFFF);
  }
  super.extractRenderState(g,mx,my,delta);
 }
 private MouseButtonEvent translate(MouseButtonEvent e){return new MouseButtonEvent(viewport.pixelX(e.x()),viewport.pixelY(e.y()),e.buttonInfo());}
 public boolean mouseClicked(MouseButtonEvent e,boolean doubled){
  if(imageViewer&&e.button()==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT&&e.x()>=left&&e.x()<left+panelWidth-146&&e.y()>=top&&e.y()<top+25){draggingWindow=true;dragX=e.x()-left;dragY=e.y()-top;return true;}
  browserFocus=browser!=null&&viewport.contains(e.x(),e.y());
  if(browser!=null)browser.setFocus(browserFocus);
  if(browserFocus){
   minecraft.textInputManager().startTextInput(this);
   minecraft.textInputManager().setTextInputArea((int)e.x(),(int)e.y(),(int)e.x()+1,(int)e.y()+12);
  } else {editingKeys.clear();minecraft.textInputManager().stopTextInput(this);preedit="";}
  if(browserFocus){setFocused(null);browser.onMouseClicked(translate(e),doubled);return true;}
  return super.mouseClicked(e,doubled);
 }
 public boolean mouseReleased(MouseButtonEvent e){if(draggingWindow){draggingWindow=false;return true;}if(browser!=null&&browserFocus){browser.onMouseReleased(translate(e));return true;}return super.mouseReleased(e);}
 public void mouseMoved(double x,double y){if(draggingWindow){moveImageWindow(x,y);return;}if(browser!=null)browser.onMouseMoved((int)viewport.pixelX(x),(int)viewport.pixelY(y));super.mouseMoved(x,y);}
 public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(draggingWindow){moveImageWindow(e.x(),e.y());return true;}return super.mouseDragged(e,dx,dy);}
 public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(browser!=null&&viewport.contains(x,y)){browser.onMouseScrolled((int)viewport.pixelX(x),(int)viewport.pixelY(y),vertical);return true;}return super.mouseScrolled(x,y,horizontal,vertical);}
 public boolean keyPressed(KeyEvent e){
  if(e.key()==com.mojang.blaze3d.platform.InputConstants.KEY_ESCAPE){
   if(!preedit.isEmpty()){preedit="";minecraft.textInputManager().stopTextInput(this);minecraft.textInputManager().startTextInput(this);return true;}
   onClose();return true;
  }
  if(browser!=null&&browserFocus){
   if(preedit.isEmpty()) {
    if(editKey(e)){editingKeys.add(e.key());return true;}
    browser.onKeyPressed(e);
   }
   return true;
  }
  return super.keyPressed(e);
 }
 private boolean editKey(KeyEvent e) {
  var action=BrowserEditing.action(e.key(),e.modifiers(),!preedit.isEmpty());
  if(action==BrowserEditing.Action.NONE)return false;
  var frame=browser.getCefBrowser().getFocusedFrame();
  if(frame==null)return false;
  switch(action) {
   case SELECT_ALL->frame.selectAll();case COPY->frame.copy();case CUT->frame.cut();case PASTE->frame.paste();case UNDO->frame.undo();case REDO->frame.redo();
   case BACKSPACE,DELETE->frame.executeJavaScript(BrowserEditing.deletionScript(action==BrowserEditing.Action.DELETE),browser.getCefBrowser().getURL(),0);
   default->{return false;}
  }
  return true;
 }
 public boolean keyReleased(KeyEvent e){if(editingKeys.remove(e.key()))return true;if(browser!=null&&browserFocus){if(preedit.isEmpty())browser.onKeyReleased(e);return true;}return super.keyReleased(e);}
 public boolean capturesInput(){return browserFocus;}
 public boolean preeditUpdated(PreeditEvent e){preedit=e==null?"":e.fullText();return browserFocus;}
 public boolean charTyped(CharacterEvent e){
  if(browser!=null&&browserFocus){
   preedit="";
   // MCEF's AWT adapter accepts UTF-16 chars, so preserve supplementary Unicode.
   for(char c:Character.toChars(e.codepoint()))browser.onCharTyped(new CharacterEvent(c));
   return true;
  }
  return super.charTyped(e);
 }
 public void removed(){editingKeys.clear();minecraft.textInputManager().stopTextInput(this);preedit="";browserFocus=false;generation++;requested=false;if(browser!=null){url=feedChannel==null?currentUrl():"about:blank";browser.close();browser=null;}}
 public void onClose(){minecraft.gui.setScreen(parent);if(chatDraft!=null)for(var child:parent.children())if(child instanceof EditBox box){box.setValue(chatDraft);parent.setFocused(box);break;}}
}