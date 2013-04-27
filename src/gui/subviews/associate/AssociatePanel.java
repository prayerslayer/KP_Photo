package gui.subviews.associate;

import gui.subviews.SubView;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import photo.InterestPoint;
import photo.OrientationFailedException;
import photo.PointAssociation;
import photo.StitcherFacade;
import util.Utility;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * UI for interest point association step.
 * @author xnikp
 *
 */
public class AssociatePanel extends SubView {
	/**
	 * Left image
	 */
	private BufferedImage left;
	/**
	 * Right image
	 */
	private BufferedImage right;
	/**
	 * Both
	 */
	private BufferedImage combined;
	/**
	 * Interest points of all the images
	 */
	private Map<BufferedImage, List<InterestPoint>> images;
	/**
	 * All associations between interest points
	 */
	private List<PointAssociation> associations;
	/**
	 * Successful associations between interest points
	 */
	private List<PointAssociation> matches;
	private JLabel lblImage;
	
	/**
	 * Creates new AssociatePanel
	 */
	public AssociatePanel() {
		setLayout(new BorderLayout(0, 0));
		
		lblImage = new JLabel();
		lblImage.setHorizontalAlignment( JLabel.CENTER );
		lblImage.setVerticalAlignment( JLabel.CENTER );
		add(lblImage, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton btnAlleAssoziationen = new JButton("Alle Assoziationen");
		btnAlleAssoziationen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawAssociations();
			}
		});
		panel.add(btnAlleAssoziationen);
	}

	@Override
	/**
	 * Draws UI controls
	 */
	public void init() {
		controller = new AssociateController( this, StitcherFacade.getInstance() );
		// get images and interest points
		images = new HashMap<BufferedImage, List<InterestPoint>>();
		List<BufferedImage> imgs = ((AssociateController)controller).getRegisteredImages();
		for ( BufferedImage img : imgs ) {
			List<InterestPoint> ips = ((AssociateController)controller).getInterestPointsFor( img );
			images.put( img, ips );
		}
	
		left = imgs.get(0);
		right = imgs.get(1);
		// get associations
		associations = ((AssociateController)controller).associateImages( left, right );
		
		try {
			// no joke, needed for matched associations
			((AssociateController)controller).orientImages( left, right );
			matches = ((AssociateController)controller).getMatchedAssociations(left, right);
		} catch (OrientationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// draw them
		BufferedImage leftcopy = Utility.duplicateImage( left );
		BufferedImage rightcopy = Utility.duplicateImage( right );
		((AssociateController)controller).renderInterestPointsIn( left, leftcopy );
		((AssociateController)controller).renderInterestPointsIn( right, rightcopy );
		// combine images
		combined = Utility.combineImages( leftcopy, rightcopy );
		lblImage.setIcon( new ImageIcon( Utility.resizeImage( combined, 2f ) ) );
	}
	
	/**
	 * Draws associations between interest points in left and right image
	 */
	private void drawAssociations() {
		List<InterestPoint> leftinterest = images.get( left );
		List<InterestPoint> rightinterest = images.get( right );
		for ( PointAssociation pa : associations ) {
			InterestPoint l = leftinterest.get( pa.getSource() );
			InterestPoint r = rightinterest.get( pa.getDestination() );
			
			drawAssociation( l, r, false );
		}
		for ( PointAssociation pa : matches ) {
			InterestPoint l = leftinterest.get( pa.getSource() );
			InterestPoint r = rightinterest.get( pa.getDestination() );
			
			drawAssociation( l, r, true );
		}
		lblImage.setIcon( new ImageIcon( Utility.resizeImage( combined, 2f ) ) );
	}
	
	/**
	 * Draw one association
	 * @param l
	 * @param r
	 * @param match
	 */
	private void drawAssociation(InterestPoint l, InterestPoint r, boolean match) {
		Graphics2D g = combined.createGraphics();
		g.setColor( match ? Color.GREEN : Color.RED );
		g.setStroke( new BasicStroke( 3 ) );
		g.drawLine( (int) l.getX(), (int) l.getY(), (int)( left.getWidth()+Utility.GUTTER+r.getX() ), (int) r.getY() );
		g.dispose();
	}

	@Override
	public void reset() {
		
	}

	@Override
	public boolean canNext() {
		return true;
	}

}
