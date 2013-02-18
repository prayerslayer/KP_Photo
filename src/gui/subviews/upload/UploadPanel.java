package gui.subviews.upload;

import gui.subviews.SubController;
import gui.subviews.SubView;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JScrollPane;

import photo.StitcherFacade;

public class UploadPanel extends SubView {

	/**
	 * Create the panel.
	 * @throws IOException 
	 */
	public UploadPanel() {
		this.controller = new SubController( this, StitcherFacade.getInstance() );
		
		setLayout(new BorderLayout(0, 0));
		
		JPanel pnButtons = new JPanel();
		add(pnButtons, BorderLayout.EAST);
		pnButtons.setLayout( new GridLayout( 3, 1, 0, 0 ) );
		
		JButton btAdd = new JButton("Hinzuf\u00FCgen");
		pnButtons.add(btAdd);
		
		JButton btRemove = new JButton("Entfernen");
		pnButtons.add(btRemove);
		
		JScrollPane spImages = new JScrollPane( );
		add(spImages, BorderLayout.CENTER);
		
		JPanel pnImages = new JPanel();
		pnImages.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		pnImages.setSize( 350, 250 );
		spImages.add( pnImages );
		
	}

}
