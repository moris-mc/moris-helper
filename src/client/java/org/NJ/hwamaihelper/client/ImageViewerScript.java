package org.NJ.hwamaihelper.client;
import java.nio.charset.StandardCharsets;
public final class ImageViewerScript {
 public static final String INSTALL;
 static {try(var in=ImageViewerScript.class.getResourceAsStream("/assets/senyouhelper/image-viewer.js")){if(in==null)throw new IllegalStateException("Missing image viewer script");INSTALL=new String(in.readAllBytes(),StandardCharsets.UTF_8);}catch(java.io.IOException e){throw new ExceptionInInitializerError(e);}}
 private ImageViewerScript(){}
}
