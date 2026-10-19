package org.NJ.hwamaihelper.client;
/** Places the actor left of centre using the actual vertical FOV and window aspect. */
public final class InspectFraming {
 public static double yawOffset(double fov,double aspect){if(!Double.isFinite(fov)||fov<=0)fov=70;return Math.toDegrees(Math.atan(0.36*Math.tan(Math.toRadians(Math.clamp(fov,1,170))/2)*Math.max(0.1,aspect)));}
 private InspectFraming(){}
}
