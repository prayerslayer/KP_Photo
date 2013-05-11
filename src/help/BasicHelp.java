package help;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import java.awt.BorderLayout;
import javax.swing.JButton;


import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;
import java.awt.Font;

/**
 * Base class for help dialogues.
 * @author xnikp
 *
 */
public class BasicHelp extends JFrame {
	protected JPanel pnContent;
	protected JLabel lbHeader;
	protected JTextArea taContent;
	
	protected void close() {
		this.setVisible( false );
	}
	
	/**
	 * Creates a help dialogue for given step
	 * @param helpstep
	 */
	public BasicHelp( ) {
		pnContent = new JPanel();
		getContentPane().add(pnContent, BorderLayout.CENTER);
		
		JButton btClose = new JButton("Close");
		btClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		getContentPane().add(btClose, BorderLayout.SOUTH);
		this.setSize(300, 200);
		loadContent();
	}
	
	/**
	 * Loads help content for step from xml file.
	 */
	protected void loadContent( ) {
		lbHeader = new JLabel( "Hilfe" );
		lbHeader.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lbHeader.setHorizontalAlignment(SwingConstants.CENTER);
		taContent = new JTextArea( "Die Hilfe wurde in ihrem Standardbrowser geöffnet." );
		taContent.setLineWrap( true );
		taContent.setWrapStyleWord( true );
		taContent.setEditable( false );
		taContent.setBackground( this.getBackground() );
		pnContent.setLayout(new BorderLayout(0, 0));
		pnContent.add( lbHeader, BorderLayout.NORTH );
		pnContent.add( taContent );
	}
	
}
