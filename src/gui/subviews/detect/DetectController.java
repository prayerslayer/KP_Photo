package gui.subviews.detect;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import photo.FastHessianConfig;
import photo.InterestPoint;
import photo.StitcherFacade;
import gui.subviews.SubController;

public class DetectController extends SubController {

	public DetectController(JPanel view, StitcherFacade stitchy) {
		super(view, stitchy);
	}

	public List<InterestPoint> getInterestPoints(BufferedImage img) {
		return stitcher.detectInterestPoints( img );
	}
	
	public List<InterestPoint> getInterestPoints( BufferedImage img, FastHessianConfig config ) {
		return stitcher.detectInterestPoints( img, config == null ? new FastHessianConfig() : config );
	}

}
