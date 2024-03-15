package bb.utils;

/*
 * Download file from Internet
 * Non blocking routine
 * 
 * Usage :
 * request (String url)
 * Download is returned in a byte array in the dataReadDone event
 * 
 * Events :
 * void dataReadProgress (int done, int total, byte data[]);
 *   done : Byte currently downloaded
 *   total: length of download
 *   data [] byte array curently downloaded
 *   
 * void dataReadDone(boolean error, byte data[]);
 * 	error : false if download is complete
 * 	data  byte array
 * 
 * bb - october 2013
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;


public class FileDownload extends Thread implements Runnable {

	private FileDownloadEvent ie;
    private InputStream is = null;
    private DataInputStream dis = null;
    private int dataReadSize = 1024;
    private String downloadURL = null;
    public FileDownload (FileDownloadEvent event)
    
    {
        // Save the event object for later use.
        ie = event;
    }
 
    public void request (String url)
    {
        this.downloadURL = url;
        this.start();
    }
 
    //...
    public void run ()
    {
        boolean error = false;
        byte [] data = null; 
        try {
           URL url = new URL(this.downloadURL);
           URLConnection fdCon = url.openConnection();
           // Todo if non supported  returns -1
           int total = fdCon.getContentLength();
           is = url.openStream();  // throws an IOException
           dis = new DataInputStream(new BufferedInputStream(is));
           // os will be loaded with content
           ByteArrayOutputStream os= new ByteArrayOutputStream();
           data = new byte[dataReadSize];
           int progress = 0, n;
           while ((n = dis.read(data)) > 0) { //!= -1) {
        	    os.write(data, 0, n);
           		progress += n;
           		this.ie.dataReadProgress (progress, total, data);
           		Arrays.fill(data, (byte)0);
           }
           // transfer stream in "is"        
           is = new ByteArrayInputStream(os.toByteArray()) ;    
           os.close();
        } catch (Exception e)
        {
            error = true;
        }
        this.ie.dataReadDone(error, is);
    }
    // ...

}
