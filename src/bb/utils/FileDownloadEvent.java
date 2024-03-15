package bb.utils;

import java.io.InputStream;

/*
 * Part of non blocking file download system 
 * 
 */

public interface FileDownloadEvent {
	// Used to implement a progress bar or toher things
    public void dataReadProgress (int done, int total, byte data[]);
    // Download done; returns input stream content
    public void dataReadDone(boolean error, InputStream is);
}

