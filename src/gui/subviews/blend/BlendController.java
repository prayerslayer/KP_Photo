package gui.subviews.blend;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import photo.OrientationFailedException;
import photo.StitcherFacade;
import gui.subviews.SubController;

public class BlendController extends SubController {

	public BlendController(JPanel view, StitcherFacade stitchy) {
		super(view, stitchy);
	}
	
	public BufferedImage makePanorama( int width, int height ) throws OrientationFailedException {
		return stitcher.makePanorama( width, height );
	}

}
