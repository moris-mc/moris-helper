import org.NJ.hwamaihelper.client.InspectFraming;
public class InspectFramingCheck {
 public static void main(String[] args){for(double aspect:new double[]{4.0/3,16.0/9,21.0/9})for(double fov:new double[]{50,70,100}){double offset=Math.toRadians(InspectFraming.yawOffset(fov,aspect));double ndc=-Math.tan(offset)/(Math.tan(Math.toRadians(fov)/2)*aspect);double fraction=(ndc+1)/2;if(Math.abs(fraction-0.32)>0.00001)throw new AssertionError("Actor overlaps right-hand panel: "+fraction);}
 System.out.println("PASS: actor projects to 32% screen width, left of information panel, at 4:3 / 16:9 / 21:9 and FOV 50 / 70 / 100.");}
}
