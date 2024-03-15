package bb.utils;

/*
 * Geolocation routine using the google api.
 * More info on the service at : 
 * https://developers.google.com/maps/documentation/geocoding/#Geocoding
 *  
 * Usage : Set a new instance of httpgeoloc, call getGeolog with the address to check :
 *  	httpgeoloc geo = new httpgeoloc()
 *  	Boolean = geo.getGeoloc("number+street,postcode+town,country);
 *  
 *  Even if web now accept spaces, it is better to replace spaces with "+" character
 *  
 *  Retrieve latitude and longitude in public variables
 *  
 *  Google maps prefix and suffix public variables can be changed if needed 
 *  
 *  bb - sdtp - march 2015
 * 
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class httpgeoloc {
	public String status = "";
	public Float longitude = 0.0f;
	public Float latitude = 0.0f;
	public String goog_prefix = "http://maps.googleapis.com/maps/api/geocode/xml?address=";
	public String goog_suffix = "&sensor=false";
			
	
	public boolean getGeoloc(String surl) {
		InputStream is = null;
		String saddr = goog_prefix;
		try {
			saddr += URLEncoder.encode(surl, "UTF-8");
			saddr += goog_suffix;
			URL url = new URL(saddr);
			is = url.openStream();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder builder;
			builder = factory.newDocumentBuilder();
			Document googXML = builder.parse(is);
			Element geocode =googXML.getDocumentElement();
			if (!geocode.getNodeName().equalsIgnoreCase("GeocodeResponse")) ;
				//Liste tous les nodes de 1er niveau du fichier
				String sn;
				String nn;			
				NodeList geolist = geocode.getChildNodes();
				int geolen = geolist.getLength();
				for (int i=0; i< geolen; i++) {
					Node iNode= geolist.item(i);
					if (iNode.getNodeType() == Node.ELEMENT_NODE) {
						sn = iNode.getTextContent();
						nn = iNode.getNodeName();
						if (nn.equalsIgnoreCase("status")) status = sn;
						if (nn.equalsIgnoreCase("result")) {
							//On va lire la suite à condition que status soit OK
							if (!status.equalsIgnoreCase("OK")) throw new Exception();
								// liste de tous les nodes de result	
								geolist = iNode.getChildNodes();
								geolen = geolist.getLength();
								for (int j=0; j< geolen; j++) {
									iNode= geolist.item(j);
										if (iNode.getNodeType() == Node.ELEMENT_NODE) {
											sn = iNode.getTextContent();
											nn = iNode.getNodeName();
											if (nn.equalsIgnoreCase("geometry")) {
												// Liste de tous les nodes de geometry
												geolist = iNode.getChildNodes();
												geolen = geolist.getLength();
												for (int k= 0; k< geolen; k++) {
													iNode= geolist.item(k);
													if (iNode.getNodeType() == Node.ELEMENT_NODE) {
														sn = iNode.getTextContent();
														nn = iNode.getNodeName();
														if (nn.equalsIgnoreCase("location")) {
															//Liste des nodes de location
															geolist = iNode.getChildNodes();
															geolen = geolist.getLength();
															for (int l= 0; l < geolen; l++) {
																iNode = geolist.item(l);
																if (iNode.getNodeType() == Node.ELEMENT_NODE) {
																	sn = iNode.getTextContent();
																	nn = iNode.getNodeName();
																	if (nn.equalsIgnoreCase("lng")) {
																		longitude = Float.parseFloat(sn); 
																	}
																	else if (nn.equalsIgnoreCase("lat")) {
																		latitude = Float.parseFloat(sn);
																	}
																}
															}
														}
													}
												}
											}
										}
									}
							}
						}  //Fin du test element geolist
					}  // Fin de la routine de geolist
				// Fin de la routine de parser
				is.close();
			} catch (Exception e) {
				try {
					is.close();
				} catch (IOException e1) {
					// Nothing to do
				}
				return false;
			}
			return true;
		}
}
