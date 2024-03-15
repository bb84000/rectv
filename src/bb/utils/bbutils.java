package bb.utils;

/*
 * Utilities for various bb - sdtp papplications  
 *
 * OpenURL(String url)
 * 	launch web browser.
 * 
 * copyFile (String source, String dest)
 * 	Copy file by streams. boolean return
 * 
 * copyFileWithStream(File source, File dest)
 * 	same as previous, use File instead filenames
 * 
 * toBufferedImage(Image src)
 * 	returns BufferedImage
 * 
 * getFilenameWoExt (String filename)
 *  Get filename without extension
 *  
 * getFilenameExt (String filename)
 *	Get filename extension
 *
 * set_language(String locale_path, String default_language, String lang)
 * 	Return property with the strings in the choosen language. 
 *  	locale_path : path relative to the resource folder
 *  	lang 2 chars ISO language
 *  	file names must have the format : stringsxx.xml, xx is the 2 chars iso language.   
 *  
 * lngStr(Properties lang_props, String key, String defstr)
 * 	Returns the localized string retated to the key.
 * 		lang_props is the property obtained with the previous method
 * 		def_str is the default string if key is not found 
 * 
 * addDays(Date dt, int days)
 * 	Returns new date
 * 
 * deletewildcardfiles (String dir, String token)
 * 	delete file in directory dir containing token
 * 
 * indexOfignoreCase (String string, String strtofind)
 * Same as IndexOf not case sensitive
 * 
 * toLowerCase (String string)
 * 	Convert to lower case, accentued characters are converted to non accentued characters
 * 
 * toUpperCase (String string)
 *	Convert to upper case, accentued characters are converted to non accentued characters
 *
 *
 */


