package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

/**
 * Class with helper functions
 * @author xnikp
 *
 */
public class Utility {
	/**
	 * Maximum length of an image side
	 */
	public static int IMAGE_SIZE = 600;
	
	/**
	 * Resizes an image with BufferedImage.getScaledInstance
	 * @param img the image to resize
	 * @return a resized image. The longest side is now @{IMAGE_SIZE} px long
	 */
	public static Image resizeImage( BufferedImage img ) {
		Image small;
		float ratio = ( float ) img.getWidth() / img.getHeight();
		if ( ratio > 1 ) {
			// landscape
			small = img.getScaledInstance( IMAGE_SIZE, Math.round( IMAGE_SIZE/ratio) , Image.SCALE_FAST );
		} else if ( ratio < 1 ){
			// portrait
			small = img.getScaledInstance( Math.round( IMAGE_SIZE/ratio ), IMAGE_SIZE, Image.SCALE_FAST );
		} else  {
			// square
			small = img.getScaledInstance( IMAGE_SIZE, IMAGE_SIZE, 0 );
		}
		return small;
	}
	
	/**
	 * Clones an image.
	 * @param bi image to duplicate
	 * @return copy of bi
	 */
	public static BufferedImage duplicateImage( BufferedImage bi ) {
		ColorModel cm = bi.getColorModel();
		 boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		 WritableRaster raster = bi.copyData(null);
		 return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}
	
	/**
	 * Scales an image without BufferedImage.getScaledInstance
	 * @param img
	 * @param background
	 * @return
	 */
	public static BufferedImage scaleImage(BufferedImage img, Color background) {
	    int imgWidth = img.getWidth();
	    int imgHeight = img.getHeight();
	    int width = 0;
	    int height = 0;
	    
	    float ratio = (float) imgWidth / imgHeight;
	    if ( ratio > 1 ) {
	    	//landscape
	    	width = IMAGE_SIZE;
	    	height = Math.round( IMAGE_SIZE/ratio );
	    } else if ( ratio < 1 ) {
	    	//portrait
	    	height = IMAGE_SIZE;
	    	width = Math.round( IMAGE_SIZE/ratio );
	    } else {
	    	width = IMAGE_SIZE;
	    	height = width;
	    }
	    	
	    
	    if (imgWidth*height < imgHeight*width) {
	        width = imgWidth*height/imgHeight;
	    } else {
	        height = imgHeight*width/imgWidth;
	    }
	    BufferedImage newImage = new BufferedImage(width, height,
	            BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g.setBackground(background);
	        g.clearRect(0, 0, width, height);
	        g.drawImage(img, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}
}
