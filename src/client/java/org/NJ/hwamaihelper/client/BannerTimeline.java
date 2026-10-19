package org.NJ.hwamaihelper.client;
import java.util.*;
/** Monotonic-time animation and bounded channel-coalescing queue, independent of the game renderer. */
public final class BannerTimeline {
 public static final long ENTER=260,HOLD=6000,EXIT=320,TOTAL=ENTER+HOLD+EXIT;
 public record Notice(String channel,String name,String text){}
 private final LinkedHashMap<String,Notice> queue=new LinkedHashMap<>();
 private Notice active;private long started;
 public void offer(Notice notice,long now){if(active!=null&&active.channel.equals(notice.channel)){active=notice;return;}queue.put(notice.channel,notice);while(queue.size()>7)queue.remove(queue.keySet().iterator().next());update(now);}
 public void update(long now){if(active!=null&&now-started>=TOTAL)active=null;if(active==null&&!queue.isEmpty()){String id=queue.keySet().iterator().next();active=queue.remove(id);started=now;}}
 public Notice current(long now){update(now);return active;}
 public double visibility(long now){update(now);if(active==null)return 0;long age=Math.max(0,now-started);double t=age<ENTER?(double)age/ENTER:age<ENTER+HOLD?1:1-(double)(age-ENTER-HOLD)/EXIT;return t*t*(3-2*t);}
 public Notice dismiss(long now){Notice result=active;active=null;update(now);return result;}
 public void clear(){queue.clear();active=null;}
}
