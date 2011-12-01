package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpringLayout;
import javax.swing.border.BevelBorder;
import javax.swing.table.TableColumn;

import mmcorej.CMMCore;
import mmcorej.Configuration;

import org.micromanager.utils.MMDialog;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.ShowFlags;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class ConfigDialog extends MMDialog {

	private static final long serialVersionUID = 5819669941239786807L;
	protected CMMCore _core;
	protected MMMainFrame _gui;
	private JTable _table;
	protected PropertyTableData _data;
	private JScrollPane _scrollPane;
	private ShowFlags _flags;
	private ShowFlagsPanel _showFlagsPanel;
	private SpringLayout _springLayout;
	private JTextArea textArea_;
	protected JTextField nameField_;
	private JLabel nameFieldLabel_;
	private JButton okButton_;
	private JButton cancelButton_;
	protected String TITLE;
	protected String instructionsText_;
	protected String nameFieldLabelText_;
	protected String initName_;
	protected String groupName_;
	protected String presetName_;
	protected Boolean showUnused_;
	protected int numColumns;
	protected boolean newItem_;
	protected boolean showFlagsPanelVisible;
	protected int scrollPaneTop_;
	
	public ConfigDialog(String groupName, String presetName, MMMainFrame gui_2, CMMCore core, boolean newItem) {
		TITLE = "";
		instructionsText_ = "Instructions go here.";
		nameFieldLabelText_ = "GroupOrPreset Name";
		initName_ = "";
		showUnused_ = Boolean.valueOf(true);
		numColumns = 3;
		newItem_ = true;
		showFlagsPanelVisible = true;
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent arg0) {
				savePosition();
			}
		});
		groupName_ = groupName;
		presetName_ = presetName;
		newItem_ = newItem;
		_gui = gui_2;
		_core = core;
		_springLayout = new SpringLayout();
		getContentPane().setLayout(_springLayout);
		setBounds(100, 100, 550, 600);
		Rectangle r = getBounds();
		loadPosition(r.x, r.y);
		setResizable(false);
	}

	public void initialize() {
		initializePropertyTable();
		initializeWidgets();
		initializeFlags();
		setupKeys();
		setVisible(true);
		setTitle(TITLE);
		nameField_.requestFocus();
		setFocusable(true);
		update();
	}

	protected void setupKeys() {
		JRootPane rootPane = getRootPane();
		InputMap inputMap = rootPane.getInputMap(2);
		ActionMap actionMap = rootPane.getActionMap();
		inputMap.put(KeyStroke.getKeyStroke(10, 0), "enter");
		actionMap.put("enter", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				okChosen();
			}
		});
		inputMap.put(KeyStroke.getKeyStroke(27, 0), "escape");
		actionMap.put("escape", new AbstractAction() {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				cancelChosen();
			}
		});
		_table.getInputMap(1).put(KeyStroke.getKeyStroke(10, 0), "none");
	}

	public void initializeData() {
		_data.setGUI(_gui);
		_data.setShowUnused(showUnused_.booleanValue());
	}

	protected void initializeWidgets() {
		textArea_ = new JTextArea();
		textArea_.setFont(new Font("Arial", 0, 12));
		textArea_.setWrapStyleWord(true);
		textArea_.setText(instructionsText_);
		textArea_.setEditable(false);
		textArea_.setOpaque(false);
		getContentPane().add(textArea_);
		_springLayout.putConstraint("East", textArea_, 250, "West", getContentPane());
		_springLayout.putConstraint("West", textArea_, 5, "West", getContentPane());
		_springLayout.putConstraint("South", textArea_, 37, "North", getContentPane());
		_springLayout.putConstraint("North", textArea_, 5, "North", getContentPane());
		nameField_ = new JTextField();
		nameField_.setText(initName_);
		nameField_.setEditable(true);
		nameField_.setSelectionStart(0);
		nameField_.setSelectionEnd(nameField_.getText().length());
		getContentPane().add(nameField_);
		_springLayout.putConstraint("East", nameField_, 280, "West", getContentPane());
		_springLayout.putConstraint("West", nameField_, 95, "West", getContentPane());
		_springLayout.putConstraint("South", nameField_, -3, "North", _scrollPane);
		_springLayout.putConstraint("North", nameField_, -30, "North", _scrollPane);
		nameFieldLabel_ = new JLabel();
		nameFieldLabel_.setText(nameFieldLabelText_);
		nameFieldLabel_.setFont(new Font("Arial", 1, 12));
		nameFieldLabel_.setHorizontalAlignment(4);
		getContentPane().add(nameFieldLabel_);
		_springLayout.putConstraint("East", nameFieldLabel_, 90, "West", getContentPane());
		_springLayout.putConstraint("West", nameFieldLabel_, 5, "West", getContentPane());
		_springLayout.putConstraint("South", nameFieldLabel_, -3, "North", _scrollPane);
		_springLayout.putConstraint("North", nameFieldLabel_, -30, "North", _scrollPane);
		okButton_ = new JButton("OK");
		getContentPane().add(okButton_);
		_springLayout.putConstraint("East", okButton_, -5, "East", getContentPane());
		_springLayout.putConstraint("West", okButton_, -105, "East", getContentPane());
		_springLayout.putConstraint("South", okButton_, 30, "North", getContentPane());
		_springLayout.putConstraint("North", okButton_, 5, "North", getContentPane());
		okButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				okChosen();
			}
		});
		cancelButton_ = new JButton("Cancel");
		getContentPane().add(cancelButton_);
		_springLayout.putConstraint("East", cancelButton_, -5, "East", getContentPane());
		_springLayout.putConstraint("West", cancelButton_, -105, "East", getContentPane());
		_springLayout.putConstraint("South", cancelButton_, 57, "North", getContentPane());
		_springLayout.putConstraint("North", cancelButton_, 32, "North", getContentPane());
		cancelButton_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				cancelChosen();
			}
		});
	}

	protected void initializeFlags() {
		_flags = new ShowFlags();
		if (showFlagsPanelVisible) {
			_flags.load(getPrefsNode());
			try {
				Configuration cfg;
				if (presetName_.length() == 0)
					cfg = new Configuration();
				else
					cfg = _core.getConfigState(groupName_, presetName_);
				_showFlagsPanel = new ShowFlagsPanel(_data, _flags, _core, cfg);
			} catch (Exception e) {
				ReportingUtils.showError(e);
			}
			getContentPane().add(_showFlagsPanel);
			_springLayout.putConstraint("East", _showFlagsPanel, 440, "West", getContentPane());
			_springLayout.putConstraint("West", _showFlagsPanel, 290, "West", getContentPane());
			_springLayout.putConstraint("South", _showFlagsPanel, 135, "North", getContentPane());
			_springLayout.putConstraint("North", _showFlagsPanel, 5, "North", getContentPane());
		}
		_data.setFlags(_flags);
	}

	public void initializePropertyTable() {
		_scrollPane = new JScrollPane();
		_scrollPane.setFont(new Font("Arial", 0, 10));
		_scrollPane.setBorder(new BevelBorder(1));
		getContentPane().add(_scrollPane);
		_springLayout.putConstraint("South", _scrollPane, -5, "South", getContentPane());
		_springLayout.putConstraint("North", _scrollPane, scrollPaneTop_, "North", getContentPane());
		_springLayout.putConstraint("East", _scrollPane, -5, "East", getContentPane());
		_springLayout.putConstraint("West", _scrollPane, 5, "West", getContentPane());
		_table = new JTable();
		_table.setSelectionMode(2);
		_table.setAutoCreateColumnsFromModel(false);
		_scrollPane.setViewportView(_table);
		_table = new JTable();
		_table.setSelectionMode(2);
		_table.setAutoCreateColumnsFromModel(false);
		_scrollPane.setViewportView(_table);
		_table.setModel(_data);
		if (numColumns == 3) {
			_table.addColumn(new TableColumn(0, 200, new PropertyNameCellRenderer(), null));
			_table.addColumn(new TableColumn(1, 75, new PropertyUsageCellRenderer(), new PropertyUsageCellEditor()));
			_table.addColumn(new TableColumn(2, 200, new PropertyValueCellRenderer(true), new PropertyValueCellEditor(
					true)));
		} else if (numColumns == 2) {
			_table.addColumn(new TableColumn(0, 200, new PropertyNameCellRenderer(), null));
			_table.addColumn(new TableColumn(1, 200, new PropertyValueCellRenderer(false), new PropertyValueCellEditor(
					false)));
		}
	}

	public void okChosen() {
		_gui.notifyConfigChanged(null);
	}

	public void cancelChosen() {
		dispose();
	}

	public void dispose() {
		super.dispose();
		savePosition();
		_gui.refreshGUI();
		if (_gui instanceof MMMainFrame)
			((MMMainFrame) _gui).selectConfigGroup(groupName_);
		_gui.notifyConfigChanged(null);
	}

	public void update() {
		_data.update();
	}

	public void showMessageDialog(String message) {
		JOptionPane.showMessageDialog(this, message);
	}
}