import org.NJ.hwamaihelper.client.BannerTimeline;
public class BannerCheck {
 public static void main(String[] args){
  var model=new BannerTimeline();var one=new BannerTimeline.Notice("a","公告","new");model.offer(one,1000);
  if(model.visibility(1000)!=0||model.visibility(1000+BannerTimeline.ENTER)!=1)throw new AssertionError("slide in");
  model.offer(new BannerTimeline.Notice("a","公告","edited"),1500);model.offer(new BannerTimeline.Notice("b","更新","new"),1500);
  if(!model.current(1500).text().equals("edited"))throw new AssertionError("active update");
  if(model.visibility(1000+BannerTimeline.TOTAL-1)>=0.01)throw new AssertionError("slide out");
  if(!model.current(1000+BannerTimeline.TOTAL).channel().equals("b"))throw new AssertionError("next channel");
  if(!model.dismiss(1000+BannerTimeline.TOTAL+300).channel().equals("b")||model.current(1000+BannerTimeline.TOTAL+301)!=null)throw new AssertionError("click dismiss");
  model.offer(one,10000);model.clear();if(model.current(10001)!=null)throw new AssertionError("disconnect clear");
  System.out.println("PASS: banner slide in/out, automatic expiry, same-channel coalescing, queued channel, click dismissal and disconnect clear.");
 }
}
