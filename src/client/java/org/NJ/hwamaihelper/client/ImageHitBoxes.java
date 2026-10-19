package org.NJ.hwamaihelper.client;
import java.net.URI;
import java.util.*;
/** Hit areas are rebuilt from the visible, clipped chat image rectangles each frame. */
public final class ImageHitBoxes {
 private record Hit(URI uri,double left,double top,double right,double bottom){}
 private static final List<Hit> HITS=new ArrayList<>();
 public static void clear(){HITS.clear();}
 public static void add(URI uri,double left,double top,double right,double bottom){if(right>left&&bottom>top)HITS.add(new Hit(uri,left,top,right,bottom));}
 public static URI at(double x,double y){for(int i=HITS.size()-1;i>=0;i--){Hit h=HITS.get(i);if(x>=h.left&&x<h.right&&y>=h.top&&y<h.bottom)return h.uri;}return null;}
 private ImageHitBoxes(){}
}
