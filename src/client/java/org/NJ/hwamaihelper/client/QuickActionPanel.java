package org.NJ.hwamaihelper.client;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class QuickActionPanel {
 private final List<Button> buttons=new ArrayList<>();private final int x,y,w,h;private boolean open;
 public QuickActionPanel(int width,int height,Consumer<Button> add){
  w=Math.min(204,width-8);h=128;x=width-w-4;y=Math.max(4,height-h-44);
  String[] labels={"坐下","躺下","趴下","旋轉","家點","公共家點","飛行","戴頭上"};String[] commands={"sit","lay","crawl","spin","home","phome","fly","hat"};
  int cell=(w-24)/2;
  for(int i=0;i<labels.length;i++){String command=commands[i];Button b=new MouseOnlyButton(x+8+i%2*(cell+8),y+27+i/2*24,cell,20,labels[i],pressed->{var player=Minecraft.getInstance().player;if(player!=null)player.connection.sendCommand(command);close();});b.setTooltip(Tooltip.create(Component.literal("/"+command)));buttons.add(b);add.accept(b);}update();
 }
 public void toggle(){open=!open;update();}public void close(){open=false;update();}private void update(){for(Button b:buttons)b.visible=b.active=open;}
 public boolean contains(double mx,double my){return open&&mx>=x&&mx<x+w&&my>=y&&my<y+h;}
 public void click(MouseButtonEvent e,boolean doubled){for(Button b:buttons)if(b.visible&&b.mouseClicked(e,doubled))return;}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){if(!open)return;g.fill(x,y,x+w,y+h,0xEE35232F);g.fill(x,y,x+w,y+24,0xCC9E5879);g.fill(x,y,x+w,y+2,0xFFF2A5C6);g.fill(x,y+h-2,x+w,y+h,0xFFF2A5C6);g.fill(x,y,x+2,y+h,0xFFF2A5C6);g.fill(x+w-2,y,x+w,y+h,0xFFF2A5C6);g.text(Minecraft.getInstance().font,Component.literal("快捷選單"),x+9,y+9,0xFFFFFFFF);}
}
