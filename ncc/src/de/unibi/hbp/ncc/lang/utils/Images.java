package de.unibi.hbp.ncc.lang.utils;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public final class Images {
   private Images () { }

   /**
    * Convenience method that returns a scaled instance of the * provided {@code BufferedImage}.
    *
    * @param img the original image to be scaled
    * @param targetWidth the desired width of the scaled instance, in pixels
    * @param targetHeight the desired height of the scaled instance, in pixels
    * @param hint one of the rendering hints that corresponds to {@code RenderingHints.KEY_INTERPOLATION} (e.g.
    *             {@code RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR},
    *             {@code RenderingHints.VALUE_INTERPOLATION_BILINEAR},
    *             {@code RenderingHints.VALUE_INTERPOLATION_BICUBIC})
    * @param higherQuality if true, this method will use a multi-step
    *                      scaling technique that provides higher quality than the usual
    *                      one-step technique (only useful in downscaling cases, where
    *                      {@code targetWidth} or {@code targetHeight} is
    *                      smaller than the original dimensions, and generally only when
    *                      the {@code BILINEAR} hint is specified)
    * @return a scaled version of the original {@code BufferedImage}
    */
   public static BufferedImage scale (BufferedImage img, int targetWidth, int targetHeight, Object hint,
                                      boolean higherQuality) {
      int type = (img.getTransparency() == Transparency.OPAQUE)
            ? BufferedImage.TYPE_INT_RGB
            : BufferedImage.TYPE_INT_ARGB;
      BufferedImage ret = img;
      int w, h;
      if (higherQuality) {
         // Use multi-step technique: start with original size, then
         // scale down in multiple passes with drawImage()
         // until the target size is reached
         w = img.getWidth();
         h = img.getHeight();
      }
      else {
         // Use one-step technique: scale directly from original
         // size to target size with a single drawImage() call
         w = targetWidth;
         h = targetHeight;
      }
      do {
         if (higherQuality && w > targetWidth) {
            w /= 2;
            if (w < targetWidth)w = targetWidth;
         }
         if (higherQuality && h > targetHeight) {
            h /= 2;
            if (h < targetHeight)
               h = targetHeight;
         }
         BufferedImage tmp = new BufferedImage(w, h, type);
         Graphics2D g2 = tmp.createGraphics();
         g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
         g2.drawImage(ret, 0, 0, w, h, null);
         g2.dispose();
         if (ret != tmp)
            ret.flush();  // clean up intermediate temporary images, but not the original input image
         ret = tmp;
      } while (w != targetWidth || h != targetHeight);
      return ret;
   }

   // TODO does Scalr provide a higher quality down-scaling?
   public static BufferedImage scale (BufferedImage img, int targetWidth, int targetHeight) {
      return scale(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
   }

   public static BufferedImage fit (BufferedImage img, int maxWidth, int maxHeight, Object hint,
                                    boolean higherQuality) {
      int width = img.getWidth();
      int height = img.getHeight();
      if (width <= 0 || height <= 0)
         return img;  // degenerate or incompletely loaded image
      if (width > maxWidth || height > maxHeight) {
         float scale = Math.min(maxWidth / (float) width, maxHeight / (float) height);
         return scale(img, (int) (width * scale + 0.5f), (int) (height * scale + 0.5f),hint, higherQuality);
      }
      else
         return img;
   }

   public static BufferedImage fit (BufferedImage img, int maxWidth, int maxHeight) {
      return fit(img, maxWidth, maxHeight, RenderingHints.VALUE_INTERPOLATION_BILINEAR, true);
   }

   public static BufferedImage fromIcon (Icon icon, Component intendedUse) {
      // icons will usually have transparency
      BufferedImage ret = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = ret.createGraphics();
      icon.paintIcon(intendedUse, g2, 0, 0);
      g2.dispose();
      return ret;
   }

   // other classes implementing Icon may rely on a component argument (of a specific type), but ImageIcon should be safe without
   public static BufferedImage fromIcon (ImageIcon icon) {
      return fromIcon(icon, null);
   }

}
