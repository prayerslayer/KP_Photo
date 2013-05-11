package help;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

/**
 * Factory for help dialogues.
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
	
	public void openHelp( int step ) {
		if ( step < 0 || step >= helps.size() )
			throw new IllegalArgumentException( step + "");
		
		try {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse( getClass().getResource("/help/contents/" + helps.get( step ) + ".html").toURI() );
		} catch (IOException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		helpFrame.setVisible( true );
		
	}
}
