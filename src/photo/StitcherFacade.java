package photo;

/**
 * Singleton to encapsulate and hide various stitching functionality. Provides connection to library (OpenCV, BoofCV or whatever).
 * @author xnikp
 *
 */
public class StitcherFacade {
	private static StitcherFacade instance;
	
	private StitcherFacade() {
		
	}
	
	public static StitcherFacade getInstance() {
		if ( instance == null )
			instance = new StitcherFacade();
		return instance;
	}

}
