package gui.subviews.detect;

import javax.swing.JDialog;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JButton;

import photo.FastHessianConfig;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Dialogue for interest point parameter.
 * @author xnikp
 *
 */
public class ParameterDialog extends JDialog {

	private boolean ok = false;
	private JSlider sldThreshold;
	private JSlider sldRadius;
	private JSlider sldMaxFeatures;
	private JSlider sldSampleSize;
	private JSlider sldInitialSize;
	private JSlider sldNumberScales;
	private JSlider sldNumberOctaves;
	private FastHessianConfig conf;
	
	/**
	 * Creates a new dialogue
	 * @param deflt default configuration, may be null
	 */
	public ParameterDialog(FastHessianConfig deflt ) {
		setModal(true);
		setTitle("Interest Point Parameter");
		setSize( 300, 600 );
		setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
		getContentPane().setLayout(new GridLayout(8, 2, 0, 0));
		
		if ( deflt == null)
			deflt = new FastHessianConfig();
		
		JLabel lblThreshold = new JLabel("Threshold");
		getContentPane().add(lblThreshold);
		
		sldThreshold = new JSlider();
		sldThreshold.setMinimum( 1 );
		sldThreshold.setValue( (int)deflt.getDetectThreshold() );
		sldThreshold.setMaximum( 51 );
		sldThreshold.setMajorTickSpacing( 10 );
		sldThreshold.setMinorTickSpacing( 5 );
		sldThreshold.setPaintTicks(true);
		sldThreshold.setPaintLabels( true );
		sldThreshold.setSnapToTicks( true );
		getContentPane().add(sldThreshold);
		
		JLabel lblRadius = new JLabel("Radius");
		getContentPane().add(lblRadius);
		
		sldRadius = new JSlider();
		sldRadius.setPaintLabels(true);
		sldRadius.setPaintTicks(true);
		sldRadius.setMajorTickSpacing( 1 );
		sldRadius.setMinimum( 1 );
		sldRadius.setMaximum( 5 );
		sldRadius.setValue( deflt.getExtractRadius() );
		sldRadius.setSnapToTicks( true );
		getContentPane().add(sldRadius);
		
		JLabel lblMaxfeatures = new JLabel("MaxFeatures");
		getContentPane().add(lblMaxfeatures);
		
		sldMaxFeatures = new JSlider();
		sldMaxFeatures.setPaintLabels(true);
		sldMaxFeatures.setPaintTicks(true);
		sldMaxFeatures.setMaximum( 499 );
		sldMaxFeatures.setMinimum( -1 );
		sldMaxFeatures.setValue( deflt.getMaxFeaturesPerScale() );
		sldMaxFeatures.setMajorTickSpacing( 100 );
		sldMaxFeatures.setMinorTickSpacing( 50 );
		sldMaxFeatures.setSnapToTicks( true );
		getContentPane().add(sldMaxFeatures);
		
		JLabel lblSampleSize = new JLabel("Sample Size");
		getContentPane().add(lblSampleSize);
		
		sldSampleSize = new JSlider();
		sldSampleSize.setPaintLabels(true);
		sldSampleSize.setPaintTicks(true);
		sldSampleSize.setMinimum( 1 );
		sldSampleSize.setMaximum( 5 );
		sldSampleSize.setValue( deflt.getInitialSampleSize() );
		sldSampleSize.setMajorTickSpacing( 1 );
		sldSampleSize.setSnapToTicks( true );
		getContentPane().add(sldSampleSize);
		
		JLabel lblInitialSize = new JLabel("Initial Size");
		getContentPane().add(lblInitialSize);
		
		sldInitialSize = new JSlider();
		sldInitialSize.setPaintLabels(true);
		sldInitialSize.setPaintTicks(true);
		sldInitialSize.setMinimum( 1 );
		sldInitialSize.setMaximum( 18 );
		sldInitialSize.setValue( deflt.getInitialSize() );
		sldInitialSize.setMajorTickSpacing( 5 );
		sldInitialSize.setMinorTickSpacing( 1 );
		sldInitialSize.setSnapToTicks( true );
		getContentPane().add(sldInitialSize);
		
		JLabel lblNumberOfScales = new JLabel("Number of scales");
		getContentPane().add(lblNumberOfScales);
		
		sldNumberScales = new JSlider();
		sldNumberScales.setPaintLabels(true);
		sldNumberScales.setPaintTicks(true);
		sldNumberScales.setMinimum( 1 );
		sldNumberScales.setMaximum( 15 );
		sldNumberScales.setValue( deflt.getNumberScalesPerOctave() );
		sldNumberScales.setMajorTickSpacing( 5 );
		sldNumberScales.setMinorTickSpacing( 1 );
		sldNumberScales.setSnapToTicks( true );
		getContentPane().add(sldNumberScales);
		
		JLabel lblNumberOfOctaves = new JLabel("Number of octaves");
		getContentPane().add(lblNumberOfOctaves);
		
		sldNumberOctaves = new JSlider();
		sldNumberOctaves.setPaintLabels(true);
		sldNumberOctaves.setPaintTicks(true);
		sldNumberOctaves.setMinimum( 1 );
		sldNumberOctaves.setMaximum( 15 );
		sldNumberOctaves.setValue( deflt.getNumberOfOctaves() );
		sldNumberOctaves.setMajorTickSpacing( 5 );
		sldNumberOctaves.setMinorTickSpacing( 1 );
		sldNumberOctaves.setSnapToTicks( true );
		getContentPane().add(sldNumberOctaves);
		
		JButton btCancel = new JButton("Verwerfen");
		btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ok = false;
				setVisible( false );
			}
		});
		getContentPane().add(btCancel);
		
		JButton btOK = new JButton("Bestätigen");
		btOK.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				conf = createConfig();
				ok = true;
				setVisible( false );
			}
		});
		getContentPane().add(btOK);
		
	}
	/**
	 * Creates a FastHessianConfig out of the various sliders.
	 * @return a FastHessianConfig
	 */
	private FastHessianConfig createConfig() {
		float threshold = sldThreshold.getValue();
		int radius = sldRadius.getValue();
		int features = sldMaxFeatures.getValue();
		int samplesize = sldInitialSize.getValue();
		int size = sldSampleSize.getValue();
		int scales = sldNumberScales.getValue();
		int octaves = sldNumberOctaves.getValue();
		return new FastHessianConfig( threshold, radius, features, samplesize, size, scales, octaves);
	}
	
	public FastHessianConfig getConfig() {
		return this.conf;
	}
	/**
	 * Indicates whether or not the dialogue was okayed or cancelled
	 * @return true if okay
	 */
	public boolean isOK() {
		return ok;
	}
}
