package bb.aboutbox;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;




import java.awt.Font;

import bb.stretchicon.StretchIcon;
import bb.utils.*;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class aboutBox extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel lblicon;
	private JLabel lblprogname;
	private JLabel lblVersion;
	private JLabel lblvendor;
	private JLabel lblsitecaption;
	private JLabel lblWebsite;
	private JLabel lblChkUpdate;
	private String updToolTip;
	public String urlUpdate = "";
	public Date lstUpdate = null;
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			aboutBox dialog = new aboutBox(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public aboutBox(JFrame frm) {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent arg0) {
				
			}
		});
		setName("dlgAbout");
		setResizable(false);
		setBounds(100, 100, 360, 225);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 334, 165);
		contentPanel.add(panel);
		panel.setLayout(null);
		
		// program icon
		lblicon = new JLabel("");
		lblicon.setBounds(0, 0, 64, 64);
		panel.add(lblicon);
		
		// Program name 
		lblprogname = new JLabel("text");
		lblprogname.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblprogname.setHorizontalTextPosition(SwingConstants.CENTER);
		lblprogname.setHorizontalAlignment(SwingConstants.CENTER);
		lblprogname.setBounds(0, 11, 334, 14);
		panel.add(lblprogname);
		
		
		// Version
		lblVersion = new JLabel("text");
		lblVersion.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblVersion.setHorizontalTextPosition(SwingConstants.CENTER);
		lblVersion.setHorizontalAlignment(SwingConstants.CENTER);
		lblVersion.setBounds(0, 36, 334, 14);
		panel.add(lblVersion);
		
		
		lblvendor = new JLabel("text");
		lblvendor.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblvendor.setHorizontalTextPosition(SwingConstants.CENTER);
		lblvendor.setHorizontalAlignment(SwingConstants.CENTER);
		lblvendor.setBounds(0, 61, 334, 14);
		panel.add(lblvendor);
		
		JPanel panel_website = new JPanel();
		panel_website.setAlignmentY(0.0f);
		panel_website.setAlignmentX(0.0f);
		panel_website.setBounds(0, 86, 334, 14);
		panel.add(panel_website);
		((FlowLayout)panel_website.getLayout()).setVgap(0);
		
		lblsitecaption = new JLabel("Site Web :");
		lblsitecaption.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblsitecaption.setBounds(new Rectangle(0, 0, 0, 14));
		lblsitecaption.setHorizontalTextPosition(SwingConstants.CENTER);
		lblsitecaption.setAlignmentY(0.0f);
		panel_website.add(lblsitecaption);
		
		MouseListener ml = new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent ev) {
				// double clic event !
				if (ev.getClickCount()==2) {
					JLabel jl = (JLabel) ev.getComponent();
					try {
						if (jl.getName().equals("lblWebsite")) bbutils.openURL(jl.getText());
						else if (jl.getName().equals("lblChkUpdate")) {
							bbutils.openURL(urlUpdate);
							setLastUpdate(new Date());
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
					}
				}
			}
		};
		lblWebsite = new JLabel("New label");
		lblWebsite.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblWebsite.setName("lblWebsite");
		lblWebsite.setForeground(Color.BLUE);
		lblWebsite.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblWebsite.addMouseListener(ml);
		panel_website.add(lblWebsite);
		
		lblChkUpdate = new JLabel("Recherche de mise \u00E0 jour");
		lblChkUpdate.setName("lblChkUpdate");
		lblChkUpdate.setForeground(Color.BLUE);
		lblChkUpdate.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblChkUpdate.setHorizontalTextPosition(SwingConstants.CENTER);
		lblChkUpdate.setHorizontalAlignment(SwingConstants.CENTER);
		lblChkUpdate.setBounds(0, 111, 334, 14);
		lblChkUpdate.addMouseListener(ml);
		panel.add(lblChkUpdate);

		// Button pane
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						setVisible(false);
					}
				});
				okButton.setHorizontalTextPosition(SwingConstants.CENTER);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		
	}
	
	public void setImage (Image img) {
		setIconImage(img);
		lblicon.setIcon(new StretchIcon(img));
	}
	public void setProgram(String name, String title) {
		setTitle(title+name);
		lblprogname.setText(name);
	}
	
	public void setVersion (String ver) {
		lblVersion.setText(ver);
	}
	
	public void setVendor (String name, Date blddate) {
		String s0;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		try {
			s0 = " - ";
			s0 += formatter.format(blddate);
		} catch (Exception e1) {
			// invalid or no date
			s0="";
		}
		lblvendor.setText(name+s0);
	}
	
	public void setWebsite (String caption, String url) {
		lblsitecaption.setText(caption);
		lblWebsite.setText(url);
	}
	
	public void setURLupdate (String caption, String url, String tooltip, Date lastupdate) {
		lblChkUpdate.setText(caption);
		urlUpdate = url;
		updToolTip = tooltip;
		setLastUpdate(lastupdate);
	}
	
	public void setLastUpdate(Date lastupdate){ 
		String s0;
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		lstUpdate= lastupdate;
		try {
			s0 = " : ";
			s0 += formatter.format(lastupdate);
		} catch (Exception e1) {
			// invalid or no date
			s0="";
		}
		lblChkUpdate.setToolTipText(updToolTip+s0);
		
	}
}
