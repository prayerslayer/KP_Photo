package gui.subviews.orient;

import gui.subviews.SubView;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import photo.OrientationFailedException;
import photo.StitcherFacade;

import boofcv.gui.image.HomographyStitchPanel;

import java.awt.BorderLayout;
import java.util.List;
import java.awt.image.BufferedImage;

public class OrientPanel extends SubView {
	
	private HomographyStitchPanel panel;
	
	public OrientPanel() {
		setLayout(new BorderLayout(0, 0));
		
		panel = new HomographyStitchPanel( .5f, 800, 800 );
		add( panel );
	}

	@Override
	public void init() {
		controller = new OrientController( this, StitcherFacade.getInstance());
		List<BufferedImage> images = ((OrientController)controller).getRegisteredImages();
		BufferedImage img1 = images.get( 0 );
		BufferedImage img2 = images.get( 1 );
		try {
			panel.configure( img1, img2, ((OrientController)controller).orientImages( img1, img2 ) );
		} catch (OrientationFailedException oex ) {
			JOptionPane.showMessageDialog(this, "Orientierung fehlgeschlagen!", "Fehler", JOptionPane.ERROR_MESSAGE );
			oex.printStackTrace();
		}
	}

	@Override
	public void reset() {
		
	}
}
