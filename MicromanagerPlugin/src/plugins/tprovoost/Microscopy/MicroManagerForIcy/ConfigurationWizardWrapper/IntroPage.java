/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   IntroPage.java

package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigurationWizardWrapper;

import icy.main.Icy;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.micromanager.conf.MMConfigFileException;
import org.micromanager.conf.PagePanel;
import org.micromanager.utils.ReportingUtils;

// Referenced classes of package org.micromanager.conf:
//            PagePanel, MMConfigFileException, MicroscopeModel

public class IntroPage extends PagePanel {

	public IntroPage(Preferences prefs) {
		buttonGroup = new ButtonGroup();
		initialized_ = false;
		title_ = "Select the configuration file";
		helpText_ = "Welcome to the Micro-Manager Configurator.\nThe Configurator will guide you through the process of configuring the software to work with your hardware setup.\nIn this step you choose if you are creating a new hardware configuration or editing an existing one.";
		setLayout(null);
		prefs_ = prefs;
		setHelpFileName("conf_intro_page.html");
		createNewRadioButton_ = new JRadioButton();
		createNewRadioButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				model_.reset();
				initialized_ = false;
				filePathField_.setEnabled(false);
				browseButton_.setEnabled(false);
			}
		});
		buttonGroup.add(createNewRadioButton_);
		createNewRadioButton_.setText("Create new configuration");
		createNewRadioButton_.setBounds(10, 31, 424, 23);
		add(createNewRadioButton_);
		modifyRadioButton_ = new JRadioButton();
		buttonGroup.add(modifyRadioButton_);
		modifyRadioButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				filePathField_.setEnabled(true);
				browseButton_.setEnabled(true);
			}

		});
		modifyRadioButton_.setText("Modify or explore existing configuration");
		modifyRadioButton_.setBounds(10, 55, 424, 23);
		add(modifyRadioButton_);
		filePathField_ = new JTextField();
		filePathField_.setBounds(10, 84, 424, 19);
		add(filePathField_);
		browseButton_ = new JButton();
		browseButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				loadConfiguration();
			}

		});
		browseButton_.setText("Browse...");
		browseButton_.setBounds(440, 82, 100, 23);
		add(browseButton_);
		createNewRadioButton_.setSelected(true);
		filePathField_.setEnabled(false);
		browseButton_.setEnabled(false);
	}

	public void loadSettings() {
		if (model_ != null)
			filePathField_.setText(model_.getFileName());
		if (filePathField_.getText().length() > 0) {
			modifyRadioButton_.setSelected(true);
			filePathField_.setEnabled(true);
			browseButton_.setEnabled(true);
		}
	}

	public void saveSettings() {
	}

	public boolean enterPage(boolean fromNextPage) {
		if (fromNextPage)
			filePathField_.setText(model_.getFileName());
		return true;
	}

	public boolean exitPage(boolean toNextPage) {
		if (modifyRadioButton_.isSelected()
				&& (!initialized_ || filePathField_.getText().compareTo(
						model_.getFileName()) != 0)) {
			try {
				model_.loadFromFile(filePathField_.getText());
			} catch (MMConfigFileException e) {
				ReportingUtils.showError(e);
				model_.reset();
				return false;
			}
			initialized_ = true;
		}
		return true;
	}

	public void refresh() {
	}

	private void loadConfiguration() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Configuration Files (.cfg)", "cfg"));
		int returnVal = fc.showDialog(Icy.getMainInterface().getMainFrame(), "Launch Configuration");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			filePathField_.setText(fc.getSelectedFile().getAbsolutePath());
		}
	}

	private static final long serialVersionUID = 1L;
	private ButtonGroup buttonGroup;
	private JTextField filePathField_;
	private boolean initialized_;
	private JRadioButton modifyRadioButton_;
	private JRadioButton createNewRadioButton_;
	private JButton browseButton_;

}