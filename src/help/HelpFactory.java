package help;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

public class HelpFactory {
	private static HelpFactory instance;
	
	private List<BasicHelp> helps;
	
	private HelpFactory() {
		helps = new LinkedList<BasicHelp>();
		helps.add( 0, new HelloHelp() );
		helps.add( 1, new LoadHelp() );
		helps.add( 2, new DetectHelp() );
		helps.add( 3, new MatchHelp() );
		helps.add( 4, new OrientHelp() );
		helps.add( 5, new BlendHelp() );
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
