package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigurationWizardWrapper;

import icy.main.Icy;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import mmcorej.CMMCore;
import mmcorej.StrVector;

import org.micromanager.conf.ComPortsPage;
import org.micromanager.conf.DelayPage;
import org.micromanager.conf.EditPropertiesPage;
import org.micromanager.conf.LabelsPage;
import org.micromanager.conf.MicroscopeModel;
import org.micromanager.conf.PagePanel;
import org.micromanager.conf.PeripheralDevicesPage;
import org.micromanager.conf.PeripheralDevicesPreInitializationPropertiesPage;
import org.micromanager.conf.RolesPage;
import org.micromanager.conf.SynchroPage;
import org.micromanager.utils.HotKeys;
import org.micromanager.utils.HttpUtils;
import org.micromanager.utils.ReportingUtils;

public class ConfiguratorDialog extends JDialog {

	public ConfiguratorDialog(CMMCore core, String defFile) {
		curPage_ = 0;
		core_ = core;
		defaultPath_ = defFile;
		showSynchroPage_ = false;
		setDefaultCloseOperation(2);
		setModal(true);
		initialize();
	}

	private void initialize() {
		prefs_ = Preferences.userNodeForPackage(getClass());
		HotKeys.active_ = false;
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent arg0) {
				onCloseWindow();
			}
		});
		setResizable(false);
		getContentPane().setLayout(null);
		setTitle("Hardware Configuration Wizard");
		setBounds(50, 100, 602, 529);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(9, 320, 578, 136);
		getContentPane().add(scrollPane);
		helpTextPane_ = new JEditorPane();
		scrollPane.setViewportView(helpTextPane_);
		helpTextPane_.setEditable(false);
		helpTextPane_.setContentType("text/html; charset=ISO-8859-1");
		nextButton_ = new JButton();
		nextButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				if (curPage_ == pages_.length - 1) {
					pages_[curPage_].exitPage(true);
					onCloseWindow();
				} else {
					setPage(curPage_ + 1);
				}
			}
		});
		nextButton_.setText("Next >");
		nextButton_.setBounds(494, 462, 93, 23);
		getContentPane().add(nextButton_);
		getRootPane().setDefaultButton(nextButton_);
		backButton_ = new JButton();
		backButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				setPage(curPage_ - 1);
			}

		});
		backButton_.setText("< Back");
		backButton_.setBounds(395, 462, 93, 23);
		getContentPane().add(backButton_);
		pagesLabel_ = new JLabel();
		pagesLabel_.setBorder(new LineBorder(Color.black, 1, false));
		pagesLabel_.setBounds(9, 28, 578, 286);
		getContentPane().add(pagesLabel_);
		pages_ = new PagePanel[showSynchroPage_ ? 11 : 10];
		int pageNumber = 0;
		pages_[pageNumber++] = new IntroPage(prefs_);
		pages_[pageNumber++] = new DevicesPage(prefs_);
		pages_[pageNumber++] = new EditPropertiesPage(prefs_);
		pages_[pageNumber++] = new ComPortsPage(prefs_);
		pages_[pageNumber++] = new PeripheralDevicesPage(prefs_);
		pages_[pageNumber++] = new PeripheralDevicesPreInitializationPropertiesPage(prefs_);
		pages_[pageNumber++] = new RolesPage(prefs_);
		pages_[pageNumber++] = new DelayPage(prefs_);
		if (showSynchroPage_)
			pages_[pageNumber++] = new SynchroPage(prefs_);
		pages_[pageNumber++] = new LabelsPage(prefs_);
		pages_[pageNumber++] = new FinishPage(prefs_);
		microModel_ = new MicroscopeModel();
		boolean bvalue = prefs_.getBoolean("CFG_Okay_To_Send", true);
		microModel_.setSendConfiguration(bvalue);
		microModel_.loadAvailableDeviceList(core_);
		microModel_.setFileName(defaultPath_);
		microModel_.scanComPorts(core_);
		java.awt.Rectangle r = pagesLabel_.getBounds();
		titleLabel_ = new JLabel();
		titleLabel_.setText("Title");
		titleLabel_.setBounds(9, 4, 578, 21);
		getContentPane().add(titleLabel_);
		for (int i = 0; i < pages_.length; i++)
			try {
				pages_[i].setModel(microModel_, core_);
				pages_[i].loadSettings();
				pages_[i].setBounds(r);
				pages_[i].setTitle((new StringBuilder()).append("Step ").append(i + 1).append(" of ").append(pages_.length).append(": ").append(pages_[i].getTitle()).toString());
				pages_[i].setParentDialog(this);
			} catch (Exception e) {
				ReportingUtils.logError(e);
			}

		setPage(0);
	}

	private void setPage(int i) {
		if (i > 0 && !pages_[curPage_].exitPage(curPage_ < i))
			return;
		int newPage = 0;
		if (i < 0)
			newPage = 0;
		else if (i >= pages_.length)
			newPage = pages_.length - 1;
		else
			newPage = i;
		if (!pages_[newPage].enterPage(curPage_ > newPage))
			return;
		getContentPane().remove(pages_[curPage_]);
		curPage_ = newPage;
		getContentPane().add(pages_[curPage_]);
		getContentPane().repaint();
		pages_[curPage_].refresh();
		if (curPage_ == 0)
			backButton_.setEnabled(false);
		else
			backButton_.setEnabled(true);
		if (curPage_ == pages_.length - 1)
			nextButton_.setText("Finish");
		else
			nextButton_.setText("Next >");
		titleLabel_.setText(pages_[curPage_].getTitle());
		helpTextPane_.setContentType("text/plain");
		helpTextPane_.setText(pages_[curPage_].getHelpText());
		String helpFileName;
		helpFileName = pages_[curPage_].getHelpFileName();
		if (helpFileName == null)
			return;
		String helpText = pages_[curPage_].getHelpText();
		helpTextPane_.setContentType("text/html; charset=ISO-8859-1");
		helpTextPane_.setText(helpText);
		return;
	}

	private String UploadCurrentConfigFile() {
		String returnValue = "";
		try {
			HttpUtils httpu = new HttpUtils();
			File conff = new File(getFileName());
			if (conff.exists()) {
				String prependedLine = "#";
				String qualifiedConfigFileName = "";
				try {
					SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
					qualifiedConfigFileName = (new StringBuilder()).append(qualifiedConfigFileName).append(df.format(new Date())).toString();
					String shortTZName = TimeZone.getDefault().getDisplayName(false, 0);
					qualifiedConfigFileName = (new StringBuilder()).append(qualifiedConfigFileName).append(shortTZName).toString();
					qualifiedConfigFileName = (new StringBuilder()).append(qualifiedConfigFileName).append("@").toString();
					try {
						String physicalAddress = "00-00-00-00-00-00";
						StrVector ss = core_.getMACAddresses();
						if (0L < ss.size()) {
							String pa2 = ss.get(0);
							if (null != pa2 && 0 < pa2.length())
								physicalAddress = pa2;
						}
						qualifiedConfigFileName = (new StringBuilder()).append(qualifiedConfigFileName).append(physicalAddress).toString();
						prependedLine = (new StringBuilder()).append(prependedLine).append("Host: ").append(InetAddress.getLocalHost().getHostName()).append(" ").toString();
					} catch (UnknownHostException e) {
					}
					prependedLine = (new StringBuilder()).append(prependedLine).append("User: ").append(core_.getUserId()).append(" configuration file: ").append(conff.getName()).append("\n")
							.toString();
				} catch (Throwable t) {
				}
				qualifiedConfigFileName.replace(':', '_');
				qualifiedConfigFileName.replace(';', '_');
				File fileToSend = new File(qualifiedConfigFileName);
				FileReader reader = new FileReader(conff);
				FileWriter writer = new FileWriter(fileToSend);
				writer.append(prependedLine);
				int c;
				while (-1 != (c = reader.read()))
					writer.write(c);
				try {
					reader.close();
				} catch (Exception e) {
					ReportingUtils.logError(e);
				}
				try {
					writer.close();
				} catch (Exception e) {
					ReportingUtils.logError(e);
				}
				try {
					URL url = new URL("http://valelab.ucsf.edu/~MM/upload_file.php");
					java.util.List<File> flist = new ArrayList<File>();
					flist.add(fileToSend);
					for (Iterator<File> i$ = flist.iterator(); i$.hasNext();) {
						Object o0 = i$.next();
						File f0 = (File) o0;
						try {
							httpu.upload(url, f0);
						} catch (UnknownHostException e) {
							returnValue = e.toString();
						} catch (IOException e) {
							returnValue = e.toString();
						} catch (SecurityException e) {
							returnValue = e.toString();
						} catch (Exception e) {
							returnValue = e.toString();
						}
					}

				} catch (MalformedURLException e) {
					returnValue = e.toString();
				}
				if (!fileToSend.delete())
					ReportingUtils.logError((new StringBuilder()).append("Couldn't delete temporary file ").append(qualifiedConfigFileName).toString());
			}
		} catch (IOException e) {
			returnValue = e.toString();
		}
		return returnValue;
	}

	private void onCloseWindow() {
		for (int i = 0; i < pages_.length; i++)
			pages_[i].saveSettings();

		if (microModel_.isModified()) {
			int result = JOptionPane.showConfirmDialog(this, "Save changes to the configuration file?\nIf you press YES you will get a chance to change the file name.", "Configurator", 1, 1);
			switch (result) {
			case 0: // '\0'
				saveConfiguration();
				break;

			case 2: // '\002'
				return;
			}
		}
		if (microModel_.getSendConfiguration()) {
			class _cls1Uploader extends Thread {

				public void run() {
					statusMessage_ = UploadCurrentConfigFile();
				}

				public String Status() {
					return statusMessage_;
				}

				private String statusMessage_;

				public _cls1Uploader() {
					statusMessage_ = "";
				}
			}

			_cls1Uploader u = new _cls1Uploader();
			u.start();
			try {
				u.join();
			} catch (InterruptedException ex) {
			}
			if (0 < u.Status().length()) {
				ReportingUtils.logError((new StringBuilder()).append("Error uploading configuration file: ").append(u.Status()).toString());
				ReportingUtils.showMessage((new StringBuilder()).append("Error uploading configuration file:\n").append(u.Status()).toString());
			}
		}
		prefs_.putBoolean("CFG_Okay_To_Send", microModel_.getSendConfiguration());
		HotKeys.active_ = true;
		dispose();
	}

	private void saveConfiguration() {
		File f;
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Configuration Files (.cfg)", "cfg"));
		int returnVal = fc.showDialog(Icy.getMainInterface().getFrame(), "Launch Configuration");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
		} else
			return;
		try {
			microModel_.saveToFile(f.getAbsolutePath());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}

	public String getFileName() {
		return microModel_.getFileName();
	}

	@SuppressWarnings("unused")
	private static String readStream(InputStream is) throws IOException {
		StringBuffer bf = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			bf.append(line);
			bf.append("\n");
		}
		return bf.toString();
	}

	private static final long serialVersionUID = 1L;
	private JLabel pagesLabel_;
	private JButton backButton_;
	private JButton nextButton_;
	private PagePanel pages_[];
	private int curPage_;
	private MicroscopeModel microModel_;
	private CMMCore core_;
	private Preferences prefs_;
	private JLabel titleLabel_;
	private JEditorPane helpTextPane_;
	private String defaultPath_;
	private boolean showSynchroPage_;

}