package gui.subviews.detect;

import gui.subviews.SubView;
import javax.swing.JLabel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import photo.InterestPoint;
import photo.StitcherFacade;
import util.Utility;

import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DetectPanel extends SubView {
	
	private List<BufferedImage> images;
	private Map<BufferedImage, Iterator<InterestPoint>> interests;
	private int currentImage = 0;
	private JLabel lbImage;
	private JButton btPreviousImage;
	private JButton btNextImage;
	private JButton btNextIP;
	private JButton btAllIP;
	
	public DetectPanel() {
		setLayout(new BorderLayout(0, 0));
		
		lbImage = new JLabel("");
		lbImage.setHorizontalAlignment( JLabel.CENTER );
		lbImage.setSize(600, 600);
		add(lbImage, BorderLayout.CENTER);
		
		btPreviousImage = new JButton("<<");
		add(btPreviousImage, BorderLayout.WEST);
		
		btNextImage = new JButton(">>");
		add(btNextImage, BorderLayout.EAST);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(1, 0, 0, 0));
		
		btNextIP = new JButton("Nächster Interest Point");
		panel.add(btNextIP);
		
		btAllIP = new JButton("Alle Interest Points");
		panel.add(btAllIP);
	}
	
	public void init() {
		controller = new DetectController( this, StitcherFacade.getInstance() );
		images = StitcherFacade.getInstance().getRegisteredImages();
		interests = new HashMap<BufferedImage, Iterator<InterestPoint>>();
		BufferedImage img = images.get( currentImage );
		interests.put( img, ((DetectController)controller).getInterestPoints( img ) ); 
		lbImage.setIcon( new ImageIcon( Utility.resizeImage( img ) ) );
		btPreviousImage.setEnabled( false );
		btNextImage.setEnabled( images.size() > 1 );
		
		// button stuff
		btPreviousImage.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				//TODO compute IPs if necessary
				setEnabled( currentImage - 1 < 0 );
			}
		});
		btNextImage.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				//TODO compute IPs if necessary
				setEnabled( currentImage + 1 >= images.size());
			}
		});
		btNextIP.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				BufferedImage img = images.get( currentImage );
				
				Iterator<InterestPoint> iterator = interests.get( img );
				if ( iterator.hasNext() ) {
					InterestPoint ip = iterator.next();
					drawInterestPoint( img, ip );
					lbImage.setIcon( new ImageIcon( Utility.resizeImage( img ) ) );
				}
			}
		});
		btAllIP.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				BufferedImage img = images.get( currentImage );
				
				Iterator<InterestPoint> iterator = interests.get( img );
				while( iterator.hasNext() ) {
					InterestPoint ip = iterator.next();
					drawInterestPoint( img, ip );
				}
				lbImage.setIcon( new ImageIcon( Utility.resizeImage( img ) ) );
				
			}
		});
	}

	private void drawInterestPoint( BufferedImage img, InterestPoint ip ) {
		Graphics2D render = img.createGraphics();
		render.setColor( Color.RED );
		render.setStroke( new BasicStroke( 3 ) );
		render.drawOval( (int) ip.getX(), (int) ip.getY(), (int) ip.getRadius(), (int) ip.getRadius() );
		render.dispose();
	}
}
