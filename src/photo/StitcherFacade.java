package photo;

import georegression.struct.point.Point2D_F64;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.alg.misc.GPixelMath;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
import boofcv.struct.BoofDefaults;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

/**
 * Singleton to encapsulate and hide various stitching functionality. Provides connection to library (OpenCV, BoofCV or whatever).
 * @author xnikp
 *
 */
public class StitcherFacade {
	private static StitcherFacade instance;
	private Map< BufferedImage, MultiSpectral< ImageUInt8 > > images;
	private Map< BufferedImage, List<InterestPoint> > interestPoints;
	
	private StitcherFacade() {
		images = new HashMap< BufferedImage, MultiSpectral< ImageUInt8 > >();
		interestPoints = new HashMap< BufferedImage, List<InterestPoint>>();
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
		MultiSpectral< ImageUInt8 > img = convertToBoof( image );
		// add to images
		images.put( image, img );
		System.out.println( "Converted and added image for processing" );
	}
	
	/**
	 * Returns registered images as List.
	 * @return
	 */
	public List<BufferedImage> getRegisteredImages() {
		BufferedImage[] bimgs = new BufferedImage[ images.keySet().size() ];
		images.keySet().toArray( bimgs );
		List<BufferedImage> list = new LinkedList<BufferedImage>( Arrays.asList( bimgs ));
		return list;
	}
	
	public List<InterestPoint> detectInterestPoints( BufferedImage img, FastHessianConfig config ) {
		if ( img == null )
		 	return null;
		// check if image exists
		MultiSpectral<ImageUInt8> image = this.images.get( img );
		if ( image == null ) {
			return null;
		}
		//convert to grayscale image
		ImageUInt8 gray = new ImageUInt8( img.getWidth(), img.getHeight() );
		GPixelMath.averageBand(image, gray);
		// compute
		List<InterestPoint> ips = new LinkedList<InterestPoint>();
		// 10f, 2, 100, 2, 9, 3, 4
		InterestPointDetector<ImageUInt8> detector = FactoryInterestPoint.fastHessian(
				config.getDetectThreshold(),
				config.getExtractRadius(),
				config.getMaxFeaturesPerScale(),
				config.getInitialSampleSize(),
				config.getInitialSize(),
				config.getNumberScalesPerOctave(),
				config.getNumberOfOctaves() );
		detector.detect( gray );
		System.out.println( detector.getNumberOfFeatures() + " features detected" );
		for( int i = 0; i < detector.getNumberOfFeatures(); i++ ) {
			Point2D_F64 point = detector.getLocation( i );
			InterestPoint ip = new InterestPoint( point.x, point.y, detector.getScale( i )*BoofDefaults.SCALE_SPACE_CANONICAL_RADIUS );
			ips.add( ip );
		}
		// add to computed values
		interestPoints.put( img, ips );
		return ips;
	}

	public List<InterestPoint> detectInterestPoints(BufferedImage img) {
		return this.detectInterestPoints( img, new FastHessianConfig() );
	}
}
