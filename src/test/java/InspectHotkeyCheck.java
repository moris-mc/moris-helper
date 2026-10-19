import org.NJ.hwamaihelper.client.InspectHotkey;
public class InspectHotkeyCheck {
 public static void main(String[] args){
  // Actual SDL keyboard-state indices for G, left Shift and right Shift.
  boolean[] keys=new boolean[512];java.util.function.IntPredicate down=k->keys[k];
  if(InspectHotkey.pressed(down))throw new AssertionError("No keys pressed");
  keys[10]=true;if(InspectHotkey.pressed(down))throw new AssertionError("G alone");
  keys[225]=true;if(!InspectHotkey.pressed(down))throw new AssertionError("Left Shift + G");
  keys[225]=false;keys[229]=true;if(!InspectHotkey.pressed(down))throw new AssertionError("Right Shift + G");
  keys[10]=false;if(InspectHotkey.pressed(down))throw new AssertionError("Shift alone");
  keys[229]=false;keys[71]=keys[340]=keys[344]=true;if(InspectHotkey.pressed(down))throw new AssertionError("Legacy GLFW codes accepted");
  System.out.println("PASS: real SDL scancodes, left/right Shift + G, release, and legacy-code rejection.");
 }
}
