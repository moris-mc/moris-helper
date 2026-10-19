import org.NJ.hwamaihelper.client.InspectTransition;
public class InspectTransitionCheck {
 public static void main(String[] args){
  if(InspectTransition.opening(0)!=0||InspectTransition.opening(800000000)!=1||InspectTransition.opening(900000000)!=1)throw new AssertionError("Open endpoints");
  double previous=0;for(long n=0;n<=800000000;n+=10000000){double value=InspectTransition.opening(n);if(value<previous||value>1)throw new AssertionError("Nonmonotonic camera");previous=value;}
  double partial=InspectTransition.opening(200000000);if(InspectTransition.closing(partial,0)!=partial||InspectTransition.closing(partial,500000000)!=0)throw new AssertionError("Early exit jumps or fails to restore");
  if(InspectTransition.closing(1,250000000)!=0.5)throw new AssertionError("Return curve");
  System.out.println("PASS: camera transition endpoints, monotonic movement, early-exit continuity and complete return.");
 }
}
