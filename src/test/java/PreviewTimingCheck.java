import org.NJ.hwamaihelper.client.PreviewTiming;
public class PreviewTimingCheck {
 public static void main(String[] args){
  long start=10000;
  if(PreviewTiming.mayRequest(start+100,start,0,false,false,0))throw new AssertionError("Prefetch on a passing glance");
  if(!PreviewTiming.mayRequest(start+150,start,0,false,false,0))throw new AssertionError("No aim prefetch");
  if(!PreviewTiming.mayRequest(start,start,0,false,true,0))throw new AssertionError("Key press waits for dwell");
  if(PreviewTiming.mayRequest(start+250,start,start+150,false,true,start+350))throw new AssertionError("Repeated key press sends another request");
  if(PreviewTiming.mayRequest(start+300,start+250,0,false,true,start+350))throw new AssertionError("Retarget ignores global cooldown");
  if(!PreviewTiming.mayRequest(start+350,start+250,0,false,true,start+350))throw new AssertionError("Retarget stalls unnecessarily");
  if(PreviewTiming.mayRequest(start+1000,start,start+150,true,true,0))throw new AssertionError("Slow response superseded");
  if(!PreviewTiming.mayRequest(start+2200,start,start+150,true,true,0))throw new AssertionError("Missing response never retried");
  System.out.println("PASS: aim prefetch, immediate held-key request, repeated key press, shared retarget cooldown, slow reply preservation and timeout retry.");
 }
}
