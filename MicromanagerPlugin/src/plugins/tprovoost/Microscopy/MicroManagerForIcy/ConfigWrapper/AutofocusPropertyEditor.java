package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.micromanager.utils.MMDialog;
import org.micromanager.utils.MMException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.SliderPanel;

import com.swtdesigner.SwingResourceManager;

public class AutofocusPropertyEditor extends MMDialog {
	public class PropertyCellRenderer implements TableCellRenderer {

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
			PropertyTableData data = (PropertyTableData) table.getModel();
			item_ = data.getPropertyItem(rowIndex);
			if (!isSelected)
				;
			if (!hasFocus)
				;
			Component comp;
			if (colIndex == 0) {
				JLabel lab = new JLabel();
				lab.setText((String) value);
				lab.setOpaque(true);
				lab.setHorizontalAlignment(2);
				comp = lab;
			} else if (colIndex == 1) {
				if (item_.hasRange) {
					SliderPanel slider = new SliderPanel();
					slider.setLimits(item_.lowerLimit, item_.upperLimit);
					slider.setText((String) value);
					slider.setToolTipText((String) value);
					comp = slider;
				} else {
					JLabel lab = new JLabel();
					lab.setOpaque(true);
					lab.setText(item_.value.toString());
					lab.setHorizontalAlignment(2);
					comp = lab;
				}
			} else {
				comp = new JLabel("Undefinded");
			}
			if (item_.readOnly)
				comp.setBackground(Color.LIGHT_GRAY);
			else
				comp.setBackground(Color.white);
			return comp;
		}

		public void validate() {
		}

		public void revalidate() {
		}

		protected void firePropertyChange(String s, Object obj, Object obj1) {
		}

		public void firePropertyChange(String s, boolean flag, boolean flag1) {
		}

