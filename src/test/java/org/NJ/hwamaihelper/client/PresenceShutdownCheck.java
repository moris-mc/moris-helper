package org.NJ.hwamaihelper.client;
import java.io.*;
import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/** Reproduces a pipe close that stays blocked while Discord is not consuming pending I/O. */
public class PresenceShutdownCheck {
 public static void main(String[] args)throws Exception{
  CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);AtomicInteger calls=new AtomicInteger();
  DiscordRpcTransport stuck=new DiscordRpcTransport(InputStream.nullInputStream(),OutputStream.nullOutputStream(),()->{calls.incrementAndGet();entered.countDown();release.await();});
  Field field=DiscordPresence.class.getDeclaredField("connection");field.setAccessible(true);field.set(null,stuck);
  ExecutorService game=Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"fake-render-shutdown");t.setDaemon(true);return t;});
  try{
   long start=System.nanoTime();game.submit(DiscordPresence::stop).get(500,TimeUnit.MILLISECONDS);
   if(!entered.await(2,TimeUnit.SECONDS))throw new AssertionError("Close not dispatched");
   if(release.getCount()!=1)throw new AssertionError("Close was not actually blocked");
   if(field.get(null)!=null)throw new AssertionError("Session was not detached");
   stuck.close();stuck.close();Thread.sleep(30);if(calls.get()!=1)throw new AssertionError("Double close");
   if(Thread.getAllStackTraces().keySet().stream().filter(t->t.getName().equals("moris-discord-close")).anyMatch(t->!t.isDaemon()))throw new AssertionError("Closer could keep JVM alive");
   System.out.println("PASS: stop returns with a deliberately blocked pipe close; session detached, close runs once on daemon, elapsed "+TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start)+"ms.");
  }finally{release.countDown();game.shutdownNow();}
 }
}