import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class bbutils {
	   
	public static void openURL(String url) {
		   Desktop desktop;
		   // short url don't work with  some OS
		   if (!url.startsWith("http")) url = "http://"+url; 
		   if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.BROWSE)) {
			   try {
				   desktop.browse(new URI(url));
			   } catch (Exception e) {
				// An error anywhere...
			   }
		   }
      }
	   
	   
	   // return 0 if succeed, 1 if no client found and 2 if error in mail address or/and subject
	   public static int sendEmail(String mailaddr, String subject) {
	   Desktop desktop;
	   if (Desktop.isDesktopSupported() && (desktop = Desktop.getDesktop()).isSupported(Desktop.Action.MAIL)) {
			try {
				subject = URLEncoder.encode(subject, "UTF-8");
				subject = subject.replace("+", "%20");
				URI mailto = new URI("mailto:"+mailaddr+"?subject="+subject);
				desktop.mail(mailto);
			} catch (Exception e) {
				// Error sending mail
				return 2;
			}
			return 0;
		}
	   else return 1;
	   } 
	   
	   // File copy by stream
	   
	   public static boolean copyFile (String source, String dest) {
		   File fs = new File(source);
		   File fb = new File(dest);
		   try {
			   copyFileWithStream(fs, fb);
		   } catch (Exception e) {
			return false;
		   }
		  return true; 
	   }
	   
	   // compatibility version of file copy
	  public static void copyFileWithStream(File source, File dest) throws Exception {
		    InputStream is = null;
		    OutputStream os = null;
		    try {
		        is = new FileInputStream(source);
		        os = new FileOutputStream(dest);
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = is.read(buffer)) > 0) {
		            os.write(buffer, 0, length);
		        }
		    } finally {
		        is.close(); 
		        os.close();
		    }
		}
	   
	// Needed to avoid conbversion error between awt.image and buffered image
		
		public static BufferedImage toBufferedImage(Image src) {
	        int w = src.getWidth(null);
	        int h = src.getHeight(null);
	        int type = BufferedImage.TYPE_INT_RGB;  // other options
	        BufferedImage dest = new BufferedImage(w, h, type);
	        Graphics2D g2 = dest.createGraphics();
	        g2.drawImage(src, 0, 0, null);
	        g2.dispose();
	        return dest;
	    }

		// Get filename without extension
		// take care of non usual situations
		public static String getFilenameWoExt (String filename) {
			int dotpos = filename.lastIndexOf( '.' );
		    int antislashpos = filename.lastIndexOf( '\\' );
			int slashpos = filename.lastIndexOf( '/' );
			// There is no dot !!
			if (dotpos == -1) {
				return filename;
			}
			// Dot is before any slash or antislash
			else if ((dotpos < antislashpos) || (dotpos < slashpos)) {
				return filename;
			}
			else {
				
				return filename.substring(0, dotpos);
			}
		}
		
		// Get filename extension
		public static String getFilenameExt (String filename) {
			//String filext = "";
			int dotpos = filename.lastIndexOf( '.' );
			int antislashpos = filename.lastIndexOf( '\\' );
			int slashpos = filename.lastIndexOf( '/' );
			// There is no dot !!
			if (dotpos == -1) {
				return "";
			}
			// Dot is before any slash or antislash
			else if ((dotpos < antislashpos) || (dotpos < slashpos)) {
				return "";
			}
			else {
				return filename.substring(dotpos+1);
			}
		}
		
		// set language
		public static Properties set_language(String locale_path, String default_language) {
			// get language
			String lang = System.getProperty("user.language");
			
			return set_language(locale_path, default_language, lang);
		}
	
		public static Properties set_language(String locale_path, String default_language, String lang) {
			// get language
			//String lang = System.getProperty("user.language");
			Properties lang_props = new Properties();
			InputStream is = ClassLoader.class.getResourceAsStream(locale_path+"strings_"+lang+".xml");
			if (is == null) is = ClassLoader.class.getResourceAsStream(locale_path+"strings_"+default_language+".xml");
			try {
				 lang_props.loadFromXML(is);
				 is.close();
				 return lang_props;
			} catch (IOException e) {
				return null;
			}
		}
		
		// token to translate, defaut string
		public static String lngStr(Properties lang_props, String key, String defstr) {
			try {
				return lang_props.get(key).toString();
			} catch (Exception e) {
				return defstr;
			}
		}
		
		public static Date addDays(Date dt, int days){
			SimpleDateFormat sdfmt = new SimpleDateFormat("yyyyMMdd");
			Date ndt;
			try {
				String s = sdfmt.format(dt);
				int idt = Integer.parseInt(s);
				idt += days;
				s = String.valueOf(idt);
				ndt = sdfmt.parse(s);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				return dt;
			}
			
			return ndt;
		}
	   
		// delete file 
		public static void deletewildcardfiles (String dir, String token) {
	        String pathToScan = dir;
	        String target_file ;  // fileThatYouWantToFilter
	        File folderToScan = new File(pathToScan); 
	        File[] listOfFiles = folderToScan.listFiles();
	        for (int i = 0; i < listOfFiles.length; i++) {
	            if (listOfFiles[i].isFile()) {
	                target_file = listOfFiles[i].getName();
	                //if (  target_file.startsWith(token))
	                if (indexOfignoreCase(target_file, token)>= 0) 
	                // && target_file.endsWith(".jpg"))
	                {
	                       //System.out.println("found" + " " + target_file); 
	                	 File file = new File(dir+target_file);
	                	 file.delete();
	                 }
	             }
	         }    
	     }
		
		public static int indexOfignoreCase (String string, String strtofind) {
			return string.toLowerCase().indexOf(strtofind.toLowerCase());
		}
	
		// Convert string to lowercase and non-accent string
		public static String toLowerCase (String string)
		  {
		    char [] charsData = new char [string.length ()];
		    string.getChars (0, charsData.length, charsData, 0);
		    
		    char c;
		    for (int i = 0; i < charsData.length; i++) 
		      if (   (c = charsData [i]) >= 'A'
		          && c <= 'Z')
		        charsData [i] = (char)(c - 'A' + 'a');
		      else
		        switch (c)
		        {
		          case '\u00e0' :
		          case '\u00e2' :
		          case '\u00e4' : charsData [i] = 'a';
		                          break;
		          case '\u00e7' : charsData [i] = 'c';
		                          break;
		          case '\u00e8' :
		          case '\u00e9' :
		          case '\u00ea' :
		          case '\u00eb' : charsData [i] = 'e';
		                          break;
		          case '\u00ee' :
		          case '\u00ef' : charsData [i] = 'i';
		                          break;
		          case '\u00f4' :
		          case '\u00f6' : charsData [i] = 'o';
		                          break;
		          case '\u00f9' :
		          case '\u00fb' :
		          case '\u00fc' : charsData [i] = 'u';
		                          break;
		        }
		 
		    return new String (charsData);
		  
		}
		
		// Convert string to lowercase and non-accent string
		public static String toUpperCase (String string) {
			
			return toLowerCase(string).toUpperCase();
		}
		
		public static boolean is7bitString (String s) {
			for (int i=0; i < s.length(); i++)  {
				if ((int) s.charAt(i) > 127) {	
					return false;
				}
			}
			return true;
		}
}