		PropertyItem item_;
	}

	public class PropertyCellEditor extends AbstractCellEditor implements TableCellEditor {

		public void stopEditing() {
			fireEditingStopped();
		}

		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int colIndex) {
			if (!isSelected)
				;
			editingCol_ = colIndex;
			PropertyTableData data = (PropertyTableData) table.getModel();
			item_ = data.getPropertyItem(rowIndex);
			if (colIndex == 1) {
				if (item_.allowed.length == 0)
					if (item_.hasRange) {
						if (item_.isInteger())
							slider_.setLimits((int) item_.lowerLimit, (int) item_.upperLimit);
						else
							slider_.setLimits(item_.lowerLimit, item_.upperLimit);
						slider_.setText((String) value);
						return slider_;
					} else {
						text_.setText((String) value);
						return text_;
					}
				ActionListener l[] = combo_.getActionListeners();
				for (int i = 0; i < l.length; i++)
					combo_.removeActionListener(l[i]);

				combo_.removeAllItems();
				for (int i = 0; i < item_.allowed.length; i++)
					combo_.addItem(item_.allowed[i]);

				combo_.setSelectedItem(item_.value);
				combo_.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						fireEditingStopped();
					}
				});
				return combo_;
			}
			if (colIndex == 2)
				return check_;
			else
				return null;
		}

		public Object getCellEditorValue() {
			if (editingCol_ == 1)
				if (item_.allowed.length == 0) {
					if (item_.hasRange)
						return slider_.getText();
					else
						return text_.getText();
				} else {
					return combo_.getSelectedItem();
				}
			if (editingCol_ == 2)
				return check_;
			else
				return null;
		}

		private static final long serialVersionUID = 1L;
		JTextField text_;
		JComboBox combo_;
		JCheckBox check_;
		SliderPanel slider_;
		int editingCol_;
		PropertyItem item_;

		public PropertyCellEditor() {
			text_ = new JTextField();
			combo_ = new JComboBox();
			check_ = new JCheckBox();
			slider_ = new SliderPanel();
			check_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			slider_.addEditActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
			slider_.addSliderMouseListener(new MouseAdapter() {

				public void mouseReleased(MouseEvent e) {
					fireEditingStopped();
				}
			});
		}
	}

	class PropertyTableData extends AbstractTableModel {

		public void setShowReadOnly(boolean show) {
			showReadOnly_ = show;
		}

		public int getRowCount() {
			return propList_.size();
		}

		public int getColumnCount() {
			return columnNames_.length;
		}

		public PropertyItem getPropertyItem(int row) {
			return (PropertyItem) propList_.get(row);
		}

		public Object getValueAt(int row, int col) {
			PropertyItem item = (PropertyItem) propList_.get(row);
			if (col == 0)
				return (new StringBuilder()).append(item.device).append("-").append(item.name).toString();
			if (col == 1)
				return item.value;
			else
				return null;
		}

		public void setValueAt(Object value, int row, int col) {
			PropertyItem item = (PropertyItem) propList_.get(row);
			if (col == 1 && afMgr_.getDevice() != null)
				try {
					if (item.isInteger())
						afMgr_.getDevice().setPropertyValue(item.name, NumberUtils.intStringDisplayToCore(value));
					else if (item.isFloat())
						afMgr_.getDevice().setPropertyValue(item.name, NumberUtils.doubleStringDisplayToCore(value));
					else
						afMgr_.getDevice().setPropertyValue(item.name, value.toString());
					refresh();
					fireTableCellUpdated(row, col);
				} catch (Exception e) {
					handleException(e);
				}
		}

		public String getColumnName(int column) {
			return columnNames_[column];
		}

		public boolean isCellEditable(int nRow, int nCol) {
			if (nCol == 1)
				return !((PropertyItem) propList_.get(nRow)).readOnly;
			else
				return false;
		}

		public void refresh() {
			if (afMgr_.getDevice() == null)
				return;
			try {
				for (int i = 0; i < propList_.size(); i++) {
					PropertyItem item = (PropertyItem) propList_.get(i);
					item.value = afMgr_.getDevice().getPropertyValue(item.name);
				}

				fireTableDataChanged();
			} catch (Exception e) {
				handleException(e);
			}
		}

		public void updateStatus() {
			propList_.clear();
			PropertyItem properties[] = new PropertyItem[0];
			if (afMgr_.getDevice() != null)
				properties = afMgr_.getDevice().getProperties();
			for (int j = 0; j < properties.length; j++)
				if (showReadOnly_ && properties[j].readOnly || !properties[j].readOnly)
					propList_.add(properties[j]);

			fireTableStructureChanged();
		}

		public boolean isShowReadOnly() {
			return showReadOnly_;
		}

		private static final long serialVersionUID = 1L;
		public final String columnNames_[] = { "Property", "Value" };
		ArrayList<PropertyItem> propList_;
		private boolean showReadOnly_;

		public PropertyTableData() {
			propList_ = new ArrayList<PropertyItem>();
			showReadOnly_ = true;
			updateStatus();
		}
	}

	public AutofocusPropertyEditor(AutofocusManager afmgr) {
		afMgr_ = afmgr;
		setModal(false);
		data_ = new PropertyTableData();
		table_ = new JTable();
		table_.setAutoCreateColumnsFromModel(false);
		table_.setModel(data_);
		cellEditor_ = new PropertyCellEditor();
		PropertyCellRenderer renderer = new PropertyCellRenderer();
		for (int k = 0; k < data_.getColumnCount(); k++) {
			TableColumn column = new TableColumn(k, 200, renderer, cellEditor_);
			table_.addColumn(column);
		}

		Preferences root = Preferences.userNodeForPackage(getClass());
		setPrefsNode(root.node((new StringBuilder()).append(root.absolutePath()).append("/AutofocusPropertyEditor").toString()));
		springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		setSize(551, 514);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				cleanup();
			}

			public void windowOpened(WindowEvent e) {
				Preferences prefs = getPrefsNode();
				showReadonlyCheckBox_.setSelected(prefs.getBoolean("show_readonly", true));
				data_.updateStatus();
				data_.fireTableStructureChanged();
			}
		});
		setTitle("Autofocus properties");
		loadPosition(100, 100, 400, 300);
		scrollPane_ = new JScrollPane();
		scrollPane_.setFont(new Font("Arial", 0, 10));
		scrollPane_.setBorder(new BevelBorder(1));
		getContentPane().add(scrollPane_);
		springLayout.putConstraint("South", scrollPane_, -5, "South", getContentPane());
		springLayout.putConstraint("North", scrollPane_, 70, "North", getContentPane());
		springLayout.putConstraint("East", scrollPane_, -5, "East", getContentPane());
		springLayout.putConstraint("West", scrollPane_, 5, "West", getContentPane());
		scrollPane_.setViewportView(table_);
		table_ = new JTable();
		table_.setAutoCreateColumnsFromModel(false);
		JButton refreshButton = new JButton();
		springLayout.putConstraint("North", refreshButton, 10, "North", getContentPane());
		springLayout.putConstraint("West", refreshButton, 10, "West", getContentPane());
		springLayout.putConstraint("South", refreshButton, 33, "North", getContentPane());
		springLayout.putConstraint("East", refreshButton, 110, "West", getContentPane());
		refreshButton.setIcon(SwingResourceManager.getIcon("org / micromanager / utils / AutofocusPropertyEditor", "/org/micromanager/icons/arrow_refresh.png"));
		refreshButton.setFont(new Font("Arial", 0, 10));
		getContentPane().add(refreshButton);
		refreshButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		refreshButton.setText("Refresh! ");
		showReadonlyCheckBox_ = new JCheckBox();
		springLayout.putConstraint("North", showReadonlyCheckBox_, 41, "North", getContentPane());
		springLayout.putConstraint("West", showReadonlyCheckBox_, 10, "West", getContentPane());
		springLayout.putConstraint("South", showReadonlyCheckBox_, 64, "North", getContentPane());
		springLayout.putConstraint("East", showReadonlyCheckBox_, 183, "West", getContentPane());
		showReadonlyCheckBox_.setFont(new Font("Arial", 0, 10));
		showReadonlyCheckBox_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				data_.setShowReadOnly(showReadonlyCheckBox_.isSelected());
				data_.updateStatus();
				data_.fireTableStructureChanged();
			}
		});
		showReadonlyCheckBox_.setText("Show read-only properties");
		getContentPane().add(showReadonlyCheckBox_);
		Preferences prefs = getPrefsNode();
		showReadonlyCheckBox_.setSelected(prefs.getBoolean("show_readonly", true));
		btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				cleanup();
				dispose();
			}
		});
		springLayout.putConstraint("South", btnClose, 0, "South", refreshButton);
		springLayout.putConstraint("East", btnClose, -10, "East", getContentPane());
		getContentPane().add(btnClose);
		if (afMgr_ != null) {
			methodCombo_ = new JComboBox();
			String afDevs[] = afMgr_.getAfDevices();
			String arr$[] = afDevs;
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; i$++) {
				String devName = arr$[i$];
				methodCombo_.addItem(devName);
			}

			if (afMgr_.getDevice() != null)
				methodCombo_.setSelectedItem(afMgr_.getDevice().getDeviceName());
			methodCombo_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					changeAFMethod((String) methodCombo_.getSelectedItem());
				}
			});
			springLayout.putConstraint("West", methodCombo_, 80, "East", refreshButton);
			springLayout.putConstraint("South", methodCombo_, 0, "South", refreshButton);
			springLayout.putConstraint("East", methodCombo_, -6, "West", btnClose);
			getContentPane().add(methodCombo_);
		}
		data_.setShowReadOnly(showReadonlyCheckBox_.isSelected());
	}

	protected void changeAFMethod(String focusDev) {
		try {
			cellEditor_.stopEditing();
			afMgr_.selectDevice(focusDev);
		} catch (MMException e) {
			handleException(e);
		}
		updateStatus();
	}

	protected void refresh() {
		data_.refresh();
	}

	public void rebuild() {
		String afDevice = afMgr_.getDevice().getDeviceName();
		ActionListener l = methodCombo_.getActionListeners()[0];
		try {
			if (l != null)
				methodCombo_.removeActionListener(l);
		} catch (Exception e) {
			ReportingUtils.showError(e);
		}
		methodCombo_.removeAllItems();
		if (afMgr_ != null) {
			String afDevs[] = afMgr_.getAfDevices();
			String arr$[] = afDevs;
			int len$ = arr$.length;
			for (int i$ = 0; i$ < len$; i$++) {
				String devName = arr$[i$];
				methodCombo_.addItem(devName);
			}

			methodCombo_.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					changeAFMethod((String) methodCombo_.getSelectedItem());
				}
			});
			if (afDevice != null)
				methodCombo_.setSelectedItem(afDevice);
			else if (afMgr_.getDevice() != null)
				methodCombo_.setSelectedItem(afMgr_.getDevice().getDeviceName());
		}
	}

	public void updateStatus() {
		if (data_ != null)
			data_.updateStatus();
	}

	private void handleException(Exception e) {
		ReportingUtils.showError(e);
	}

	public void cleanup() {
		savePosition();
		Preferences prefs = getPrefsNode();
		prefs.putBoolean("show_readonly", showReadonlyCheckBox_.isSelected());
		if (afMgr_ != null && afMgr_.getDevice() != null)
			afMgr_.getDevice().saveSettings();
	}

	private SpringLayout springLayout;
	private static final long serialVersionUID = 1507097881635431043L;
	private JTable table_;
	private PropertyTableData data_;
	private PropertyCellEditor cellEditor_;
	private JCheckBox showReadonlyCheckBox_;
	// private static final String PREF_SHOW_READONLY = "show_readonly";
	private JScrollPane scrollPane_;
	private AutofocusManager afMgr_;
	private JButton btnClose;
	private JComboBox methodCombo_;

}
