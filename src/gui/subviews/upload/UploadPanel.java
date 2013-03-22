package gui.subviews.upload;

import gui.subviews.SubView;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import photo.StitcherFacade;
import util.Utility;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UploadPanel extends SubView implements Observer {

	private JScrollPane spImages;
	private JPanel pnImages;
	private JButton btnStandardbilderLaden;
	/**
	 * Create the panel.
	 */
	public UploadPanel() {
		controller = new UploadController( this, StitcherFacade.getInstance() );
		
		setLayout(new BorderLayout(0, 0));
		
		final Container parent = this.getParent();
		JPanel pnButtons = new JPanel();
		add(pnButtons, BorderLayout.EAST);
		pnButtons.setLayout( new GridLayout( 3, 1, 0, 0 ) );
		
		JButton btAdd = new JButton("Eigene Bilder laden");

		btAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				// We don't want "all files". Who knows what could happen!
				chooser.setMultiSelectionEnabled( true );
				// Implement image filter.
				chooser.setFileFilter( new FileFilter() {
					@Override
					public boolean accept(File f) {
						// Display it if it's a directory
						if ( f.isDirectory() )
							return true;
						if ( f.isFile() ) {
							String name = f.getName();
							// On Unix/Mac many files don't have a dot
							if ( name.lastIndexOf( "." ) > -1 ) {
								// Check extension
								String ext = name.substring( name.lastIndexOf( '.' ) + 1, name.length() );
								return ext.equalsIgnoreCase( "jpg") ||
									 ext.equalsIgnoreCase( "jpeg" ) ||
									 ext.equalsIgnoreCase( "png");
							}								
						}
						return false;
					}

					@Override
					public String getDescription() {
						return "*.jpeg, *.jpg, *.png";
					}
					
				});
				int action = chooser.showDialog( parent, "Bilder laden" );
				if ( action == JFileChooser.APPROVE_OPTION ) {
					File[] files = chooser.getSelectedFiles();
					for ( File file : files ) {
						loadImage( file );
					}
					spImages.revalidate();
				}
			}
		});
		
		pnButtons.add(btAdd);
		
		btnStandardbilderLaden = new JButton("Standardbilder laden");
		btnStandardbilderLaden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				File pano1 = new File( getClass().getResource( "/gui/images/panorama1.jpg" ).getFile() );
				File pano2 = new File( getClass().getResource( "/gui/images/panorama2.jpg" ).getFile() );
				File pano3 = new File( getClass().getResource( "/gui/images/panorama3.jpg" ).getFile() );
				loadImage( pano1 );
				loadImage( pano2 );
				loadImage( pano3 );
			}
		});
		pnButtons.add(btnStandardbilderLaden);
		
		pnImages = new JPanel();
		pnImages.setLayout( new FlowLayout( FlowLayout.LEFT ) );
		//add( pnImages, BorderLayout.CENTER );
		spImages = new JScrollPane( pnImages );
		add(spImages, BorderLayout.CENTER);
	}
	
	private void loadImage( File file ) {
		//read image
		try {
			

			BufferedImage img = ImageIO.read( file );
			//resize if necessary
			if ( img.getWidth() > 1000 || img.getHeight() > 1000 )
				img = Utility.scaleImage(img, 1000 );
			//register image at controller for further calculations
			((UploadController) controller).registerImage( img );
			
			ImagePanel imgPanel = new ImagePanel( file.getName(), img );
			imgPanel.addPropertyChangeListener("delete", new PropertyChangeListener()  {
				
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					// delete it
					BufferedImage img = ( BufferedImage ) evt.getOldValue();
					((UploadController) controller).unregisterImage( img );
					for ( Component panel : pnImages.getComponents() ) {
						if ( ((ImagePanel) panel).getBufferedImage().equals( img ) ) {
							pnImages.remove( panel );
							// a little hack because revalidate() did not update the container when the last image was deleted. nor did validate() and invalidate()
							spImages.setSize( spImages.getWidth() +1, spImages.getHeight() );
						}
					}
				}
			});
			pnImages.add( imgPanel );
			pnImages.setSize( spImages.getWidth() - 1, spImages.getHeight() );
			System.out.println( "Added image " + file.getName() + " ( " + img.getWidth() + " x " + img.getHeight() + " px )" );
		}  catch ( IOException ioex ) {
			System.err.println( "Could not load " + file.getName() );
			ioex.printStackTrace();
		} catch ( Exception ex ) {
			System.err.println( ex.toString() );
			ex.printStackTrace();
		}
	}
	
	@Override
	public void update(Observable o, Object arg) {
		System.out.println( "Delete image " + ( (int) arg ));
	}
	
	public void init() {
		//everything happens in constructors
	}
	
	public void reset() {
		
	}
	@Override
	public boolean canNext() {
		if ( pnImages.getComponentCount() < 2 )
			System.out.println( "Bitte mindestens 2 Bilder hinzufügen!" );
		return pnImages.getComponentCount() >= 2;
	}

}
