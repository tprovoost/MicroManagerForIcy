package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.gui.component.button.IcyButton;
import icy.resource.icon.IcyIcon;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mmcorej.CMMCore;

import org.micromanager.utils.ReportingUtils;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

// Referenced classes of package org.micromanager:
// MMStudioMainFrame, GroupEditor, PresetEditor, ConfigGroupPad

public class ConfigButtonsPanel extends JPanel {

	public ConfigButtonsPanel() {
		setLayout(new GridLayout());
		createLabel("Group: ");
		_btn_addGroup = createButton("", "sq_plus.png");
		_btn_addGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addGroup();
			}
		});
		_btn_removeGroup = createButton("", "sq_minus.png");
		_btn_removeGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeGroup();
			}
		});
		_btn_editGroup = createButton("", "doc_lines.png");
		_btn_editGroup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editGroup();
			}
		});
		_btn_editGroup.setMargin(new Insets(-10, -50, -10, -50));
		_btn_editGroup.setPreferredSize(new Dimension(25, 15));

		createLabel("   Preset: ");
		_btn_addPreset = createButton("", "sq_plus.png");
		_btn_addPreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addPreset();
			}
		});
		_btn_removePreset = createButton("", "sq_minus.png");
		_btn_removePreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removePreset();
			}
		});
		_btn_editPreset = createButton("", "doc_lines.png");
		_btn_editPreset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				editPreset();
			}
		});
		_btn_editPreset.setMargin(new Insets(-10, -50, -10, -50));
		createLabel("  ");
		_btn_refresh = createButton("", "rot_unclock.png");
		_btn_refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionevent) {
				_gui.updateGUI(true);
			}
		});
		_btn_refresh.setSize(new Dimension(40, 20));
	}

	public void setConfigPad(ConfigGroupPad configPad) {
		configPad_ = configPad;
	}

	public void setGUI(MMMainFrame mmMainFrame) {
		_gui = mmMainFrame;
	}

	public void setCore(CMMCore core) {
		core_ = core;
	}

	public void format(JComponent theComp) {
		theComp.setFont(new Font("Arial", 0, 8));
		add(theComp);
	}

	public JLabel createLabel(String labelText) {
		JLabel theLabel = new JLabel(labelText);
		theLabel.setFont(new Font("Arial", 1, 10));
		add(theLabel);
		return theLabel;
	}

	public IcyButton createButton(String buttonText, String iconPath) {
		IcyButton theButton;
		if (iconPath.equals("")) {
			theButton = new IcyButton("",new IcyIcon( "", IcyIcon.DEFAULT_SIZE));
			theButton.setText(buttonText);
		} else
			theButton = new IcyButton(buttonText, new IcyIcon(iconPath));
		add(theButton);
		return theButton;
	}

	public void addGroup() {
		new GroupEditor("", "", _gui, core_, true);
	}

	public void removeGroup() {
		String groupName = configPad_.getGroup();
		if (groupName.length() > 0) {
			int result = JOptionPane.showConfirmDialog(
					this,
					(new StringBuilder()).append("Are you sure you want to remove group ").append(groupName)
							.append(" and all associated presets?").toString(),
					(new StringBuilder()).append("Remove the ").append(groupName).append(" group?").toString(), 0, 1);
			if (result == 0)
				try {
					core_.deleteConfigGroup(groupName);
					_gui.setConfigChanged(true);
				} catch (Exception e) {
					handleException(e);
				}
		} else {
			JOptionPane.showMessageDialog(this,
					"If you want to remove a group, select it on the Configurations panel first.");
		}
	}

	public void editGroup() {
		String groupName = configPad_.getGroup();
		if (groupName.length() == 0)
			JOptionPane.showMessageDialog(this, "To edit a group, please select it first, then press the edit button.");
		else
			new GroupEditor(groupName, configPad_.getPreset(), _gui, core_, false);
	}

	public void addPreset() {
		String groupName = configPad_.getGroup();
		if (groupName.length() == 0)
			JOptionPane.showMessageDialog(this,
					"To add a preset to a group, please select the group first, then press the edit button.");
		else
			new PresetEditor(groupName, "", _gui, core_, true);
	}

	public void removePreset() {
		String groupName = configPad_.getGroup();
		String presetName = configPad_.getPreset();
		if (groupName.length() > 0)
			if (core_.getAvailableConfigs(groupName).size() == 1L) {
				int result = JOptionPane.showConfirmDialog(
						this,
						(new StringBuilder()).append("\"").append(presetName)
								.append("\" is the last preset for the \"").append(groupName)
								.append("\" group.\nDelete both preset and group?").toString(),
						"Remove last preset in group", 0, 1);
				if (result == 0)
					try {
						core_.deleteConfig(groupName, presetName);
						core_.deleteConfigGroup(groupName);
					} catch (Exception e) {
						handleException(e);
					}
			} else {
				int result = JOptionPane.showConfirmDialog(this,
						(new StringBuilder()).append("Are you sure you want to remove preset ").append(presetName)
								.append(" from the ").append(groupName).append(" group?").toString(), "Remove preset",
						0, 1);
				if (result == 0)
					try {
						core_.deleteConfig(groupName, presetName);
					} catch (Exception e) {
						handleException(e);
					}
			}
	}

	public void editPreset() {
		String presetName = configPad_.getPreset();
		String groupName = configPad_.getGroup();
		if (groupName.length() == 0 || presetName.length() == 0)
			JOptionPane.showMessageDialog(this,
					"To edit a preset, please select the preset first, then press the edit button.");
		else
			new PresetEditor(groupName, presetName, _gui, core_, false);
	}

	public void handleException(Exception e) {
		ReportingUtils.logError(e);
	}

	private static final long serialVersionUID = 6481082898578589473L;
	private IcyButton _btn_addGroup;
	private IcyButton _btn_removeGroup;
	private IcyButton _btn_editGroup;
	private IcyButton _btn_addPreset;
	private IcyButton _btn_removePreset;
	private IcyButton _btn_editPreset;
	private IcyButton _btn_refresh;
	private ConfigGroupPad configPad_;
	private CMMCore core_;
	private MMMainFrame _gui;
}
