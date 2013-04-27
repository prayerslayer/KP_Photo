package util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageBase;

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
	 * Space between images if they are combined to one
	 */
	public static int GUTTER = 20;
	/**
	 * Resizes an image with BufferedImage.getScaledInstance
	 * @param img the image to resize
	 * @return a resized image. The longest side is now @{IMAGE_SIZE} px long
	 */
	public static Image resizeImage( BufferedImage img ) {
		return resizeImage( img, 1f );
	}
	
	public static Image resizeImage( BufferedImage img, double factor ) {
		Image small;
		float ratio = ( float ) img.getWidth() / img.getHeight();
		if ( ratio > 1 ) {
			// landscape
			small = img.getScaledInstance( (int)Math.round( factor*IMAGE_SIZE ), (int)Math.round( IMAGE_SIZE*factor/ratio) , Image.SCALE_FAST );
		} else if ( ratio < 1 ){
			// portrait
			small = img.getScaledInstance( (int)Math.round( IMAGE_SIZE*factor/ratio ), (int)Math.round( factor*IMAGE_SIZE ), Image.SCALE_FAST );
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
	
	public static BufferedImage scaleImage( BufferedImage img, int longestSide ) {
		int imgWidth = img.getWidth();
	    int imgHeight = img.getHeight();
	    int width = 0;
	    int height = 0;
	    
	    float ratio = (float) imgWidth / imgHeight;
	    if ( ratio > 1 ) {
	    	//landscape
	    	width = longestSide;
	    	height = Math.round( longestSide/ratio );
	    } else if ( ratio < 1 ) {
	    	//portrait
	    	height = longestSide;
	    	width = Math.round( longestSide/ratio );
	    } else {
	    	width = longestSide;
	    	height = width;
	    }
	    	
	    
	    if (imgWidth*height < imgHeight*width) {
	        width = imgWidth*height/imgHeight;
	    } else {
	        height = imgHeight*width/imgWidth;
	    }
	    BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = newImage.createGraphics();
	    try {
	        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
	                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	        g.setBackground(Color.BLACK);
	        g.clearRect(0, 0, width, height);
	        g.drawImage(img, 0, 0, width, height, null);
	    } finally {
	        g.dispose();
	    }
	    return newImage;
	}
	
	/**
	 * Scales an image without BufferedImage.getScaledInstance
	 * @param img
	 * @param background
	 * @return
	 */
	public static BufferedImage scaleImage(BufferedImage img) {
		return scaleImage( img, IMAGE_SIZE );
	}

	/**
	 * Draws two image in one.
	 * @param left left image
	 * @param right right image
	 * @return image containing left and right with some space between
	 */
	public static BufferedImage combineImages(BufferedImage left, BufferedImage right) {
		int wleft = left.getWidth();
		int wright = right.getWidth();
		int hleft = left.getHeight();
		int hright = right.getHeight();
		int totalWidth = wleft + GUTTER + wright;
		int totalHeight = Math.max( hleft, hright );
		
		BufferedImage combined = new BufferedImage( totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB );
		Graphics2D g = combined.createGraphics();
		try {
			g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
			g.setBackground( Color.black );
			// draw left
			g.clearRect(0, 0, wleft, hleft );
			g.drawImage( left, 0, 0, wleft, hleft, null );
			// draw right
			g.clearRect( wleft + GUTTER, 0, wright, hright );
			g.drawImage( right, wleft + GUTTER, 0, wright, hright, null );
		} finally {
			g.dispose();
		}
		return combined;
	}
	
	public static void saveImage( BufferedImage output ) {
		saveImage( output, null );
	}

	public static void saveImage(BufferedImage output, String filename ) {
		if ( filename == null) {
			filename = output.hashCode() + "";
		}
		try {
			File folder = new File( "temp" );
			if ( !folder.exists() )
				folder.mkdir();
			File file = new File( "temp" + File.separator + filename +".png" );
			if ( file.exists() ) {
				file = new File( "temp" + File.separator + filename + "_" + new GregorianCalendar().getTimeInMillis() + ".png" );
			}
			ImageIO.write(output, "png", file );
		} catch( IOException e ) {
			e.printStackTrace();
		}
	}
	
	public static void saveImage( ImageBase image ) {
		BufferedImage output = new BufferedImage( image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB );
		ConvertBufferedImage.convertTo( image, output );
		saveImage( output );
	}
	
	public static void saveImage( ImageBase image, String filename ) {
		BufferedImage output = new BufferedImage( image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB );
		ConvertBufferedImage.convertTo( image, output );
		saveImage( output, filename );
	}
}
