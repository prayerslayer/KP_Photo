package help;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;

import util.XMLHelpLoader;

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
	protected JLabel lbHelp;
	protected int step;
	
	protected void close() {
		this.setVisible( false );
	}
	
	/**
	 * Creates a help dialogue for given step
	 * @param helpstep
	 */
	public BasicHelp( int helpstep ) {
		step = helpstep;
		pnContent = new JPanel();
		getContentPane().add(pnContent, BorderLayout.CENTER);
		
		JButton btClose = new JButton("Close");
		btClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		getContentPane().add(btClose, BorderLayout.SOUTH);
		this.setSize(600, 400);
		loadContent();
	}
	
	/**
	 * Loads help content for step from xml file.
	 */
	protected void loadContent( ) {
		HelpContent help = XMLHelpLoader.getInstance().load( this.step );
		lbHeader = new JLabel( help.getTitle() );
		lbHeader.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		lbHeader.setHorizontalAlignment(SwingConstants.CENTER);
		lbHelp = new JLabel( help.getContent() );
		lbHelp.setVerticalAlignment(SwingConstants.TOP);
		pnContent.setLayout(new BorderLayout(0, 0));
		pnContent.add( lbHeader, BorderLayout.NORTH );
		pnContent.add( lbHelp );
	}
	
}
