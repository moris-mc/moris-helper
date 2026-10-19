package org.NJ.hwamaihelper.client;
/** Monotonic real-time camera easing, including closing partway through opening. */
public final class InspectTransition {
 public static double ease(double t){t=Math.clamp(t,0,1);return t*t*(3-2*t);}
 public static double opening(long elapsed){return ease(elapsed/800_000_000.0);}
 public static double closing(double from,long elapsed){return from*(1-ease(elapsed/500_000_000.0));}
 private InspectTransition(){}
}
