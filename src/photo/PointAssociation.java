package photo;

/**
 * Class to hold an association between two points in distinct images.
 * @author xnikp
 *
 */
public class PointAssociation {
	private int source;
	private int destination;
	private double score;
	
	public PointAssociation( int src, int dest, double score) {
		this.source = src;
		this.destination = dest;
		this.score = score;
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

	public double getScore() {
		return score;
	}
	
	public String toString() {
		return source + " - " + destination + "(" + score + ")";
	}
}
