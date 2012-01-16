package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigurationWizardWrapper;

import icy.main.Icy;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.micromanager.conf.MMConfigFileException;
import org.micromanager.conf.PagePanel;
import org.micromanager.utils.GUIUtils;
import org.micromanager.utils.ReportingUtils;

public class FinishPage extends PagePanel {
	private static final long serialVersionUID = 1L;
	private JButton browseButton_;
	private JTextField fileNameField_;
	private boolean overwrite_ = false;
	JCheckBox sendCheck_;

	public FinishPage(Preferences paramPreferences) {
		this.title_ = "Save configuration and exit";
		setHelpFileName("conf_finish_page.html");
		this.prefs_ = paramPreferences;
		setLayout(null);

		JLabel localJLabel1 = new JLabel();
		localJLabel1.setText("Configuration file:");
		localJLabel1.setBounds(14, 11, 123, 21);
		add(localJLabel1);

		this.fileNameField_ = new JTextField();
		this.fileNameField_.setBounds(12, 30, 429, 24);
		add(this.fileNameField_);

		this.browseButton_ = new JButton();
		this.browseButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				FinishPage.this.browseConfigurationFile();
			}
		});
		this.browseButton_.setText("Browse...");
		this.browseButton_.setBounds(443, 31, 100, 23);
		add(this.browseButton_);

		this.sendCheck_ = new JCheckBox();
		this.sendCheck_.setBounds(10, 100, 360, 33);
		this.sendCheck_.setFont(new Font("", 0, 12));
		this.sendCheck_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent paramActionEvent) {
				FinishPage.this.model_.setSendConfiguration(FinishPage.this.sendCheck_.isSelected());
			}
		});
		this.sendCheck_.setText("Send configuration to Micro-manager.org");
		add(this.sendCheck_);

		JLabel localJLabel2 = new JLabel();
		localJLabel2.setAutoscrolls(true);
		localJLabel2.setText("Providing the configuration data will assist securing further project funding.");
		localJLabel2.setBounds(10, 150, 500, 33);
		localJLabel2.setFont(this.sendCheck_.getFont());
		add(localJLabel2);
	}

	public boolean enterPage(boolean paramBoolean) {
		this.sendCheck_.setSelected(this.model_.getSendConfiguration());
		this.fileNameField_.setText(this.model_.getFileName());
		return true;
	}

	public boolean exitPage(boolean paramBoolean) {
		if (paramBoolean) {
			saveAndTest();
		}
		return true;
	}

	public void refresh() {
	}

	public void loadSettings() {
	}

	public void saveSettings() {
	}

	private void browseConfigurationFile() {
		
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Configuration Files (.cfg)", "cfg"));
		int returnVal = fc.showDialog(Icy.getMainInterface().getMainFrame(), "Launch Configuration");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			setFilePath(fc.getSelectedFile());
			this.overwrite_ = true;
		}
	}

	private void setFilePath(File paramFile) {
		String str = paramFile.getAbsolutePath();
		if (!str.endsWith(".cfg")) {
			str = str + ".cfg";
		}
		fileNameField_.setText(str);
	}

	private void saveAndTest() {
		Container localContainer = getTopLevelAncestor();
		Cursor localCursor = null;
		Object localObject1;
		if (null != localContainer) {
			localCursor = localContainer.getCursor();
			localObject1 = new Cursor(3);
			localContainer.setCursor((Cursor) localObject1);
		}
		try {
			this.core_.unloadAllDevices();
			GUIUtils.preventDisplayAdapterChangeExceptions();

			localObject1 = new File(this.fileNameField_.getText());
			if ((((File) localObject1).exists()) && (!this.overwrite_)) {
				int i = JOptionPane.showConfirmDialog(this, "Overwrite " + ((File) localObject1).getName() + "?", "File Save", 0);

				if (i == 1) {
					ReportingUtils.logMessage("H.W. Configuration problem: File must be saved in order to test the configuration!");
					return;
				}
			}
			setFilePath((File) localObject1);
			this.model_.removeInvalidConfigurations();
			this.model_.saveToFile(this.fileNameField_.getText());
			this.core_.loadSystemConfiguration(this.model_.getFileName());
			GUIUtils.preventDisplayAdapterChangeExceptions();

			if ((null != localContainer) && (null != localCursor))
				localContainer.setCursor(localCursor);
		} catch (MMConfigFileException localMMConfigFileException) {
			ReportingUtils.showError(localMMConfigFileException);

			if ((null != localContainer) && (null != localCursor))
				localContainer.setCursor(localCursor);
		} catch (Exception localException) {
			ReportingUtils.showError(localException);

			if ((null != localContainer) && (null != localCursor))
				localContainer.setCursor(localCursor);
		} finally {
			if ((null != localContainer) && (null != localCursor))
				localContainer.setCursor(localCursor);
		}
	}
}