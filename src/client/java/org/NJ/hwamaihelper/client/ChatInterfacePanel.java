package org.NJ.hwamaihelper.client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import java.util.*;
import java.util.function.Consumer;
/** A two-level panel owned by the current ChatScreen; closing chat discards it. */
public final class ChatInterfacePanel implements Renderable {
 private final List<Button> entries=new ArrayList<>();
 private final Button category,back,notifications,testNotification;
 private final List<Button> folders=new ArrayList<>(),channels=new ArrayList<>();
 private final List<Button> websites=new ArrayList<>();
 private final int x,y,w,h;
 private final Map<Button,String> channelIds=new LinkedHashMap<>(),channelNames=new LinkedHashMap<>();
 private boolean open;
 private int page;
 public ChatInterfacePanel(int screenWidth,int screenHeight,Consumer<Button> add) {
  w=Math.min(224,screenWidth-8);h=128;x=screenWidth-w-4;y=Math.max(4,screenHeight-h-44);
  category=new MouseOnlyButton(x+8,y+27,100,20,"開啟功能介面",b->{page=1;update();});add.accept(category);
  back=new MouseOnlyButton(x+w-46,y+5,40,16,"返回",b->{page=0;update();});add.accept(back);
  for(int i=0;i<InterfaceCommands.ENTRIES.size();i++) {
   var entry=InterfaceCommands.ENTRIES.get(i);
   Button button=new ItemIconButton(x+10+(i%7)*28,y+30+(i/7)*28,entry,()->{
    var player=Minecraft.getInstance().player;
    if(player!=null) player.connection.sendCommand(entry.command());
    open=false;page=0;update();
   });
   entries.add(button);add.accept(button);
  }
  String[] labels={"公告","更新","Wiki","指令查詢"};
  for(int i=0;i<labels.length;i++) {
   String group=labels[i],target=i==3?"https://www.mcmoris.net/commands":i==2?"https://www.mcmoris.net/wiki":"https://www.mcmoris.net/news";
   Button button=new MouseOnlyButton(x+8+(i%2)*104,y+30+(i/2)*24,100,20,group,b->{var mc=Minecraft.getInstance();mc.gui.setScreen(new MorisWebScreen(mc.gui.screen(),target,group));});
   websites.add(button);add.accept(button);
  }
  String[] roots={"開啟官網頁面","Discord 頻道","Discord 狀態"};
  for(int i=0;i<roots.length;i++){int index=i;Button b=new MouseOnlyButton(x+8,y+51+i*24,100,20,roots[i],pressed->{if(index==2){var mc=Minecraft.getInstance();mc.gui.setScreen(new DiscordStatusScreen(mc.gui.screen(),null));}else{page=index+2;update();}});folders.add(b);add.accept(b);}
  String[] names={"公告","活動公告","更新公告","不重要公告","提醒與警告","贊助小森友","贊助感謝"};
  String[] ids={"1496456026893975552","1543903711808593941","1526988528674799729","1500375995457470485","1559943033963679744","1507814531903262863","1528619418123370647"};
  for(int i=0;i<names.length;i++){String label=names[i],id=ids[i];Button b=new MouseOnlyButton(x+8+i%2*104,y+28+i/2*23,100,20,label,pressed->{var mc=Minecraft.getInstance();mc.gui.setScreen(MorisWebScreen.discord(mc.gui.screen(),id,label));});channels.add(b);channelIds.put(b,id);channelNames.put(b,label);add.accept(b);}
  notifications=new MouseOnlyButton(x+116,y+27,100,20,"",b->{DiscordBanner.toggle();update();});add.accept(notifications);
  testNotification=new MouseOnlyButton(x+116,y+51,100,20,"測試通知",b->DiscordBanner.test());add.accept(testNotification);
  update();
 }
 public void close(){open=false;update();}
 public void toggle() { open=!open;page=0;update(); }
 private void update() {
  notifications.setMessage(Component.literal(DiscordBanner.enabled()?"關閉通知":"開啟通知"));notifications.visible=notifications.active=open&&page==0;testNotification.visible=testNotification.active=open&&page==0;
  category.visible=category.active=open&&page==0;
  back.visible=back.active=open&&page!=0;
  for(Button b:folders)b.visible=b.active=open&&page==0;
  for(Button b:channels)b.visible=b.active=open&&page==3;
  for(Button b:websites)b.visible=b.active=open&&page==2;
  for(Button b:entries) b.visible=b.active=open&&page==1;
 }
 public void click(net.minecraft.client.input.MouseButtonEvent event,boolean doubled) {
  if(notifications.visible&&notifications.mouseClicked(event,doubled))return;
  if(testNotification.visible&&testNotification.mouseClicked(event,doubled))return;
  for(Button b:folders)if(b.visible&&b.mouseClicked(event,doubled))return;
  for(Button b:channels)if(b.visible&&b.mouseClicked(event,doubled))return;
  if(category.visible&&category.mouseClicked(event,doubled)) return;
  for(Button b:websites)if(b.visible&&b.mouseClicked(event,doubled))return;
  if(back.visible&&back.mouseClicked(event,doubled)) return;
  for(Button b:entries) if(b.visible&&b.mouseClicked(event,doubled)) return;
 }
 public boolean contains(double mx,double my) { return open&&mx>=x&&mx<x+w&&my>=y&&my<y+h; }
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta) {
  if(!open) return;
  folders.get(1).setMessage(DiscordFeed.unreadLabel("Discord 頻道",DiscordFeed.totalUnread()));
  for(Button button:channels)button.setMessage(DiscordFeed.unreadLabel(channelNames.get(button),DiscordFeed.unreadCount(channelIds.get(button))));
  // Pink replaces the reference's blue panel; buttons retain vanilla textures.
  g.fill(x,y,x+w,y+h,0xEE35232F);
  g.fill(x,y,x+w,y+2,0xFFF2A5C6);g.fill(x,y+h-2,x+w,y+h,0xFFF2A5C6);
  g.fill(x,y,x+2,y+h,0xFFF2A5C6);g.fill(x+w-2,y,x+w,y+h,0xFFF2A5C6);
  g.fill(x+2,y+2,x+w-2,y+24,0xCC9E5879);
  g.text(Minecraft.getInstance().font,Component.literal(page==1?"開啟功能介面":page==2?"開啟官網頁面":page==3?"Discord 頻道":"選單"),x+9,y+9,0xFFFFFFFF);
 }
}
