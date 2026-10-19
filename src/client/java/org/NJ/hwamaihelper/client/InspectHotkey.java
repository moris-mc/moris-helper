package org.NJ.hwamaihelper.client;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.IntPredicate;
/** Minecraft 26.3 polls SDL scancodes, not legacy GLFW key codes. */
public final class InspectHotkey {
 public static boolean pressed(IntPredicate down){return down.test(InputConstants.KEY_G)&&(down.test(InputConstants.KEY_LSHIFT)||down.test(InputConstants.KEY_RSHIFT));}
 private InspectHotkey(){}
}
