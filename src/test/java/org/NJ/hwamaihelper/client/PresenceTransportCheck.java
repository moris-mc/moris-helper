package org.NJ.hwamaihelper.client;
public final class PresenceTransportCheck {
 public static void main(String[] args)throws Exception{
  try(var socket=DiscordRpcTransport.windowsPipe(args[0])){
   PresenceProtocol.write(socket.output,0,PresenceProtocol.handshake());
   if(!PresenceProtocol.read(socket.input).text().contains("READY"))throw new AssertionError("ready");
   PresenceProtocol.write(socket.output,1,PresenceProtocol.activity(123,456,"fixture",true));
   var ping=PresenceProtocol.read(socket.input);if(ping.opcode()!=3)throw new AssertionError("ping");
   PresenceProtocol.write(socket.output,4,ping.data());
   PresenceProtocol.write(socket.output,1,PresenceProtocol.activity(123,0,"clear",false));
   var entered=new java.util.concurrent.CountDownLatch(1);
   Thread reader=new Thread(()->{entered.countDown();try{PresenceProtocol.read(socket.input);}catch(Exception ignored){}},"mock-blocked-discord-reader");reader.setDaemon(true);reader.start();entered.await();Thread.sleep(50);
   long start=System.nanoTime();socket.close();
   if(java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)>500)throw new AssertionError("Native pipe close blocked caller");
  }
  System.out.println("PASS: Windows named-pipe handshake, activity write, server PING/PONG, clear and non-blocking close with a pending native read.");
 }
}
