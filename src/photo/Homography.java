package photo;

import georegression.struct.homo.Homography2D_F64;

import java.awt.image.BufferedImage;

/**
 * Describes a homography to transform the second image into the first. That's weird, I know!
 * @author xnikp
 *
 */
public class Homography {
	private ImagePair pair;
	private Homography2D_F64 homo;
	
	public Homography( BufferedImage a, BufferedImage b, Homography2D_F64 h ) {
		pair = new ImagePair( a, b );
		homo = h;
	}

	public BufferedImage getImage1() {
		return pair.getImage1();
	}

	public BufferedImage getImage2() {
		return pair.getImage2();
	}

	public Homography2D_F64 getHomography() {
		return homo;
	}
	
	public void setImage1( BufferedImage img ) {
		pair.setImage1( img );
	}
	
	public void setImage2( BufferedImage img ) {
		pair.setImage2( img );
	}

	public void setHomo(Homography2D_F64 homo) {
		this.homo = homo;
	}
}
