package gui.subviews.associate;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import photo.InterestPoint;
import photo.PointAssociation;
import photo.StitcherFacade;
import gui.subviews.SubController;

public class AssociateController extends SubController {

	public AssociateController(JPanel view, StitcherFacade stitchy) {
		super(view, stitchy);
	}

	public List<PointAssociation> associateImages(BufferedImage lefty, BufferedImage righty) {
		return stitcher.associateInterestPoints( lefty, righty );
	}

	public List<BufferedImage> getRegisteredImages() {
		return stitcher.getRegisteredImages();
	}

	public void renderInterestPointsIn(BufferedImage lefty,
			BufferedImage leftcopy) {
		stitcher.renderIP(lefty, leftcopy );
		
	}

	public List<InterestPoint> getInterestPointsFor(BufferedImage img) {
		return stitcher.getInterestPointsFor( img );
	}
}
