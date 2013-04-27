package gui.subviews.orient;

import gui.subviews.SubView;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import photo.Homography;
import photo.OrientationFailedException;
import photo.StitcherFacade;
import util.Utility;

import boofcv.gui.image.HomographyStitchPanel;

import java.awt.BorderLayout;
import java.util.List;
import java.awt.image.BufferedImage;

/**
 * UI for image orientation step
 * @author xnikp
 *
 */
public class OrientPanel extends SubView {
	
	private JLabel lbImage;
	/**
	 * Relative orientations
	 */
	private List<Homography> homos;
	
	/**
	 * Creates new OrientPanel
	 */
	public OrientPanel() {
		setLayout(new BorderLayout(0, 0));
		
		//JScrollPane scroller = new JScrollPane();
		//add( scroller, BorderLayout.CENTER );
		
		lbImage = new JLabel();
		//scroller.add( lbImage );
		add( lbImage, BorderLayout.CENTER );
	}

	@Override
	public void init() {
		controller = new OrientController( this, StitcherFacade.getInstance());
		
		BufferedImage img;
		try {
			img = ((OrientController)controller).stitchTogether( getWidth(), getHeight(), true );
			lbImage.setIcon( new ImageIcon( img ) );
		} catch (OrientationFailedException oex ) {
			JOptionPane.showMessageDialog(this, "Orientierung fehlgeschlagen!", "Fehler", JOptionPane.ERROR_MESSAGE );
			oex.printStackTrace();
		}
	}

	@Override
	public void reset() {
		
	}

	@Override
	public boolean canNext() {
		return true;
	}
}
