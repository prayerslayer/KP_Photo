package gui;

import gui.subviews.upload.UploadPanel;

import javax.swing.JFrame;

/**
 * Class to start program, yo.
 * 
 * @author xnikp
 *
 */
public class Panorama {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BasicWindow window = new BasicWindow();
		window.setTitle( "Panorama Stitching" );
		/*JFrame window = new JFrame();
		window.add( new DetectPanel() );
		window.setSize( 800, 600 );*/
		window.setSize( 800, 600 );
		window.setVisible( true );
	}

}
