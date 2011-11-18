package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.preferences.XMLPreferences;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import mmcorej.CMMCore;
import mmcorej.Configuration;
import mmcorej.MMCoreJ;
import mmcorej.StrVector;

import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.StateItem;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class ConfigGroupPad extends JScrollPane {

	private static final long serialVersionUID = 1L;
	private JTable table_;
	private StateTableData data_;
	private MMMainFrame parentGUI_;
	private ArrayList<ChannelSpec> channels_;
	XMLPreferences prefs;
	private String COLUMN_WIDTH;
	public PresetEditor presetEditor_;
	public String groupName_;

	public ConfigGroupPad(MMMainFrame parentGUI) {
		COLUMN_WIDTH = "group_col_width";
		presetEditor_ = null;
		groupName_ = "";
		channels_ = new ArrayList<ChannelSpec>();
		parentGUI_ = parentGUI;
		prefs = parentGUI._prefs.node("ConfigPad");
	}

	private void handleException(Exception e) {
		ReportingUtils.showError(e);
	}

	public void setCore(CMMCore core) {
		table_ = new JTable();
		table_.setSelectionMode(0);
		table_.setAutoCreateColumnsFromModel(false);
		table_.setRowSelectionAllowed(true);
		setViewportView(table_);
		data_ = new StateTableData(core);
		table_.setModel(data_);
		table_.addColumn(new TableColumn(0, 200, new StateGroupCellRenderer(), null));
		table_.addColumn(new TableColumn(1, 200, new StatePresetCellRenderer(), new StatePresetCellEditor()));
		int colWidth = prefs.getInt(COLUMN_WIDTH, 0);
		if (colWidth > 0)
			table_.getColumnModel().getColumn(0).setPreferredWidth(colWidth);
	}

	public void saveSettings() {
		prefs.putInt(COLUMN_WIDTH, table_.getColumnModel().getColumn(0).getWidth());
	}

	public void setParentGUI(MMMainFrame parentGUI) {
		parentGUI_ = parentGUI;
	}

	@Override
	public void setPreferredSize(Dimension dimension) {
		super.setPreferredSize(dimension);
		table_.setPreferredSize(dimension);
	}

	@Override
	public void setMaximumSize(Dimension dimension) {
		super.setMaximumSize(dimension);
		table_.setMaximumSize(dimension);
	}

	public void refreshStructure() {
		if (data_ != null) {
			data_.updateStatus();
			data_.refreshStatus();
			data_.fireTableStructureChanged();
			table_.repaint();
		}
	}

	public String getGroup() {
		int idx = table_.getSelectedRow();
		if (idx < 0 || data_.getRowCount() <= 0)
			return "";
		else
			return (String) data_.getValueAt(idx, 0);
	}

	public void setGroup(String groupName) {
		for (int i = 0; i < data_.getRowCount(); i++)
			if (data_.getValueAt(i, 0).toString().contentEquals(groupName))
				table_.setRowSelectionInterval(i, 1);

	}

	public String getPreset() {
		int idx = table_.getSelectedRow();
		if (idx < 0 || data_.getRowCount() <= 0)
			return "";
		try {
			return data_.core_.getCurrentConfig((String) data_.getValueAt(idx, 0));
		} catch (Exception e) {
			ReportingUtils.logError(e);
		}
		return null;
	}

	public class StateTableData extends AbstractTableModel {

		private static final long serialVersionUID = -6584881796860806078L;
		public final String columnNames_[] = { "Group", "Preset" };
		ArrayList<StateItem> groupList_;
		private CMMCore core_;
		private boolean configDirty_;

		public StateTableData(CMMCore core) {
			super();
			groupList_ = new ArrayList<StateItem>();
			core_ = null;
			core_ = core;
			updateStatus();
			configDirty_ = false;
		}

		public int getRowCount() {
			return groupList_.size();
		}

		public int getColumnCount() {
			return columnNames_.length;
		}

		public StateItem getPropertyItem(int row) {
			return (StateItem) groupList_.get(row);
		}

		public Object getValueAt(int row, int col) {
			StateItem item = (StateItem) groupList_.get(row);
			if (col == 0)
				return item.group;
			if (col == 1)
				return item.config;
			else
				return null;
		}

		public void setValueAt(Object value, int row, int col) {
			StateItem item = (StateItem) groupList_.get(row);
			if (col == 1)
				try {
					if (value != null && value.toString().length() > 0) {
						if (parentGUI_ != null)
							parentGUI_.notifyPluginsConfigAboutToChange(item);
						boolean wasRunning;
						if (wasRunning = core_.isSequenceRunning())
							core_.stopSequenceAcquisition();
						if (item.singleProp) {
							if (item.hasLimits) {
								if (item.isInteger()) {
									core_.setProperty(item.device, item.name, NumberUtils.intStringDisplayToCore(value));
								} else {
									core_.setProperty(item.device, item.name, NumberUtils.doubleStringDisplayToCore(value));
								}
							} else
								core_.setProperty(item.device, item.name, value.toString());
							core_.waitForDevice(item.device);
						} else {
							core_.setConfig(item.group, value.toString());
							core_.waitForConfig(item.group, value.toString());
						}
						refreshStatus();
						repaint();
						if (parentGUI_ != null)
							parentGUI_.updateGUI(true);
						if (item.group.equals(MMCoreJ.getG_Keyword_Channel())) {
							ChannelSpec csFound = null;
							int i = 0;
							do {
								if (i >= channels_.size())
									break;
								ChannelSpec cs = (ChannelSpec) channels_.get(i);
								if (cs != null && cs.name_.equals(item.config)) {
									csFound = cs;
									break;
								}
								i++;
							} while (true);
							if (csFound == null) {
								csFound = new ChannelSpec();
								csFound.name_ = item.config;
								csFound.exposure_ = core_.getExposure();
								if (parentGUI_ != null) {
									org.micromanager.utils.ContrastSettings ctr = parentGUI_.getContrastSettings();
									if (ctr != null)
										if (parentGUI_.is16bit())
											csFound.contrast16_ = ctr;
										else
											csFound.contrast8_ = ctr;
									channels_.add(csFound);
								}
							}
						}
						if (wasRunning)
							core_.startContinuousSequenceAcquisition(0.0D);
						if (parentGUI_ != null)
							parentGUI_.notifyPluginsConfigChanged(item);
					}
				} catch (Exception e) {
					handleException(e);
				}

		}

		public String getColumnName(int column) {
			return columnNames_[column];
		}

		public boolean isCellEditable(int nRow, int nCol) {
			return nCol != 0;
		}

		@SuppressWarnings("unused")
		private boolean containsValue(String strs[], String theValue) {
			String arr$[] = strs;
			int len$ = arr$.length;
			for (int it = 0; it < len$; it++) {
				String str = arr$[it];
				if (theValue.equals(str))
					return true;
			}
			return false;
		}

		public void updateStatus() {
			try {
				StrVector groups = core_.getAvailableConfigGroups();
				HashMap<String, String> oldGroupHash = new HashMap<String, String>();
				StateItem group;
				for (Iterator<StateItem> it = groupList_.iterator(); it.hasNext(); oldGroupHash.put(group.group, group.config))
					group = (StateItem) it.next();

				groupList_.clear();
				StateItem item;
				for (Iterator<String> it = groups.iterator(); it.hasNext(); groupList_.add(item)) {
					String group1 = (String) it.next();
					item = new StateItem();
					item.group = group1;
					item.config = core_.getCurrentConfig(item.group);
					item.allowed = core_.getAvailableConfigs(item.group).toArray();
					if (item.config.length() > 0) {
						Configuration curCfg = core_.getConfigData(item.group, item.config);
						item.descr = curCfg.getVerbose();
					} else {
						item.descr = "";
					}
					if (item.allowed.length == 1) {
						Configuration cfg = core_.getConfigData(item.group, item.allowed[0]);
						if (cfg.size() == 1L) {
							item.device = cfg.getSetting(0L).getDeviceLabel();
							item.name = cfg.getSetting(0L).getPropertyName();
							item.hasLimits = core_.hasPropertyLimits(item.device, item.name);
							boolean itemHasAllowedValues = 0L < core_.getAllowedPropertyValues(item.device, item.name).size();
							if (item.hasLimits || !itemHasAllowedValues) {
								item.singleProp = true;
								item.type = core_.getPropertyType(item.device, item.name);
								item.setValueFromCoreString(core_.getProperty(item.device, item.name));
								item.config = item.value;
								item.lowerLimit = core_.getPropertyLowerLimit(item.device, item.name);
								item.upperLimit = core_.getPropertyUpperLimit(item.device, item.name);
								item.singlePropAllowed = core_.getAllowedPropertyValues(item.device, item.name).toArray();
							}
						}
					}
				}

			} catch (Exception e) {
				handleException(e);
			}
		}

		public void refreshStatus() {
			try {
				for (int i = 0; i < groupList_.size(); i++) {
					StateItem item = (StateItem) groupList_.get(i);
					if (item.singleProp)
						item.config = core_.getProperty(item.device, item.name);
					else
						item.config = core_.getCurrentConfig(item.group);
				}

			} catch (Exception e) {
				handleException(e);
			}
		}

		public boolean isConfigDirty() {
			return configDirty_;
		}
	}
}