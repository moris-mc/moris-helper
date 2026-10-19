package org.NJ.hwamaihelper.client;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.*;
/** Local Discord IPC only. No bot token, network socket or authentication secret. */
public final class DiscordRpcTransport implements AutoCloseable {
 final InputStream input;final OutputStream output;private final AutoCloseable handle;
 private final AtomicBoolean closing=new AtomicBoolean();
 private static final AtomicInteger PENDING_CLOSES=new AtomicInteger();
 DiscordRpcTransport(InputStream input,OutputStream output,AutoCloseable handle){this.input=input;this.output=output;this.handle=handle;}
 public static DiscordRpcTransport connect()throws IOException{
  if(PENDING_CLOSES.get()>=4)throw new IOException("Waiting for previous Discord IPC handles to close");
  if(System.getProperty("os.name","").toLowerCase(Locale.ROOT).contains("win")){
   for(int i=0;i<10;i++)try{return windowsPipe("discord-ipc-"+i);}catch(IOException ignored){}
  }else{
   LinkedHashSet<String> dirs=new LinkedHashSet<>();for(String key:List.of("XDG_RUNTIME_DIR","TMPDIR","TMP","TEMP")){String value=System.getenv(key);if(value!=null&&!value.isBlank())dirs.add(value);}dirs.add("/tmp");
   for(String dir:dirs)for(String prefix:List.of("","app/com.discordapp.Discord/"))for(int i=0;i<10;i++){
    Path socket=Path.of(dir).resolve(prefix+"discord-ipc-"+i);if(!Files.exists(socket))continue;
    SocketChannel channel=null;try{
     channel=SocketChannel.open(StandardProtocolFamily.UNIX);channel.connect(UnixDomainSocketAddress.of(socket));SocketChannel c=channel;
     InputStream in=new InputStream(){public int read()throws IOException{byte[] b=new byte[1];return read(b,0,1)<0?-1:b[0]&255;}public int read(byte[] b,int off,int len)throws IOException{return c.read(ByteBuffer.wrap(b,off,len));}};
     OutputStream out=new OutputStream(){public void write(int b)throws IOException{write(new byte[]{(byte)b});}public void write(byte[] b,int off,int len)throws IOException{ByteBuffer data=ByteBuffer.wrap(b,off,len);while(data.hasRemaining())c.write(data);}};
     return new DiscordRpcTransport(in,out,c);
    }catch(IOException e){if(channel!=null)try{channel.close();}catch(IOException ignored){}}
   }
  }
  throw new IOException("Discord desktop IPC is not available");
 }
 static DiscordRpcTransport windowsPipe(String name)throws IOException{
  String slash=String.valueOf((char)92);
  RandomAccessFile pipe=new RandomAccessFile(slash+slash+"."+slash+"pipe"+slash+name,"rw");
  InputStream in=new InputStream(){public int read()throws IOException{return pipe.read();}public int read(byte[] b,int off,int len)throws IOException{return pipe.read(b,off,len);}};
  OutputStream out=new OutputStream(){public void write(int b)throws IOException{pipe.write(b);}public void write(byte[] b,int off,int len)throws IOException{pipe.write(b,off,len);}};
  return new DiscordRpcTransport(in,out,pipe);
 }
 /** Windows CloseHandle can wait for a synchronous pipe read/write. Never run it on the game thread. */
 public void close(){
  if(!closing.compareAndSet(false,true))return;
  PENDING_CLOSES.incrementAndGet();
  Thread closer=new Thread(()->{try{handle.close();}catch(Exception ignored){}finally{PENDING_CLOSES.decrementAndGet();}},"moris-discord-close");
  closer.setDaemon(true);closer.start();
 }
}
