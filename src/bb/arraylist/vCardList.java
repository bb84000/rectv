package bb.arraylist;


/**

 * ArrayList <String[] with vCard load and save functions 
 * 
 * Array : vCards fields :
 *  {version, name, surname, street, bp, lieudit, postcode, town, region, country,  home phone, cell phone, email, web , longitude, latitude, photo,
 *   function/title, service, company, street, bp, lieudit, postcode, town, region, country,   } 
 *  To get other fields, you can add them in the code
 * 
 * boolean readvCardfile(String filename [, String csname])
 *   	String filename : filename
 *  	String csname : charset string , "UTF-8", "Cp1252" (etc)
 * 	v2.1 and v3.0compatible
 * 
 *  boolean readvCardstream(InputStream is[, String  csname])
 *  	InputStream is : stream (from resource or other source)
 *  	String  csname : see above
 *  
 *  long writevCardfile(String filename[, String  csname])
 *  	String filename : filename
 * 	 	String  csname : see above
 * 		return byte written
 * 
 *  
 * bb - march 2015
 * 
 */


import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import bb.utils.bbutils;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;


public class vCardList extends ArrayList<String[]>{

	private static final long serialVersionUID = 1L;
	private String [] vcarray; 
	private String tmpdir = System.getProperty("java.io.tmpdir");
	private String vcardBegin = "BEGIN:VCARD";
	private String vcardVersion = "VERSION";
	private String vcardEnd = "END:VCARD";
	
	
	// read vcard file with default charset
	public boolean readvCardfile(String filename){
		return readvCardfile(filename, Charset.defaultCharset().name());
    }
	
	// read file with charset support
	public boolean readvCardfile(String filename, String csname){
     	try {
 			File file = new File(filename);
			if (!file.exists()) {
				return false;
			}
			else {
				FileInputStream fis = new FileInputStream(filename);
				return readvCardstream (fis, csname);
			}
		} catch (Exception e) {
			return false;
		}
    }
	
    // read stream w/o charset support
    public boolean readvCardstream(InputStream is) {
    	return readvCardstream(is,  Charset.defaultCharset().name());
     }

    // read stream with charset support 
    public boolean readvCardstream(InputStream is, String  csname){
    	Charset cs; 
		try {
			cs = Charset.forName(csname);
		} catch (Exception e1) {
			cs = Charset.defaultCharset();
		}
		InputStreamReader r = new InputStreamReader(is, cs);
		try {
			if (is.available()>0) return readstream (r);
			else return false;
		} catch (IOException e) {
			return false;
		}
    }


