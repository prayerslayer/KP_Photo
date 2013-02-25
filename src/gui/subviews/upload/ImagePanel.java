package gui.subviews.upload;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.Utility;

/**
 * Panel to hold an image, a delete button and a label.
 * @author xnikp
 *
 */
public class ImagePanel extends JPanel {

	private JButton btDelete;
	private JLabel lbDescription;
	private JLabel lbImage;
	private BufferedImage img;
	
	/**
	 * Creates a new ImagePanel
	 * @param description the description of the image
	 * @param img the image
	 */
	public ImagePanel( String description, final BufferedImage img ) {
		super();
		this.img = img;
		btDelete = new JButton();
		lbDescription = new JLabel( description );
		lbImage = new JLabel();
		
		setLayout( new BorderLayout());
		add( btDelete, BorderLayout.NORTH );
		add( lbDescription, BorderLayout.SOUTH );
		add( lbImage, BorderLayout.CENTER );
		
		// resize
		Image small = Utility.resizeImage( img );
		
		btDelete.setSize( 32, 32 );
		btDelete.setIcon( new ImageIcon( getClass().getResource( "/gui/images/trash.png" ) ) );
		lbDescription.setHorizontalTextPosition( JLabel.CENTER );
		lbImage.setIcon( new ImageIcon( small ) );
		
		btDelete.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				firePropertyChange( "delete", img, null );
			}
		});
	}
	
	public BufferedImage getBufferedImage() {
		return img;
	}
	
	
}
