package org.NJ.hwamaihelper.client;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
public final class PresenceProtocol {
 public static final String APPLICATION_ID="1527675374874529823";
 public static final String LOGO="https://www.mcmoris.net/media/moris-logo.webp";
 public record Packet(int opcode,byte[] data){public String text(){return new String(data,StandardCharsets.UTF_8);}}
 public static boolean isMoris(String address){
  if(address==null)return false;
  String host=address.trim().toLowerCase(Locale.ROOT);
  if(host.indexOf(':')==host.lastIndexOf(':')&&host.contains(":"))host=host.substring(0,host.lastIndexOf(':'));
  if(host.endsWith("."))host=host.substring(0,host.length()-1);
  return host.equals("mcmoris.net")||host.endsWith(".mcmoris.net");
 }
 public static String handshake(){return """
 {"v":1,"client_id":"%s"}
 """.formatted(APPLICATION_ID).strip();}
 public static String activity(long pid,long start,String nonce,boolean active){
  String value=active?"""
  {"type":0,"details":"我在森友伺服器｜mcmoris.net","state":"等你一起來玩ouo!!","timestamps":{"start":%d},"assets":{"large_image":"%s","large_text":"森友伺服器"}}
  """.formatted(start,LOGO).strip():"null";
  return """
  {"cmd":"SET_ACTIVITY","args":{"pid":%d,"activity":%s},"nonce":"%s"}
  """.formatted(pid,value,nonce).strip();
 }
 public static void write(OutputStream out,int opcode,byte[] data)throws IOException{
  writeInt(out,opcode);writeInt(out,data.length);out.write(data);out.flush();
 }
 public static void write(OutputStream out,int opcode,String text)throws IOException{write(out,opcode,text.getBytes(StandardCharsets.UTF_8));}
 private static void writeInt(OutputStream out,int n)throws IOException{for(int i=0;i<4;i++)out.write((n>>>(i*8))&255);}
 private static int readInt(InputStream in)throws IOException{int n=0;for(int i=0;i<4;i++){int value=in.read();if(value<0)throw new EOFException();n|=value<<(i*8);}return n;}
 public static Packet read(InputStream in)throws IOException{
  int opcode=readInt(in),length=readInt(in);if(length<0||length>1024*1024)throw new IOException("Invalid Discord packet size");
  byte[] data=in.readNBytes(length);if(data.length!=length)throw new EOFException();return new Packet(opcode,data);
 }
 private PresenceProtocol(){}
}
