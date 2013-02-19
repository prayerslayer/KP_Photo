package gui.subviews;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import photo.StitcherFacade;

/**
 * Class that encapsulates methods used in each step, provides connecton to StitcherFacade.
 * @author xnikp
 *
 */
public class SubController {
	
	protected JPanel subview;
	protected StitcherFacade stitcher;
	
	public SubController( JPanel view, StitcherFacade stitchy ) {
		subview = view;
		stitcher = stitchy; // Stitchy & Scratchy!
	}
}
