package org.NJ.hwamaihelper.client;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
public class MouseOnlyButton extends Button.Plain {
 public MouseOnlyButton(int x,int y,int w,int h,String label,OnPress action){super(x,y,w,h,Component.literal(label),action,DEFAULT_NARRATION);}
 protected MouseOnlyButton(int x,int y,int w,int h,Component label,OnPress action,CreateNarration narration){super(x,y,w,h,label,action,narration);}
 public ComponentPath nextFocusPath(FocusNavigationEvent event){return null;}
 public boolean keyPressed(KeyEvent event){return false;}
 public boolean shouldTakeFocusAfterInteraction(){return false;}
}
