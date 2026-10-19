package org.NJ.hwamaihelper.client;
/** Keep supplementary Unicode out of the Java/JCEF source-string conversion boundary. */
public final class BrowserJson {
 public static String ascii(String json){StringBuilder out=new StringBuilder(json.length());for(int i=0;i<json.length();i++){char c=json.charAt(i);if(c<128)out.append(c);else{out.append("\\u");String h=Integer.toHexString(c);out.append("0".repeat(4-h.length())).append(h);}}return out.toString();}
 private BrowserJson(){}
}
