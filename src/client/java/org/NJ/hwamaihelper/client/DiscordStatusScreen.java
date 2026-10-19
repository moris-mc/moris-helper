package org.NJ.hwamaihelper.client;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
public final class DiscordStatusScreen extends Screen {
 private final Screen parent; private final String channel;
 public DiscordStatusScreen(Screen parent,String channel){super(Component.literal(channel==null?"Discord 狀態":"Discord 頻道"));this.parent=parent;this.channel=channel;}
 protected void init(){addRenderableWidget(Button.builder(Component.literal("返回"),b->onClose()).bounds(width/2-40,height-40,80,20).build());}
 public void onClose(){minecraft.gui.setScreen(parent);}
 public boolean isPauseScreen(){return false;}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
  g.fill(12,20,width-12,height-16,0xEE35232F);
  String[] lines=channel==null?DiscordPresence.diagnostics():new String[]{channel,"尚未連接頻道資料服務","等待自製 DiscordSRV 的唯讀訊息串接。","接通後才能顯示內容與更新通知。"};
  g.text(minecraft.font,title,24,30,0xFFF2A5C6);int y=54;
  for(String line:lines){for(var text:minecraft.font.split(Component.literal(line),width-48)){g.text(minecraft.font,text,24,y,0xFFFFFFFF);y+=12;}y+=5;}
  super.extractRenderState(g,mx,my,delta);
 }
}
