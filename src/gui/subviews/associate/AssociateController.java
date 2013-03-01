package gui.subviews.associate;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import photo.Homography;
import photo.InterestPoint;
import photo.OrientationFailedException;
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
	
	public List<PointAssociation> getMatchedAssociations( BufferedImage left, BufferedImage right ) {
		return stitcher.getMatchedAssociations( left, right );
	}

	public Homography orientImages(BufferedImage left, BufferedImage right) throws OrientationFailedException {
		return stitcher.orientImages(left, right);
	}
}
