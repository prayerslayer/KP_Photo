package gui.subviews;

import javax.swing.JPanel;

/**
 * Class that encapsulates UI for each step.
 * @author xnikp
 *
 */
public abstract class SubView extends JPanel {

	protected SubController controller;
	
	public abstract void init();
}
