package util;

import help.HelpContent;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLHelpLoader {
	private static XMLHelpLoader instance;
	private String schritt = "schritt";
	private String beschreibung = "beschreibung";
	private String nummer = "nummer";
	private String titel = "titel";
	
	private XMLHelpLoader() {
		
	}
	
	public static XMLHelpLoader getInstance() {
		if ( instance == null )
			instance = new XMLHelpLoader();
		return instance;
	}
	
	public HelpContent load( int step ) {
		File xml = new File( getClass().getResource( "/help/hilfe.xml" ).getFile() );
		HelpContent content = new HelpContent();
		Document dom;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder builder = dbf.newDocumentBuilder();
			dom = builder.parse( xml );
			Element doc = dom.getDocumentElement();
			NodeList schritte = doc.getElementsByTagName( schritt );
			if ( schritte.getLength() == 0 )
				throw new Exception( schritt + " is not a valid tag name because there are none." );
			int i = 0;
			while ( schritte.item( i ) != null ) {
				Node schritt = schritte.item( i );
				if ( schritt.getAttributes().getNamedItem( nummer ).getTextContent().equals( step + "" ) ) {
					NodeList children = schritt.getChildNodes();
					if ( children.getLength() == 0 )
						throw new Exception( "Desired help step " + step + " has no content" );
					int j = 0;
					while ( children.item( j ) != null ) {
						Node help = children.item( j );
						if ( help.getNodeName().equals( beschreibung ) ) {
							content.setContent( help.getTextContent() );
						}
						if ( help.getNodeName().equals( titel ) ) {
							content.setTitle( help.getTextContent() );
						}
						j++;
					}
					if ( !content.hasTitle() )
						throw new Exception( "No tag " + titel + " found." );
					if ( !content.hasContent() )
						throw new Exception( "No tag " + beschreibung + "found." );
				}
				i++;
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			
		}
		return content;
	}

}
