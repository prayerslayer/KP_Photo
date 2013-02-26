package photo;

import boofcv.struct.feature.SurfFeature;
import boofcv.struct.feature.TupleDesc;

/**
 * Class to hold a Fast Hessian interest point.
 * @author xnikp
 *
 */
public class InterestPoint {
	private double x;
	private double y;
	private double radius;
	private SurfFeature description;
	
	public InterestPoint( double x, double y, double radius, SurfFeature description ) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.description = description;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getRadius() {
		return radius;
	}
	
	public SurfFeature getDescription() {
		return this.description;
	}
}
