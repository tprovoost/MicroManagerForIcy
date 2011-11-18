package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.gui.frame.IcyFrame;
import icy.system.thread.ThreadUtil;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;

import mmcorej.CMMCore;
import mmcorej.StrVector;

import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.PropertyNameCellRenderer;
import org.micromanager.utils.PropertyTableData;
import org.micromanager.utils.PropertyValueCellEditor;
import org.micromanager.utils.PropertyValueCellRenderer;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.ShowFlags;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

import com.swtdesigner.SwingResourceManager;

public class PropertyEditor extends IcyFrame {
	private SpringLayout springLayout;
	private JTable table;
	private PropertyEditorTableData data;
	private ShowFlags flags;
	private JCheckBox showCamerasCheckBox;
	private JCheckBox showShuttersCheckBox;
	private JCheckBox showStagesCheckBox;
	private JCheckBox showStateDevicesCheckBox;
	private JCheckBox showOtherCheckBox;
	private JCheckBox showReadonlyCheckBox;
	private JScrollPane scrollPane;
	private MMMainFrame gui;

	public PropertyEditor(MMMainFrame gui) {
		super("Property Browser", true, true, true, true);
		this.gui = gui;
		flags = new ShowFlags();
		springLayout = new SpringLayout();
		getContentPane().setLayout(springLayout);
		setSize(551, 514);

		scrollPane = new JScrollPane();
		scrollPane.setFont(new Font("Arial", 0, 10));
		scrollPane.setBorder(new BevelBorder(1));
		getContentPane().add(scrollPane);
		springLayout.putConstraint("East", scrollPane, -5, "East", getContentPane());
		springLayout.putConstraint("West", scrollPane, 5, "West", getContentPane());

		table = new JTable();
		table.setAutoCreateColumnsFromModel(false);

		JButton refreshButton = new JButton();
		refreshButton.setIcon(SwingResourceManager.getIcon(PropertyEditor.class, "/org/micromanager/icons/arrow_refresh.png"));
		refreshButton.setFont(new Font("Arial", 0, 10));
		getContentPane().add(refreshButton);
		springLayout.putConstraint("East", refreshButton, 285, "West", getContentPane());
		springLayout.putConstraint("West", refreshButton, 185, "West", getContentPane());
		springLayout.putConstraint("South", refreshButton, 32, "North", getContentPane());
		springLayout.putConstraint("North", refreshButton, 9, "North", getContentPane());
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		refreshButton.setText("Refresh! ");

		showReadonlyCheckBox = new JCheckBox();
		showReadonlyCheckBox.setFont(new Font("Arial", 0, 10));
		showReadonlyCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				data.setShowReadOnly(showReadonlyCheckBox.isSelected());
				data.update();
				data.fireTableStructureChanged();
			}
		});
		showReadonlyCheckBox.setText("Show read-only properties");
		getContentPane().add(showReadonlyCheckBox);
		springLayout.putConstraint("East", showReadonlyCheckBox, 358, "West", getContentPane());
		springLayout.putConstraint("West", showReadonlyCheckBox, 185, "West", getContentPane());
		springLayout.putConstraint("South", showReadonlyCheckBox, 63, "North", getContentPane());
		springLayout.putConstraint("North", showReadonlyCheckBox, 40, "North", getContentPane());

		showCamerasCheckBox = new JCheckBox();
		showCamerasCheckBox.setFont(new Font("", 0, 10));
		showCamerasCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				flags.cameras_ = showCamerasCheckBox.isSelected();
				data.update();
			}
		});
		showCamerasCheckBox.setText("Show cameras");
		getContentPane().add(showCamerasCheckBox);
		springLayout.putConstraint("South", showCamerasCheckBox, 28, "North", getContentPane());
		springLayout.putConstraint("West", showCamerasCheckBox, 10, "West", getContentPane());
		springLayout.putConstraint("East", showCamerasCheckBox, 111, "West", getContentPane());

		showShuttersCheckBox = new JCheckBox();
		showShuttersCheckBox.setFont(new Font("", 0, 10));
		showShuttersCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				flags.shutters_ = showShuttersCheckBox.isSelected();
				data.update();
			}
		});
		showShuttersCheckBox.setText("Show shutters");
		getContentPane().add(showShuttersCheckBox);
		springLayout.putConstraint("East", showShuttersCheckBox, 111, "West", getContentPane());
		springLayout.putConstraint("West", showShuttersCheckBox, 10, "West", getContentPane());
		springLayout.putConstraint("South", showShuttersCheckBox, 50, "North", getContentPane());

		showStagesCheckBox = new JCheckBox();
		showStagesCheckBox.setFont(new Font("", 0, 10));
		showStagesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				flags.stages_ = showStagesCheckBox.isSelected();
				data.update();
			}
		});
		showStagesCheckBox.setText("Show stages");
		getContentPane().add(showStagesCheckBox);
		springLayout.putConstraint("East", showStagesCheckBox, 111, "West", getContentPane());
		springLayout.putConstraint("West", showStagesCheckBox, 10, "West", getContentPane());
		springLayout.putConstraint("South", showStagesCheckBox, 73, "North", getContentPane());
		springLayout.putConstraint("North", showStagesCheckBox, 50, "North", getContentPane());

		showStateDevicesCheckBox = new JCheckBox();
		showStateDevicesCheckBox.setFont(new Font("", 0, 10));
		showStateDevicesCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				flags.state_ = showStateDevicesCheckBox.isSelected();
				data.update();
			}
		});
		showStateDevicesCheckBox.setText("Show discrete changers");
		getContentPane().add(showStateDevicesCheckBox);
		springLayout.putConstraint("East", showStateDevicesCheckBox, 200, "West", getContentPane());
		springLayout.putConstraint("West", showStateDevicesCheckBox, 10, "West", getContentPane());
		springLayout.putConstraint("South", showStateDevicesCheckBox, 95, "North", getContentPane());
		springLayout.putConstraint("North", showStateDevicesCheckBox, 72, "North", getContentPane());

		showOtherCheckBox = new JCheckBox();
		showOtherCheckBox.setFont(new Font("", 0, 10));
		showOtherCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				flags.other_ = showOtherCheckBox.isSelected();
				data.update();
			}
		});
		showOtherCheckBox.setText("Show other devices");
		getContentPane().add(showOtherCheckBox);
		springLayout.putConstraint("East", showOtherCheckBox, 155, "West", getContentPane());
		springLayout.putConstraint("West", showOtherCheckBox, 10, "West", getContentPane());
		springLayout.putConstraint("North", showOtherCheckBox, 95, "North", getContentPane());
		springLayout.putConstraint("South", scrollPane, -5, "South", getContentPane());
		springLayout.putConstraint("North", scrollPane, 5, "South", showOtherCheckBox);
	}

	public void start() {
		data.start();
	}

	public void stop() {
		data.stop();
	}

	public void pauseThread() {
		data.pause();
	}

	public void resumeThread() {
		data.resume();
	}

	public void refresh() {
		data.flags_ = flags;
		data.showUnused_ = true;
		data.refresh();
	}

	public void updateStatus() {
		if (data != null)
			data.update();
	}

	public void setCore(CMMCore core) {
		data = new PropertyEditorTableData(core, "", "", 1, 2, getContentPane());
		data.flags_ = flags;
		data.showUnused_ = true;
		data.setColumnNames("Property", "Value", "");

		table = new JTable();
		table.setAutoCreateColumnsFromModel(false);
		table.setModel(data);
		scrollPane.setViewportView(table);

		table.addColumn(new TableColumn(0, 200, new PropertyNameCellRenderer(), null));
		table.addColumn(new TableColumn(1, 200, new PropertyValueCellRenderer(false), new PropertyValueCellEditor(false)));

		showCamerasCheckBox.setSelected(flags.cameras_);
		showStagesCheckBox.setSelected(flags.stages_);
		showShuttersCheckBox.setSelected(flags.shutters_);
		showStateDevicesCheckBox.setSelected(flags.state_);
		showOtherCheckBox.setSelected(flags.other_);

		data.setShowReadOnly(showReadonlyCheckBox.isSelected());
	}

	private void handleException(Exception e) {
		ReportingUtils.showError(e);
	}

	public class PropertyEditorTableData extends PropertyTableData {

		private static final long serialVersionUID = 1L;
		ThreadUpdater thread = null;

		public PropertyEditorTableData(CMMCore core, String groupName, String presetName, int PropertyValueColumn, int PropertyUsedColumn, Component parentComponent) {
			super(core, groupName, presetName, PropertyValueColumn, PropertyUsedColumn, parentComponent);
		}

		public void setValueAt(Object value, int row, int col) {
			PropertyItem item = (PropertyItem) propListVisible_.get(row);
			ReportingUtils.logMessage("Setting value " + value + " at row " + row);
			if (col == PropertyValueColumn_) {
				setValueInCore(item, value);
			}
			refresh();
			fireTableCellUpdated(row, col);
		}

		@Override
		public void refresh() {
			update();
			try {
				for (PropertyItem it : propList_) {
					if (showDevice(flags_, it.device).booleanValue())
						it.setValueFromCoreString(core_.getProperty(it.device, it.name));
				}
				fireTableDataChanged();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void update() {
			update(flags_, groupName_, presetName_);
		}

		public void update(String device, String propName, String newValue) {
			PropertyItem item = getItem(device, propName);
			if (item != null) {
				item.value = newValue;
				fireTableDataChanged();
			}
		}

		public void update(ShowFlags flags, String groupName, String presetName) {
			try {
				StrVector devices = core_.getLoadedDevices();
				propList_.clear();
				for (int i = 0; i < devices.size(); i++) {
					if (data.showDevice(flags, devices.get(i)).booleanValue()) {
						StrVector properties = core_.getDevicePropertyNames(devices.get(i));
						for (int j = 0; j < properties.size(); j++) {
							PropertyItem item = new PropertyItem();
							item.readFromCore(core_, devices.get(i), properties.get(j));

							if (((!item.readOnly) || (showReadOnly_)) && (!item.preInit)) {
								propList_.add(item);
							}
						}
					}
				}
				updateRowVisibility(flags);
			} catch (Exception e) {
				handleException(e);
			}
			fireTableStructureChanged();
		}

		public void start() {
			if (thread == null) {
				thread = new ThreadUpdater();
				ThreadUtil.bgRun(thread);
			}
		}

		public void stop() {
			if (thread != null) {
				thread.stop();
				thread = null;
			}
		}

		public void pause() {
			if (thread != null) {
				thread.pause();
			}
		}

		public void resume() {
			if (thread != null) {
				thread.resume();
			}
		}

		class ThreadUpdater implements Runnable {

			private boolean pause = false;
			private boolean run = true;

			@Override
			public void run() {
				while (run) {
					while (pause) {
						try {
							Thread.sleep(50);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					ThreadUtil.invokeLater(new Runnable() {

						@Override
						public void run() {
							gui.configChanged();
						}
					});
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			public synchronized void stop() {
				run = false;
			}

			public synchronized void pause() {
				pause = true;
			}

			public void resume() {
				pause = false;
			}

		}
	}
}