package photo;

/**
 * Class to hold a Fast Hessian interest point.
 * @author xnikp
 *
 */
public class InterestPoint {
	private double x;
	private double y;
	private double radius;
	
	public InterestPoint( double x, double y, double radius ) {
		this.x = x;
		this.y = y;
		this.radius = radius;
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
	
	
}
