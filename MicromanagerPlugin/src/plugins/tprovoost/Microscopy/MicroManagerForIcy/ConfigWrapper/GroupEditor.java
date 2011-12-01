package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.util.Arrays;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.PropertyType;
import mmcorej.StrVector;

import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.SortFunctionObjects;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class GroupEditor extends ConfigDialog {
	private static final long serialVersionUID = 8281144157746745260L;

	public GroupEditor(String groupName, String presetName, MMMainFrame gui_, CMMCore core, boolean newItem) {
		super(groupName, presetName, gui_, core, newItem);
		gui_.notifyConfigAboutToChange(null);
		instructionsText_ = "Here you can specifiy the properties included\nin a configuration group.";
		nameFieldLabelText_ = "Group name:";
		initName_ = this.groupName_;
		TITLE = "Group Editor";
		showUnused_ = Boolean.valueOf(true);
		showFlagsPanelVisible = true;
		scrollPaneTop_ = 140;
		numColumns = 3;
		_data = new PropertyTableData(_core, groupName_, presetName_, 2, 1, this);
		initializeData();
		_data.setColumnNames("Property Name", "Use in Group?", "Current Property Value");
		initialize();
	}

	public void okChosen() {
		String newName = this.nameField_.getText();

		if (writeGroup(this.initName_, newName)) {
			this.groupName_ = newName;
			dispose();
		}
	}

	public boolean writeGroup(String initName, String newName) {
		int itemsIncludedCount = 0;
		for (PropertyItem item : this._data.getPropList())
			if (item.confInclude)
				itemsIncludedCount++;
		if (itemsIncludedCount == 0) {
			showMessageDialog("Please select at least one property for this group.");
			return false;
		}

		if (newName.length() == 0) {
			showMessageDialog("Please enter a name for this group.");
			return false;
		}

		StrVector groups = this._core.getAvailableConfigGroups();
		for (int i = 0; i < groups.size(); i++) {
			if ((groups.get(i).contentEquals(newName)) && (!newName.contentEquals(initName))) {
				showMessageDialog("A group by this name already exists. Please enter a different name.");
				return false;
			}
		}
		StrVector cfgs = this._core.getAvailableConfigs(newName);
		try {
			if (!this.newItem_) {
				for (int i = 0; i < cfgs.size(); i++) {
					Configuration first = this._core.getConfigData(initName, cfgs.get(i));
					for (int j = i + 1; j < cfgs.size(); j++) {
						boolean same = true;

						Configuration second = this._core.getConfigData(initName, cfgs.get(j));
						for (PropertyItem item : this._data.getPropList())
							if ((item.confInclude)
									&& (first.isPropertyIncluded(item.device, item.name))
									&& (second.isPropertyIncluded(item.device, item.name))
									&& (!first
											.getSetting(item.device, item.name)
											.getPropertyValue()
											.contentEquals(second.getSetting(item.device, item.name).getPropertyValue()))) {
								same = false;
							}
						if (same) {
							showMessageDialog("By removing properties, you would create duplicate presets.\nTo avoid duplicates when you remove properties, you should\nfirst delete some of the presets in this group.");
							return false;
						}
					}
				}
			}
		} catch (Exception e) {
			ReportingUtils.logError(e);
		}

		if ((!this.newItem_) && (!initName.contentEquals(newName))) {
			try {
				this._core.renameConfigGroup(initName, newName);
			} catch (Exception e1) {
				ReportingUtils.logError(e1);
			}
		}
		if (this.newItem_) {
			try {
				this._core.defineConfigGroup(newName);

				for (PropertyItem item : this._data.getPropList()) {
					if (item.confInclude)
						if ((itemsIncludedCount == 1) && (item.allowed.length > 0)) {
							if (PropertyType.Float == item.type) {
								Arrays.sort(item.allowed, new SortFunctionObjects.DoubleStringComp());
							} else if (PropertyType.Integer == item.type) {
								Arrays.sort(item.allowed, new SortFunctionObjects.IntStringComp());
							} else if (PropertyType.String == item.type) {
								boolean allNumeric = true;

								for (int k = 0; k < item.allowed.length; k++) {
									if (!Character.isDigit(item.allowed[k].charAt(0))) {
										allNumeric = false;
										break;
									}
								}
								if (allNumeric)
									Arrays.sort(item.allowed, new SortFunctionObjects.NumericPrefixStringComp());
								else {
									Arrays.sort(item.allowed);
								}
							}
							for (String allowedValue : item.allowed)
								this._core.defineConfig(newName, allowedValue, item.device, item.name, allowedValue);
						} else {
							this._core.defineConfig(newName, "NewPreset", item.device, item.name,
									item.getValueInCoreFormat());
						}
				}
			} catch (Exception e) {
				ReportingUtils.logError(e);
			}

			if (itemsIncludedCount > 1)
				new PresetEditor(newName, "NewPreset", this._gui, this._core, false);
		} else {
			String cfg = null;
			try {
				Configuration unionCfg = this._core.getConfigGroupState(newName);

				for (PropertyItem item : this._data.getPropList())
					if ((!item.confInclude) && (unionCfg.isPropertyIncluded(item.device, item.name))) {
						for (int i = 0; i < cfgs.size(); i++) {
							cfg = cfgs.get(i);
							if (this._core.getConfigData(newName, cfg).isPropertyIncluded(item.device, item.name))
								this._core.deleteConfig(newName, cfg, item.device, item.name);
						}
					} else if ((item.confInclude) && (!unionCfg.isPropertyIncluded(item.device, item.name))) {
						for (int i = 0; i < cfgs.size(); i++) {
							cfg = cfgs.get(i);
							if (!this._core.getConfigData(this.groupName_, cfg).isPropertyIncluded(item.device,
									item.name))
								this._core.defineConfig(newName, cfg, item.device, item.name,
										item.getValueInCoreFormat());
						}
					}
			} catch (Exception e) {
				ReportingUtils.logError(e);
			}
		}

		// this.gui_.setConfigChanged(true);
		return true;
	}
}
