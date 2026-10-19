package org.NJ.hwamaihelper.client;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.awt.image.BufferedImage;
import java.util.*;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.scale.AWTUtil;
/** Decode silent MP4 GIF renditions without relying on Chromium's optional H.264 codec. */
public final class VideoFrames {
 public static DiscordImageData.Animation decode(byte[] bytes)throws IOException {
  try(var channel=ByteBufferSeekableByteChannel.readFromByteBuffer(ByteBuffer.wrap(bytes))){
   var grab=FrameGrab.createFrameGrab(channel);var size=grab.getVideoTrack().getMeta().getVideoCodecMeta().getSize();int w=size.getWidth(),h=size.getHeight();
   if(w<1||h<1||w>1920||h>1080)throw new IOException("Video dimensions exceed limit");
   var frames=new ArrayList<BufferedImage>();var times=new ArrayList<Double>();long pixels=0,deadline=System.nanoTime()+12_000_000_000L;double previous=-1;
   for(int i=0;i<600&&frames.size()<240&&pixels+(long)w*h<=128000000L&&System.nanoTime()<deadline;i++){
    var frame=grab.getNativeFrameWithMetadata();if(frame==null)break;pixels+=(long)w*h;double time=frame.getTimestamp();if(previous>=0&&time-previous<.045)continue;
    var raw=AWTUtil.toBufferedImage(frame.getPicture());double scale=Math.min(1,Math.min(320.0/raw.getWidth(),180.0/raw.getHeight()));var thumb=new BufferedImage(Math.max(1,(int)(raw.getWidth()*scale)),Math.max(1,(int)(raw.getHeight()*scale)),BufferedImage.TYPE_INT_ARGB);var graphics=thumb.createGraphics();try{graphics.drawImage(raw,0,0,thumb.getWidth(),thumb.getHeight(),null);}finally{graphics.dispose();raw.flush();}
    frames.add(thumb);times.add(time);previous=time;
   }
   if(frames.isEmpty())throw new IOException("No supported video frames");int[] delays=new int[frames.size()];for(int i=0;i<delays.length;i++)delays[i]=i+1<times.size()?(int)Math.clamp(Math.round((times.get(i+1)-times.get(i))*1000),20,1000):i>0?delays[i-1]:100;
   return new DiscordImageData.Animation(List.copyOf(frames),delays);
  }catch(IOException e){throw e;}catch(Exception e){throw new IOException("Unsupported MP4 video: "+e.getClass().getSimpleName(),e);}
 }
 private VideoFrames(){}
}
