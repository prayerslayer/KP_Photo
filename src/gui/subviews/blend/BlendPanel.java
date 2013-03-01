package gui.subviews.blend;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;

import gui.subviews.SubView;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import photo.OrientationFailedException;
import photo.StitcherFacade;

public class BlendPanel extends SubView {
	
	private JLabel lbImage;
	private JButton btSave;
	
	public BlendPanel() {
		
		lbImage = new JLabel();
		lbImage.setHorizontalAlignment( JLabel.CENTER );
		add(lbImage, BorderLayout.CENTER );
		
		btSave = new JButton();
		add( btSave, BorderLayout.SOUTH );
	}

	@Override
	public void init() {
		controller = new BlendController( this, StitcherFacade.getInstance() );
		try {
			BufferedImage pano = ((BlendController)controller).makePanorama( getWidth(), getHeight() );
			lbImage.setIcon( new ImageIcon( pano ) );
		} catch (OrientationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset() {
		
	}

}
