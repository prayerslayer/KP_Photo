package gui.subviews.orient;

import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import photo.Homography;
import photo.OrientationFailedException;
import photo.StitcherFacade;
import georegression.struct.homo.Homography2D_F64;
import gui.subviews.SubController;

public class OrientController extends SubController {

	public OrientController(JPanel view, StitcherFacade stitchy) {
		super(view, stitchy);
	}

	public List<BufferedImage> getRegisteredImages() {
		return stitcher.getRegisteredImages();
	}

	public Homography orientImages(BufferedImage img1, BufferedImage img2) throws OrientationFailedException {
		return stitcher.orientImages(img1, img2);
	}

	public List<Homography> orientImages() throws OrientationFailedException {
		return stitcher.orientImages();		
	}
	
	public BufferedImage stitchTogether ( int width, int height, boolean preview ) throws OrientationFailedException {
		return stitcher.stitchTogether(width, height, preview);
	}

}
