package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.DeviceType;
import mmcorej.PropertySetting;
import mmcorej.StrVector;

import org.micromanager.utils.MMPropertyTableModel;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.ShowFlags;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class PropertyTableData extends AbstractTableModel implements MMPropertyTableModel {
	private static final long serialVersionUID = -5582899855072387637L;
	int PropertyNameColumn_;
	protected int PropertyValueColumn_;
	int PropertyUsedColumn_;
	public boolean disabled = false;
	public String groupName_;
	public String presetName_;
	public ShowFlags flags_;
	public MMMainFrame gui_;
	public boolean showUnused_;
	protected boolean showReadOnly_;
	String[] columnNames_ = new String[3];

	public ArrayList<PropertyItem> propList_ = new ArrayList<PropertyItem>();
	public ArrayList<PropertyItem> propListVisible_ = new ArrayList<PropertyItem>();
	protected CMMCore core_ = null;
	Configuration[] groupData_;
	PropertySetting[] groupSignature_;
	private String[] presetNames_;

	public PropertyTableData(CMMCore core, String groupName, String presetName, int PropertyValueColumn,
			int PropertyUsedColumn, Component parentComponent) {
		this.core_ = core;
		this.groupName_ = groupName;
		this.presetName_ = presetName;
		this.PropertyNameColumn_ = 0;
		this.PropertyValueColumn_ = PropertyValueColumn;
		this.PropertyUsedColumn_ = PropertyUsedColumn;
	}

	public ArrayList<PropertyItem> getProperties() {
		return this.propList_;
	}

	public String findMatchingPreset() {
		ArrayList<PropertyItem> selectedItems = new ArrayList<PropertyItem>();
		for (PropertyItem item : this.propList_) {
			if (item.confInclude) {
				selectedItems.add(item);
			}
		}
		for (int i = 0; i < this.groupData_.length; i++) {
			int matchCount = 0;
			for (PropertyItem selectedItem : selectedItems) {
				PropertySetting ps = new PropertySetting(selectedItem.device, selectedItem.name, selectedItem.value);
				if (this.groupData_[i].isSettingIncluded(ps)) {
					matchCount++;
				}
			}
			if (matchCount == selectedItems.size()) {
				return this.presetNames_[i];
			}
		}
		return null;
	}

	public PropertyItem getItem(String device, String propName) {
		for (PropertyItem item : this.propList_) {
			if ((item.device.contentEquals(device)) && (item.name.contentEquals(propName)))
				return item;
		}
		return null;
	}

	public boolean verifyPresetSignature() {
		return true;
	}

	public void deleteConfig(String group, String config) {
		try {
			this.core_.deleteConfig(group, config);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public StrVector getAvailableConfigGroups() {
		return this.core_.getAvailableConfigGroups();
	}

	public int getRowCount() {
		return this.propListVisible_.size();
	}

	public int getColumnCount() {
		return this.columnNames_.length;
	}

	public boolean isEditingGroup() {
		return true;
	}

	public PropertyItem getPropertyItem(int row) {
		return (PropertyItem) this.propListVisible_.get(row);
	}

	public Object getValueAt(int row, int col) {
		PropertyItem item = (PropertyItem) this.propListVisible_.get(row);
		if (col == this.PropertyNameColumn_)
			return item.device + "-" + item.name;
		if (col == this.PropertyValueColumn_)
			return item.value;
		if (col == this.PropertyUsedColumn_) {
			return new Boolean(item.confInclude);
		}

		return null;
	}

	public void setValueInCore(PropertyItem item, Object value) {
		ReportingUtils.logMessage(item.device + "/" + item.name + ":" + value);
		try {
			if (item.isInteger())
				this.core_.setProperty(item.device, item.name, NumberUtils.intStringDisplayToCore(value));
			else if (item.isFloat())
				this.core_.setProperty(item.device, item.name, NumberUtils.doubleStringDisplayToCore(value));
			else {
				this.core_.setProperty(item.device, item.name, value.toString());
			}
			item.value = value.toString();
			this.core_.waitForDevice(item.device);
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void setValueAt(Object value, int row, int col) {
		PropertyItem item = (PropertyItem) this.propListVisible_.get(row);
		ReportingUtils.logMessage("Setting value " + value + " at row " + row);
		if (col == this.PropertyValueColumn_) {
			if (item.confInclude)
				setValueInCore(item, value);
		} else if (col == this.PropertyUsedColumn_) {
			item.confInclude = ((Boolean) value).booleanValue();
		}
		refresh();
		this.gui_.refreshGUI();
		fireTableCellUpdated(row, col);
	}

	public String getColumnName(int column) {
		return this.columnNames_[column];
	}

	public boolean isCellEditable(int nRow, int nCol) {
		if (nCol == this.PropertyValueColumn_)
			return !((PropertyItem) this.propListVisible_.get(nRow)).readOnly;
		if (nCol == this.PropertyUsedColumn_) {
			return isEditingGroup();
		}

		return false;
	}

	StrVector getAvailableConfigs(String group) {
		return this.core_.getAvailableConfigs(group);
	}

	public void refresh() {
		try {
			for (int i = 0; i < this.propList_.size(); i++) {
				PropertyItem item = (PropertyItem) this.propList_.get(i);
				if (showDevice(this.flags_, item.device).booleanValue()) {
					item.setValueFromCoreString(this.core_.getProperty(item.device, item.name));
				}
			}
			fireTableDataChanged();
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void update() {
		update(this.flags_, this.groupName_, this.presetName_);
	}

	public void setShowReadOnly(boolean showReadOnly) {
		this.showReadOnly_ = showReadOnly;
	}

	public void update(ShowFlags flags, String groupName, String presetName) {
		try {
			StrVector devices = this.core_.getLoadedDevices();
			this.propList_.clear();

			Configuration cfg = this.core_.getConfigGroupState(groupName);

			for (int i = 0; i < devices.size(); i++) {
				if (!showDevice(flags, devices.get(i)).booleanValue())
					continue;
				StrVector properties = this.core_.getDevicePropertyNames(devices.get(i));
				for (int j = 0; j < properties.size(); j++) {
					PropertyItem item = new PropertyItem();
					item.readFromCore(this.core_, devices.get(i), properties.get(j));

					if (((!item.readOnly) || (this.showReadOnly_)) && (!item.preInit)) {
						if (cfg.isPropertyIncluded(item.device, item.name)) {
							item.confInclude = true;
							item.setValueFromCoreString(cfg.getSetting(item.device, item.name).getPropertyValue());
						} else {
							item.confInclude = false;
							item.setValueFromCoreString(this.core_.getProperty(devices.get(i), properties.get(j)));
						}

						this.propList_.add(item);
					}
				}

			}

			updateRowVisibility(flags);
		} catch (Exception e) {
			handleException(e);
		}
		fireTableStructureChanged();
	}

	public void updateRowVisibility(ShowFlags flags) {
		this.propListVisible_.clear();

		for (PropertyItem item : this.propList_) {
			boolean showDevice = showDevice(flags, item.device).booleanValue();

			if ((!this.showUnused_) && (!item.confInclude)) {
				showDevice = false;
			}
			if (showDevice) {
				this.propListVisible_.add(item);
			}
		}
		fireTableStructureChanged();
		fireTableDataChanged();
	}

	public Boolean showDevice(ShowFlags flags, String deviceName) {
		DeviceType dType = null;
		try {
			dType = this.core_.getDeviceType(deviceName);
		} catch (Exception e) {
			handleException(e);
		}

		Boolean showDevice = Boolean.valueOf(false);
		if (dType == DeviceType.SerialDevice)
			showDevice = Boolean.valueOf(false);
		else if (dType == DeviceType.CameraDevice)
			showDevice = Boolean.valueOf(flags.cameras_);
		else if (dType == DeviceType.ShutterDevice)
			showDevice = Boolean.valueOf(flags.shutters_);
		else if (dType == DeviceType.StageDevice)
			showDevice = Boolean.valueOf(flags.stages_);
		else if (dType == DeviceType.XYStageDevice)
			showDevice = Boolean.valueOf(flags.stages_);
		else if (dType == DeviceType.StateDevice)
			showDevice = Boolean.valueOf(flags.state_);
		else {
			showDevice = Boolean.valueOf(flags.other_);
		}
		return showDevice;
	}

	public void setColumnNames(String col0, String col1, String col2) {
		this.columnNames_[0] = col0;
		this.columnNames_[1] = col1;
		this.columnNames_[2] = col2;
	}

	private void handleException(Exception e) {
		ReportingUtils.showError(e);
	}

	public void setGUI(MMMainFrame gui) {
		this.gui_ = gui;
	}

	public void setFlags(ShowFlags flags) {
		this.flags_ = flags;
	}

	public void setShowUnused(boolean showUnused) {
		this.showUnused_ = showUnused;
	}

	public ArrayList<PropertyItem> getPropList() {
		return this.propList_;
	}
}