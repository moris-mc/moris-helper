package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.Component;
import org.NJ.hwamaihelper.client.ChatImages;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
 @Shadow @Final private List<GuiMessage.Line> trimmedMessages;
 @Shadow private int chatScrollbarPos;
 @Shadow private double getScale(){throw new AssertionError();}
 @Shadow private int getLineHeight(){throw new AssertionError();}
 @Shadow public abstract int getLinesPerPage();
 @Shadow private int getWidth(){throw new AssertionError();}
 @Shadow private void refreshTrimmedMessages(){throw new AssertionError();}
 @Unique private long morisRevision=-1;
 @Inject(method="tick",at=@At("HEAD")) private void morisReflow(CallbackInfo ci){ChatImages.tick();if(morisRevision!=ChatImages.revision()){morisRevision=ChatImages.revision();int scroll=chatScrollbarPos;refreshTrimmedMessages();chatScrollbarPos=Math.min(scroll,Math.max(0,trimmedMessages.size()-getLinesPerPage()));}}
 @Inject(method="extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;IIILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;Z)V",at=@At("TAIL"))
 private void morisDrawImages(GuiGraphicsExtractor g,Font font,int ticks,int mouseX,int mouseY,ChatComponent.DisplayMode mode,boolean b,CallbackInfo ci){
  org.NJ.hwamaihelper.client.ImageHitBoxes.clear();
  if(Minecraft.getInstance().options.chatVisibility().get()==net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)return;
  double scale=getScale();int lineHeight=getLineHeight(),base=(int)Math.floor((g.guiHeight()-40)/scale),visible=getLinesPerPage();
  g.pose().pushMatrix();g.pose().scale((float)scale,(float)scale);g.pose().translate(4,0);
  g.enableScissor(0,base-visible*lineHeight,getWidth(),base);
  for(int start=0;start<trimmedMessages.size();) {
   GuiMessage parent=trimmedMessages.get(start).parent();int end=start+1;while(end<trimmedMessages.size()&&trimmedMessages.get(end).parent()==parent)end++;
   if(end>chatScrollbarPos&&start<chatScrollbarPos+visible&&(mode.foreground||ticks-parent.addedTime()<180)) {
    var urls=ChatImages.urls(parent.content());
    for(int line=start;line<end;line++)if(line>=chatScrollbarPos&&line<chatScrollbarPos+visible)org.NJ.hwamaihelper.client.ChatEmoji.draw(g,font,trimmedMessages.get(line).content(),base-(line-chatScrollbarPos)*lineHeight-(int)(8+4*Minecraft.getInstance().options.chatLineSpacing().get()));
    for(int i=0;i<urls.size();i++) {
     int rows=ChatImages.rows(urls.get(i),Math.min(192,getWidth()-8),lineHeight),after=0;for(int j=i+1;j<urls.size();j++)after+=ChatImages.rows(urls.get(j),Math.min(192,getWidth()-8),lineHeight);
     int bottom=base-(start+after-chatScrollbarPos)*lineHeight;
     int y=bottom-rows*lineHeight+1;
     int[] drawn=ChatImages.draw(g,urls.get(i),2,y,Math.min(192,getWidth()-8),rows*lineHeight-2);
     if(mode.foreground)org.NJ.hwamaihelper.client.ImageHitBoxes.add(urls.get(i),6*scale,Math.max(y,base-visible*lineHeight)*scale,(6+drawn[0])*scale,Math.min(y+drawn[1],base)*scale);
    }
   }
   start=end;
  }
  g.disableScissor();g.pose().popMatrix();
 }
 @Inject(method="clearMessages",at=@At("HEAD"))
 private void morisClear(boolean clearHistory,CallbackInfo ci){ChatImages.clear();}
}
