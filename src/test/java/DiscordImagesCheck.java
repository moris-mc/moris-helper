import org.NJ.hwamaihelper.client.DiscordImageUrl;
import java.net.URI;
public final class DiscordImagesCheck {
 public static void main(String[] args)throws Exception{
  animation();
  movie();
  URI share=URI.create("https://klipy.com/gifs/spongebob-wink-1");if(!DiscordImageUrl.accepted(share))throw new AssertionError("Klipy share rejected");
  String html="<META content='https://static2.klipy.com/a.gif?x=1&amp;y=2' property='og:image'>";
  if(!org.NJ.hwamaihelper.client.GifShareResolver.image(share,html).toString().equals("https://static2.klipy.com/a.gif?x=1&y=2"))throw new AssertionError("Metadata resolution");
  for(String invalid:new String[]{"https://127.0.0.1/a.gif","https://static2.klipy.com.evil.example/a.gif","http://static2.klipy.com/a.gif"}){
   try{org.NJ.hwamaihelper.client.GifShareResolver.image(share,"<meta property='og:image' content='"+invalid+"'>");throw new AssertionError("Untrusted redirect accepted");}catch(IllegalArgumentException expected){}
  }
  String mixed="<meta property='og:image' content='https://static2.klipy.com/poster.png'><meta property='og:video' content='https://static2.klipy.com/movie.mp4'>";
  if(!org.NJ.hwamaihelper.client.GifShareResolver.image(share,mixed).getPath().endsWith(".mp4"))throw new AssertionError("Static poster preferred to movie");
  if(!org.NJ.hwamaihelper.client.GifShareResolver.image(share,mixed+"<meta property='og:image' content='https://static2.klipy.com/animated.gif'>").getPath().endsWith(".mp4"))throw new AssertionError("Animated MP4 preferred to potentially single-frame GIF poster");
  for(int line:new int[]{9,12,18}){int h=org.NJ.hwamaihelper.client.MediaLayout.height(192,32,192);int rows=org.NJ.hwamaihelper.client.MediaLayout.rows(h,line);if(rows*line<h+2||rows*line-(h+2)>=line)throw new AssertionError("Thumbnail whitespace");}
  if(!DiscordImageUrl.emoji(URI.create("https://cdn.discordapp.com/emojis/123456.gif?size=48"))||DiscordImageUrl.emoji(URI.create("https://evil.example/emojis/123456.gif")))throw new AssertionError("Emoji URLs");
  System.out.println("PASS: Klipy metadata resolution, signed query preservation and untrusted target rejection.");
  String signed="https://cdn.discordapp.com/attachments/123/456/photo.PNG?ex=abc&is=def&hm=123";
  if(!DiscordImageUrl.accepted(URI.create(signed))||!URI.create(signed).toASCIIString().equals(signed))throw new AssertionError("original signed URL");
  for(String ext:new String[]{"png","jpg","jpeg","gif","webp","avif"})if(!DiscordImageUrl.accepted(URI.create("https://cdn.discordapp.com/attachments/1/2/photo."+ext)))throw new AssertionError(ext);
  for(String url:new String[]{"http://cdn.discordapp.com/a.png","https://cdn.discordapp.com.evil.example/a.png","https://127.0.0.1/a.png","https://cdn.discordapp.com:123/a.png","https://user@cdn.discordapp.com/a.png","https://cdn.discordapp.com/a.html"})if(DiscordImageUrl.accepted(URI.create(url)))throw new AssertionError(url);
  System.out.println("PASS: original Discord signed URLs, image extensions, host validation.");
 }
 private static void animation()throws Exception{
  var bytes=new java.io.ByteArrayOutputStream();var writer=javax.imageio.ImageIO.getImageWritersByFormatName("gif").next();
  try(var out=javax.imageio.ImageIO.createImageOutputStream(bytes)){
   writer.setOutput(out);writer.prepareWriteSequence(null);
   for(int color:new int[]{0xffff0000,0xff00ff00}){
    var frame=new java.awt.image.BufferedImage(4,4,java.awt.image.BufferedImage.TYPE_INT_ARGB);for(int y=0;y<4;y++)for(int x=0;x<4;x++)frame.setRGB(x,y,color);
    var meta=writer.getDefaultImageMetadata(new javax.imageio.ImageTypeSpecifier(frame),null);var root=meta.getAsTree("javax_imageio_gif_image_1.0");
    var control=(javax.imageio.metadata.IIOMetadataNode)((javax.imageio.metadata.IIOMetadataNode)root).getElementsByTagName("GraphicControlExtension").item(0);control.setAttribute("delayTime","7");meta.setFromTree("javax_imageio_gif_image_1.0",root);
    writer.writeToSequence(new javax.imageio.IIOImage(frame,null,meta),null);
   }
   writer.endWriteSequence();
  }finally{writer.dispose();}
  var animation=org.NJ.hwamaihelper.client.DiscordImageData.animation(bytes.toByteArray());
  if(animation.frames().size()!=2||animation.delays()[0]!=70||animation.frames().get(0).getRGB(0,0)!=0xffff0000||animation.frames().get(1).getRGB(0,0)!=0xff00ff00)throw new AssertionError("GIF frames and timing");
  System.out.println("PASS: GIF frame colors and original animation delays.");
 }
 private static void movie()throws Exception{
  var buffer=java.nio.ByteBuffer.allocate(1024*1024);var channel=org.jcodec.common.io.ByteBufferSeekableByteChannel.writeToByteBuffer(buffer);
  var encoder=new org.jcodec.api.SequenceEncoder(channel,org.jcodec.common.model.Rational.R(10,1),org.jcodec.common.Format.MOV,org.jcodec.common.Codec.H264,null);
  for(int color:new int[]{0xffff0000,0xff00ff00,0xff0000ff}){var image=new java.awt.image.BufferedImage(32,32,java.awt.image.BufferedImage.TYPE_INT_RGB);for(int y=0;y<32;y++)for(int x=0;x<32;x++)image.setRGB(x,y,color);encoder.encodeNativeFrame(org.jcodec.scale.AWTUtil.fromBufferedImageRGB(image));}
  encoder.finish();var data=channel.getContents();byte[] bytes=new byte[data.remaining()];data.get(bytes);var animation=org.NJ.hwamaihelper.client.VideoFrames.decode(bytes);
  if(animation.frames().size()!=3||animation.frames().get(0).getRGB(0,0)==animation.frames().get(2).getRGB(0,0))throw new AssertionError("MP4 decode frozen");
  System.out.println("PASS: real H.264 MP4 decoded to multiple coloured frames without a browser.");
 }
}
