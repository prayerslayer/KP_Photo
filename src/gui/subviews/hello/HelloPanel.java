package gui.subviews.hello;

import gui.subviews.SubView;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.SwingConstants;

public class HelloPanel extends SubView {
	public HelloPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel lbHeadline = new JLabel("Panorama Stitching");
		lbHeadline.setFont(new Font("Helvetica", Font.BOLD, 13));
		add(lbHeadline, BorderLayout.NORTH);
		
		JLabel lbInfo = new JLabel("Hier kommt einmal eine einf\u00FChrende Erkl\u00E4rung hin, yo.");
		lbInfo.setVerticalAlignment(SwingConstants.TOP);
		add(lbInfo, BorderLayout.CENTER);
	}

	@Override
	public void init() {
		
		
	}
	@Override
	public void reset() {
		
	}
}
