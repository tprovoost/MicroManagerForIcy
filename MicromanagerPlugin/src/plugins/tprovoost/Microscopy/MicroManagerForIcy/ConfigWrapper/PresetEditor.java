package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.StrVector;

import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.ReportingUtils;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class PresetEditor extends ConfigDialog {
	private static final long serialVersionUID = 8281144157746745260L;

	public PresetEditor(String groupName, String presetName, MMMainFrame gui, CMMCore core, boolean newItem) {
		super(groupName, presetName, gui, core, newItem);
		this.instructionsText_ = "Here you can specifiy the property values\nin a configuration preset.";
		this.nameFieldLabelText_ = "Preset name:";
		this.initName_ = this.presetName_;
		this.TITLE = ("Preset editor for the \"" + groupName + "\" configuration group");
		this.showUnused_ = Boolean.valueOf(false);
		this.showFlagsPanelVisible = false;
		this.scrollPaneTop_ = 70;
		this.numColumns = 2;
		this._data = new PropertyTableData(this._core, this.groupName_, this.presetName_, 1, 2, this);
		initializeData();
		this._data.setColumnNames("Property Name", "Preset Value", "");
		initialize();
	}

	public void okChosen() {
		String newName = this.nameField_.getText();
		if (writePreset(this.initName_, newName))
			dispose();
	}

	public boolean writePreset(String initName, String newName) {
		if (newName.length() == 0) {
			showMessageDialog("Please enter a name for this preset.");
			return false;
		}

		StrVector groups = this._core.getAvailableConfigs(this.groupName_);
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.get(i).contentEquals(newName)) && (!newName.contentEquals(initName))) {
				showMessageDialog("A preset by this name already exists in the \"" + this.groupName_
						+ "\" group.\nPlease enter a different name.");
				return false;
			}
		}
		StrVector cfgs = this._core.getAvailableConfigs(this.groupName_);
		try {
			for (int j = 0; j < cfgs.size(); j++) {
				boolean same = true;
				if ((this.newItem_) || (!cfgs.get(j).contentEquals(initName))) {
					Configuration otherPreset = this._core.getConfigData(this.groupName_, cfgs.get(j));
					for (PropertyItem item : this._data.getPropList()) {
						if ((item.confInclude)
								&& (otherPreset.isPropertyIncluded(item.device, item.name))
								&& (!item.getValueInCoreFormat().contentEquals(
										otherPreset.getSetting(item.device, item.name).getPropertyValue())))
							same = false;
					}
					if (same) {
						showMessageDialog("This combination of properties is already in found in the \"" + cfgs.get(j)
								+ "\" preset.\nPlease choose unique property values for your new preset.");
						return false;
					}
				}
			}
		} catch (Exception e) {
			ReportingUtils.logError(e);
		}

		if ((!this.newItem_) && (!initName.contentEquals(newName))) {
			try {
				this._core.renameConfig(this.groupName_, initName, newName);
			} catch (Exception e1) {
				ReportingUtils.logError(e1);
			}
		}

		for (PropertyItem item_ : this._data.getPropList()) {
			if (item_.confInclude) {
				try {
					this._core.defineConfig(this.groupName_, newName, item_.device, item_.name,
							item_.getValueInCoreFormat());
				} catch (Exception e) {
					ReportingUtils.logError(e);
				}
			}
		}

		this._gui.setConfigChanged(true);
		return true;
	}
}