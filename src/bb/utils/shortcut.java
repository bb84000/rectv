package bb.utils;

/*
* Create shortcuts in Windows startup folder
* .desktop in linux autostart folder
* .plist file in Os X LaunchAgents folder
*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
public class shortcut {
	
	/*AllUsersDesktop     AllUsersStartMenu     AllUsersPrograms     AllUsersStartup
    Desktop     Favorites     Fonts     MyDocuments     NetHood     PrintHood
    Programs     Recent     SendTo     StartMenu     Startup     Templates*/

	public enum sh_Type {
	    U_STARTUP, U_DESKTOP, U_STARTMENU, U_MYDOCUMENTS
	}
	
	// create link in Windows startup folder;
	public static void createWinShortcut(sh_Type  shtype, String appPath, String appName, String param, String shcutname, String icon) {
		
		String strtype = "Desktop";
		switch (shtype) {
        	case U_STARTUP:
            	strtype= "Startup";
            	break;
        	case U_DESKTOP:
        		strtype= "Desktop";
        		break;
        	case U_STARTMENU:
        		strtype= "StartMenu"; 
        		break;
        	default:
        		strtype = "MyDocuments";
        		break;
		}
		
		// Create vbs script to create shortcut 
		try {
			String tmpdir = System.getProperty("java.io.tmpdir");
			File file = new File(tmpdir+"/creshcut.vbs");
			FileOutputStream	fop = new FileOutputStream(file);
	 		
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			
			//  To have two quotes in vbs, double the quote.
			String content = "Set Shell = CreateObject(\"WScript.Shell\")\r\n";
			content += "startupDir = Shell.SpecialFolders(\""+strtype+"\")\r\n";
			content += "Set link= Shell.CreateShortcut(startupDir & \"\\"+shcutname+"\")\r\n";
			content += "link.Arguments = \"-jar \"\""+appPath+appName+"\"\" "+param+"\"\r\n";
			content += "link.IconLocation = \""+icon+"\"\r\n";
			content += "link.WindowStyle = 7  \r\n"; 
			content += "link.TargetPath = \"javaw.exe\" \r\n";
			content += "link.WorkingDirectory = \""+appPath+"\"\r\n";
			content += "link.Save\r\n";
	
			// get the content in bytes
			byte[] contentInBytes = content.getBytes();
			// Write the file
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			// Run the vbs
			Process p = Runtime.getRuntime().exec("cscript \""+tmpdir+"creshcut.vbs\"");
				
			// Read script execution results to know when it is done
			InputStream is = p.getInputStream();
			int i = 0;
			StringBuffer sb = new StringBuffer();
			while ( (i = is.read()) != -1)
	              sb.append((char)i);
			// we can delete the script
			file.delete();
		} catch (IOException e) {
	          // e.printStackTrace();
	    }
	}
	
	// delete windows shortcut
	public static void deleteWinShortcut(sh_Type  shtype, String shcutname) {
		try {
			String strtype = "Desktop";
			switch (shtype) {
	        	case U_STARTUP:
	            	strtype= "Startup";
	            	break;
	        	case U_DESKTOP:
	        		strtype= "Desktop";
	        		break;
	        	case U_STARTMENU:
	        		strtype= "StartMenu"; 
	        		break;
	        	default:
	        		strtype = "MyDocuments";
	        		break;
			}
			String tmpdir = System.getProperty("java.io.tmpdir");
			File file = new File(tmpdir+"/delshcut.vbs");
			FileOutputStream	fop = new FileOutputStream(file);
					
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			String content = "Set Shell = CreateObject(\"WScript.Shell\")\r\n";
			content += "startupDir = Shell.SpecialFolders(\""+strtype+"\")\r\n";
			content += "Set fso =  CreateObject(\"Scripting.FileSystemObject\")\r\n";
			content += "fso.DeleteFile startupDir & \"\\"+shcutname+"\"\r\n";
			
			// get the content in bytes
			byte[] contentInBytes = content.getBytes();
 
			// write the file
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			// run the script
			Process p = Runtime.getRuntime().exec("cscript \""+tmpdir+"delshcut.vbs\"");
			// Read script execution results to know when it is done
			InputStream is = p.getInputStream();
	        int i = 0;
	        StringBuffer sb = new StringBuffer();
	        while ( (i = is.read()) != -1)
	              sb.append((char)i);
	        // delete script
	        file.delete();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} 
	}
	
	// Create LInux shortcut
	
	public static void createLinuxShortcut(String appPath, String appName, String param, String shcutname, String icon) {
		// echo ${XDG_DESKTOP_DIR:-$HOME/Desktop}
		// Create desktop file
		try {
			String home =System.getProperty("user.home")+"/";
			
			File file= null;
			// Gnome and consorts
			String gnomedir = home+".config/autostart/";
			// KDE and consorts
			String kdedir = home+".kde/Autostart/";
			String kde4dir = home+".kde4/Autostart/";
			if (new File(gnomedir).exists() && new File(gnomedir).isDirectory()) file = new File(gnomedir+shcutname);
			else if (new File(kdedir).exists() && new File(kdedir).isDirectory()) file = new File(kdedir+shcutname);
			else if (new File(kde4dir).exists() && new File(kde4dir).isDirectory()) file = new File(kde4dir+shcutname);
			FileOutputStream	fop = new FileOutputStream(file);
			
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			// Create .desktop file
			String content = "[Desktop Entry]\r\n";
			content += "Type=Application\r\n";
			content += "Exec=java -jar \""+appPath+appName+"\" "+param+"\r\n";
			content += "Icon= "+icon+"\r\n";
			content += "Hidden=false\r\n";
			content += "NoDisplay=false\r\n";
			content += "X-GNOME-Autostart-enabled=true\r\n";
			content += "X-KDE-autostart-after=panel\r\n";
			content += "Name[fr]=Calendrier\r\n";
			content += "Name=Calendrier\r\n";
			content += "Comment[fr]=\r\n";
			content += "Comment=\r\n";
			
			// get the content in bytes
			byte[] contentInBytes = content.getBytes();
				
			// Write the file
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			file.setExecutable(true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} 
	}

	public static void deleteLinuxShortcut(String shcutname) {
		String home =System.getProperty("user.home")+"/";
		// Gnome and consorts
		String gnomefile = home+".config/autostart/"+shcutname;
		// KDE and consorts
		String kdefile = home+".kde/Autostart/"+shcutname;
		String kde4file = home+".kde4/Autostart/"+shcutname;
		if (new File(gnomefile).exists()) (new File(gnomefile)).delete();
		else if (new File(kdefile).exists()) (new File(kdefile)).delete();
		else if (new File(kde4file).exists()) (new File(kde4file+shcutname)).delete();
	}
	
	public static void createOSXShortcut(String appPath, String appName, String param, String shcutname, String icon) {
		
		// We will write a plist file in the proper directory
		String launchAg = System.getProperty("user.home")+"/Library/LaunchAgents";
		String javahome = System.getProperty("java.home");
		
		// Create launchagent if it doesnt exists
		File folderExisting = new File(launchAg);
		if (!folderExisting.exists()) {
			boolean success = (new File(launchAg).mkdirs());
			if (!success) return;
		}
		try {
			File file= new File(launchAg+"/"+shcutname+".plist");
			FileOutputStream	fop = new FileOutputStream(file);
			
			// if file doesn't exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
			// Create .plist file
			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n";
			content += "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\r\n";
			content += "<plist version=\"1.0\">\r\n";
			content += "	<dict>\r\n";
			content += "		<key>Label</key>\r\n";
			content += "		<string>"+shcutname+"</string>\r\n";
			content += "		<key>ProgramArguments</key>\r\n";
			content += "		<array>\r\n";
			content += "			<string>"+javahome+"/bin/java</string>\r\n";
			content += "			<string>-jar</string>\r\n";
			content += "			<string>"+appPath+appName+"</string>\r\n";
			content += "			<string>"+param+"</string>\r\n";
			content += "		</array>\r\n";
			content += "		<key>KeepAlive</key>\r\n";
			content += "		<true/>\r\n";
			content += "		<key>LaunchOnlyOnce</key>\r\n";
			content += "		<true/>\r\n";
			content += "	</dict>\r\n";
			content += "</plist>\r\n";
			byte[] contentInBytes = content.getBytes();
			
			// Write the file
			fop.write(contentInBytes);
			fop.flush();
			fop.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void deleteOSXShortcut(String shcutname) {
		String launchAg = System.getProperty("user.home")+"/Library/LaunchAgents";
		File file= new File(launchAg+"/"+shcutname+".plist");
		if (file.exists()) file.delete();
	}

}
