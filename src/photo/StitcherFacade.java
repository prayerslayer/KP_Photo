package photo;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

/**
 * Singleton to encapsulate and hide various stitching functionality. Provides connection to library (OpenCV, BoofCV or whatever).
 * @author xnikp
 *
 */
public class StitcherFacade {
	private static StitcherFacade instance;
	private Map< BufferedImage, MultiSpectral < ImageUInt8 > > images;
	
	private StitcherFacade() {
		images = new HashMap< BufferedImage, MultiSpectral < ImageUInt8 > >();
	}
	
	public static StitcherFacade getInstance() {
		if ( instance == null )
			instance = new StitcherFacade();
		return instance;
	}
	
	/**
	 * Converts BufferedImage to BoofCV multispectral image type.
	 * 
	 * @param img
	 * @return
	 */
	private MultiSpectral < ImageUInt8 > convertToBoof( BufferedImage img ) {
		return ConvertBufferedImage.convertFromMulti( img, null, ImageUInt8.class );
	}

	
	/**
	 * Removes an image from the stitching process in case the user deleted it.
	 * @param image
	 */
	public void unregisterImage( BufferedImage image ) {
		if ( image == null )
			return;
		
		//delete
		images.remove( image );
		System.out.println( "Removed image from processing" );
	}

	/**
	 * Adds an image to the stitching process.
	 * @return The index of the image, -1 if it was not added
	 * @param image The image to add
	 */
	public void registerImage( BufferedImage image ) {
		if ( image == null )
			return;
		// convert to boofcv multispectral image
		MultiSpectral < ImageUInt8 > img = convertToBoof( image );
		// add to images
		images.put( image, img );
		System.out.println( "Converted and added image for processing" );
	}
	
	
}
