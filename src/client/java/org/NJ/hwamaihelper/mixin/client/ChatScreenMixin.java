package org.NJ.hwamaihelper.mixin.client;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.*;
import org.NJ.hwamaihelper.client.ChatInterfacePanel;
@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen {
 @Shadow protected net.minecraft.client.gui.components.EditBox input;
 @Unique private ChatInterfacePanel morisPanel;
 @Unique private Button morisMenuButton;
 @Unique private org.NJ.hwamaihelper.client.QuickActionPanel morisQuickPanel;
 protected ChatScreenMixin(Component title) { super(title); }
 @Inject(method="init",at=@At("TAIL"))
 private void morisButtons(CallbackInfo ci) {
  String[] names={"公共","公會","領地"}; String[] commands={"hms","kms","rms"};
  int start=4;
  // Register the panel background before its widgets so it renders behind them.
  addRenderableOnly((g,mx,my,delta)->{if(morisMenuButton!=null)morisMenuButton.setMessage(org.NJ.hwamaihelper.client.DiscordFeed.unreadLabel("選單",org.NJ.hwamaihelper.client.DiscordFeed.totalUnread()));if(morisPanel!=null)morisPanel.extractRenderState(g,mx,my,delta);if(morisQuickPanel!=null)morisQuickPanel.extractRenderState(g,mx,my,delta);});
  morisPanel=new ChatInterfacePanel(width,height,b->addRenderableWidget(b));
  morisQuickPanel=new org.NJ.hwamaihelper.client.QuickActionPanel(width,height,b->addRenderableWidget(b));
  for(int i=0;i<3;i++) {
   final String command=commands[i];
   addRenderableWidget(new org.NJ.hwamaihelper.client.MouseOnlyButton(start+i*36,height-36,34,16,names[i],button->{
    var player=Minecraft.getInstance().player;
    if(player!=null) player.connection.sendCommand(command);
   }));
  }
  morisMenuButton=addRenderableWidget(new org.NJ.hwamaihelper.client.MouseOnlyButton(width-64,height-36,60,16,"選單",b->{morisQuickPanel.close();morisPanel.toggle();}));
  addRenderableWidget(new org.NJ.hwamaihelper.client.MouseOnlyButton(width-128,height-36,60,16,"快捷選單",b->{morisPanel.close();morisQuickPanel.toggle();}));
 }
 @Inject(method="handleComponentClicked",at=@At("HEAD"),cancellable=true)
 private void morisOpenImage(net.minecraft.network.chat.Style style,boolean insertionClick,CallbackInfoReturnable<Boolean> ci){
  if(!insertionClick&&style.getClickEvent() instanceof net.minecraft.network.chat.ClickEvent.OpenUrl link&&org.NJ.hwamaihelper.client.DiscordImageUrl.accepted(link.uri())){
   minecraft.gui.setScreen(org.NJ.hwamaihelper.client.NativeMediaScreen.open((Screen)(Object)this,org.NJ.hwamaihelper.client.ChatImages.viewUrl(link.uri())));
   ci.setReturnValue(true);
  }
 }
 @Inject(method="keyPressed",at=@At("HEAD"))
 private void morisKeepTypingFocus(net.minecraft.client.input.KeyEvent event,CallbackInfoReturnable<Boolean> ci){if(getFocused()!=input)setFocused(input);}
 @Inject(method="mouseClicked",at=@At("HEAD"),cancellable=true)
 private void morisPanelClick(net.minecraft.client.input.MouseButtonEvent event,boolean doubled,CallbackInfoReturnable<Boolean> ci) {
  if(morisQuickPanel!=null&&morisQuickPanel.contains(event.x(),event.y())){morisQuickPanel.click(event,doubled);setFocused(input);ci.setReturnValue(true);return;}
  if(morisPanel!=null&&morisPanel.contains(event.x(),event.y())) {
   morisPanel.click(event,doubled);
   setFocused(input);
   ci.setReturnValue(true);
   return;
  }
  if(event.button()==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT){
   var image=org.NJ.hwamaihelper.client.ImageHitBoxes.at(event.x(),event.y());
   if(image!=null){minecraft.gui.setScreen(org.NJ.hwamaihelper.client.NativeMediaScreen.open((Screen)(Object)this,org.NJ.hwamaihelper.client.ChatImages.viewUrl(image)));ci.setReturnValue(true);}
  }
 }
 @Inject(method="mouseClicked",at=@At("RETURN"))
 private void morisFocus(net.minecraft.client.input.MouseButtonEvent event,boolean doubled,CallbackInfoReturnable<Boolean> ci) {
  if(event.button()==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT && (((event.x()<112||event.x()>=width-128)&&event.y()>=height-36&&event.y()<height-20) || (morisPanel!=null&&morisPanel.contains(event.x(),event.y())))) setFocused(input);
 }
}
