package gui.subviews.blend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import gui.subviews.SubView;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import photo.OrientationFailedException;
import photo.StitcherFacade;

public class BlendPanel extends SubView {
	
	private JLabel lbImage;
	private JButton btSave;
	private BufferedImage pano;
	
	public BlendPanel() {
		setLayout(new BorderLayout(0, 0));
		
		lbImage = new JLabel();
		lbImage.setHorizontalAlignment( JLabel.CENTER );
		lbImage.setVerticalAlignment( JLabel.CENTER );
		add(lbImage, BorderLayout.CENTER );
		
		final JPanel parent = this;
		btSave = new JButton();
		btSave.setIcon( new ImageIcon( getClass().getResource( "/gui/images/save.png" ) ) );
		btSave.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent evt ) {
				if ( pano == null ) {
					//TODO show message pane
					return;
				}
				
				JFileChooser saveDlg = new JFileChooser();
				saveDlg.setMultiSelectionEnabled( false );
				int action = saveDlg.showSaveDialog( parent );
				if ( action == JFileChooser.APPROVE_OPTION ) {
					File file = saveDlg.getSelectedFile();
					if ( !file.getName().endsWith( ".png" ) ) {
						file = new File( file.getAbsolutePath() + ".png" );
					}
					try {
						ImageIO.write( pano, "png", file );
					} catch (IOException e) {
						e.printStackTrace();
						//TODO show message pane
					}
				}
			}
		});
		add( btSave, BorderLayout.SOUTH );
	}

	@Override
	public void init() {
		controller = new BlendController( this, StitcherFacade.getInstance() );
		try {
			pano = ((BlendController)controller).makePanorama( getWidth(), getHeight() );
			lbImage.setIcon( new ImageIcon( pano ) );
		} catch (OrientationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void reset() {
		
	}

	@Override
	public boolean canNext() {
		return false;
	}

}
