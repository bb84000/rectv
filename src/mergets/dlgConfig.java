package mergets;

/*
 * Configuration dialog and processing
 * read property file containing version informations
 * get user directeories and other
 * read/write configuration file
 * manage program options
 */

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bb.utils.bbutils;
import bb.utils.shortcut;

import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.Color;

import javax.swing.ImageIcon;


// Config dialog main class
public class dlgConfig extends JDialog {
	
	// Configuration variables must be public to allow change their language from main class 
	public String OS;
	public String workingDirectory;
	public String dataDirectory = "";
	public String olddataDirectory= "";
	public String execDirectory;
	public String tempDirectory;
	public String sourceDirectory ="";
	public String destDirectory ="";
	
	public Dimension size = new Dimension (640,500);
	public Point location = new Point(0,0);
	public int saveState= 0;
	public boolean chknewver = false;
	public boolean loadstart = false;
	public boolean startmini = false;
	public boolean savepos = false;
	public Date  lastupdchk ;
	public String version = "";
	public String build ="";
	public Date builddate = null;
	public String vendor = "";
	public  Properties lang_props;
	public TitledBorder tborder;
	public JLabel lblpathcaption;
	public JButton cancelButton;
	public JCheckBox cbChknewver;	
	public JCheckBox cbStartup;
	public JCheckBox cbStartMini; 
	public JCheckBox cbPos;
	public JButton okButton ;
	public JButton btndatapath;
	public String chooser_title= "Select data folder";
	public String chooser_filter= "All folders";
	
	//private ArrayList <String[]> manifest= null;
	private String config_file = "rectv.config.xml";
	private static final long serialVersionUID = 1L;
	private String iconFile= "";
	private JComboBox <String> cblanguage;
	private String curlang = "";
	private ArrayList<String []> lnglist;
	private ActionListener CBAL; 
	private  DefaultComboBoxModel<String> cbmodel;
	private JLabel lblStatus;
	private JTextArea tapath;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		try {
			dlgConfig dialog = new dlgConfig(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			// do nothing
		}
	}

	

	
	// Config dialog constructor
	public dlgConfig(JFrame frm) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// Event not fired by OK or cancel button
				cancelButton.doClick();
			}
		});		
		
		// Load language property. Can be changed once config file is loaded 
