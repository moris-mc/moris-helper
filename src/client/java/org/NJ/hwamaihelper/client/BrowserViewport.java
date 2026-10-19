package org.NJ.hwamaihelper.client;
/** GUI coordinates and Chromium pixel coordinates use one shared transform. */
public record BrowserViewport(int x,int y,int width,int height,int scale) {
 public boolean contains(double mx,double my){return mx>=x&&my>=y&&mx<x+width&&my<y+height;}
 public double pixelX(double mx){return (mx-x)*scale;}
 public double pixelY(double my){return (my-y)*scale;}
 public int pixelWidth(){return width*scale;}
 public int pixelHeight(){return height*scale;}
}
