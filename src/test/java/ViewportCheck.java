import org.NJ.hwamaihelper.client.BrowserViewport;
public final class ViewportCheck {
 public static void main(String[] args) {
  for(int scale=1;scale<=4;scale++) {
   BrowserViewport v=new BrowserViewport(14,39,292,204,scale);
   if(!v.contains(14,39)||v.contains(306,100)||v.contains(100,243)||v.contains(13.99,39))throw new AssertionError("viewport boundary");
   if(v.pixelX(14)!=0||v.pixelY(39)!=0)throw new AssertionError("origin translation");
   if(v.pixelX(160)!=146*scale||v.pixelY(141)!=102*scale)throw new AssertionError("center click");
   if(v.pixelX(305.5)!=291.5*scale||v.pixelY(242.5)!=203.5*scale)throw new AssertionError("last pixel translation");
   if(v.pixelWidth()!=292*scale||v.pixelHeight()!=204*scale)throw new AssertionError("render size");
  }
  System.out.println("PASS: browser rendering and pointer coordinates agree at GUI scales 1-4, including boundaries.");
 }
}
