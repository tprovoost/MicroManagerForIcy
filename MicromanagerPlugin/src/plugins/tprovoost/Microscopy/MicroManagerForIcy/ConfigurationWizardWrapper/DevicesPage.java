package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigurationWizardWrapper;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import mmcorej.CMMCore;
import mmcorej.MMCoreJ;

import org.micromanager.conf.Device;
import org.micromanager.conf.MicroscopeModel;
import org.micromanager.conf.PagePanel;
import org.micromanager.utils.ReportingUtils;

//Referenced classes of package org.micromanager.conf:
//         PagePanel, AddDeviceDlg, MicroscopeModel, Device

public class DevicesPage extends PagePanel {
	class DeviceTable_TableModel extends AbstractTableModel {

		public void setMicroscopeModel(MicroscopeModel mod) {
			devices_ = mod.getDevices();
			model_ = mod;
		}

		public int getRowCount() {
			return devices_.length;
		}

		public int getColumnCount() {
			return COLUMN_NAMES.length;
		}

		public String getColumnName(int columnIndex) {
			return COLUMN_NAMES[columnIndex];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return devices_[rowIndex].getName();
			if (columnIndex == 1)
				return new String((new StringBuilder()).append(devices_[rowIndex].getAdapterName()).append("/").append(devices_[rowIndex].getLibrary()).toString());
			else
				return devices_[rowIndex].getDescription();
		}

		public void setValueAt(Object value, int row, int col) {
			String newName = (String) value;
			String oldName = devices_[row].getName();
			if (col == 0)
				try {
					model_.changeDeviceName(oldName, newName);
					fireTableCellUpdated(row, col);
				} catch (Exception e) {
					handleError(e.getMessage());
				}
		}

		public boolean isCellEditable(int nRow, int nCol) {
			return nCol == 0;
		}

		public void refresh() {
			devices_ = model_.getDevices();
			fireTableDataChanged();
		}

		private static final long serialVersionUID = 1L;
		public final String COLUMN_NAMES[] = { "Name", "Adapter/Library", "Description" };
		MicroscopeModel model_;
		Device devices_[];

		public DeviceTable_TableModel(MicroscopeModel model) {
			setMicroscopeModel(model);
		}
	}

	public DevicesPage(Preferences prefs) {
		title_ = "Add or remove devices";
		helpText_ = "The list of selected devices is displayed above. You can add or remove devices to/from this list.\nThe first column shows the device's assigned name for this particular configuration. In subsequent steps devices will be referred to by their assigned names.\n\nYou can edit device names by double-clicking in the first column. Device name must be unique and should not contain any special characters.";
		setLayout(null);
		prefs_ = prefs;
		setHelpFileName(HELP_FILE_NAME);
		scrollPane_ = new JScrollPane();
		scrollPane_.setBounds(10, 10, 453, 262);
		add(scrollPane_);
		deviceTable_ = new JTable();
		deviceTable_.setSelectionMode(0);
		scrollPane_.setViewportView(deviceTable_);
		JButton addButton = new JButton();
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				addDevice();
			}

		});
		addButton.setText("Add...");
		addButton.setBounds(469, 10, 93, 23);
		add(addButton);
		JButton removeButton = new JButton();
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				removeDevice();
			}

		});
		removeButton.setText("Remove");
		removeButton.setBounds(469, 39, 93, 23);
		add(removeButton);
	}

	protected void removeDevice() {
		int sel = deviceTable_.getSelectedRow();
		if (sel < 0)
			return;
		String devName = (String) deviceTable_.getValueAt(sel, 0);
		if (devName.contentEquals((new StringBuffer()).append(MMCoreJ.getG_Keyword_CoreDevice()))) {
			handleError((new StringBuilder()).append(MMCoreJ.getG_Keyword_CoreDevice()).append(" device can't be removed!").toString());
			return;
		} else {
			model_.removePeripherals(devName, core_);
			model_.removeDevice(devName);
			rebuildTable();
			return;
		}
	}

	protected void addDevice() {
		AddDeviceDlg dlg = new AddDeviceDlg(model_, this);
		dlg.setVisible(true);
		rebuildTable();
	}

	public void rebuildTable() {
		javax.swing.table.TableModel tm = deviceTable_.getModel();
		DeviceTable_TableModel tmd;
		if (tm instanceof DeviceTable_TableModel) {
			tmd = (DeviceTable_TableModel) deviceTable_.getModel();
			tmd.refresh();
		} else {
			tmd = new DeviceTable_TableModel(model_);
			deviceTable_.setModel(tmd);
		}
		tmd.fireTableStructureChanged();
		tmd.fireTableDataChanged();
	}

	public void refresh() {
		rebuildTable();
	}

	public boolean enterPage(boolean fromNextPage) {
		try {
			CMMCore.getDeviceLibraries();
			model_.removeDuplicateComPorts();
			rebuildTable();
			if (fromNextPage)
				try {
					core_.unloadAllDevices();
				} catch (Exception e) {
					handleError(e.getMessage());
				}
			return true;
		} catch (Exception e2) {
			ReportingUtils.showError(e2);
		}
		return false;
	}

	public boolean exitPage(boolean toNextPage) {
		boolean status = true;
		if (toNextPage) {
			Container ancestor = getTopLevelAncestor();
			Cursor oldc = null;
			if (null != ancestor) {
				oldc = ancestor.getCursor();
				Cursor waitc = new Cursor(3);
				ancestor.setCursor(waitc);
			}
			status = model_.loadModel(core_, true);
			if (null != ancestor && null != oldc)
				ancestor.setCursor(oldc);
		}
		return status;
	}

	public void loadSettings() {
	}

	public void saveSettings() {
	}

	private static final long serialVersionUID = 1L;
	private JTable deviceTable_;
	private JScrollPane scrollPane_;
	private static final String HELP_FILE_NAME = "conf_devices_page.html";
}
