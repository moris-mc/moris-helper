import org.NJ.hwamaihelper.client.ImageHitBoxes;
import java.net.URI;
public class ImageHitBoxesCheck {
 public static void main(String[] args){
  URI image=URI.create("https://cdn.discordapp.com/attachments/1/2/a.png?hm=signed");
  ImageHitBoxes.clear();ImageHitBoxes.add(image,12,80,200,180);
  if(!image.equals(ImageHitBoxes.at(12,80))||!image.equals(ImageHitBoxes.at(199.9,179.9)))throw new AssertionError("visible image area");
  if(ImageHitBoxes.at(11.9,100)!=null||ImageHitBoxes.at(200,100)!=null||ImageHitBoxes.at(20,180)!=null)throw new AssertionError("clipped boundaries");
  ImageHitBoxes.clear();if(ImageHitBoxes.at(20,100)!=null)throw new AssertionError("stale click region");
  ImageHitBoxes.add(image,12,180,200,80);if(ImageHitBoxes.at(20,100)!=null)throw new AssertionError("invisible image");
  System.out.println("PASS: visible thumbnail click areas, clipped edges, signed URL preservation and stale area clearing.");
 }
}
