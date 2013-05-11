package help;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

/**
 * Factory for help.
 * @author xnikp
 *
 */
public class HelpFactory {
	private static HelpFactory instance;
	private LinkedList<String> helps;
	private BasicHelp helpFrame;
	
	private HelpFactory() {
		helpFrame = new BasicHelp();
		helps = new LinkedList<String>();
		helps.add( 0, "hello" );
		helps.add( 1, "load" );
		helps.add( 2, "detect" );
		helps.add( 3, "match" );
		helps.add( 4, "orient" );
		helps.add( 5, "blend" );
	}
	
	public static HelpFactory getInstance() {
		if ( instance == null )
			instance = new HelpFactory();
		return instance;
	}
	
	/**
	 * Opens the according help site in default browser.
	 * @param step Step for which help is needed
	 */
	public void openHelp( int step ) {
		if ( step < 0 || step >= helps.size() )
			throw new IllegalArgumentException( step + "");
		String filename = helps.get( step );
		try {
			// define file
			File tempHtml = new File( System.getProperty( "java.io.tmpdir" ) + filename + ".html" );
			File tempPng = new File( System.getProperty( "java.io.tmpdir" ) + filename + ".png" );
			tempHtml.deleteOnExit();
			tempPng.deleteOnExit();
			// if doesn't exist we need to copy it out from the jar file in temp directory
			if ( !tempHtml.exists() ) {
				OutputStream out = new FileOutputStream( tempHtml );
				InputStream in = getClass().getResourceAsStream( "/help/contents/" + filename + ".html" );
				try {
					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) {
					    out.write(buffer, 0, len);
					} 
				} finally {
					out.flush();
					out.close();
					in.close();
				}
			}
			if ( !tempPng.exists() ) {
				OutputStream out = new FileOutputStream( tempPng );
				InputStream in = getClass().getResourceAsStream( "/help/contents/" + filename + ".png" );
				try {
					byte[] buffer = new byte[1024];
					int len;
					while ((len = in.read(buffer)) != -1) {
					    out.write(buffer, 0, len);
					} 
				} finally {
					out.flush();
					out.close();
					in.close();
				}
			}
			// open it in default application which should be the browser 
			Desktop desktop = Desktop.getDesktop();
			desktop.open( tempHtml );
			// in case operating system didn't switch to browser, show user that she should do it
			helpFrame.setVisible( true );
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
}
