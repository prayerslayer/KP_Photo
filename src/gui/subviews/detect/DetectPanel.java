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

import photo.FastHessianConfig;
import photo.InterestPoint;
import photo.StitcherFacade;
import util.Utility;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JSlider;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class DetectPanel extends SubView {
	
	private List<BufferedImage> images;
	private FastHessianConfig config;
	private Map<BufferedImage, List<InterestPoint>> interests;
	private int currentImage = 0;
	private AddInterestPointLabel lbImage;
	private JButton btPreviousImage;
	private JButton btNextImage;
	private JButton btFindIP;
	private JButton btSettingsIP;
	
	public DetectPanel() {
		setLayout(new BorderLayout(0, 0));
		
		lbImage = new AddInterestPointLabel();
		lbImage.setHorizontalAlignment( JLabel.CENTER );
		lbImage.setSize(600, 600);
		add(lbImage, BorderLayout.CENTER);
		
		btPreviousImage = new JButton("<<");
		add(btPreviousImage, BorderLayout.WEST);
		
		btNextImage = new JButton(">>");
		add(btNextImage, BorderLayout.EAST);
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		
		btSettingsIP = new JButton("Parameter einstellen");
		panel.add(btSettingsIP);
		
		btFindIP = new JButton("Finde Interest Points");
		panel.add(btFindIP);
	}
	
	private void showImage() {
		BufferedImage img = images.get( currentImage );
		
		if ( interests.get( img ) != null ) {
			renderInterestPoints(img);
		} else {
			BufferedImage copy = Utility.duplicateImage( img );
			lbImage.setImage( copy );
			lbImage.setIcon( new ImageIcon( Utility.resizeImage( copy ) ) );
		}
	}
	
	public void init() {
		controller = new DetectController( this, StitcherFacade.getInstance() );
		images = StitcherFacade.getInstance().getRegisteredImages();
		interests = new HashMap<BufferedImage, List<InterestPoint>>();
		btPreviousImage.setEnabled( false );
		btNextImage.setEnabled( images.size() > 1 );
		showImage();
		// button stuff
		btPreviousImage.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				btNextImage.setEnabled( images.size() > 1 );	
				currentImage = currentImage - 1;
				btPreviousImage.setEnabled( currentImage - 1 >= 0 );
				showImage();
			}
		});
		btNextImage.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				btPreviousImage.setEnabled( images.size() > 1 );
				currentImage = currentImage + 1;
				btNextImage.setEnabled( currentImage + 1 < images.size());
				showImage();
			}
		});
		btSettingsIP.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				ParameterDialog dlg = new ParameterDialog( config );
				dlg.setVisible( true );
				if ( dlg.isOK() ) {
					config = dlg.getConfig();
				}
			}
		});
		btFindIP.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				BufferedImage img = images.get( currentImage );
				interests.put( img, ((DetectController)controller).getInterestPoints( img, config ) ); 
				renderInterestPoints( img );
				
			}
		});
	}
	
	private void renderInterestPoints( BufferedImage img ) {
		BufferedImage copy = Utility.duplicateImage( img );
		Iterator<InterestPoint> iterator = interests.get( img ).iterator();
		while( iterator.hasNext() ) {
			InterestPoint ip = iterator.next();
			drawInterestPoint( copy, ip );
		}
		lbImage.setImage( copy );
		lbImage.setIcon( new ImageIcon( Utility.resizeImage( copy ) ) );
	}

	private void drawInterestPoint( BufferedImage img, InterestPoint ip ) {
		Graphics2D render = img.createGraphics();
		render.setColor( Color.RED );
		render.setStroke( new BasicStroke( 3 ) );
		render.drawOval( (int) ip.getX(), (int) ip.getY(), (int) ip.getRadius(), (int) ip.getRadius() );
		render.dispose();
	}
}
