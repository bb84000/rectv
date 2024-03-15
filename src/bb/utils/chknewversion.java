package bb.utils;

/*
* Check if a new version is available;
*
*/

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import bb.arraylist.csvList;

public class chknewversion {
	private static String progname="";
	private static String versionURL="http://www.sdtp.com/versions/versions.csv";
	private static String updateURL="";
	public static String optiontext = "A new version %s is available.%sDo you want download it?";
	public static String yesbutton = "Yes";
	public static String nobutton = "No";
	
	private static String version;
		
	public static void setProgname(String prgname){
		progname= prgname;
	}
	
	public static void setVersionURL(String verURL) {
		versionURL= verURL;
	}
	
	public static void setUpdateURL(String updURL) {
		updateURL= updURL;
	}
	
	public static void getLastVersion(String program, String curver) {
		final String prog = program;
		final String cver = curver;
		
		
		// Create the event notifier and pass ourself to it.
	    FileDownload req = new FileDownload (new FileDownloadEvent() {
	            // Define the actual handler for the event.
	            
				public void dataReadProgress (int done, int total, byte[] data)
	            {
	                //System.out.println("Progress: " + ((float)done/(float)total) * 100 + "%");
	            }
	            
	            public void dataReadDone (boolean error, InputStream is) 
	            {
	            	//InputStream is =  new ByteArrayInputStream(data);   ; 
	            	csvList al = new csvList();
                	al.setSeparator(';');
                	al.readCSVstream(is); 
                	if (!al.isEmpty()) {
                		Iterator<String[]> itr = al.iterator();
                		while(itr.hasNext()) {
                			String [] element =  itr.next();
                			//System.out.println (element [0]);
                			if (element[0].equals(prog)) {
                				version= element[1];
                				break;
                			}
                		} 
                	}
                	try {
						is.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						//e1.printStackTrace();
					}
	            	try {
						if (version.length() > 0) {
							if (VersionToInt(version) > VersionToInt(cver)){
								UIManager.put("OptionPane.yesButtonText", yesbutton);
								UIManager.put("OptionPane.noButtonText", nobutton);
								if (JOptionPane.showConfirmDialog(null, String.format(optiontext, version, System.lineSeparator()),  progname, JOptionPane.YES_NO_OPTION)== JOptionPane.OK_OPTION) {
									bbutils.openURL(updateURL);
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
	            }
	        });
	        req.request(versionURL);

	}
	
	public static long VersionToInt (String version) {
		long result = 0;
		try {
			String ar [] = version.split(Pattern.quote("."));
			result = (65536*Integer.parseInt(ar[0]))+Integer.parseInt(ar[1]);
			result = (65536*result)+Integer.parseInt(ar[2]);
			result= (65536*result)+Integer.parseInt(ar[3]);
		} catch (Exception e) {
			return 0;
		}
		return result;
	}


}
