package gui.subviews.detect;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import photo.InterestPoint;

import util.Utility;

public class AddInterestPointLabel extends JLabel {
	AddCircleMouseListener listener;
	BufferedImage image;
	float factor = 1;
	List<InterestPoint> ips = new LinkedList<InterestPoint>();
	
	public AddInterestPointLabel() {
		super();
	}
	
	public List<InterestPoint> getCustomIPs() {
		return ips;
	}
	
	public void drawCircle( int x, int y, int r ) {
		ips.add( new InterestPoint( x, y, r ) );
		Graphics2D graph = image.createGraphics();
		graph.setColor( Color.BLUE );
		graph.setStroke( new BasicStroke( 3 ) );
		graph.drawOval( Math.round( (x-r/2)*factor ), Math.round( (y-r/2)*factor ), Math.round( r * factor ), Math.round( r * factor ));
		//System.out.println( "Draw to " + Math.round( (x-r/2)*factor ) + "/" + Math.round( (y-r/2)*factor ) + ", " +Math.round( r * factor ) );
		graph.dispose();
		setIcon( new ImageIcon( Utility.resizeImage( image)));
	}
	
	public void setImage( BufferedImage img ) {
		this.image = img;
		this.removeMouseListener(listener);
		this.removeMouseMotionListener(listener);
		listener = new AddCircleMouseListener( this );
		this.addMouseListener( listener );
		this.addMouseMotionListener( listener );
		factor = img.getWidth()/Utility.IMAGE_SIZE;
	}

}

class AddCircleMouseListener extends MouseInputAdapter {
	private boolean pressed = false;
	private int centerX = 0;
	private int centerY = 0;
	private int radius = 0;
	private AddInterestPointLabel label;
	
	public AddCircleMouseListener( AddInterestPointLabel label ) {
		this.label = label;
	}
	
	public void mouseReleased( MouseEvent evt ) {
		pressed = false;
		label.drawCircle(centerX, centerY, radius );
	}
	
	public void mousePressed( MouseEvent evt ) {
		pressed = true;
		centerX = evt.getX();
		centerY = evt.getY();
		//System.out.println( "Center " + centerX + "/" + centerY );
	}
	
	public void mouseDragged( MouseEvent evt ) {
		int x = centerX - evt.getX();
		int y =  centerY - evt.getY();
		radius = (int) Math.round( Math.sqrt( x*x + y*y ) );
		//System.out.println( "r: " + radius );
	}
}
