package help;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BasicHelp extends JFrame {
	protected JPanel pnContent;
	
	protected void close() {
		this.setVisible( false );
	}

	public BasicHelp() {
		
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
	}

	
	
}
