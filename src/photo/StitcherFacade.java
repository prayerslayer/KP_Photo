package photo;

import georegression.struct.homo.Homography2D_F64;
import georegression.struct.line.LineSegment2D_F32;
import georegression.struct.point.Point2D_F32;
import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.transform.homo.HomographyPointOps_F64;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.ddogleg.fitting.modelset.ModelMatcher;
import org.ddogleg.fitting.modelset.ransac.Ransac;

import util.Utility;

import boofcv.abst.feature.associate.AssociateDescription;
import boofcv.abst.feature.associate.ScoreAssociation;
import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.line.DetectLineSegmentsGridRansac;
import boofcv.alg.distort.ImageDistort;
import boofcv.alg.distort.PixelTransformHomography_F32;
import boofcv.alg.distort.impl.DistortSupport;
import boofcv.alg.feature.UtilFeature;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.interpolate.InterpolatePixel;
import boofcv.alg.interpolate.impl.ImplBilinearPixel_F32;
import boofcv.alg.misc.GPixelMath;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.sfm.robust.DistanceHomographySq;
import boofcv.alg.sfm.robust.GenerateHomographyLinear;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.factory.feature.associate.FactoryAssociation;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.struct.BoofDefaults;
import boofcv.struct.FastQueue;
import boofcv.struct.feature.AssociatedIndex;
import boofcv.struct.feature.SurfFeature;
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
	private BufferedImage panorama;
	/**
	 * Corner points of regions in panorama
	 */
	private List<ImageRegion> regions;
	/**
	 * Original images
	 */
	private List<BufferedImage> registeredImages;
	/**
	 * Converted images
	 */
	private Map< BufferedImage, MultiSpectral< ImageFloat32 > > images;
	/**
	 * Interest points in images
	 */
	private Map< BufferedImage, List<InterestPoint> > interestPoints;
	/**
	 * Homographies between images (from left to middle and right to middle)
	 */
	private List<Homography> orientedImages;
	/**
	 * Which associations were successful.
	 */
	private Map<ImagePair, List<PointAssociation>> matchedAssociations;
	private DetectDescribePoint detectDescribe;
	private ScoreAssociation<SurfFeature> scorer;
	private AssociateDescription<SurfFeature> association;
	
	private StitcherFacade() {
		registeredImages = new LinkedList<BufferedImage>();
		images = new HashMap< BufferedImage, MultiSpectral< ImageFloat32 > >();
		interestPoints = new HashMap< BufferedImage, List<InterestPoint>>();
		orientedImages = new  LinkedList<Homography>();
		regions = new LinkedList<ImageRegion>();
		matchedAssociations = new HashMap<ImagePair, List<PointAssociation>>();
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
	
	public List<PointAssociation> getMatchedAssociations( BufferedImage img1, BufferedImage img2 ) {
		return matchedAssociations.get( new ImagePair( img1, img2 ) );
	}
	
	private Point2D_I32 projectPoint( int x0 , int y0 , Homography2D_F64 fromBtoWork )
	{
		Point2D_F64 result = new Point2D_F64();
		HomographyPointOps_F64.transform(fromBtoWork, new Point2D_F64(x0, y0), result);
		return new Point2D_I32((int)result.x,(int)result.y);
	}
	
	public BufferedImage makePanorama( int width, int height ) throws OrientationFailedException {
		// stitch all images together
		BufferedImage rawPano = stitchTogether( width, height, false );
		MultiSpectral<ImageFloat32> input = convertToBoof( rawPano );
		ImageFloat32 gray = grayscale( input );
		// task: cut black area
		
		/**
		 * Inverse Star Wars or Inside-out Gargabe Compactor (IOGC) algorithm
		 * ===
		 * 1) Binarize Image
		 * 2) Detect Line Segments
		 * 3) Find enclosing rectangle
		 * 4) Put small square at center of enclosing rectangle
		 * 5) Try to enlarge <amount of pixel> in every direction (top, left, bottom, right)
		 * 		Enlargement is possible if there are (almost?) no white pixels in claimed area
		 * 6) Repeat 5) until no further enlargement is possible
		 */
		
		// 1) binarize image
		BufferedImage bin = binary( input, 1f );
		ImageFloat32 binary = new ImageFloat32( bin.getWidth(), bin.getHeight() );
		ConvertBufferedImage.convertFromSingle( bin, binary, ImageFloat32.class);
		Utility.saveImage( bin );
		
		// 2) detect line segments
		DetectLineSegmentsGridRansac<ImageFloat32, ImageFloat32> detector = FactoryDetectLineAlgs.lineRansac(40, 30, 2.36, true, ImageFloat32.class, ImageFloat32.class);
		List<LineSegment2D_F32> found = detector.detect( binary );
		
		Graphics2D g = bin.createGraphics();
		g.setStroke( new BasicStroke( 3 ) );
		g.setColor( Color.GREEN );
		for ( LineSegment2D_F32 line : found ) {
			g.drawLine( (int)line.a.x, (int)line.a.y, (int)line.b.x, (int)line.b.y );
		}
		g.dispose();
		Utility.saveImage( bin );
		
		// 3) find enclosing rectangle = left-most, top-most, right-most, bottom-most point
		int lmost = binary.width, 
			  tmost = binary.height,
			  rmost = 0,
			  bmost = 0;
		
		for ( LineSegment2D_F32 line : found ) {
			Point2D_F32 left = line.a.x < line.b.x ? line.a : line.b,
						right = line.a.x > line.b.x ? line.a : line.b,
						top = line.a.y < line.b.y ? line.a : line.b,
						bottom = line.a.y > line.b.y ? line.a : line.b;
			
			if ( left.x < lmost ) {
				lmost = (int)left.x;
			}
			if ( right.x > rmost ) {
				rmost = (int)right.x;
			}
			if ( top.y < tmost ) {
				tmost = (int)top.y;
			}
			if ( bottom.y > bmost ) {
				bmost = (int)bottom.y;
			}
		}
		
		// 3a) cut it out
		ImageFloat32 rectangle = binary.subimage(lmost, tmost, rmost, bmost);
		Utility.saveImage( rectangle );
		
		// 4) define small square inside of rectangle
		int squareSize = 10; // 10px
		int centerWidth = rectangle.width/2,
			centerHeight = rectangle.height/2;
		
		ImageRegion square = ImageRegion.getFromSquare( centerWidth, centerHeight, squareSize );
		System.out.println( "Square: " + square );
		
		BufferedImage rect = new BufferedImage( rectangle.width, rectangle.height, BufferedImage.TYPE_INT_RGB );
		ConvertBufferedImage.convertTo( rectangle, rect );
		g = rect.createGraphics();
		g.setStroke( new BasicStroke( 3 ) );
		g.setColor( Color.RED );
		g.drawRect( square.tleft[0], square.tleft[1], square.getWidth(), square.getHeight() );
		g.dispose();
		Utility.saveImage( rect );
		
		// 5) grow square iteratively
		boolean growLeft = true,
				growRight = true,
				growBottom = true,
				growTop = true;
		int push = 10;
		float threshold = 0.95f; 
		int maxiterations = Math.max( rectangle.width/push, rectangle.height/push );
		while ( ( growLeft || growRight || growBottom || growTop ) && maxiterations >= 0 ) {
			if ( growLeft ) {
				// grow square <push> px to left
				boolean grown = square.grow( Direction.LEFT, push );
				if ( grown ) {
					// if square is actually bigger now
					// check area there
					ImageFloat32 claimed = rectangle.subimage( square.tleft[0], square.tleft[1], square.bleft[0] + push, square.bleft[1] + push );
					double mean = ImageStatistics.mean( claimed );
					growLeft = mean < ( 1 - threshold )*255;
				} else
					growLeft = false;
			}
			if ( growTop ) {
				boolean grown = square.grow( Direction.TOP,  push );
				if ( grown ) {
					ImageFloat32 claimed = rectangle.subimage( square.tleft[0], square.tleft[1], square.tright[0], square.tright[1] + push );
					double mean = ImageStatistics.mean( claimed );
					growTop = mean < ( 1 - threshold )*255;
					
				} else
					growTop = false;
			}
			if ( growBottom ) {
				square.grow( Direction.BOTTOM, push );
				if ( square.bleft[1] > rectangle.height )
					square.bleft[1] = rectangle.height;
				if ( square.bright[1] > rectangle.height )
					square.bright[1] = rectangle.height;
				ImageFloat32 claimed = rectangle.subimage( square.bleft[0], square.bleft[1] - push, square.bright[0], square.bright[1] );
				double mean = ImageStatistics.mean( claimed );
				growBottom = mean < ( 1 - threshold )*255;
			}
			if ( growRight ) {
				square.grow( Direction.RIGHT, push );
				// pluck new values if necessary
				if ( square.tright[0] > rectangle.width )
					square.tright[0] = rectangle.width;
				if ( square.bright[0] > rectangle.width )
					square.bright[0] = rectangle.width;
				ImageFloat32 claimed = rectangle.subimage( square.tright[0] - push, square.tright[1], square.bright[0], square.bright[1] );
				double mean = ImageStatistics.mean( claimed );
				growRight = mean < ( 1 - threshold )*255;
			}
			maxiterations--;
		}
		
		// 6) cut region from rawPano
		
		int x0 = square.tleft[0],
			y0 = square.tleft[1],
			x1 = square.bright[0],
			y1 = square.bright[1];
		// now we have a rectangle definded by (x0,y0) and (x1,y)
		// cut black area
		MultiSpectral<ImageFloat32> pano = input.subimage(lmost, tmost, rmost, bmost); // cut outer rectangle
		pano = pano.subimage( (int) x0, (int) y0, (int) x1, (int) y1 ); // cut inner rectangle
		panorama = new BufferedImage( pano.width, pano.height, BufferedImage.TYPE_INT_RGB );
		ConvertBufferedImage.convertTo( pano, panorama );
		
		Utility.saveImage( panorama );
		return panorama;
	}
	
	/**
	 * Stitches registered images to panorama.
	 * @param preview if borders of individual images should be drawn
	 * @return panorama image
	 * @throws OrientationFailedException 
	 */
	public BufferedImage stitchTogether( int desiredWidth, int desiredHeight, boolean preview ) throws OrientationFailedException {
		int size = registeredImages.size();
		int m = size % 2 == 0 ? size / 2 : ( size - 1 ) / 2;
		int width = size * registeredImages.get( 0 ).getWidth();
		int height = registeredImages.get( 0 ).getHeight();
		regions.clear();
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
			ImageRegion region = new ImageRegion( 
					projectPoint( 0, 0, toSingle ),
					projectPoint( toStitchBoof.width, 0, toSingle ),
					projectPoint( toStitchBoof.width, toStitchBoof.height, toSingle ),
					projectPoint( 0, toStitchBoof.height, toSingle )
			);
			regions.add( region );
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
			ImageRegion region = new ImageRegion( 
					projectPoint( 0, 0, toSingle ),
					projectPoint( toStitchBoof.width, 0, toSingle ),
					projectPoint( toStitchBoof.width, toStitchBoof.height, toSingle ),
					projectPoint( 0, toStitchBoof.height, toSingle )
			);
			regions.add( region );
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
			for( ImageRegion region : regions ) {
				int[] a = region.tleft,
					  b = region.tright,
					  c = region.bright,
					  d = region.bleft;
				g.drawLine( a[0], a[1], b[0], b[1] );
				g.drawLine( b[0], b[1], c[0], c[1] );
				g.drawLine( c[0], c[1], d[0], d[1] );
				g.drawLine( d[0], d[1], a[0], a[1] );
			}
			g.dispose();
		}
		Utility.saveImage( Utility.scaleImage( output ) );
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
		for ( int i = 0; i <= associations.size() - 1; i++ ) {
			PointAssociation pa = associations.get(i); 
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
		
		// save matched associations
		List<PointAssociation> matches = new LinkedList<PointAssociation>();
		ImagePair pair = new ImagePair( img1, img2 );
		for ( AssociatedPair p : modelMatcher.getMatchSet() ) {
			int idx = pairs.indexOf( p );
			matches.add( associations.get( idx ) );
		}
		matchedAssociations.put( pair, matches );
		// return homography
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
	
	private ImageFloat32 grayscale( MultiSpectral<ImageFloat32> img ) {
		ImageFloat32 gray = new ImageFloat32( img.getWidth(), img.getHeight() );
		GPixelMath.averageBand(img, gray);
		return gray;
	}
	
	private BufferedImage binary( MultiSpectral<ImageFloat32> img, float threshold ) {
		ImageFloat32 gray = grayscale( img );
		ImageUInt8 bin = new ImageUInt8( gray.width, gray.height );
		ThresholdImageOps.threshold( gray, bin, threshold, true );
		return VisualizeBinaryData.renderBinary(bin,null);
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
		ImageFloat32 gray = grayscale( image );
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
