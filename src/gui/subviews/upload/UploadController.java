package gui.subviews.upload;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import photo.StitcherFacade;
import gui.subviews.SubController;

public class UploadController extends SubController {

	public UploadController(JPanel view, StitcherFacade stitchy) {
		super(view, stitchy);
		
	}
	
	public void registerImage(BufferedImage img) {
		StitcherFacade.getInstance().registerImage( img );
	}
	
	public void unregisterImage( BufferedImage img ) {
		StitcherFacade.getInstance().unregisterImage( img );
	}

}
