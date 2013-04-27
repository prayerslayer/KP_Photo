package help;

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
	
	private List<BasicHelp> helps;
	
	private HelpFactory() {
		helps = new LinkedList<BasicHelp>();
		helps.add( 0, new HelloHelp( 0 ) );
		helps.add( 1, new LoadHelp( 1 ) );
		helps.add( 2, new DetectHelp( 2 ) );
		helps.add( 3, new MatchHelp( 3 ) );
		helps.add( 4, new OrientHelp( 4 ) );
		helps.add( 5, new BlendHelp( 5 ) );
	}
	
	public static HelpFactory getInstance() {
		if ( instance == null )
			instance = new HelpFactory();
		return instance;
	}
	
	public JFrame getHelp( int step ) {
		if ( step < 0 || step >= helps.size() )
			throw new IllegalArgumentException( step + "");
		
		return helps.get( step );
	}
}
