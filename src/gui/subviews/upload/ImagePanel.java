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
	int nr = -1;
	
	//TODO kann nicht mit index die löschung indizieren, da sich der nach einer löschung ja ändert. muss feste nummern vergeben
	
	public ImagePanel( int number, String description, Image small ) {
		super();
		nr = number;
		btDelete = new JButton();
		lbDescription = new JLabel( description );
		lbImage = new JLabel();
		
		setLayout( new BorderLayout());
		add( btDelete, BorderLayout.NORTH );
		add( lbDescription, BorderLayout.SOUTH );
		add( lbImage, BorderLayout.CENTER );
		
		btDelete.setSize( 32, 32 );
		btDelete.setIcon( new ImageIcon( getClass().getResource( "/gui/images/trash.png" ) ) );
		lbDescription.setHorizontalTextPosition( JLabel.CENTER );
		lbImage.setIcon( new ImageIcon( small ) );
		
		btDelete.addMouseListener( new MouseAdapter() {
			public void mouseClicked( MouseEvent evt ) {
				firePropertyChange("number", nr, -1 );
			}
		});
	}
}