/*		lang_props = bbutils.set_language("/resources/locale/", "en");
		
		// Create a arraylist of iso language 
		// by checking if the related language file is present in the jar
		lnglist = new ArrayList<String []> ();
		// First element is locale language
		String [] lngelm = {"", bbutils.lngStr(lang_props, "default_lang", "English")};
		lnglist.add (lngelm);
		// check if a property file exists with each iso language
		String[]langs = Locale.getISOLanguages();
		Properties tmpprop = new Properties();
		for(String lang:langs) {
			URL rb = ClassLoader.class.getResource("/resources/locale/strings_"+lang+".xml");
			if(rb!=null)  {
				// We have found a language file present 
				lngelm = new String [2];
	           	lngelm [0] = lang;
	           	// We get its name in the file
	           	tmpprop = bbutils.set_language("/resources/locale/", "en", lang);
	           	lngelm [1] = bbutils.lngStr(tmpprop, "language", "English") ;			
	           	lnglist.add(lngelm); 
		      }
		}*/

		// dialog initialization	
		getContentPane().setPreferredSize(new Dimension(0, 15));
		setResizable(false);
		setTitle("RecTV settings");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 470, 230);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{434, 0};
		gridBagLayout.rowHeights = new int[]{295, 33, 15, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		// Main panel
		JPanel panel_main = new JPanel();
		panel_main.setLayout(null);
		GridBagConstraints gbc_panel_main = new GridBagConstraints();
		gbc_panel_main.insets = new Insets(0, 0, 5, 0);
		gbc_panel_main.fill = GridBagConstraints.BOTH;
		gbc_panel_main.gridx = 0;
		gbc_panel_main.gridy = 0;
		getContentPane().add(panel_main, gbc_panel_main);
			
		// System box
		tborder= new TitledBorder(UIManager.getBorder("TitledBorder.border"), "System", TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 11), null);
		JPanel panel_system = new JPanel();
		panel_system.setBounds(5, 5, 455, 128);
		panel_system.setFont(new Font("Tahoma", Font.BOLD, 11));
		panel_system.setBorder(tborder);
		panel_system.setLayout(null);
		panel_main.add(panel_system);
		
		// Display user data path
		lblpathcaption = new JLabel("Data folder path");
		lblpathcaption.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblpathcaption.setBounds(17, 20, 116, 14);
		panel_system.add(lblpathcaption);

		tapath = new JTextArea();
		tapath.setEditable(false);
		tapath.setBorder(new LineBorder(new Color(192, 192, 192)));
		tapath.setBounds(134, 15, 285, 22);
		panel_system.add(tapath);

		// Settings check boxes
		cbStartup = new JCheckBox("Launch at startup");
		cbStartup.setEnabled(false);
		cbStartup.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cbStartup.setBounds(13, 41, 200, 23);
		panel_system.add(cbStartup);
		// It has it own listener to disable startmini when not launched at startup 
		cbStartup.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				cbStartMini.setEnabled(cbStartup.isSelected());
			}
		});			
			
		cbChknewver = new JCheckBox("Search update");
		cbChknewver.setEnabled(true);
		cbChknewver.setSelected(false);
		cbChknewver.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cbChknewver.setBounds(225, 41, 200, 23);
		panel_system.add(cbChknewver);
		
		cbStartMini = new JCheckBox("Start minimized");
		cbStartMini.setEnabled(false);
		cbStartMini.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cbStartMini.setBounds(13, 63, 200, 23);
		panel_system.add(cbStartMini);
			
		cbPos = new JCheckBox("Save location");
		cbPos.setFont(new Font("Tahoma", Font.PLAIN, 11));
		cbPos.setBounds(225, 63, 200, 23);
		panel_system.add(cbPos);

		// combo action listener
		CBAL = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int ndx = cblanguage.getSelectedIndex();
				if (ndx == 0) {
					// still locale but its language can have changed
					curlang="";
					lang_props = bbutils.set_language("/resources/locale/", "en");
				}
				else {
					// not locale
					curlang = lnglist.get(ndx)[0];
					lang_props = bbutils.set_language("/resources/locale/", "en",  curlang);
				}
				// needed if the locale has changed
				cbmodel.removeElementAt(0);
				cbmodel.insertElementAt(bbutils.lngStr(lang_props, "default_lang", "English"), 0);
				cblanguage.setSelectedIndex(ndx);
			}
		};
		
		// combo language initialization
		cbmodel = new DefaultComboBoxModel <String> ();
		cblanguage = new JComboBox <String> (cbmodel);
		cblanguage.setFont(new Font("Tahoma", Font.PLAIN, 12));
		cblanguage.setMaximumRowCount(5);
		cblanguage.setBounds(17, 95, 150, 20);
		panel_system.add(cblanguage);
		
		//now, put languages in the list
		//for (int i = 0; i < lnglist.size(); i++) cbmodel.addElement(lnglist.get(i)[1]);
		// Only after initialization
		cblanguage.addActionListener(CBAL);
			
	
		// Buttons pane
		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
		GridBagConstraints gbc_buttonPane = new GridBagConstraints();
		gbc_buttonPane.anchor = GridBagConstraints.NORTH;
		gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonPane.gridx = 0;
		gbc_buttonPane.gridy = 1;
		getContentPane().add(buttonPane, gbc_buttonPane);
			
		// Buttons action lsitener 
			
		ActionListener BTAL = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String acmd = arg0.getActionCommand();
				// OKbutton pressed
				if (acmd.equalsIgnoreCase("okButton")) {
					savepos = cbPos.isSelected();
					chknewver = cbChknewver.isSelected();
					if (loadstart != cbStartup.isSelected()) {
						loadstart= cbStartup.isSelected();
						startmini= cbStartMini.isSelected();
						if (loadstart) processStartup(true, startmini);
						else processStartup(false, startmini);
					}
					// load at startup not changed but startmini has changed
					// we change startup only if it is activated
					else if (startmini != cbStartMini.isSelected()) {
						startmini= cbStartMini.isSelected();
						if (loadstart)  processStartup(true, startmini);
					}
					setVisible(false);
				}
				//CancelButton pressed
				if (acmd.equalsIgnoreCase("cancelButton")) {
					cbPos.setSelected(savepos) ;
					cbChknewver.setSelected(chknewver);
					cbStartMini.setSelected(startmini);
					cbStartup.setSelected(loadstart);
					dataDirectory= olddataDirectory;
					String wd = dataDirectory.replace('\\', '/');
		    		tapath.setText(wd);
		    		// In case the text is too long, set tooltip
		    		tapath.setToolTipText(wd);						
					dispose();
				}
				// data path button
				
				if (acmd.equalsIgnoreCase("btndatapath")) {
					JFileChooser j = new JFileChooser();
					j.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					j.setAcceptAllFileFilterUsed(false);
					j.setFileFilter(new FileNameExtensionFilter(chooser_filter, "*"));
					j.setDialogTitle(chooser_title);
					Integer opt = j.showOpenDialog(null);
					if (opt == JFileChooser.APPROVE_OPTION) {
					olddataDirectory = dataDirectory;
					dataDirectory= j.getSelectedFile().getAbsolutePath();	
					String wd = dataDirectory.replace('\\', '/');
		    		tapath.setText(wd);
		    		// In case the text is too long, set tooltip
		    		tapath.setToolTipText(wd);	
				}

					//System.out.println(j.getSelectedFile());
				}
			}
		};	

		// OK button
		okButton = new JButton("OK");
		okButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		okButton.setActionCommand("okButton");
		okButton.setPreferredSize(new Dimension(65, 23));
		okButton.setMinimumSize(new Dimension(65, 23));
		okButton.setMaximumSize(new Dimension(65, 23));
		buttonPane.add(okButton);
		okButton.addActionListener(BTAL);
			
		// Cancel button
		cancelButton = new JButton("Cancel");
		cancelButton.setMargin(new Insets(2, 5, 2, 5));
		cancelButton.setActionCommand("cancelButton");
		cancelButton.setFont(new Font("Tahoma", Font.BOLD, 11));
		cancelButton.setPreferredSize(new Dimension(65, 23));
		cancelButton.setMinimumSize(new Dimension(65, 23));
		cancelButton.setMaximumSize(new Dimension(65, 23));
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(BTAL);
			
		getRootPane().setDefaultButton(okButton);
			
		/*btndatapath = new JButton("");
		btndatapath.setDisabledIcon(new ImageIcon(dlgConfig.class.getResource("/resources/images/openpath_d.png")));
		btndatapath.setIcon(new ImageIcon(dlgConfig.class.getResource("/resources/images/openpath_e.png")));
		btndatapath.setToolTipText("Browse to open a file");
		btndatapath.setActionCommand("btndatapath");
		btndatapath.setBounds(425, 13, 24, 24);
		panel_system.add(btndatapath);
		btndatapath.addActionListener(BTAL);*/

		// Satus bar panel
		JPanel panel_status = new JPanel();
		panel_status.setBounds(new Rectangle(0, 0, 0, 13));
		FlowLayout fl_panel_status = (FlowLayout) panel_status.getLayout();
		fl_panel_status.setAlignment(FlowLayout.LEFT);
		GridBagConstraints gbc_panel_status = new GridBagConstraints();
		gbc_panel_status.anchor = GridBagConstraints.SOUTH;
		gbc_panel_status.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_status.gridx = 0;
		gbc_panel_status.gridy = 2;
		getContentPane().add(panel_status, gbc_panel_status);
				
		lblStatus = new JLabel("status");
		lblStatus.setPreferredSize(new Dimension(435, 12));
		lblStatus.setBounds(new Rectangle(0, 0, 0, 15));
		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblStatus.setHorizontalTextPosition(SwingConstants.LEFT);
		lblStatus.setHorizontalAlignment(SwingConstants.LEFT);
		panel_status.add(lblStatus);
				
	} // end constructor dlgConfig
	
	// Find and/or create the configuration directory
	public void set_config_file(String configName) {
		
		config_file = configName;
		// Exec directory
		execDirectory= ClassLoader.getSystemClassLoader().getResource(".").getPath();
		try {
			execDirectory = URLDecoder.decode(execDirectory, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// exec directory is undetermined
		}
		
		// Working directory
		String os = System.getProperty("os.name");
		OS = os.toUpperCase();
		// icon filename
		iconFile= "rectv.png";
		if (OS.contains("WIN")) {
			workingDirectory = System.getenv("AppData");	// Win location of the "AppData" folder
			// remove leading "/" if windows
			try {
				if (execDirectory.charAt(0)=='/') execDirectory= execDirectory.substring(1);
				iconFile= "rectv.ico";
			} catch (Exception e) {
				iconFile="";
			}
		}
		else if (OS.contains("MAC")) {
			workingDirectory = System.getProperty("user.home")+"/Library/Application Support"; // Mac, look for "Application Support"
			iconFile= "rectv.icns";
		}
		else workingDirectory = System.getProperty("user.home"); //Otherwise, we assume Linux
		
		workingDirectory += "/Rectv";	
		
		// check where is the config file
		File f = new File(config_file);
		
		if (!f.exists()) {
			// not in current directory
			f = new File (workingDirectory+"/"+config_file);
			if (f.exists()) config_file= workingDirectory+"/"+config_file;
			else {
				File folderExisting = new File(workingDirectory);
				if (!folderExisting.exists()) {
					boolean success = (new File(workingDirectory)).mkdirs();
					if (success) config_file= workingDirectory+"/"+config_file;
					else JOptionPane.showMessageDialog(null, bbutils.lngStr(lang_props, "dlgconfig_err_appfolder", "Cannot create the application folder")); 
				}
				else config_file= workingDirectory+"/"+config_file;
			}
		}
		
		// Write icon in user data path
		if (!(new File(workingDirectory+"/"+iconFile).exists())) {
			try {
				InputStream bpng = ClassLoader.class.getResourceAsStream("/resources/images/"+iconFile);
				FileOutputStream fpng = new FileOutputStream(new File(workingDirectory+"/"+iconFile));
				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = bpng.read(bytes)) != -1) {
					fpng.write(bytes, 0, read);
				}
				fpng.close();
			} catch (Exception e1) {
				// Do nothing
			}
		}	
		// Set status text
		String st = os;
		st += " v"+System.getProperty("os.version");
		st += " ("+System.getProperty("os.arch")+")";
		st += " - Java runtime v"+System.getProperty("java.version");
		
		lblStatus.setText(st);
		// temp directory
		tempDirectory= System.getProperty("java.io.tmpdir");
		
		// Read version infos properties 
		Properties versinfo = new Properties();
		try {
			InputStream in = rectv.class.getResourceAsStream("/resources/version.properties");
			versinfo.load(in);
			version =versinfo.getProperty("Specification-Version", "n/a")+"."+versinfo.getProperty("build.number", "n/a");
			build = versinfo.getProperty("Implementation-Version");
			String bldate = versinfo.getProperty("Build-Date","");
			vendor = versinfo.getProperty("Specification-Vendor","");
			in.close();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			builddate= formatter.parse(bldate);
		} catch (Exception e) {
			builddate = new Date();
		}
	}// end version info properties
	
		
	// Read XML configuration file
	public boolean loadConfigXML() {

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
		    Document configXML = builder.parse(new File(config_file));
		    // Here we get the root element of XML 
		    // to check if we are in the proper file
	        Element rootElement = configXML.getDocumentElement();
	        if (!rootElement.getAttribute("name").equals("rectv")) throw new Exception();
	        NodeList list = rootElement.getChildNodes();
	        for (int i = 0; i < list.getLength(); i++) {
	            Node cNode = list.item(i);
	            if (cNode.getNodeType() == Node.ELEMENT_NODE) {
	            	String s = cNode.getTextContent();
	            	//System.out.println(s);
	            	if (cNode.getNodeName().equals("savePos")) {
	            		savepos = s.equalsIgnoreCase("true");
	            		cbPos.setSelected(savepos);
	            	}
	            	else if (cNode.getNodeName().equals("locatX")) location.x = Integer.parseInt(s);
	            	else if (cNode.getNodeName().equals("locatY")) location.y = Integer.parseInt(s);
	            	else if (cNode.getNodeName().equals("sizeW")) size.width = Integer.parseInt(s);
	            	else if (cNode.getNodeName().equals("sizeH")) size.height = Integer.parseInt(s); 
	            	else if (cNode.getNodeName().equals("saveState")) saveState = Integer.parseInt(s); 
	            	
	            	else if (cNode.getNodeName().equals("chknewver")) {
	            		chknewver= s.equalsIgnoreCase("true");
	            		cbChknewver.setSelected(chknewver);
	            	}
	            	else if (cNode.getNodeName().equals("lastupdchk")) {
	            			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	            		try {
	            			lastupdchk= formatter.parse(s);
						} catch (Exception e) {
							// date absent or invalid
							lastupdchk= formatter.parse("2013-10-01");
						}
	            	}
	            /*	else if (cNode.getNodeName().equals("loadstart")) {
	            		loadstart = s.equalsIgnoreCase("true");
	            		cbStartup.setSelected(loadstart);
	            	}
	            	else if (cNode.getNodeName().equals("startmini")) {
	            		startmini = s.equalsIgnoreCase("true");
	            		cbStartMini.setSelected(startmini);
	            	}
	            	else if (cNode.getNodeName().equals("language")) curlang = s;*/
	            	else if  (cNode.getNodeName().equals("datapath")) dataDirectory= s;
	            	else if (cNode.getNodeName().equals("sourcepath")) sourceDirectory= s;	 
	            	else if (cNode.getNodeName().equals("destpath")) destDirectory= s;	 
	            	}
	        }
	      
	        File f = new File(dataDirectory);
    		if (!f.exists()) dataDirectory= workingDirectory;
    		dataDirectory= workingDirectory;
    		olddataDirectory=  dataDirectory;
    		// display path
    		String wd = dataDirectory.replace('\\', '/');
    		tapath.setText(wd);
    		// In case the text is too long, set tooltip
    		tapath.setToolTipText(wd);
       } catch (Exception e) {
    	   // config file erreur ou pas encore créé; working directory par défaut 
    	   dataDirectory= workingDirectory;
			return false;
		}
		/*if (curlang.length() == 0) {
			lang_props = bbutils.set_language("/resources/locale/", "en");
			cblanguage.setSelectedIndex(0);
		}
		else {
			lang_props = bbutils.set_language("/resources/locale/", "en", curlang);
			for (int i=0; i < lnglist.size(); i++) {
				if (curlang.equalsIgnoreCase(lnglist.get(i)[0])) cblanguage.setSelectedIndex(i);
			}
		}*/
	
	  return true;	
	}
	
	// Save config file to XML
	public boolean saveConfigXML()  {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    // create a new XML document
			DocumentBuilder builder = factory.newDocumentBuilder();
		    Document configXML = builder.newDocument();
		    // To identify right config
		        Element el = configXML.createElement("config");
		        el.setAttribute("name", "rectv");
		        el.appendChild(createXMLEntry(configXML, "savePos", "boolean", savepos));
		        el.appendChild(createXMLEntry(configXML,"locatX", "int",location.x));
				el.appendChild(createXMLEntry(configXML,"locatY", "int",location.y));
				el.appendChild(createXMLEntry(configXML,"sizeW", "int",size.width));
				el.appendChild(createXMLEntry(configXML,"sizeH", "int",size.height));
				el.appendChild(createXMLEntry(configXML,"saveState", "int", saveState)); 
				el.appendChild(createXMLEntry(configXML,"chknewver", "boolean", chknewver));
				el.appendChild(createXMLEntry(configXML,"lastupdchk", "string", formatter.format(lastupdchk)));
				//el.appendChild(createXMLEntry(configXML,"loadstart", "boolean", loadstart));
				//el.appendChild(createXMLEntry(configXML,"startmini", "boolean", startmini));
				//el.appendChild(createXMLEntry(configXML,"language", "string", curlang));
				el.appendChild(createXMLEntry(configXML,"datapath", "string", dataDirectory));
				el.appendChild(createXMLEntry(configXML,"sourcepath", "string", sourceDirectory));
				el.appendChild(createXMLEntry(configXML,"destpath", "string", destDirectory));
				configXML.appendChild(el);
		        // The XML document we created above is still in memory
		        //  create DOM source, then save it to file
		        DOMSource source = new DOMSource(configXML);
		        try {
					PrintStream ps = new PrintStream(config_file);
					StreamResult result = new StreamResult(ps);
					TransformerFactory transformerFactory = TransformerFactory.newInstance();
					Transformer transformer = transformerFactory.newTransformer(); 
					transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // line breaks			
					transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // indent
					transformer.transform(source, result);
				} catch (Exception e) {
					return false;
				}
		} catch (ParserConfigurationException e) {
			return false;
		}
		return true;
	}
	
	private Element createXMLEntry(Document xml, String name, String type, Object value) {
		Element e = xml.createElement(name);
		e.setAttribute("type", type);
		String svalue= String.valueOf(value);
		e.setTextContent(svalue);
		return  e;
	}
	
	private void processStartup(boolean load, boolean minimized){
	/*	if (load) {
			String progname = "rectv.jar";
			String param = "";
			if (minimized) param= "MINI" ;
			// Windows. Create a shortcut in user startup
			// Todo check if jar or exe
			if (OS.contains("WIN")) {
				// Check if exe or jar
				File f = new File(execDirectory+"/rectv.exe");
				if (f.exists())  progname = "rectv.exe";
				shortcut.createWinShortcut(shortcut.sh_Type.U_STARTUP, execDirectory,progname, param, "contact_manager.lnk", workingDirectory+"/"+iconFile) ;	
			}
			else if (OS.contains("NUX")) {
				shortcut.createLinuxShortcut(execDirectory, progname, param, "contact_manager.desktop", workingDirectory+"/"+iconFile);
			}
			else if (OS.contains("MAC")) {
				shortcut.createOSXShortcut(execDirectory, progname, param, "com.sdtp.contact_manager", workingDirectory+"/"+iconFile);
			}
		}
		else {
			
			if (OS.contains("WIN")) {
				shortcut.deleteWinShortcut(shortcut.sh_Type.U_STARTUP, "contact_manager.lnk");
			}
			else if (OS.contains("NUX")) {
				shortcut.deleteLinuxShortcut("contact_manager.desktop");
			}
			else if (OS.contains("MAC")) {
				shortcut.deleteOSXShortcut("com.sdtp.contact_manager");
			}
		}*/
		
	
	}
}