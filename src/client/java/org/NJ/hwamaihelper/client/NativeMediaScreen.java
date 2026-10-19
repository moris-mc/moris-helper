package org.NJ.hwamaihelper.client;
import java.net.URI;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
/** Native animated viewer shares decoded frames with chat; no browser video element is involved. */
public final class NativeMediaScreen extends Screen {
 private final Screen parent;private final URI uri;private final String draft;private int left,top,pw,ph;private double zoom=1,panX,panY;private boolean fit=true,drag;private double lastX,lastY;
 public static Screen open(Screen parent,URI uri){String path=uri.getPath().toLowerCase(java.util.Locale.ROOT);return path.endsWith(".gif")||path.endsWith(".mp4")?new NativeMediaScreen(parent,uri):new MorisWebScreen(parent,uri.toASCIIString(),"圖片");}
 private NativeMediaScreen(Screen parent,URI uri){super(Component.literal("圖片／GIF"));this.parent=parent;this.uri=uri;draft=parent instanceof ChatScreen?parent.children().stream().filter(e->e instanceof EditBox).map(e->((EditBox)e).getValue()).findFirst().orElse(""):null;ChatImages.image(uri);}
 protected void init(){pw=Math.min(600,Math.max(220,(int)(width*.74)));ph=Math.min(420,Math.max(120,(int)(height*.76)));left=(width-pw)/2;top=(height-ph)/2;String[] labels={"符合","1:1","－","＋","×"};int x=left+pw-142;for(int i=0;i<labels.length;i++){int key=i,w=i<2?32:22;addRenderableWidget(Button.builder(Component.literal(labels[i]),b->{if(key==4)onClose();else if(key==0){fit=true;panX=panY=0;}else if(key==1){fit=false;zoom=1;panX=panY=0;}else{if(fit)zoom=fitScale();fit=false;zoom=Math.clamp(zoom*(key==2?.8:1.25),.1,16);}}).bounds(x,top+5,w,18).build());x+=w+2;}}
 private double fitScale(){var image=ChatImages.image(uri);return image.width<=0||image.height<=0?1:Math.min((pw-12)/(double)image.width,(ph-52)/(double)image.height);}
 public boolean isPauseScreen(){return false;}
 public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){g.fill(left,top,left+pw,top+ph,0xF0201720);g.fill(left,top+25,left+pw,top+27,0xFFE5A2C3);g.text(font,Component.literal("圖片／GIF"),left+7,top+10,0xFFFFFFFF);g.enableScissor(left+4,top+29,left+pw-4,top+ph-18);var image=ChatImages.image(uri);if(image.texture==null){g.text(font,Component.literal(font.plainSubstrByWidth(image.state,pw-20)),left+10,top+40,0xFFCCCCCC);}else{double scale=fit?fitScale():zoom;double x=left+pw/2.0-image.width*scale/2+panX,y=top+29+(ph-47-image.height*scale)/2+panY;g.pose().pushMatrix();g.pose().translate((float)x,(float)y);g.pose().scale((float)scale,(float)scale);ChatImages.draw(g,uri,0,0,image.width,image.height);g.pose().popMatrix();}g.disableScissor();g.text(font,Component.literal("滾輪縮放 · 拖曳圖片 · Esc 返回"),left+6,top+ph-12,0xFFBBBBBB);super.extractRenderState(g,mx,my,delta);}
 public boolean mouseClicked(MouseButtonEvent e,boolean doubled){if(e.y()>=top+29&&e.y()<top+ph-18&&e.x()>=left+4&&e.x()<left+pw-4&&e.button()==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT){drag=true;lastX=e.x();lastY=e.y();return true;}return super.mouseClicked(e,doubled);}
 public boolean mouseDragged(MouseButtonEvent e,double dx,double dy){if(drag){panX+=e.x()-lastX;panY+=e.y()-lastY;lastX=e.x();lastY=e.y();return true;}return super.mouseDragged(e,dx,dy);}
 public boolean mouseReleased(MouseButtonEvent e){if(drag){drag=false;return true;}return super.mouseReleased(e);}
 public boolean mouseScrolled(double x,double y,double horizontal,double vertical){if(fit)zoom=fitScale();fit=false;zoom=Math.clamp(zoom*Math.pow(1.15,vertical),.1,16);return true;}
 public void onClose(){minecraft.gui.setScreen(parent);if(draft!=null)for(var child:parent.children())if(child instanceof EditBox box){box.setValue(draft);parent.setFocused(box);break;}}
}
