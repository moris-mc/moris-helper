package org.NJ.hwamaihelper.client;
import com.mojang.blaze3d.platform.InputConstants;
/** Explicit edit commands avoid OSR/AWT control-character translation differences. */
public final class BrowserEditing {
 public enum Action { NONE, BACKSPACE, DELETE, SELECT_ALL, COPY, CUT, PASTE, UNDO, REDO }
 public static Action action(int key,int modifiers,boolean composing) {
  if(composing)return Action.NONE;
  boolean ctrl=(modifiers&InputConstants.MOD_CONTROL)!=0;
  boolean shift=(modifiers&InputConstants.MOD_SHIFT)!=0;
  if((modifiers&InputConstants.MOD_ALT)!=0)return Action.NONE;
  if(ctrl)return switch(key) {
   case InputConstants.KEY_A->Action.SELECT_ALL;
   case InputConstants.KEY_C->Action.COPY;
   case InputConstants.KEY_X->Action.CUT;
   case InputConstants.KEY_V->Action.PASTE;
   case InputConstants.KEY_Z->shift?Action.REDO:Action.UNDO;
   case InputConstants.KEY_Y->Action.REDO;
   default->Action.NONE;
  };
  return switch(key){case InputConstants.KEY_BACKSPACE->Action.BACKSPACE;case InputConstants.KEY_DELETE->Action.DELETE;default->Action.NONE;};
 }
 public static String deletionScript(boolean forward) {
  return "(()=>{const e=document.activeElement;if(e&&(e instanceof HTMLInputElement||e instanceof HTMLTextAreaElement||e.isContentEditable)){document.execCommand('"+(forward?"forwardDelete":"delete")+"');}})();";
 }
 private BrowserEditing(){}
}
