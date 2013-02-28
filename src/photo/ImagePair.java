package photo;

import java.awt.image.BufferedImage;

/**
 * Holds an ordered pair of two images. That's what it does.
 * @author xnikp
 *
 */
public class ImagePair{
	BufferedImage image1;
	BufferedImage image2;
	
	public ImagePair( BufferedImage img1, BufferedImage img2 ) {
		image1 = img1;
		image2 = img2;
	}
	
	/**
	 * Returns true if and only if first image equals first image of other pair and second image equals second image of other pair.
	 */
	public boolean equals( Object obj ) {
		if ( obj instanceof ImagePair ) {
			ImagePair other = (ImagePair)obj;
			return other.image1.equals(image1) && other.image2.equals(image2);
		}
		return false;
	}

	public BufferedImage getImage1() {
		return image1;
	}

	public BufferedImage getImage2() {
		return image2;
	}

	public void setImage1(BufferedImage image1) {
		this.image1 = image1;
	}

	public void setImage2(BufferedImage image2) {
		this.image2 = image2;
	}
}
