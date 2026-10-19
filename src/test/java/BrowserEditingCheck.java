import org.NJ.hwamaihelper.client.BrowserEditing;
import static org.NJ.hwamaihelper.client.BrowserEditing.Action.*;
public class BrowserEditingCheck {
 private static void check(int key,int modifiers,boolean composing,BrowserEditing.Action expected){if(BrowserEditing.action(key,modifiers,composing)!=expected)throw new AssertionError("key="+key+" modifiers="+modifiers+" composing="+composing);}
 public static void main(String[] args){
  check(42,0,false,BACKSPACE);check(76,0,false,DELETE);
  check(4,64,false,SELECT_ALL);check(4,128,false,SELECT_ALL);
  check(6,64,false,COPY);check(27,64,false,CUT);check(25,64,false,PASTE);
  check(29,64,false,UNDO);check(29,65,false,REDO);check(28,64,false,REDO);
  check(4,0,false,NONE);check(42,0,true,NONE);check(76,0,true,NONE);check(4,64,true,NONE);
  check(4,64|256,false,NONE);
  if(!BrowserEditing.deletionScript(false).contains("execCommand('delete')")||!BrowserEditing.deletionScript(true).contains("execCommand('forwardDelete')"))throw new AssertionError("delete direction");
  System.out.println("PASS: Backspace/Delete, left/right Ctrl+A, clipboard, undo/redo, IME and AltGr exclusions.");
 }
}
