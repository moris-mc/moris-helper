package org.NJ.hwamaihelper.client;
/** Stepped ascent with deliberate pauses, inspired by character-switch cinematics. */
public final class CinematicPath {
 public static final int DEPARTURE_MS=3000,TRAVEL_MS=1100,RETURN_MS=2800;
 public static double ease(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
 public static double height(double t){t=Math.clamp(t,0,1);if(t<.20)return 12*ease(t/.20);if(t<.34)return 12;if(t<.56)return 12+28*ease((t-.34)/.22);if(t<.72)return 40;return 40+32*ease((t-.72)/.28);}
 public static double fadeOut(double elapsed){return ease((elapsed-2400)/600);}
 public static double filter(int phase,long elapsed){return phase==0?ease(elapsed/500.0):phase==1?1:1-ease((elapsed-RETURN_MS*.65)/(RETURN_MS*.35));}
 public static int ascentStage(long ms){return ms<1020?0:ms<2160?1:2;}
 public static int descentStage(long ms){return ms<1232?0:ms<2240?1:2;}
 private CinematicPath(){}
}
