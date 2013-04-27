package photo;

import georegression.struct.point.Point2D_I32;

/**
 * Class to define a rectangular image region.
 * @author xnikp
 *
 */
public class ImageRegion {
	public int[] tleft = new int[2];
	public int[] bleft = new int[2];
	public int[] tright = new int[2];
	public int[] bright = new int[2];
	
	public ImageRegion( Point2D_I32 tleft, Point2D_I32 tright, Point2D_I32 bright, Point2D_I32 bleft ) {
		this.tleft[0] = tleft.x;
		this.tleft[1] = tleft.y;
		
		this.tright[0] = tright.x;
		this.tright[1] = tright.y;
		
		this.bright[0] = bright.x;
		this.bright[1] = bright.y;
		
		this.bleft[0] = bleft.x;
		this.bleft[1] = bleft.y;
	}
	
	public static ImageRegion getFromSquare( int centerX, int centerY, int size ) {
		Point2D_I32 tl = new Point2D_I32( centerX - size, centerY - size );
		Point2D_I32 tr = new Point2D_I32( centerX + size, centerY - size );
		Point2D_I32 br = new Point2D_I32( centerX + size, centerY + size );
		Point2D_I32 bl = new Point2D_I32( centerX - size, centerY + size );
		return new ImageRegion( tl, tr, br, bl );
	}
	
	public boolean grow( Direction dir, int size ) {
		//negative size is allowed, see shrink()
		if ( size == 0 )
			return false;
		
		if ( dir == Direction.LEFT ) {
			if ( tleft[0] - size < 0 || bleft[0] - size < 0 )
				return false;
			tleft[0] = tleft[0] - size;
			bleft[0] = bleft[0] - size;
		} else if ( dir == Direction.RIGHT ) {
			tright[0] = tright[0] + size;
			bright[0] = bright[0] + size;
		} else if ( dir == Direction.TOP ) {
			if ( tleft[1] - size < 0 || tright[1] - size < 0 )
				return false;
			tleft[1] = tleft[1] - size;
			tright[1] = tright[1] - size;
		} else {
			bleft[1] = bleft[1] + size;
			bright[1] = bright[1] + size;
		}
		return true;
	}
	
	public void shrink( Direction dir, int size ) {
		grow( dir, -size );
	}
	
	public int getHeight() {
		int top = Math.min( tleft[1], tright[1] );
		int bot = Math.max( bleft[1], bright[1] );
		return bot - top;
	}
	
	public int getWidth() {
		int left = Math.min( tleft[0], bleft[0] );
		int right = Math.max( tright[0], bright[0] );
		return right - left;
	}
	
	public String toString() {
		return "From Top-Left clockwise: " + tleft[0] + "/" + tleft[1] + ", " + tright[0] + "/" + tright[1] + ", " + bright[0] + "/" + bright[1] + ", " + bleft[0] + "/" + bleft[1];
	}
}