	// common routine to read vCard streams	
	private boolean	readstream (InputStreamReader r) {
		try {
			 tmpdir = System.getProperty("java.io.tmpdir");
			// Generate a random numer
			Random randomGenerator = new Random();
			//nimgfile = contact_list.get(current_contact).Name+String.format("%05d", rInt)+".jpg" ;
			String filename ;
			BufferedReader vCardFile = new BufferedReader(r);
				String dataRow = vCardFile.readLine(); // Read first line.
				// The while checks to see if the data is null. If 
				// it is, we've hit the end of the file. If not, 
				boolean photo = false;
				String imagetype = "";
				String b64string = "";
				while (dataRow != null){
					// process base64 line if we are in a photo
					if (photo) {
						if (dataRow.length() > 0) {
							b64string += dataRow+"\n";
						}
						else {
							try {
								//byte[] valueDecoded= Base64.decode(b64string );
								byte[] valueDecoded= DatatypeConverter.parseBase64Binary(b64string);
								BufferedImage img = ImageIO.read(new ByteArrayInputStream(valueDecoded));
								if (vcarray[1].length() > 0) {// || (vcarray[1].length() == 0)) {
									filename = "VCIMP"+vcarray[1];
									filename = filename.toUpperCase();
								}
								else {
									filename = "VCIMPNONAME";
								}
								int rInt = randomGenerator.nextInt(10000);
								filename += String.format("%05d", rInt);
								filename += "."+imagetype ;
								File outputfile = new File(tmpdir+filename);
								ImageIO.write(img, imagetype, outputfile);
								vcarray[16]= filename;
							} catch (Exception e) {
								// DO nothing.
								// Allow continue parse if there is corrupted photo data
							}
						photo = false;
						//System.out.println();
						}
					}
					else {
						if (bbutils.indexOfignoreCase(dataRow, vcardBegin) == 0) {
							// begin vCard
							vcarray = new String [40];
						}
						else if (bbutils.indexOfignoreCase(dataRow, vcardVersion) == 0) {
							// Get version number
							vcarray [0] = dataRow.substring(8);
							
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "N:") == 0) || (bbutils.indexOfignoreCase(dataRow, "N;") == 0)) {
							// Name field  (skip charset) : LASTNAME; FIRSTNAME; ADDITIONAL NAME; NAME PREFIX(Mr.,Mrs.); NAME SUFFIX) 
							int i = dataRow.indexOf(":");
							String s; 
							if (i >= 0) {
								s = dataRow.substring(i+1);
								String line [] = s.split(";");
								try {
									vcarray [1] = (line [0] != null) ? line [0] : ""; // name
									vcarray [2] = (line [1] != null) ? line [1] : ""; // surname
									// contact_manager does't support other name fields
									// You can add yours if needed !
								} catch (Exception e) {
									// do nothing, we have reached the end of line !
								} 
							}
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "ADR;HOME") == 0) || (bbutils.indexOfignoreCase(dataRow, "ADR;TYPE=HOME") == 0)) {
							// Home address field  : PO Box; Extended addr; Street; Town; Region/state; Post code; Country
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								String line [] = s.split(";");
								try {
									vcarray [4] = (line [0] != null) ? line [0] : ""; // Po box
									vcarray [5] = (line [1] != null) ? line [1] : ""; // Lieudit
									vcarray [3] = (line [2] != null) ? line [2] : ""; // street
									vcarray [7] = (line [3] != null) ? line [3] : ""; // town 
									vcarray [8] = (line [4] != null) ? line [4] : ""; // region
									vcarray [6] = (line [5] != null) ? line [5] : ""; // post code
									vcarray [9] = (line [6] != null) ? line [6] : ""; // country
								} catch (Exception e) {
									// do nothing, we have reached the end of line !
								}
							}
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "ADR;WORK") == 0) || (bbutils.indexOfignoreCase(dataRow, "ADR;TYPE=WORK") == 0)) {
							// TODO
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "TEL;HOME") == 0) || (bbutils.indexOfignoreCase(dataRow, "TEL;TYPE=HOME") == 0)) {
							// Home phone field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [10] = s; // home phone
							}
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "TEL;CELL") == 0) || (bbutils.indexOfignoreCase(dataRow, "TEL;TYPE=CELL") == 0)) {
							// Cell phone field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [11] = s; // cell phone
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, "EMAIL") == 0)  {
							// Email field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [12] = s;  // email
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, "URL") == 0) {
							// URL field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [13] = s;   // web
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, "GEO") == 0) {
							// 2.1 et 3 GEO:39.95;-75.1667
							// 4.0 GEO:geo:39.95,-75.1667 et GEO;VALUE=uri:geo:39.95,-75.1667
							int i = dataRow.indexOf(":");
							String s; 
							if (i >= 0) {
								s = dataRow.substring(i+1);
								String line [] = s.split(";");
								try {
									vcarray [15] = (line [0] != null) ? line [0] : ""; // long
									vcarray [14] = (line [1] != null) ? line [1] : ""; // lat
								} catch (Exception e) {
									// do nothing, we have reached the end of line !
								} 
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, "TITLE") == 0) {
							// TITLE field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [17] = s;  // function
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, "ORG") == 0) {
							// Company; service; etc.
							int i = dataRow.indexOf(":");
							String s; 
							if (i >= 0) {
								s = dataRow.substring(i+1);
								String line [] = s.split(";");
								try {
									vcarray [19] = (line [0] != null) ? line [0] : "";
									vcarray [18] = (line [1] != null) ? line [1] : "";
									// other fields if you need
								} catch (Exception e) {
									// do nothing, we have reached the end of line !
								} 
							}
							
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "ADR;WORK") == 0) || (bbutils.indexOfignoreCase(dataRow, "ADR;TYPE=WORK") == 0)) {
							// Home address field  : PO Box; Extended addr; Street; Town; Region/state; Post code; Country
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								String line [] = s.split(";");
								try {
									vcarray [21] = (line [0] != null) ? line [0] : ""; // Po box
									vcarray [22] = (line [1] != null) ? line [1] : ""; // Lieudit
									vcarray [20] = (line [2] != null) ? line [2] : ""; // street
									vcarray [24] = (line [3] != null) ? line [3] : ""; // town 
									vcarray [25] = (line [4] != null) ? line [4] : ""; // region
									vcarray [23] = (line [5] != null) ? line [5] : ""; // post code
									vcarray [26] = (line [6] != null) ? line [6] : ""; // country
								} catch (Exception e) {
									// do nothing, we have reached the end of line !
								}
							}
						}
						else if ((bbutils.indexOfignoreCase(dataRow, "TEL;WORK") == 0) || (bbutils.indexOfignoreCase(dataRow, "TEL;TYPE=WORK") == 0)) {
							// Home phone field
							int i = dataRow.indexOf(":");
							String s;
							if (i >= 0) {
								s = dataRow.substring(i+1);
								vcarray [27] = s; // work phone
							}
						}
						else if (bbutils.indexOfignoreCase(dataRow, vcardEnd) == 0) {
							// end vcard
							add(vcarray);
							//System.out.println(Arrays.toString(vcarray));
						}
						
						else if (bbutils.indexOfignoreCase(dataRow, "PHOTO") == 0) {
							// Photo field
							if (bbutils.indexOfignoreCase(dataRow, "ENCODING=B") >= 0) {
								// embedded image, search type
								
								int i = bbutils.indexOfignoreCase(dataRow, "TYPE=");
								if (i >= 0) {
									// we get the type, searching the first ; or :;
									imagetype = dataRow.substring(i+5);
									i = imagetype.indexOf(":");
									int j = imagetype.indexOf(";");
									if (i > j) imagetype = imagetype.substring(0, i);
									else imagetype = imagetype.substring(0, j);
									b64string = dataRow.substring(dataRow.indexOf(":")+1)+"\n";
									photo= true;
 									
								}
							}
							
						}

					}
					dataRow = vCardFile.readLine(); 
					
				}
				// Close the file once all data has been read.
				vCardFile.close();
				return true; 
		} catch (IOException e) {
			return false;
		}
    }
	
	
	
	// write file to disk
	
	public long writevCardfile(String filename){
		return writevCardfile(filename, Charset.defaultCharset().name());
	}
	
	
	// write file to disk with charset support
	public long writevCardfile(String filename, String csname){
		Charset cs;
		try {
			cs = Charset.forName(csname);
		} catch (Exception e1) {
			cs = Charset.defaultCharset();
		}
		try {
			File file = new File(filename);
			FileOutputStream fs = new FileOutputStream(file.getAbsoluteFile());
			writevCardstream(fs, cs.name());
			return file.length(); 
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			return 0;
		}
	}

	private void writevCardstream(FileOutputStream fs, String csname ) throws UnsupportedEncodingException {
		OutputStreamWriter osw;
			osw = new OutputStreamWriter(fs, csname);
			writestream (osw);
	}

	private void writeLine (BufferedWriter bw, String s) throws IOException {
		bw.write(s);
		bw.newLine();

	}
	
	private void writestream(OutputStreamWriter osw) {
		try {
			BufferedWriter bw = new BufferedWriter(osw);
			// write all vcards
			
			for (int i=0; i < size(); i++) {
				//System.out.println("N:"+get(i)[1]+";"+get(i)[2]);
				// write begin line
				String [] tmparr = new String [40];
				// initialize array with empty strings to avoid exception
				for (int j=0; j< tmparr.length; j++) tmparr [j] ="";
				tmparr = get(i);
				writeLine (bw, vcardBegin);
				writeLine (bw, vcardVersion+":"+tmparr [0]);
				// check if 7 bit
				if (bbutils.is7bitString(tmparr[1]+tmparr[2])) {
					writeLine (bw, "N:"+tmparr [1]+";"+tmparr [2]);
					writeLine (bw, "FN:"+tmparr [2]+" "+tmparr [1]);
				}
				else {
					writeLine (bw, "N;CHARSET=UTF-8:"+tmparr [1]+";"+tmparr [2]);
					writeLine (bw, "FN;CHARSET=UTF-8:"+tmparr [2]+" "+tmparr [1]);
				}
				if (bbutils.is7bitString(tmparr[4]+tmparr[5]+tmparr[3]+tmparr[7]+tmparr[8]+tmparr[6]+tmparr[9])) 
					writeLine (bw, "ADR;HOME:"+tmparr[4]+"; "+tmparr[5]+"; "+tmparr[3]+"; "+tmparr[7]+"; "+tmparr[8]+" ; "+tmparr[6]+"; "+tmparr[9]);
				else writeLine (bw, "ADR;HOME;CHARSET=UTF-8:"+tmparr[4]+"; "+tmparr[5]+"; "+tmparr[3]+"; "+tmparr[7]+"; "+tmparr[8]+"; "+tmparr[6]+"; "+tmparr[9]);
				if (tmparr[10].length() > 0) writeLine (bw, "TEL;HOME:"+tmparr[10]);
				if (tmparr[11].length() > 0) writeLine (bw, "TEL;CELL:"+tmparr[11]);
				if (tmparr[12].length() > 0) writeLine (bw, "EMAIL;HOME:"+tmparr[12]);
				if (tmparr[13].length() > 0) writeLine (bw, "URL:"+tmparr[13]);
				//GEO:39.95;-75.1667 : Latitude first
				if (tmparr[15].length() > 0 || tmparr[14].length() > 0) writeLine (bw, "GEO:"+tmparr[15]+";"+tmparr[14]);
				if (tmparr[17].length() > 0) {
					if (bbutils.is7bitString(tmparr[17])) writeLine (bw, "TITLE:"+tmparr[17]);
					else writeLine (bw, "TITLE;CHARSET=UTF-8:"+tmparr[17]);
				}
				if ((tmparr[18]+tmparr[19]).length() > 0) {
					if (bbutils.is7bitString(tmparr[18]+tmparr[19])) writeLine (bw, "ORG:"+tmparr[19]+";"+tmparr[18]);
					else writeLine (bw, "ORG;CHARSET=UTF-8::"+tmparr[19]+";"+tmparr[18]);
				}
				if (bbutils.is7bitString(tmparr[21]+tmparr[22]+tmparr[20]+tmparr[24]+tmparr[25]+tmparr[23]+tmparr[26])) 
					writeLine (bw, "ADR;WORK:"+tmparr[21]+"; "+tmparr[22]+"; "+tmparr[20]+"; "+tmparr[24]+";"+tmparr[25]+"; "+tmparr[23]+"; "+tmparr[26]);
				else writeLine (bw, "ADR;WORK;CHARSET=UTF-8:"+tmparr[22]+"; "+tmparr[20]+"; "+tmparr[24]+";"+tmparr[25]+"; "+tmparr[23]+"; "+tmparr[26]);
				if (tmparr[27].length() > 0) writeLine (bw, "TEL;WORK:"+tmparr[27]);
				
				// if there is an image, process it
				if (tmparr[16].length() > 0) {
					String b64str ="";
			    	FileInputStream fis = null;
			    	File file = new File(tmparr[16]);
			    	if (file.exists()) {
			    		String filext = bbutils.getFilenameExt(tmparr[16]);
			    		// image/gif, image/jpeg, or image/png;
			    		if (filext.equalsIgnoreCase("jpg")) filext= "JPEG";
			    		else filext = filext.toUpperCase();
			    		byte[] bFile = new byte[(int) file.length()];
			    		try {
			    			fis = new FileInputStream(file);
			    			fis.read(bFile);
			    			fis.close();
			    			b64str = "PHOTO;ENCODING=BASE64;TYPE="+filext+":"+DatatypeConverter.printBase64Binary(bFile);
			    			String s = b64str.substring(0,75); 
			    			while(s.length()> 0) {
					        writeLine(bw, s);
					    	// on a encore plus d'une ligne
					        if (b64str.length() > 75) {
					        	b64str = b64str.substring(75); 
					        	s = (b64str.length() > 75) ? b64str.substring(0, 75) : b64str.substring(0); 
					        }
					        else s="";
			    			}
			    			// Add blank line at the end of data
			    			writeLine(bw, "");
			    		} catch (Exception e) {
			    			// DO nothing there is likely an error
			    		}
			    	}
				writeLine (bw, vcardEnd);
				}
			}
			bw.close();
		} catch (IOException e) {
			// Do nothing
		}
	}
	
	public void sort (int fld) {
		final int i = fld;
		try {
			Collections.sort(this,new Comparator<String[]>() {
				public int compare(String[] strings, String[] otherStrings) {
					return strings[i].compareTo(otherStrings[i]);
				}
			});
		} catch (Exception e) {
			// Do nothing, index out of bounds
		}
	} // end sort routine


}
