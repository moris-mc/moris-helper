package org.NJ.hwamaihelper.client;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.*;
public final class DiscordImageData {
 public static BufferedImage decode(byte[] bytes)throws IOException {
  try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
   var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("unsupported image format");
   var reader=readers.next();try{
    reader.setInput(input);int w=reader.getWidth(0),h=reader.getHeight(0);
    if(w<1||h<1||w>8192||h>8192||(long)w*h>32000000)throw new IOException("image dimensions");
    var param=reader.getDefaultReadParam();int sample=Math.max(1,Math.min(w/192,h/96));param.setSourceSubsampling(sample,sample,0,0);
    BufferedImage raw=reader.read(0,param);double ratio=Math.min(1,Math.min(192.0/raw.getWidth(),96.0/raw.getHeight()));
    BufferedImage thumb=new BufferedImage(Math.max(1,(int)(raw.getWidth()*ratio)),Math.max(1,(int)(raw.getHeight()*ratio)),BufferedImage.TYPE_INT_ARGB);
    var g=thumb.createGraphics();try{g.drawImage(raw,0,0,thumb.getWidth(),thumb.getHeight(),null);}finally{g.dispose();raw.flush();}return thumb;
   }finally{reader.dispose();}
  }
 }
 public record Animation(java.util.List<BufferedImage> frames,int[] delays) {}
 public static Animation animation(byte[] bytes)throws IOException {
  try(var input=ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))){
   var readers=ImageIO.getImageReaders(input);if(!readers.hasNext())throw new IOException("unsupported image format");
   var reader=readers.next();try{
    reader.setInput(input);
    if(!reader.getFormatName().equalsIgnoreCase("gif"))return new Animation(java.util.List.of(decode(bytes)),new int[]{100});
    var stream=reader.getStreamMetadata().getAsTree("javax_imageio_gif_stream_1.0");
    var logical=node(stream,"LogicalScreenDescriptor");int w=number(logical,"logicalScreenWidth",reader.getWidth(0)),h=number(logical,"logicalScreenHeight",reader.getHeight(0));
    if(w<1||h<1||w>8192||h>8192||(long)w*h>32000000)throw new IOException("image dimensions");
    double scale=Math.min(1,Math.min(192.0/w,96.0/h));int tw=Math.max(1,(int)(w*scale)),th=Math.max(1,(int)(h*scale));
    BufferedImage canvas=new BufferedImage(tw,th,BufferedImage.TYPE_INT_ARGB);
    var frames=new java.util.ArrayList<BufferedImage>();var delays=new java.util.ArrayList<Integer>();long decoded=0;
    for(int i=0;i<120;i++){
     int fw,fh;try{fw=reader.getWidth(i);fh=reader.getHeight(i);}catch(IndexOutOfBoundsException end){break;}
     decoded+=(long)fw*fh;if(fw<1||fh<1||decoded>64000000L)break;
     var meta=reader.getImageMetadata(i).getAsTree("javax_imageio_gif_image_1.0");var desc=node(meta,"ImageDescriptor");var control=node(meta,"GraphicControlExtension");
     int left=number(desc,"imageLeftPosition",0),top=number(desc,"imageTopPosition",0);
     String disposal=attribute(control,"disposalMethod","none");BufferedImage previous=disposal.equals("restoreToPrevious")?copy(canvas):null;
     BufferedImage raw=reader.read(i);var g=canvas.createGraphics();try{g.drawImage(raw,(int)(left*scale),(int)(top*scale),Math.max(1,(int)(fw*scale)),Math.max(1,(int)(fh*scale)),null);}finally{g.dispose();raw.flush();}
     frames.add(copy(canvas));delays.add(Math.max(20,number(control,"delayTime",10)*10));
     if(disposal.equals("restoreToPrevious")){canvas.flush();canvas=previous;}
     else if(disposal.equals("restoreToBackgroundColor")){g=canvas.createGraphics();try{g.setComposite(java.awt.AlphaComposite.Clear);g.fillRect((int)(left*scale),(int)(top*scale),Math.max(1,(int)Math.ceil(fw*scale)),Math.max(1,(int)Math.ceil(fh*scale)));}finally{g.dispose();}}
    }
    canvas.flush();if(frames.isEmpty())return new Animation(java.util.List.of(decode(bytes)),new int[]{100});
    return new Animation(java.util.List.copyOf(frames),delays.stream().mapToInt(Integer::intValue).toArray());
   }finally{reader.dispose();}
  }
 }
 private static BufferedImage copy(BufferedImage source){var result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);var g=result.createGraphics();try{g.drawImage(source,0,0,null);}finally{g.dispose();}return result;}
 private static org.w3c.dom.Node node(org.w3c.dom.Node parent,String name){for(var n=parent.getFirstChild();n!=null;n=n.getNextSibling())if(n.getNodeName().equals(name))return n;return null;}
 private static String attribute(org.w3c.dom.Node n,String key,String fallback){if(n==null)return fallback;var value=n.getAttributes().getNamedItem(key);return value==null?fallback:value.getNodeValue();}
 private static int number(org.w3c.dom.Node n,String key,int fallback){try{return Integer.parseInt(attribute(n,key,""));}catch(NumberFormatException e){return fallback;}}
 private DiscordImageData(){}
}
