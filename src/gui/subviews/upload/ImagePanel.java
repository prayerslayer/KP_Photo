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

public class ImagePanel extends JPanel {

	private JButton btDelete;
	private JLabel lbDescription;
	private JLabel lbImage;
	private BufferedImage img;
	
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
		Image small;
		float ratio = img.getWidth() / img.getHeight();
		if ( ratio > 1 ) {
			// landscape
			small = img.getScaledInstance( 300, Math.round( 300/ratio) , 0 );
		} else if ( ratio < 1 ){
			// portrait
			small = img.getScaledInstance( Math.round( 300/ratio ), 300, 0 );
		} else  {
			// square
			small = img.getScaledInstance( 300, 300, 0 );
		} 
		
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
