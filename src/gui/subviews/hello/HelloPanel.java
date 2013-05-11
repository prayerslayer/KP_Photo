package gui.subviews.hello;

import gui.subviews.SubView;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import java.awt.Font;
import javax.swing.SwingConstants;

/**
 * UI for initial panel
 * @author xnikp
 *
 */
public class HelloPanel extends SubView {
	/**
	 * Creates new HelloPanel
	 */
	public HelloPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JLabel lbHeadline = new JLabel("Panoramaerstellung");
		lbHeadline.setFont(new Font("Helvetica", Font.BOLD, 13));
		add(lbHeadline, BorderLayout.NORTH);
		
		JTextArea taInfo = new JTextArea("Willkommen zur Lernsoftware Panoramaerstellung. Zur Navigation benutzen Sie bitte die beiden Vor/Zurück Buttons unten links bzw. rechts. Der Vorgang der Panoramaerstellung besteht aus 5 Schritten, die hier nach der Reihe veranschaulicht werden. Genauere Informationen zu jedem Schritt bekommen Sie über den Button 'Hilfe' oben rechts.");
		taInfo.setBackground( this.getBackground() );
		taInfo.setEditable( false );
		taInfo.setLineWrap( true );
		taInfo.setWrapStyleWord( true );
		add(taInfo, BorderLayout.CENTER);
	}

	@Override
	public void init() {
		
		
	}
	@Override
	public void reset() {
		
	}

	@Override
	public boolean canNext() {
		return true;
	}
}
