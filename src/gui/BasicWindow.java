package gui;

import gui.subviews.SubView;
import gui.subviews.associate.AssociatePanel;
import gui.subviews.blend.BlendPanel;
import gui.subviews.detect.DetectPanel;
import gui.subviews.hello.HelloPanel;
import gui.subviews.orient.OrientPanel;
import gui.subviews.upload.UploadPanel;
import help.HelpFactory;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.DefaultCaret;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.DropMode;
import javax.swing.SwingUtilities;

import java.util.LinkedList;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.Action;
import javax.swing.JLayeredPane;

/**
 * The window which contains ALL the user interface and application flow logic.
 * @author xnikp
 *
 */
public class BasicWindow extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel content;
	private CardLayout layout;
	// dont delete them
	private JTextArea taInfo;
	private JButton btPrevious;
	private JButton btNext;
	private JPanel pnBreadcrumbs;
	
	// application flow logic
	private List<SubView> subviews = new LinkedList<SubView>();
	private int activeStep = 0;
	

	/**
	 * Creates subviews and adds them to panel (order is important), shows first.
	 */
	public void init() {
		subviews.add( new HelloPanel() );
		subviews.add( new UploadPanel() );
		subviews.add( new DetectPanel() );
		subviews.add( new AssociatePanel() );
		subviews.add( new OrientPanel() );
		subviews.add( new BlendPanel() );
		
		for ( SubView view : subviews ) {
			content.add( view, BorderLayout.CENTER );
		}
		this.redirectSystemStreams();
		
		btPrevious.setEnabled( false );
	}
	
	private void updateTextArea(final String text) {
	  SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
	      taInfo.append(text);
	    }
	  });
	}
		 
	private void redirectSystemStreams() {
	  OutputStream out = new OutputStream() {
	    @Override
	    public void write(int b) throws IOException {
	      updateTextArea(String.valueOf((char) b));
	    }
	 
	    @Override
	    public void write(byte[] b, int off, int len) throws IOException {
	      updateTextArea(new String(b, off, len));
	    }
	 
	    @Override
	    public void write(byte[] b) throws IOException {
	      write(b, 0, b.length);
	    }
	  };
	 
	  System.setOut(new PrintStream(out, true));
	  System.setErr(new PrintStream(out, true));
	}
	
	/**
	 * Shows the next subview, if possible.
	 */
	public void next() {
		if ( this.activeStep + 1 < subviews.size() && subviews.get( this.activeStep ).canNext()) {
			pnBreadcrumbs.getComponent( this.activeStep ).setFont( new Font( "sansserif", Font.PLAIN, 14 ) );
			this.activeStep += 1;
			layout.next( content );
			subviews.get( this.activeStep ).init();
			btNext.setEnabled( this.activeStep + 1 < subviews.size() );
			btPrevious.setEnabled( true );
			pnBreadcrumbs.getComponent( this.activeStep ).setFont( new Font( "sansserif", Font.BOLD, 16 ) );
		}
	}
	
	/**
	 * Shows the help dialogue.
	 */
	public void showHelp() {
		HelpFactory.getInstance().getHelp( this.activeStep ).setVisible( true );
	}
	
	/**
	 * Shows the previous subview, if available.	
	 */
	public void previous() {
		if ( this.activeStep - 1 >= 0 ) {
			pnBreadcrumbs.getComponent( this.activeStep ).setFont( new Font( "sansserif", Font.PLAIN, 14 ) );
			this.activeStep -= 1;
			btNext.setEnabled( true );
			btPrevious.setEnabled( this.activeStep - 1 >=0 );
			pnBreadcrumbs.getComponent( this.activeStep ).setFont( new Font( "sansserif", Font.BOLD, 16 ) );
			layout.previous( content );
		}
		System.out.println( this.activeStep);
	}

	/**
	 * Create the frame.
	 */
	public BasicWindow() {
		setFont(new Font("Helvetica", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 800, 600);
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(0, 0));
		setContentPane(panel);
		
		pnBreadcrumbs = new JPanel();
		panel.add(pnBreadcrumbs, BorderLayout.NORTH);
		pnBreadcrumbs.setLayout(new GridLayout(1, 0, 0, 0));
		
		JLabel lbStep0 = new JLabel("(0) Info");
		lbStep0.setFont(new Font("sansserif", Font.BOLD, 16));
		lbStep0.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep0);
		
		JLabel lbStep1 = new JLabel("(1) Load");
		lbStep1.setFont(new Font("sansserif", Font.PLAIN, 14));
		lbStep1.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep1);
		
		JLabel lbStep2 = new JLabel("(2) Detect IP");
		lbStep2.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lbStep2.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep2);
		
		JLabel lbStep3 = new JLabel("(3) Match IP");
		lbStep3.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lbStep3.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep3);
		
		JLabel lbStep4 = new JLabel("(4) Orient");
		lbStep4.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lbStep4.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep4);
		
		JLabel lbStep5 = new JLabel("(5) Blend");
		lbStep5.setFont(new Font("SansSerif", Font.PLAIN, 14));
		lbStep5.setHorizontalAlignment(SwingConstants.CENTER);
		pnBreadcrumbs.add(lbStep5);
		
		JButton btHelp = new JButton("Help");
		btHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showHelp();
			}
		});
		pnBreadcrumbs.add(btHelp);
		
		JPanel pnLogNav = new JPanel();
		panel.add(pnLogNav, BorderLayout.SOUTH);
		pnLogNav.setLayout(new BorderLayout(0, 0));
		
		btNext = new JButton("N\u00E4chster Schritt");
		btNext.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				next();
			}
		});
		pnLogNav.add(btNext, BorderLayout.EAST);
		
		btPrevious = new JButton("Zur\u00FCck");
		btPrevious.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				previous();
			}
		});
		pnLogNav.add(btPrevious, BorderLayout.WEST);
		
		taInfo = new JTextArea();
		DefaultCaret caret = (DefaultCaret)taInfo.getCaret();
		caret.setUpdatePolicy( DefaultCaret.ALWAYS_UPDATE );
		taInfo.setDropMode(DropMode.INSERT);
		taInfo.setRows(5);
		taInfo.setFont(new Font("monospaced", Font.PLAIN, 11));
		
		JScrollPane scrollPane = new JScrollPane( taInfo );
		scrollPane.setAutoscrolls( true );
		pnLogNav.add(scrollPane, BorderLayout.CENTER);
		
		content = new JPanel();
		panel.add(content, BorderLayout.CENTER);
		layout = new CardLayout();
		content.setLayout( layout );
		
		init();
	}


}
