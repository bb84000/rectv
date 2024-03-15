package mergets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultCaret;
import bb.aboutbox.aboutBox;
import bb.utils.chknewversion;



public class rectv extends JPanel
implements ActionListener, 
PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private static JFrame frmMergets;
	private static dlgConfig config;
	private static Boolean saved = false;
	private static class tspacket { 
		public int syncbyte;
		public int pid;
		public int adapfield;
		public boolean pcrflag;
		public long pcr;
		public long pcrtime;
		public tspacket(int fsyncbyte, int fpid, int fadapfield, boolean fpcrflag, int fpcr, int fpcrtime) {
			syncbyte= fsyncbyte;
			pid= fpid;	
			adapfield= fadapfield;
			pcrflag= fpcrflag;
			pcr=fpcr;
			pcrtime=fpcrtime;	
		}
	}; 
	private JProgressBar progressBar1;
	private JProgressBar progressBar2;
	private JButton btnmerge;
	private JTextArea talog;
	private Task task;
	private JLabel lbltsfiles;
	private JLabel lbldestfile;
	private JFileChooser chooser;
	private JButton btnopenfile;
	private JButton btnsavefile;
	private File[] tsfiles;
	private File destfile = new File("merged.ts");
	private tspacket mypacket;
	private String slog;
	private int progress2;
    private static String sourceDirectory;
    private static String destDirectory;
    private JPopupMenu popupMenu;
    private JMenuItem mntmAPropos;
 	private static aboutBox about;
 	private static Image MainIcon;

	class Task extends SwingWorker<Void, Void> {
		/*
		 * Main task. Executed in background thread.
		 */

		@Override
		public Void doInBackground()  {
			byte[] mypaq = new byte[188];; //Array [0..187] of Byte;
			long pcrbase;
			long lastpcr;
			long tmp;
			long tssize;
			long beginpos;
			long endpos;
			int progress = 0;
			BufferedInputStream fis;
			FileOutputStream fos;
			FileChannel outChan;

			slog="";
			if (destfile.exists()) {
				int result = JOptionPane.showConfirmDialog(null, "Le fichier existe, le remplacer ?",
						"Fichier existant", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					slog= "Fichier "+destfile.getName()+" existant. Fusion abandonnée.\n";
					return null;
				}
			}
			try {
				long startTime = System.currentTimeMillis();

				mypacket = new tspacket(0, 0, 0, false, 0, 0);
				pcrbase=0;
				lastpcr = 0;
				endpos= 0;
				long filesize=0;

				//destfile= new File("merge.ts");
				fos = new FileOutputStream(destfile);
				outChan = fos.getChannel();

				for (int y = 0; y < tsfiles.length; y = y + 1) {
					beginpos = 0;
					boolean pcrbeg = false;
					talog.append("Analyse du fichier "+tsfiles[y].getName()+"\n");
					progress2=y;
					setProgress(0);
					int read = 0;
					int pos = 0; 
					fis = new BufferedInputStream(new FileInputStream(tsfiles[y]));
					tssize= fis.available() /188;  
					//System.out.println(y);

					for(int x = 0; x < tssize; x = x + 1) {
						read = fis.read(mypaq, 0, 188);
						mypacket.adapfield= (mypaq[3]&0xff & 0x30) >> 4;
					if (mypacket.adapfield > 1) {  
						mypacket.pcrflag= (((mypaq[5] & 0x10) >> 4) != 0);
						if (mypacket.pcrflag) {
							endpos= pos;
							// Calcul de la valeur du PCR
							tmp=0;
						    for (int i = 6; i < 11; i++) {
						        tmp <<= 8;
						        tmp |= (mypaq[i] & 0xFF);
						    }
							mypacket.pcr= tmp >> 7; //33 bits
							
							//tmp= (mypaq[6]&0xff)*0x1000000+(mypaq[7]&0xff)*0x10000+(mypaq[8]&0xff)*0x100+(mypaq[9]&0xff) ;
							//mypacket.pcr= (tmp*0x100+(mypaq[10]&0xff)) >> 7; //33 bits
							pcrbase= mypacket.pcr;
							if ((y > 0) && (!pcrbeg) && (pcrbase == lastpcr)) {  
								beginpos= pos;
								pcrbeg= true;
								mypacket.pcrtime= (pcrbase / 90); // convert to milliseconds

								String hms = millitotime (mypacket.pcrtime );
								//System.out.println("value : "+hms);
								slog= "Raccord du fichier "+tsfiles[y].getName()+" au PCR : "+hms+"\n";
							}
						}
					} 
					if (y==0 || pcrbeg) fos.write(mypaq, 0, 188); 
					// if (y==0 || pcrbeg) outChan.write(bpaq); 
					pos+= read;
					// thread interupt pour afficher le bargraph 
					int n = (int) (tssize /1000);
					if (x % n == 0) { 
						try {
							Thread.sleep(0,1);
						} catch (InterruptedException ignore) {}
						//Make random progress.
						progress = (int) (x*100 / tssize);
						setProgress(progress+1);
					}
					}

					lastpcr= pcrbase; 
					talog.append("Dernier PCR du fichier "+tsfiles[y].getName()+" :"+ millitotime((int) (lastpcr/90))+"\n");

					filesize += endpos-beginpos;
					// on tronque le fichier
					//outChan.position(filesize);
					outChan.truncate(filesize);
					fis.close();
				}		    
				fos.flush();
				outChan.close();
				fos.close(); 
				long endTime = System.currentTimeMillis();		    
				talog.append("Fusion terminée en "+millitotime((int) (endTime-startTime), false)+".\nFichier fusionné : "+destfile.getAbsolutePath()+".\n");

				;
				FileWriter fw = new FileWriter("merge.log", true);
				talog.write(fw);
				fw.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    



			return null;
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			btnmerge.setEnabled(true);
			setCursor(null); //turn off the wait cursor
			talog.append(slog);
		}
	}

	
	public String millitotime(long pcrtime) {

		return (millitotime(pcrtime, true));
	}

	public String millitotime(long millis, Boolean dispzerohour){
		int hr = (int) (millis/(3600*1000));
		int mn = (int) (millis/(60*1000) % 60);
		int sec = (int) (millis/1000 % 60);
		int mil = (int) (millis %1000);
		if (hr == 0 && !dispzerohour ){
			return String.format("%02d:%02d,%03d", mn, sec, mil);
		} else {
			return String.format("%02d:%02d:%02d,%03d", hr, mn, sec, mil);
		}
	}

	public rectv() {
		super(new BorderLayout());
		setAlignmentY(0.0f);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setPreferredSize(new Dimension(600, 350));

		//Create the demo's UI.
		JLabel lblfichierts = new JLabel("Premier fichier TS");
		lblfichierts.setBounds(0, 12, 96, 15);
		lblfichierts.setFont(new Font("Tahoma", Font.PLAIN, 12));


		btnmerge = new JButton("Fusionner");
		btnmerge.setEnabled(false);
		btnmerge.setBounds(223, 11, 90, 23);
		btnmerge.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnmerge.setActionCommand("start");
		btnmerge.addActionListener(this);

		progressBar1 = new JProgressBar(0, 100);
		progressBar1.setBounds(0, 69, 580, 17);
		progressBar1.setValue(0);
		progressBar1.setStringPainted(true);

		talog = new JTextArea(5, 20);
		talog.setMargin(new Insets(5,5,5,5));
		talog.setEditable(false);
		DefaultCaret caret = (DefaultCaret)talog.getCaret();
		caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);

		JPanel paneltop = new JPanel();	
		paneltop.setPreferredSize(new Dimension(500, 120));
		paneltop.setBounds(new Rectangle(0, 0, 500, 120));
		paneltop.setLayout(null);
		// panel.setLayout(null);

		paneltop.add(lblfichierts);

		paneltop.add(progressBar1);

		add(paneltop, BorderLayout.PAGE_START);

		JLabel lblFichierFusion = new JLabel("Fichier fusionn\u00E9");
		lblFichierFusion.setBounds(0, 36, 96, 15);
		lblFichierFusion.setFont(new Font("Tahoma", Font.PLAIN, 12));
		paneltop.add(lblFichierFusion);

		lbltsfiles = new JLabel("");
		lbltsfiles.setBounds(106, 8, 433, 22);
		lbltsfiles.setOpaque(true);
		lbltsfiles.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbltsfiles.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lbltsfiles.setBackground(Color.WHITE);
		paneltop.add(lbltsfiles);

		lbldestfile = new JLabel("merged.ts");
		lbldestfile.setBounds(106, 32, 433, 22);
		lbldestfile.setOpaque(true);
		lbldestfile.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lbldestfile.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lbldestfile.setBackground(Color.WHITE);
		paneltop.add(lbldestfile);

		// FileOpen button
		btnopenfile = new JButton(""); 
		btnopenfile.setBounds(549, 7, 24, 24);
		btnopenfile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				opentsfiles();
			}
		});
		btnopenfile.setIcon(new ImageIcon(rectv.class.getResource("/resources/fileopen.png")));
		paneltop.add(btnopenfile);

		// Dialogue de sélection TS
		
		//try {
		//	chooser = new JFileChooser(sourceDirectory);
		//} catch (Exception e) {
			chooser = new JFileChooser();
		//}
			
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Fichiers vidéo TS", "ts");
		chooser.setFileFilter(filter);

		// Filesave button
		btnsavefile = new JButton("");
		btnsavefile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//savetsfile();
				savetsfile();
			}
		});
		btnsavefile.setBounds(549, 31, 24, 24);
		btnsavefile.setIcon(new ImageIcon(rectv.class.getResource("/resources/filesave.png")));
		paneltop.add(btnsavefile);

		progressBar2 = new JProgressBar(0, 100);
		progressBar2.setValue(0);
		progressBar2.setStringPainted(true);
		progressBar2.setBounds(0, 92, 580, 17);
		paneltop.add(progressBar2);

		JScrollPane panelog =  new JScrollPane(talog);
		
		popupMenu = new JPopupMenu();
		addPopup(talog, popupMenu);
		
		JMenuItem mntmConfiguration = new JMenuItem("Configuration");
		mntmConfiguration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setLocation(frmMergets.getLocation().x+(frmMergets.getWidth()-config.getWidth())/2, 
						          frmMergets.getLocation().y+(frmMergets.getHeight()-config.getHeight())/2) ;
				config.setVisible(true);
			}
		});
		popupMenu.add(mntmConfiguration);
		
		mntmAPropos = new JMenuItem("A propos");
		mntmAPropos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Centrage sur la fenêtre principale
				about.setLocation(frmMergets.getLocation().x+(frmMergets.getWidth()-about.getWidth())/2, 
						          frmMergets.getLocation().y+(frmMergets.getHeight()-about.getHeight())/2) ;
				about.setVisible(true);
			}
		});
		popupMenu.add(mntmAPropos);
		//add(talog), BorderLayout.CENTER);
		add(panelog, BorderLayout.CENTER);
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel panelbottom = new JPanel();
		panelbottom.setBounds(new Rectangle(0, 0, 500, 45));
		panelbottom.setPreferredSize(new Dimension(500, 45));
		add (panelbottom, BorderLayout.PAGE_END);
		panelbottom.setLayout(null);
		panelbottom.add(btnmerge);

		
	}

	private static void initialize() {
		config = new dlgConfig(frmMergets);
		config.set_config_file("rectv.config.xml");
		MainIcon = Toolkit.getDefaultToolkit().getImage(rectv.class.getResource("/resources/rectv.png"));
		//MainIcon = (new ImageIcon(rectv.class.getResource("/resources/rectv.png"))).getImage();
	    //MainIcon= rectv.getClass().getResource("/resources/rectv/rectv.png");
		try {
			config.loadConfigXML();
			config.setIconImage(MainIcon);
			sourceDirectory = config.sourceDirectory; 
			//System.out.println(sourceDirectory);
			destDirectory = config.destDirectory; 
			if (config.savepos) frmMergets.setLocation(config.location);
		} catch (Exception e1) {
			//Do nothing 
		}
		
		// About dialog implementationVersion
				about = new aboutBox(frmMergets);
				about.setProgram("Fusion de fichiers TS", "A propos de ");
				about.setVersion(String.format("Version %s", config.version ));
				about.setURLupdate("Recherche de mise à jour", "www.sdtp.com/versions/version.php?program=rectv&version="+config.version, "Dernière recherche de mise à jour", config.lastupdchk);
				about.setVendor(config.vendor, config.builddate);
				about.setWebsite("Site Web","www.sdtp.com" );
		
				// Version check if Config.nochknewverck false and 7 days after previous one
		if (config.chknewver) {
			Calendar cal = Calendar.getInstance(); 
			cal.setTime(config.lastupdchk); 
			cal.add(Calendar.DATE, 7);
			Date nextupd = cal.getTime();
			Date now = new Date();
		  if (nextupd.before(now)) {
				config.lastupdchk = new Date ();
				chknewversion.setProgname("Calendrier");
				chknewversion.setVersionURL("http://www.sdtp.com/versions/versions.csv");
        		chknewversion.setUpdateURL(about.urlUpdate);
				chknewversion.getLastVersion("rectv",  config.version+"."+config.build);
				//chknewversion.getLastVersion("jcalendrier", "0.5.0.0");
			}
		}

		

	}

	
	/**
	 * Invoked when the user presses the start button.
	 */
	public void actionPerformed(ActionEvent evt) {
		btnmerge.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		//Instances of javax.swing.SwingWorker are not reusuable, so
		//we create new instances as needed.
		task = new Task();
		task.addPropertyChangeListener(this);
		task.execute();

	}

	/**
	 * Invoked when task's progress property changes.
	 */
	public void propertyChange(PropertyChangeEvent evt) {

		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			progressBar1.setValue(progress);

			progressBar2.setValue((100*progress2+ progress)/tsfiles.length);
			//talog.append(String.format(
			//        "Completed %d%% of task.\n", task.getProgress()));

			talog.append(slog);
			slog="";

		} 
	}


	/**
	 * Create the GUI and show it. As with all GUI code, this must run
	 * on the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		MainIcon = Toolkit.getDefaultToolkit().getImage(rectv.class.getResource("/resources/rectv.png"));
		frmMergets = new JFrame("Fusion de fichiers TS");
		frmMergets.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		//Create and set up the content pane.
		JComponent newContentPane = new rectv();
		newContentPane.setOpaque(true); //content panes must be opaque
		frmMergets.setContentPane(newContentPane);
		frmMergets.setResizable(false);
		frmMergets.setIconImage(MainIcon);
		//Display the window.
		frmMergets.pack();
		frmMergets.setVisible(true);
		frmMergets.setLocationRelativeTo(null);  // *** this will center your app ***
		initialize();
		frmMergets.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				closeApp();
			}
		});
	}

	
	
	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public static void closeApp() {
	    
		config.location = frmMergets.getLocation();
		config.size = frmMergets.getSize();
		config.saveState= frmMergets.getState();
		config.sourceDirectory= sourceDirectory;
		config.destDirectory= destDirectory;
		config.saveConfigXML();

		 
		
		
	}

	// Sélection des fichiers TS à fusionner

	private void opentsfiles() {
		//System.out.println(sourceDirectory);
		
		chooser.setDialogTitle("Sélectionnez les fichiers TS à fusionner");
		chooser.setMultiSelectionEnabled(true);
		try {
		   File dir = new File(sourceDirectory);
		   chooser.setCurrentDirectory(dir);
		} catch (Exception e) {
			// DO nothing
		}
		int returnVal = chooser.showOpenDialog(btnopenfile);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			tsfiles = chooser.getSelectedFiles();
			lbltsfiles.setText(chooser.getSelectedFile().getAbsolutePath());;
			talog.setText("Fichiers à fusionner :\n");
			for (File element:tsfiles) {
				talog.append(element.getAbsolutePath()+"\n");
			}
			btnmerge.setEnabled(true);
			sourceDirectory= chooser.getSelectedFile().getAbsoluteFile().getParent();
			//System.out.println(sourceDirectory);
		}
	}

	// Détermination du fichier fusionné
	private void savetsfile() {
		chooser.setDialogTitle("Entrez le nom du fichier fusionné");
		chooser.setMultiSelectionEnabled(false);
		
		chooser.setSelectedFile(destfile);
		try {
			File dir = new File(destDirectory);
			chooser.setCurrentDirectory(dir);
		} catch (Exception e) {
			// DO nothing
		}
		int returnVal = chooser.showSaveDialog(btnopenfile);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			String destname = chooser.getSelectedFile().getAbsolutePath();
			int i = destname.lastIndexOf('.');
			if 	(i==-1) {
				destname= destname+".ts";
			}
			destfile= new File (destname);
			lbldestfile.setText(destname);
			destDirectory= chooser.getCurrentDirectory().getAbsolutePath();
			System.out.println(destDirectory);
		}	

	}

	public void tssavefile()  {
		boolean acceptable = false;
		String destname = null;
		chooser.setDialogTitle("Entrez le nom du fichier fusionné");
		chooser.setMultiSelectionEnabled(false);
		chooser.setSelectedFile(destfile);
		try {
			do {
				destname = null;
				File f = null;
				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					destname = chooser.getSelectedFile().getAbsolutePath();
					f = chooser.getSelectedFile();
					if (f.exists()) {
						int result = JOptionPane.showConfirmDialog(this, "Le fichier existe, le remplacer ?",
								"Fichier existant", JOptionPane.YES_NO_CANCEL_OPTION);
						if (result == JOptionPane.YES_OPTION) {
							acceptable = true;
						}
					} else {
						acceptable = true;
					}
				} else {
					acceptable = true;
				}
			} while (!acceptable);
		} catch (HeadlessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (destname != null) {
			destfile= new File(destname);
			lbldestfile.setText(destname);
			talog.setText("Fichier fusionné :\n");
			talog.append(destname+"\n");
		}
	} 
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
