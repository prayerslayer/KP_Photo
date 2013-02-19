package gui;

import gui.subviews.upload.UploadPanel;

import javax.swing.JFrame;

/**
 * Class to start program, yo.
 * 
 * @author xnikp
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//BasicWindow window = new BasicWindow();
		
		JFrame window = new JFrame();
		window.add( new UploadPanel() );
		window.setSize( 800, 600 );
		window.setVisible( true );
	}

}
