package org.NJ.hwamaihelper.client;
public final class MediaLayout {
 public static int height(int width,int height,int maxWidth){if(width<=0||height<=0)return 14;return Math.max(1,(int)(height*Math.min(1,Math.min(Math.max(1,maxWidth)/(double)width,96.0/height))));}
 public static int rows(int height,int lineHeight){return Math.max(1,(height+2+lineHeight-1)/lineHeight);}
 private MediaLayout(){}
}
