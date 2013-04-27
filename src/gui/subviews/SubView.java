package gui.subviews;

import javax.swing.JPanel;

/**
 * Class that encapsulates UI for each processing step.
 * @author xnikp
 *
 */
public abstract class SubView extends JPanel {

	protected SubController controller;
	
	/**
	 * Initialize UI controls
	 */
	public abstract void init();
	/**
	 * Reset UI in case user navigates backwards
	 */
	public abstract void reset();
	/**
	 * Indicates whether program is ready for next step or not
	 * @return
	 */
	public abstract boolean canNext();
}
