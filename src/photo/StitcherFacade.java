package photo;

import georegression.struct.homo.Homography2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.transform.homo.HomographyPointOps_F64;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;

import util.Utility;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.impl.ImplBilinearPixel_F32;
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
	private List<BufferedImage> registeredImages;
	private Map< BufferedImage, MultiSpectral< ImageFloat32 > > images;
	private Map< BufferedImage, List<InterestPoint> > interestPoints;
	private List<Homography> orientedImages;
	private DetectDescribePoint detectDescribe;
	private ScoreAssociation<SurfFeature> scorer;
	private AssociateDescription<SurfFeature> association;
	
	private StitcherFacade() {
		registeredImages = new LinkedList<BufferedImage>();
		images = new HashMap< BufferedImage, MultiSpectral< ImageFloat32 > >();
		interestPoints = new HashMap< BufferedImage, List<InterestPoint>>();
		orientedImages = new  LinkedList<Homography>();
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
	private MultiSpectral < ImageFloat32 > convertToBoof( BufferedImage img ) {
		return ConvertBufferedImage.convertFromMulti( img, null, ImageFloat32.class );
	}

	
	/**
	 * Removes an image from the stitching process in case the user deleted it.
	 * @param image
	 */
	public void unregisterImage( BufferedImage image ) {
		if ( image == null )
			return;
		
		//delete
		registeredImages.remove( image );
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
		MultiSpectral< ImageFloat32 > img = convertToBoof( image );
		// add to images
		registeredImages.add( image );
		images.put( image, img );
		System.out.println( "Converted and added image for processing" );
	}
	
	/**
	 * Returns registered images as List.
	 * @return
	 */
	public List<BufferedImage> getRegisteredImages() {
		return registeredImages;
	}
	
	private Point2D_I32 projectPoint( int x0 , int y0 , Homography2D_F64 fromBtoWork )
	{
		Point2D_F64 result = new Point2D_F64();
		HomographyPointOps_F64.transform(fromBtoWork, new Point2D_F64(x0, y0), result);
		return new Point2D_I32((int)result.x,(int)result.y);
	}
	
	public BufferedImage stitch( Homography homo, boolean preview ) {		
		MultiSpectral<ImageFloat32> color1 = convertToBoof( homo.getImage1() );
		MultiSpectral<ImageFloat32> color2 = convertToBoof( homo.getImage2() );
		MultiSpectral<ImageFloat32> work = new MultiSpectral<ImageFloat32>(ImageFloat32.class, color1.getWidth(), color1.getHeight(), 4);
		
		PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
		InterpolatePixel<ImageFloat32> interp = new ImplBilinearPixel_F32();
		ImageDistort<MultiSpectral<ImageFloat32>> distort = DistortSupport.createDistortMS(ImageFloat32.class, model, interp, null);
		
		// draw first image
		float s = 1f; // scale
		int w = color1.width/4;
		int h = color1.height/4;
		Homography2D_F64 workToFirst = new Homography2D_F64(s, 0, w,
															0, s, h,
															0, 0, 1 );
		workToFirst.invert(null);
		model.set( workToFirst );
		distort.apply( color1, work );
		// draw second image
		Homography2D_F64 workToSecond = workToFirst.concat(homo.getHomography(), null);
		model.set( workToSecond );
		distort.apply( color2, work );
		
		BufferedImage output = new BufferedImage( work.width, work.height, homo.getImage1().getType() );
		ConvertBufferedImage.convertTo( work, output );
		
		if ( preview ) {
			// project corner points of second image
			Point2D_I32 corners[] = { new Point2D_I32(), new Point2D_I32(), new Point2D_I32(), new Point2D_I32()};
			Homography2D_F64 secondToWork = workToSecond.invert(null);
			corners[0] = projectPoint( 0, 0, secondToWork );
			corners[1] = projectPoint( color2.width, 0, secondToWork );
			corners[2] = projectPoint( color2.width, color2.height, secondToWork );
			corners[3] = projectPoint( 0, color2.height,secondToWork );
			// draw in output
			Graphics2D g = output.createGraphics();
			g.setStroke( new BasicStroke( 3 ) );
			g.setColor( Color.RED );
			g.drawLine( corners[0].x, corners[0].y, corners[1].x, corners[1].y );
			g.drawLine( corners[1].x, corners[1].y, corners[2].x, corners[2].y );
			g.drawLine( corners[2].x, corners[2].y, corners[3].x, corners[3].y );
			g.drawLine( corners[3].x, corners[3].y, corners[0].x, corners[0].y );
			g.dispose();
		}
		
		//Utility.saveImage( output );
		
		return output;
	}
	
	/**
	 * Stitches registered images to panorama.
	 * @param preview if borders of individual images should be drawn
	 * @return panorama image
	 * @throws OrientationFailedException 
	 */
	public BufferedImage stitchTogether( int width, int height, boolean preview ) throws OrientationFailedException {
		int size = registeredImages.size();
		int m = size % 2 == 0 ? size / 2 : ( size - 1 ) / 2;
		System.out.println( "Middle index: " + m );
		// orient all image pairs
		orientImages();
		
		// define stuff
		// work is a black surface at first
		MultiSpectral<ImageFloat32> work = new MultiSpectral<ImageFloat32>(ImageFloat32.class, width, height, 3);
		PixelTransformHomography_F32 model = new PixelTransformHomography_F32();
		InterpolatePixel<ImageFloat32> interp = new ImplBilinearPixel_F32();
		ImageDistort<MultiSpectral<ImageFloat32>> distort = DistortSupport.createDistortMS(ImageFloat32.class, model, interp, null);
		MultiSpectral<ImageFloat32> middle = images.get( registeredImages.get( m ) );
		
		// draw middle image in black surface (work)
		float s = 2f; // scale
		Homography2D_F64 workToMiddle = new Homography2D_F64(s, 0, -middle.width/2,
															 0, s, -middle.height/2,
															 0, 0, 1 );
		workToMiddle.invert(null);
		model.set( workToMiddle );
		distort.apply( middle, work );
		
		// now there is our middle image on the surface. whee! let's draw all the other images too!
		List<List<Point2D_I32>> points = new LinkedList<List<Point2D_I32>>(); // that are the corner points we may need to draw too, depending on preview value
		List<Homography2D_F64> usedHomos = new LinkedList<Homography2D_F64>(); // because we need to multiply some matrices on the way from the middle to the outer sides
		usedHomos.add( workToMiddle );
		// draw from middle to outer left
		for ( int i = m; i >= 1; i-- ) {
			// get transformation matrix (homography) from left image to right image
			BufferedImage toStitch = registeredImages.get( i - 1 );
			MultiSpectral<ImageFloat32> toStitchBoof = images.get( toStitch );
			usedHomos.add( findHomography( registeredImages.get( i ), toStitch ).getHomography() );	// add this particular matrix to our homographies
			Homography2D_F64 singleHomo = new Homography2D_F64();
			// concatenate all homographies since we want to project into middle image
			int h = 0;
			while ( h < usedHomos.size() - 1 ) {
				usedHomos.get( h ).concat( usedHomos.get( h + 1 ), singleHomo );
				h += 1;
			}
			// draw toStitch on black surface!
			model.set( singleHomo );
			distort.apply( toStitchBoof, work );
			
			// awesome! now save the points to draw later!
			Homography2D_F64 toSingle = singleHomo.invert( null );
			List<Point2D_I32> popo = new ArrayList<Point2D_I32>();
			popo.add( projectPoint( 0, 0, toSingle ) );
			popo.add( projectPoint( toStitchBoof.width, 0, toSingle ) );
			popo.add( projectPoint( toStitchBoof.width, toStitchBoof.height, toSingle ) );
			popo.add( projectPoint( 0, toStitchBoof.height, toSingle ) );
			points.add( popo );
		}
		// clear used homographies
		usedHomos.clear();
		usedHomos.add( workToMiddle );
		// great! now the same from middle to outer right
		for ( int i = m; i < size - 1; i++ ) {
			// more or less copy/paste
			BufferedImage toStitch = registeredImages.get( i + 1 );
			MultiSpectral<ImageFloat32> toStitchBoof = images.get( toStitch );
			usedHomos.add( findHomography( registeredImages.get( i ), toStitch ).getHomography() );	// add this particular matrix to our homographies
			Homography2D_F64 singleHomo = new Homography2D_F64();
			
			int h = 0;
			while ( h < usedHomos.size() - 1 ) {
				usedHomos.get( h ).concat( usedHomos.get( h + 1 ), singleHomo );
				h += 1;
			}
			
			model.set( singleHomo );
			distort.apply( toStitchBoof, work );
			
			Homography2D_F64 toSingle = singleHomo.invert( null );
			List<Point2D_I32> popo = new ArrayList<Point2D_I32>();
			popo.add( projectPoint( 0, 0, toSingle ) );
			popo.add( projectPoint( toStitchBoof.width, 0, toSingle ) );
			popo.add( projectPoint( toStitchBoof.width, toStitchBoof.height, toSingle ) );
			popo.add( projectPoint( 0, toStitchBoof.height, toSingle ) );
			points.add( popo );
		}
		
		// well well well young fella. now every image is on the not-so-black-anymore surface
		// let's make us a nice buffered image
		
		BufferedImage output = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
		ConvertBufferedImage.convertTo( work, output );
		
		if ( preview ) {
			Graphics2D g = output.createGraphics();
			g.setStroke( new BasicStroke( 3 ) );
			g.setColor( Color.RED );
			// draw all the lines in it
			for( List<Point2D_I32> popo : points ) {
				Point2D_I32 a = popo.get( 0 ),
							b = popo.get( 1 ),
							c = popo.get( 2 ),
							d = popo.get( 3 );
				g.drawLine( a.x, a.y, b.x, b.y );
				g.drawLine( b.x, b.y, c.x, c.y );
				g.drawLine( c.x, c.y, d.x, d.y );
				g.drawLine( d.x, d.y, a.x, a.y );
			}
			g.dispose();
		}
		Utility.saveImage( output );
		return output;
	}
	
	private Homography findHomography( BufferedImage img1, BufferedImage img2 ) {
		for ( Homography homo : orientedImages ) {
			if ( homo.getImage1().equals(img1) && homo.getImage2().equals( img2 ) ) {
				return homo;
			}
		}
		return null;
	}
	
	public List<Homography> orientImages() throws OrientationFailedException {
		// Assumes registeredImages are ordered from left to right
		int size = registeredImages.size();
		int middle = size / 2;
		
		if ( !orientedImages.isEmpty() ) {
			orientedImages.clear();
		}
		
		// orient images from left to middle
		for ( int i = middle; i >= 1; i-- ) {
			BufferedImage img1 = registeredImages.get( i );
			BufferedImage img2 = registeredImages.get( i - 1);
			orientedImages.add( orientImages( img1, img2 ) );
		}
		
		// orient images from right to middle
		for ( int i = middle; i < size - 1; i++ ) {
			BufferedImage img1 = registeredImages.get( i );
			BufferedImage img2 = registeredImages.get( i + 1 );
			orientedImages.add( orientImages( img1, img2 ) );
		}
		return orientedImages;
	}
	
	/**
	 * Orients two images and returns the homography model.
	 * @param img1 first image
	 * @param img2 second image
	 * @return relative orientation
	 * @throws OrientationFailedException if images can not be oriented
	 */
	public Homography orientImages( BufferedImage img1, BufferedImage img2 ) throws OrientationFailedException {
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
		
		// find homography with ransac
		GenerateHomographyLinear modelFitter = new GenerateHomographyLinear( true );
		DistanceHomographySq distance = new DistanceHomographySq();
		//TODO define ransac iterations
		int iterations = Math.round( associations.size() *( 1/2f) );
		ModelMatcher<Homography2D_F64, AssociatedPair> modelMatcher = new Ransac<Homography2D_F64, AssociatedPair>( 100, modelFitter, distance, iterations, 5);
		
		if ( !modelMatcher.process( pairs ) )
			throw new OrientationFailedException();
		
		Homography2D_F64 result = modelMatcher.getModel().copy();
		System.out.println( "Rotation matrix:" );
		System.out.println( result );
		return new Homography( img1, img2, result);
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
		MultiSpectral<ImageFloat32> image = this.images.get( img );
		if ( image == null ) {
			return null;
		}
		//convert to grayscale image
		ImageFloat32 gray = new ImageFloat32( img.getWidth(), img.getHeight() );
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
				ImageFloat32.class);
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
