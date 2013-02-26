package gui.subviews.associate;

import gui.subviews.SubView;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JSplitPane;

import photo.InterestPoint;
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

public class AssociatePanel extends SubView {
	private BufferedImage left;
	private BufferedImage right;
	private BufferedImage combined;
	private Map<BufferedImage, List<InterestPoint>> images;
	private List<PointAssociation> associations;
	private JLabel lblImage;
	
	public AssociatePanel() {
		setLayout(new BorderLayout(0, 0));
		
		lblImage = new JLabel();
		lblImage.setHorizontalAlignment( JLabel.CENTER );
		lblImage.setVerticalAlignment( JLabel.CENTER );
		add(lblImage, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JButton btnNchsteAssoziation = new JButton("Nächste Assoziation");
		btnNchsteAssoziation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		panel.add(btnNchsteAssoziation);
		
		JButton btnAlleAssoziationen = new JButton("Alle Assoziationen");
		btnAlleAssoziationen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drawAssociations();
			}
		});
		panel.add(btnAlleAssoziationen);
	}

	@Override
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
		// draw them
		BufferedImage leftcopy = Utility.duplicateImage( left );
		BufferedImage rightcopy = Utility.duplicateImage( right );
		((AssociateController)controller).renderInterestPointsIn( left, leftcopy );
		((AssociateController)controller).renderInterestPointsIn( right, rightcopy );
		// combine images
		combined = Utility.combineImages( leftcopy, rightcopy );
		lblImage.setIcon( new ImageIcon( Utility.resizeImage( combined, 2f ) ) );
	}
	
	private void drawAssociations() {
		List<InterestPoint> leftinterest = images.get( left );
		List<InterestPoint> rightinterest = images.get( right );
		for ( PointAssociation pa : associations ) {
			InterestPoint l = leftinterest.get( pa.getSource() );
			InterestPoint r = rightinterest.get( pa.getDestination() );
			double score = pa.getScore();
			
			drawAssociation( l, r, score );
		}
		lblImage.setIcon( new ImageIcon( Utility.resizeImage( combined, 2f ) ) );
	}
	
	private void drawAssociation(InterestPoint l, InterestPoint r, double score) {
		Graphics2D g = combined.createGraphics();
		g.setColor( Color.RED );
		g.setStroke( new BasicStroke( (int) score*3 ) );
		g.drawLine( (int) l.getX(), (int) l.getY(), (int)( left.getWidth()+Utility.GUTTER+r.getX() ), (int) r.getY() );
		g.dispose();
	}

	@Override
	public void reset() {
		
	}

}
