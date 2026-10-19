package org.NJ.hwamaihelper.client;
public record WheelLayout(int x,int y,int radius){
 public static WheelLayout create(int width,int height,int size,int offsetX,int offsetY){int radius=Math.min(size,Math.min((width-8)/2,(height-32)/2));radius=Math.max(16,radius);return new WheelLayout(Math.clamp(width/2+offsetX,radius+4,width-radius-4),Math.clamp(height/2+offsetY,radius+4,height-radius-24),radius);}
 public int selection(double mx,double my){double dx=mx-x,dy=my-y,d=dx*dx+dy*dy;if(d<Math.pow(radius*0.16,2)||d>radius*radius)return -1;return (int)(((Math.toDegrees(Math.atan2(dy,dx))+135+360)%360)/90);}
}
