package photo;

import georegression.struct.homo.Homography2D_F64;
import georegression.struct.point.Point2D_F64;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;

import util.Utility;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.misc.GPixelMath;
import boofcv.alg.sfm.robust.DistanceHomographySq;
import boofcv.alg.sfm.robust.GenerateHomographyLinear;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
import boofcv.struct.BoofDefaults;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc;
import boofcv.struct.geo.AssociatedPair;
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
	private DetectDescribePoint detectDescribe;
	private ScoreAssociation<SurfFeature> scorer;
	private AssociateDescription<SurfFeature> association;
	
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
	
	/**
	 * Orients two images and returns the homography model.
	 * @param img1 first image
	 * @param img2 second image
	 * @return relative orientation
	 * @throws OrientationFailedException if images can not be oriented
	 */
	public Homography2D_F64 orientImages( BufferedImage img1, BufferedImage img2 ) throws OrientationFailedException {
		GenerateHomographyLinear modelFitter = new GenerateHomographyLinear( true );
		DistanceHomographySq distance = new DistanceHomographySq();
		ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher = new Ransac<Homography2D_F64, AssociatedPair>(100, modelFitter, distance, 60, 9);
		
		// associate and generate pairs for matcher
		List<AssociatedPair> pairs = new ArrayList<AssociatedPair>();
		List<PointAssociation> associations = associateInterestPoints(img1, img2);
		InterestPoint ip1, ip2;
		Point2D_F64 p1, p2;
		for ( PointAssociation pa : associations ) {
			ip1 = interestPoints.get( img1 ).get( pa.getSource() );
			ip2 = interestPoints.get( img2 ).get( pa.getDestination() );
			p1 = new Point2D_F64( ip1.getX(), ip1.getY() );
			p2 = new Point2D_F64( ip2.getX(), ip2.getY() );
			pairs.add( new AssociatedPair( p1, p2, false ) );
		}
		
		if ( !modelMatcher.process( pairs ) )
			throw new OrientationFailedException();
		return modelMatcher.getModel().copy();
	}
	
	/**
	 * Returns interest points for image
	 * @param img
	 * @return
	 */
	public List<InterestPoint> getInterestPointsFor( BufferedImage img ) {
		if ( this.interestPoints.get( img ) == null )
			detectInterestPoints( img );
		return this.interestPoints.get( img );
	}
	
	/**
	 * Associates interest points in the two images.
	 * @param img1
	 * @param img2
	 * @return List of associations
	 */
	public List<PointAssociation> associateInterestPoints( BufferedImage img1, BufferedImage img2 ) {
		// detect interest points if not done already
		if ( interestPoints.get( img1 ) == null ) {
			detectInterestPoints( img1 );
		}
		if ( interestPoints.get( img2 ) == null ) {
			detectInterestPoints( img2 );
		}
		// set up some collections
		List<InterestPoint> ip1 = interestPoints.get( img1 );
		List<InterestPoint> ip2 = interestPoints.get( img2 );
		FastQueue<SurfFeature> desc1 = UtilFeature.createQueue( detectDescribe, 100 );
		FastQueue<SurfFeature> desc2 = UtilFeature.createQueue( detectDescribe, 100 );
		List<Point2D_F64> points1 = new ArrayList<Point2D_F64>();
		List<Point2D_F64> points2 = new ArrayList<Point2D_F64>();
		// define score and association algorithm
		scorer = FactoryAssociation.scoreEuclidean( SurfFeature.class, true );
		association = FactoryAssociation.greedy(scorer, Double.MAX_VALUE, true);
		
		// collect points and descriptions for association
		for ( InterestPoint ip : ip1 ) {
			points1.add( new Point2D_F64( ip.getX(), ip.getY() ) );
			desc1.grow().setTo( ip.getDescription() );
		}
		for ( InterestPoint ip : ip2 ) {
			points2.add( new Point2D_F64( ip.getX(), ip.getY() ) );
			desc2.grow().setTo( ip.getDescription() );
		}
		
		// do the actual association
		association.setSource( desc1 );
		association.setDestination( desc2 );
		association.associate();
		
		// write result into library independent representation
		List<PointAssociation> result = new ArrayList<PointAssociation>();
		List<AssociatedIndex> list = new ArrayList<AssociatedIndex>();
		association.getMatches().copyIntoList( list );
		for ( AssociatedIndex idx : list ) {
			result.add( new PointAssociation( idx.src, idx.dst, idx.fitScore ) );
		}
		return result;
	}
	
	/**
	 * Renders interest points in image.
	 * @param img
	 * @return
	 */
	public BufferedImage renderIP( BufferedImage img ) {
		BufferedImage copy = Utility.duplicateImage( img );
		if ( interestPoints.get( img ) == null ) {
			detectInterestPoints( img );
		}
		Graphics2D graphics = copy.createGraphics();
		graphics.setStroke( new BasicStroke( 3 ) );
		graphics.setColor( Color.RED );
		for ( InterestPoint ip : interestPoints.get( img ) ) {
			int x = ( int )Math.round( ip.getX() );
			int y = ( int )Math.round( ip.getY() );
			int r = ( int )Math.round( ip.getRadius() );
			
			graphics.drawOval( x-r/2, y-r/2, r, r );
		}
		graphics.dispose();
		return copy;
	}
	
	/**
	 * Detects interest points in image.
	 * @param img
	 * @param config
	 * @return
	 */
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
		detectDescribe = FactoryDetectDescribe.surfStable(
				new ConfigFastHessian( 
					config.getDetectThreshold(),
					config.getExtractRadius(),
					config.getMaxFeaturesPerScale(),
					config.getInitialSampleSize(),
					config.getInitialSize(),
					config.getNumberScalesPerOctave(),
					config.getNumberOfOctaves()
				),
				null,
				null,
				ImageUInt8.class);
		detectDescribe.detect( gray );
		System.out.println( detectDescribe.getNumberOfFeatures() + " features detected" );
		for( int i = 0; i < detectDescribe.getNumberOfFeatures(); i++ ) {
			Point2D_F64 point = detectDescribe.getLocation( i );
			InterestPoint ip = new InterestPoint( point.x, point.y, detectDescribe.getScale( i )*BoofDefaults.SCALE_SPACE_CANONICAL_RADIUS, (SurfFeature)detectDescribe.getDescription( i ) );
			ips.add( ip );
		}
		// add to computed values
		interestPoints.put( img, ips );
		return ips;
	}

	/**
	 * Detects interest points in image with default parameters
	 * @param img
	 * @return
	 */
	public List<InterestPoint> detectInterestPoints(BufferedImage img) {
		return this.detectInterestPoints( img, new FastHessianConfig() );
	}

	/**
	 * Renders interest points of img in copy.
	 * @param img
	 * @param copy
	 */
	public void renderIP(BufferedImage img, BufferedImage copy ) {
		if ( interestPoints.get( img ) == null ) {
			detectInterestPoints( img );
		}
		Graphics2D graphics = copy.createGraphics();
		graphics.setStroke( new BasicStroke( 3 ) );
		graphics.setColor( Color.RED );
		for ( InterestPoint ip : interestPoints.get( img ) ) {
			int x = ( int )Math.round( ip.getX() );
			int y = ( int )Math.round( ip.getY() );
			int r = ( int )Math.round( ip.getRadius() );
			
			graphics.drawOval( x-r/2, y-r/2, r, r );
		}
		graphics.dispose();
	}
}
