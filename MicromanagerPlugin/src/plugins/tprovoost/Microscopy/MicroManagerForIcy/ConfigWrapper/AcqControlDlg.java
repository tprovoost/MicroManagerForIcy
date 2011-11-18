package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.gui.dialog.ConfirmDialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.micromanager.acquisition.ComponentTitledBorder;
import org.micromanager.api.AcquisitionEngine;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.ColorEditor;
import org.micromanager.utils.ColorRenderer;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.DisplayMode;
import org.micromanager.utils.MMException;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.NumberUtils;
import org.micromanager.utils.PositionMode;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.SliceMode;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

public class AcqControlDlg extends JPanel implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	
	private MicroscopeCore _core = MicroscopeCore.getCore();
	protected JButton listButton_;
	private JButton afButton_;
	private JSpinner afSkipInterval_;
	private JComboBox sliceModeCombo_;
	protected JComboBox posModeCombo_;
	public static final String NEW_ACQFILE_NAME = "MMAcquistion.xml";
	public static final String ACQ_SETTINGS_NODE = "AcquistionSettings";
	public static final String COLOR_SETTINGS_NODE = "ColorSettings";
	private JComboBox _channelGroupCombo;
	private JTextArea commentTextArea_;
	private JComboBox zValCombo_;
	private JTextField nameField_;
	private JTextField rootField_;
	private JTextArea summaryTextArea_;
	private JComboBox timeUnitCombo_;
	private JFormattedTextField interval_;
	private JFormattedTextField zStep_;
	private JFormattedTextField zTop_;
	private JFormattedTextField zBottom_;
	private MMAcquisitionEngineMT _acqEng;
	private JScrollPane channelTablePane_;
	private JTable channelTable_;
	private JSpinner _numFrames;
	private ChannelTableModel model_;
	private Preferences prefs_;
	private Preferences acqPrefs_;
	private Preferences colorPrefs_;
	private File acqFile_;
	private String acqDir_;
	private int zVals_ = 0;
	private JButton setBottomButton_;
	private JButton setTopButton_;
	protected JComboBox displayModeCombo_;
	private MMMainFrame gui_;
	private NumberFormat numberFormat_;
	private JLabel namePrefixLabel_;
	private JLabel rootLabel_;
	private JLabel commentLabel_;
	private JButton browseRootButton_;
	private JLabel displayMode_;
	private JCheckBox stackKeepShutterOpenCheckBox_;
	private JCheckBox chanKeepShutterOpenCheckBox_;
	
	private int[] columnWidth_;
	private int[] columnOrder_;
	private CheckBoxPanel framesPanel_;
	private CheckBoxPanel channelsPanel_;
	private CheckBoxPanel slicesPanel_;
	protected CheckBoxPanel positionsPanel_;
	private JPanel acquisitionOrderPanel_;
	private CheckBoxPanel afPanel_;
	private JPanel summaryPanel_;
	private CheckBoxPanel savePanel_;
	@SuppressWarnings("unused")
	private Border dayBorder_;
	@SuppressWarnings("unused")
	private Border nightBorder_;
	private Vector<JPanel> panelList_;
	private boolean disableGUItoSettings_ = false;
	private JButton _btn_acquire;
	private JButton _btn_pauseResume;
	private JButton _btn_stop;

	public void createChannelTable() {
		this.channelTable_ = new JTable();
		this.channelTable_.setFont(new Font("Dialog", 0, 10));
		this.channelTable_.setAutoCreateColumnsFromModel(false);
		this.model_ = new ChannelTableModel(this._acqEng);
		this.channelTable_.setModel(this.model_);
		this.model_.setChannels(this._acqEng.getChannels());

		ChannelCellEditor cellEditor = new ChannelCellEditor();
		ChannelCellRenderer cellRenderer = new ChannelCellRenderer(this._acqEng);
		this.channelTable_.setAutoResizeMode(0);
		for (int k = 0; k < this.model_.getColumnCount(); k++) {
			int colIndex = search(this.columnOrder_, k);
			if (colIndex < 0) {
				colIndex = k;
			}
			if (colIndex == this.model_.getColumnCount() - 1) {
				ColorRenderer cr = new ColorRenderer(true);
				ColorEditor ce = new ColorEditor(this.model_,
						this.model_.getColumnCount() - 1);
				TableColumn column = new TableColumn(
						this.model_.getColumnCount() - 1, 200, cr, ce);
				column.setPreferredWidth(this.columnWidth_[(this.model_
						.getColumnCount() - 1)]);
				this.channelTable_.addColumn(column);
			} else {
				TableColumn column = new TableColumn(colIndex, 200,
						cellRenderer, cellEditor);
				column.setPreferredWidth(this.columnWidth_[colIndex]);
				this.channelTable_.addColumn(column);
			}
		}
		this.channelTablePane_.setViewportView(this.channelTable_);
	}

	public JPanel createPanel(String text, int left, int top, int right,
			int bottom) {
		return createPanel(text, left, top, right, bottom, false);
	}

	public JPanel createPanel(String text, int left, int top, int right,
			int bottom, boolean checkBox) {
		ComponentTitledPanel thePanel;
		if (checkBox)
			thePanel = new CheckBoxPanel(text);
		else {
			thePanel = new LabelPanel(text);
		}

		thePanel.setTitleFont(new Font("Dialog", 1, 12));
		panelList_.add(thePanel);
		thePanel.setBounds(left, top, right - left, bottom - top);
		dayBorder_ = BorderFactory.createEtchedBorder();
		nightBorder_ = BorderFactory.createEtchedBorder(Color.gray,
				Color.darkGray);

		thePanel.setLayout(null);
		thePanel.setEnabled(true);
		add(thePanel);
		return thePanel;
	}

	public void updatePanelBorder(JPanel thePanel) {
		/*
		 * TitledBorder border = (TitledBorder) thePanel.getBorder(); if
		 * (gui_.getBackgroundStyle().contentEquals("Day"))
		 * border.setBorder(dayBorder_); else border.setBorder(nightBorder_);
		 */
	}

	public void createEmptyPanels() {
		panelList_ = new Vector<JPanel>();

		framesPanel_ = ((CheckBoxPanel) createPanel("Time points", 5, 5, 220,
				91, true));
		positionsPanel_ = ((CheckBoxPanel) createPanel(
				"Multiple positions (XY)", 5, 93, 220, 154, true));
		slicesPanel_ = ((CheckBoxPanel) createPanel("Z-stacks (slices)", 5,
				156, 220, 306, true));

		acquisitionOrderPanel_ = createPanel("Acquisition order", 226, 5, 415,
				91);
		summaryPanel_ = createPanel("Summary", 226, 180, 415, 306);
		afPanel_ = ((CheckBoxPanel) createPanel("Autofocus", 226, 93, 415, 178,
				true));
		channelsPanel_ = ((CheckBoxPanel) createPanel("Channels", 5, 308, 510,
				451, true));
		savePanel_ = ((CheckBoxPanel) createPanel("Save images", 5, 453, 510, 620, true));
		// savePanel_ = new CheckBoxPanel("unused");
	}

	public AcqControlDlg(MMAcquisitionEngineMT acqEng, Preferences prefs,
			MMMainFrame gui) {
		prefs_ = prefs;
		gui_ = gui;

		Preferences root = Preferences.userNodeForPackage(getClass());
		acqPrefs_ = root.node(root.absolutePath() + "/" + "AcquistionSettings");
		colorPrefs_ = root.node(root.absolutePath() + "/" + "ColorSettings");

		numberFormat_ = NumberFormat.getNumberInstance();
		_acqEng = acqEng;

		setLayout(null);

		createEmptyPanels();

		framesPanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		JLabel numberLabel = new JLabel();
		numberLabel.setFont(new Font("Arial", 0, 10));

		numberLabel.setText("Number");
		framesPanel_.add(numberLabel);
		numberLabel.setBounds(15, 25, 54, 24);

		SpinnerModel sModel = new SpinnerNumberModel(new Integer(1),
				new Integer(1), null, new Integer(1));

		_numFrames = new JSpinner(sModel);
		((JSpinner.DefaultEditor) this._numFrames.getEditor()).getTextField()
				.setFont(new Font("Arial", 0, 10));

		framesPanel_.add(this._numFrames);
		_numFrames.setBounds(60, 25, 70, 24);
		_numFrames.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		JLabel intervalLabel = new JLabel();
		intervalLabel.setFont(new Font("Arial", 0, 10));
		intervalLabel.setText("Interval");
		framesPanel_.add(intervalLabel);
		intervalLabel.setBounds(15, 52, 43, 24);

		interval_ = new JFormattedTextField(this.numberFormat_);
		interval_.setFont(new Font("Arial", 0, 10));
		interval_.setValue(new Double(1.0D));
		interval_.addPropertyChangeListener("value", this);
		framesPanel_.add(this.interval_);
		interval_.setBounds(60, 52, 55, 24);

		timeUnitCombo_ = new JComboBox();
		timeUnitCombo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.interval_.setText(NumberUtils
						.doubleToDisplayString(AcqControlDlg.this
								.convertMsToTime(AcqControlDlg.this._acqEng
										.getFrameIntervalMs(),
										AcqControlDlg.this.timeUnitCombo_
												.getSelectedIndex())));
			}
		});
		timeUnitCombo_.setModel(new DefaultComboBoxModel(new String[] { "ms",
				"s", "min" }));
		timeUnitCombo_.setFont(new Font("Arial", 0, 10));
		timeUnitCombo_.setBounds(120, 52, 67, 24);
		framesPanel_.add(this.timeUnitCombo_);

		listButton_ = new JButton();
		listButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gui_.showXYPositionList();
			}
		});
		listButton_.setToolTipText("Open XY list dialog");
		listButton_.setText("Edit position list...");
		listButton_.setMargin(new Insets(2, 5, 2, 5));
		listButton_.setFont(new Font("Dialog", 0, 10));
		listButton_.setBounds(42, 25, 136, 26);
		positionsPanel_.add(this.listButton_);

		slicesPanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		JLabel zbottomLabel = new JLabel();
		zbottomLabel.setFont(new Font("Arial", 0, 10));
		zbottomLabel.setText("Z-start [um]");
		zbottomLabel.setBounds(30, 30, 69, 15);
		slicesPanel_.add(zbottomLabel);

		zBottom_ = new JFormattedTextField(this.numberFormat_);
		zBottom_.setFont(new Font("Arial", 0, 10));
		zBottom_.setBounds(95, 27, 54, 21);
		zBottom_.setValue(new Double(1.0D));
		zBottom_.addPropertyChangeListener("value", this);
		slicesPanel_.add(this.zBottom_);

		setBottomButton_ = new JButton();
		setBottomButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.setBottomPosition();
			}
		});
		setBottomButton_.setMargin(new Insets(-5, -5, -5, -5));
		setBottomButton_.setFont(new Font("", 0, 10));
		setBottomButton_.setText("Set");
		setBottomButton_.setBounds(150, 27, 50, 22);
		slicesPanel_.add(this.setBottomButton_);

		JLabel ztopLabel = new JLabel();
		ztopLabel.setFont(new Font("Arial", 0, 10));
		ztopLabel.setText("Z-end [um]");
		ztopLabel.setBounds(30, 53, 69, 15);
		slicesPanel_.add(ztopLabel);

		zTop_ = new JFormattedTextField(this.numberFormat_);
		zTop_.setFont(new Font("Arial", 0, 10));
		zTop_.setBounds(95, 50, 54, 21);
		zTop_.setValue(new Double(1.0D));
		zTop_.addPropertyChangeListener("value", this);
		slicesPanel_.add(this.zTop_);

		setTopButton_ = new JButton();
		setTopButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.setTopPosition();
			}
		});
		setTopButton_.setMargin(new Insets(-5, -5, -5, -5));
		setTopButton_.setFont(new Font("Dialog", 0, 10));
		setTopButton_.setText("Set");
		setTopButton_.setBounds(150, 50, 50, 22);
		slicesPanel_.add(this.setTopButton_);

		JLabel zstepLabel = new JLabel();
		zstepLabel.setFont(new Font("Arial", 0, 10));
		zstepLabel.setText("Z-step [um]");
		zstepLabel.setBounds(30, 76, 69, 15);
		slicesPanel_.add(zstepLabel);

		zStep_ = new JFormattedTextField(this.numberFormat_);
		zStep_.setFont(new Font("Arial", 0, 10));
		zStep_.setBounds(95, 73, 54, 21);
		zStep_.setValue(new Double(1.0D));
		zStep_.addPropertyChangeListener("value", this);
		slicesPanel_.add(this.zStep_);

		zValCombo_ = new JComboBox();
		zValCombo_.setFont(new Font("Arial", 0, 10));
		zValCombo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.zValCalcChanged();
			}
		});
		zValCombo_.setModel(new DefaultComboBoxModel(new String[] {
				"relative Z", "absolute Z" }));
		zValCombo_.setBounds(30, 97, 110, 22);
		slicesPanel_.add(this.zValCombo_);

		stackKeepShutterOpenCheckBox_ = new JCheckBox();
		stackKeepShutterOpenCheckBox_.setText("Keep shutter open");
		stackKeepShutterOpenCheckBox_.setFont(new Font("Arial", 0, 10));
		stackKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		stackKeepShutterOpenCheckBox_.setSelected(false);
		stackKeepShutterOpenCheckBox_.setBounds(60, 121, 150, 22);
		slicesPanel_.add(this.stackKeepShutterOpenCheckBox_);

		posModeCombo_ = new JComboBox();
		posModeCombo_.setFont(new Font("", 0, 10));
		posModeCombo_.setBounds(15, 23, 151, 22);
		posModeCombo_.setEnabled(false);
		acquisitionOrderPanel_.add(this.posModeCombo_);
		posModeCombo_.addItem(new PositionMode(0));
		posModeCombo_.addItem(new PositionMode(1));

		sliceModeCombo_ = new JComboBox();
		sliceModeCombo_.setFont(new Font("", 0, 10));
		sliceModeCombo_.setBounds(15, 49, 151, 22);
		acquisitionOrderPanel_.add(this.sliceModeCombo_);
		sliceModeCombo_.addItem(new SliceMode(0));
		sliceModeCombo_.addItem(new SliceMode(1));

		summaryTextArea_ = new JTextArea();
		summaryTextArea_.setFont(new Font("Arial", 0, 10));
		summaryTextArea_.setEditable(false);
		summaryTextArea_.setBounds(4, 19, 273, 99);
		summaryTextArea_.setMargin(new Insets(2, 2, 2, 2));
		summaryTextArea_.setOpaque(false);
		summaryPanel_.add(this.summaryTextArea_);

		afPanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AcqControlDlg.this.applySettings();
			}
		});
		afButton_ = new JButton();
		afButton_.setToolTipText("Set autofocus options");
		afButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AcqControlDlg.this.afOptions();
			}
		});
		afButton_.setText("Options...");
		afButton_.setMargin(new Insets(2, 5, 2, 5));
		afButton_.setFont(new Font("Dialog", 0, 10));
		afButton_.setBounds(50, 21, 100, 28);
		afPanel_.add(this.afButton_);

		JLabel afSkipFrame1 = new JLabel();
		afSkipFrame1.setFont(new Font("Dialog", 0, 10));
		afSkipFrame1.setText("Skip frame(s): ");
		afSkipFrame1.setBounds(35, 54, 70, 21);
		afPanel_.add(afSkipFrame1);

		afSkipInterval_ = new JSpinner(new SpinnerNumberModel(
				Integer.valueOf(0), Integer.valueOf(0), null,
				Integer.valueOf(1)));
		((JSpinner.DefaultEditor) afSkipInterval_.getEditor()).getTextField()
				.setFont(new Font("Arial", 0, 10));
		afSkipInterval_.setBounds(105, 54, 55, 22);
		afSkipInterval_.setValue(new Integer(this._acqEng.getAfSkipInterval()));
		afSkipInterval_.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				AcqControlDlg.this.applySettings();
				AcqControlDlg.this.afSkipInterval_.setValue(new Integer(
						AcqControlDlg.this._acqEng.getAfSkipInterval()));
			}
		});
		afPanel_.add(this.afSkipInterval_);

		channelsPanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		JLabel channelsLabel = new JLabel();
		channelsLabel.setFont(new Font("Arial", 0, 10));
		channelsLabel.setBounds(90, 19, 80, 24);
		channelsLabel.setText("Channel group:");
		channelsPanel_.add(channelsLabel);

		_channelGroupCombo = new JComboBox();
		_channelGroupCombo.setFont(new Font("", 0, 10));
		updateGroupsCombo();

		_channelGroupCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String newGroup = (String) AcqControlDlg.this._channelGroupCombo
						.getSelectedItem();

				if (AcqControlDlg.this._acqEng.setChannelGroup(newGroup)) {
					AcqControlDlg.this.model_.cleanUpConfigurationList();
					if (gui_.getAutoFocusManager() != null)
						try {
							gui_.getAutoFocusManager().refresh();
						} catch (MMException e) {
							ReportingUtils.showError(e);
						}
				} else {
					AcqControlDlg.this.updateGroupsCombo();
				}
			}
		});
		_channelGroupCombo.setBounds(165, 20, 150, 22);
		channelsPanel_.add(this._channelGroupCombo);

		channelTablePane_ = new JScrollPane();
		channelTablePane_.setFont(new Font("Arial", 0, 10));
		channelTablePane_.setBounds(10, 45, 414, 90);
		channelsPanel_.add(channelTablePane_);

		JButton addButton = new JButton();
		addButton.setFont(new Font("Arial", 0, 10));
		addButton.setMargin(new Insets(0, 0, 0, 0));
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
				AcqControlDlg.this.model_.addNewChannel();
				AcqControlDlg.this.model_.fireTableStructureChanged();
			}
		});
		addButton.setText("New");
		addButton.setBounds(430, 45, 68, 22);
		channelsPanel_.add(addButton);

		JButton removeButton = new JButton();
		removeButton.setFont(new Font("Arial", 0, 10));
		removeButton.setMargin(new Insets(-5, -5, -5, -5));
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = AcqControlDlg.this.channelTable_.getSelectedRow();
				if (sel > -1) {
					AcqControlDlg.this.applySettings();
					AcqControlDlg.this.model_.removeChannel(sel);
					AcqControlDlg.this.model_.fireTableStructureChanged();
					if (AcqControlDlg.this.channelTable_.getRowCount() > sel)
						AcqControlDlg.this.channelTable_
								.setRowSelectionInterval(sel, sel);
				}
			}
		});
		removeButton.setText("Remove");
		removeButton.setBounds(430, 69, 68, 22);
		channelsPanel_.add(removeButton);

		JButton upButton = new JButton();
		upButton.setFont(new Font("Arial", 0, 10));
		upButton.setMargin(new Insets(0, 0, 0, 0));
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = AcqControlDlg.this.channelTable_.getSelectedRow();
				if (sel > -1) {
					AcqControlDlg.this.applySettings();
					int newSel = AcqControlDlg.this.model_.rowUp(sel);
					AcqControlDlg.this.model_.fireTableStructureChanged();
					AcqControlDlg.this.channelTable_.setRowSelectionInterval(
							newSel, newSel);
				}
			}
		});
		upButton.setText("Up");
		upButton.setBounds(430, 93, 68, 22);
		channelsPanel_.add(upButton);

		JButton downButton = new JButton();
		downButton.setFont(new Font("Arial", 0, 10));
		downButton.setMargin(new Insets(0, 0, 0, 0));
		downButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int sel = AcqControlDlg.this.channelTable_.getSelectedRow();
				if (sel > -1) {
					AcqControlDlg.this.applySettings();
					int newSel = AcqControlDlg.this.model_.rowDown(sel);
					AcqControlDlg.this.model_.fireTableStructureChanged();
					AcqControlDlg.this.channelTable_.setRowSelectionInterval(
							newSel, newSel);
				}
			}
		});
		downButton.setText("Down");
		downButton.setBounds(430, 117, 68, 22);
		channelsPanel_.add(downButton);

		chanKeepShutterOpenCheckBox_ = new JCheckBox();
		chanKeepShutterOpenCheckBox_.setText("Keep shutter open");
		chanKeepShutterOpenCheckBox_.setFont(new Font("Arial", 0, 10));
		chanKeepShutterOpenCheckBox_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		chanKeepShutterOpenCheckBox_.setSelected(false);
		chanKeepShutterOpenCheckBox_.setBounds(330, 20, 150, 22);
		channelsPanel_.add(this.chanKeepShutterOpenCheckBox_);

		savePanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!AcqControlDlg.this.savePanel_.isSelected()) {
					AcqControlDlg.this.displayModeCombo_.setSelectedIndex(0);
				}
				AcqControlDlg.this.commentTextArea_
						.setEnabled(AcqControlDlg.this.savePanel_.isSelected());
				AcqControlDlg.this.applySettings();
			}
		});
		displayMode_ = new JLabel();
		displayMode_.setFont(new Font("Arial", 0, 10));
		displayMode_.setText("Display");
		displayMode_.setBounds(150, 15, 49, 21);
		savePanel_.add(displayMode_);

		displayModeCombo_ = new JComboBox();
		displayModeCombo_.setFont(new Font("", 0, 10));
		displayModeCombo_.setBounds(188, 14, 150, 24);
		displayModeCombo_.addItem(new DisplayMode(0));
		displayModeCombo_.addItem(new DisplayMode(1));
		displayModeCombo_.addItem(new DisplayMode(2));
		displayModeCombo_.setEnabled(false);
		savePanel_.add(displayModeCombo_);

		rootLabel_ = new JLabel();
		rootLabel_.setFont(new Font("Arial", 0, 10));
		rootLabel_.setText("Directory root");
		rootLabel_.setBounds(10, 40, 72, 22);
		savePanel_.add(rootLabel_);

		rootField_ = new JTextField();
		rootField_.setFont(new Font("Arial", 0, 10));
		rootField_.setBounds(90, 40, 354, 22);
		savePanel_.add(rootField_);

		browseRootButton_ = new JButton();
		browseRootButton_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.setRootDirectory();
			}
		});
		browseRootButton_.setMargin(new Insets(2, 5, 2, 5));
		browseRootButton_.setFont(new Font("Dialog", 0, 10));
		browseRootButton_.setText("...");
		browseRootButton_.setBounds(445, 40, 47, 24);
		savePanel_.add(browseRootButton_);

		namePrefixLabel_ = new JLabel();
		namePrefixLabel_.setFont(new Font("Arial", 0, 10));
		namePrefixLabel_.setText("Name prefix");
		namePrefixLabel_.setBounds(10, 65, 76, 22);
		savePanel_.add(namePrefixLabel_);

		nameField_ = new JTextField();
		nameField_.setFont(new Font("Arial", 0, 10));
		nameField_.setBounds(90, 65, 354, 22);
		savePanel_.add(nameField_);

		commentLabel_ = new JLabel();
		commentLabel_.setFont(new Font("Arial", 0, 10));
		commentLabel_.setText("Comment");
		commentLabel_.setBounds(10, 90, 76, 22);
		savePanel_.add(commentLabel_);

		JScrollPane commentScrollPane = new JScrollPane();
		commentScrollPane.setBounds(90, 90, 354, 62);
		savePanel_.add(commentScrollPane);

		commentTextArea_ = new JTextArea();
		commentScrollPane.setViewportView(this.commentTextArea_);
		commentTextArea_.setFont(new Font("", 0, 10));
		commentTextArea_.setToolTipText("Comment for the current acquistion");
		commentTextArea_.setWrapStyleWord(true);
		commentTextArea_.setLineWrap(true);
		commentTextArea_.setBorder(new EtchedBorder(1));

		JPanel buttons_container = new JPanel();
		buttons_container.setLayout(new BoxLayout(buttons_container,
				BoxLayout.PAGE_AXIS));
		
		_btn_acquire = new JButton("Acquire!");
		_btn_acquire.setMargin(new Insets(-9, -9, -9, -9));
		_btn_acquire.setFont(new Font("Arial", 1, 12));
		_btn_acquire.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AbstractCellEditor ae = (AbstractCellEditor)channelTable_.getCellEditor();
				if (ae != null) {
					ae.stopCellEditing();
				}
				runAcquisition();
				_btn_stop.setEnabled(true);
				_btn_pauseResume.setEnabled(true);
			}
		});
		_btn_acquire.setPreferredSize(new Dimension(77, 22));
		
		_btn_pauseResume = new JButton("Pause");
		_btn_pauseResume.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean paused = _acqEng.isPaused();
				if (paused)
					_btn_pauseResume.setText("Resume");	
				else
					_btn_pauseResume.setText("Pause");
				_acqEng.setPause(!paused);
			}
		});
		_btn_pauseResume.setFont(new Font("Arial", 1, 12));
		_btn_pauseResume.setEnabled(false);
		_btn_pauseResume.setPreferredSize(new Dimension(77, 22));
		
		_btn_stop = new JButton("Stop");
		_btn_stop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_acqEng.abortRequest();
				_acqEng.stop(true);
				_btn_stop.setEnabled(false);
				_btn_pauseResume.setEnabled(false);
			}
		});
		_btn_stop.setFont(new Font("Arial", 1, 12));
		_btn_stop.setEnabled(false);
		_btn_stop.setPreferredSize(new Dimension(77, 22));

		buttons_container.add(Box.createRigidArea(new Dimension(20, 22)));

		JButton loadButton = new JButton();
		loadButton.setFont(new Font("Arial", 0, 12));
		loadButton.setMargin(new Insets(-9, -9, -9, -9));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadAcqSettingsFromFile();
			}
		});
		loadButton.setText("Load...");
		// loadButton.setBounds(430, 102, 77, 22);
		loadButton.setPreferredSize(new Dimension(77, 22));
		// buttons_container.add(loadButton);

		// buttons_container.add(Box.createRigidArea(new Dimension(20,22)));

		JButton saveAsButton = new JButton();
		saveAsButton.setFont(new Font("Arial", 0, 12));
		saveAsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.saveAsAcqSettingsToFile();
			}
		});
		saveAsButton.setText("Save...");
		// saveAsButton.setBounds(430, 126, 77, 22);
		saveAsButton.setPreferredSize(new Dimension(77, 22));
		saveAsButton.setMargin(new Insets(-9, -9, -9, -9));
		// buttons_container.add(saveAsButton);
		
		// add buttons
		buttons_container.setBounds(410, 40, 100, 85);
		buttons_container.add(_btn_acquire);
		// buttons_container.add(Box.createRigidArea(new Dimension(20, 22)));
		// buttons_container.add(_btn_pauseResume);
		buttons_container.add(Box.createRigidArea(new Dimension(20, 22)));
		buttons_container.add(_btn_stop);
		add(buttons_container);

		int x = 100;
		int y = 100;
		setBounds(x, y, 521, 645);

		if (prefs_ != null) {
			x = prefs_.getInt("x", x);
			y = prefs_.getInt("y", y);
			setLocation(x, y);
		}

		positionsPanel_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				AcqControlDlg.this.applySettings();
			}
		});
		displayModeCombo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		sliceModeCombo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		posModeCombo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				AcqControlDlg.this.applySettings();
			}
		});
		loadAcqSettings();
		createChannelTable();
		updateGUIContents();
		// channelsPanel_.checkBox.setEnabled(false);
		// savePanel_.checkBox.setEnabled(false);
	}

	public void propertyChange(PropertyChangeEvent e) {
		applySettings();
		this.summaryTextArea_.setText(this._acqEng.getVerboseSummary());
	}

	protected void afOptions() {
		if (gui_.getAutoFocusManager().getDevice() != null)
			gui_.getAutoFocusManager().showOptionsDialog();
	}

	public boolean inArray(String member, String[] group) {
		for (int i = 0; i < group.length; i++) {
			if (member.equals(group[i])) {
				return true;
			}
		}
		return false;
	}

	public void updateGroupsCombo() {
		String[] groups = this._acqEng.getAvailableGroups();
		if (groups.length != 0) {
			this._channelGroupCombo.setModel(new DefaultComboBoxModel(groups));
			if (!inArray(this._acqEng.getChannelGroup(), groups)) {
				this._acqEng
						.setChannelGroup(this._acqEng.getFirstConfigGroup());
			}

			this._channelGroupCombo.setSelectedItem(this._acqEng
					.getChannelGroup());
		}
	}

	public void updateChannelAndGroupCombo() {
		updateGroupsCombo();
		this.model_.cleanUpConfigurationList();
	}

	public synchronized void loadAcqSettings() {
		disableGUItoSettings_ = true;

		_acqEng.clear();
		int numFrames = acqPrefs_.getInt("acqNumframes", 1);
		double interval = acqPrefs_.getDouble("acqInterval", 0.0D);

		_acqEng.setFrames(numFrames, interval);
		_acqEng.enableFramesSetting(acqPrefs_.getBoolean(
				"enableMultiFrame", false));

		framesPanel_.setSelected(this._acqEng.isFramesSettingEnabled());
		_numFrames.setValue(Integer.valueOf(this._acqEng.getNumFrames()));

		int unit = this.acqPrefs_.getInt("acqTimeInit", 0);
		timeUnitCombo_.setSelectedIndex(unit);

		double bottom = acqPrefs_.getDouble("acqZbottom", 0.0D);
		double top = acqPrefs_.getDouble("acqZtop", 0.0D);
		double step = acqPrefs_.getDouble("acqZstep", 1.0D);
		if (Math.abs(step) < Math.abs(this._acqEng.getMinZStepUm())) {
			step = _acqEng.getMinZStepUm();
		}
		zVals_ = this.acqPrefs_.getInt("acqZValues", 0);
		_acqEng.setSlices(bottom, top, step, this.zVals_ != 0);
		_acqEng.enableZSliceSetting(acqPrefs_.getBoolean("enableSliceSettings", _acqEng.isZSliceSettingEnabled()));
		_acqEng.enableMultiPosition(acqPrefs_.getBoolean("enableMultiPosition", this._acqEng.isMultiPositionEnabled()));
		positionsPanel_.setSelected(_acqEng.isMultiPositionEnabled());

		slicesPanel_.setSelected(_acqEng.isZSliceSettingEnabled());

		_acqEng.enableChannelsSetting(acqPrefs_.getBoolean("enableMultiChannels", false));
		channelsPanel_.setSelected(_acqEng.isChannelsSettingEnabled());
		savePanel_.setSelected(acqPrefs_.getBoolean("acqSaveFiles", false));

		nameField_.setText(acqPrefs_.get("acqDirName", "Untitled"));
		String os_name = System.getProperty("os.name", "");
		if (os_name.startsWith("Window"))
			rootField_.setText(acqPrefs_.get("acqRootName","C:/AcquisitionData"));
		else {
			rootField_.setText(this.acqPrefs_.get("acqRootName","AcquisitionData"));
		}

		_acqEng.setSliceMode(acqPrefs_.getInt("sliceMode",_acqEng.getSliceMode()));
		_acqEng.setDisplayMode(acqPrefs_.getInt("acqDisplayMode",_acqEng.getDisplayMode()));
		_acqEng.setPositionMode(acqPrefs_.getInt("positionMode",_acqEng.getPositionMode()));
		_acqEng.enableAutoFocus(acqPrefs_.getBoolean("autofocus_enabled",_acqEng.isAutoFocusEnabled()));
		_acqEng.setAfSkipInterval(acqPrefs_.getInt("autofocusSkipInterval", this._acqEng.getAfSkipInterval()));
		_acqEng.setChannelGroup(acqPrefs_.get("acqChannelGroup",_acqEng.getFirstConfigGroup()));
		afPanel_.setSelected(_acqEng.isAutoFocusEnabled());
		_acqEng.keepShutterOpenForChannels(acqPrefs_.getBoolean("acqChannelsKeepShutterOpen", false));
		_acqEng.keepShutterOpenForStack(acqPrefs_.getBoolean("acqStackKeepShutterOpen", false));

		int numChannels = acqPrefs_.getInt("acqNumchannels", 0);

		ChannelSpec defaultChannel = new ChannelSpec();

		_acqEng.getChannels().clear();
		for (int i = 0; i < numChannels; i++) {
			String name = acqPrefs_.get("acqChannelName" + i, "Undefined");
			boolean use = acqPrefs_.getBoolean("acqChannelUse" + i, true);
			double exp = acqPrefs_.getDouble("acqChannelExp" + i, 0.0D);
			Boolean doZStack = Boolean.valueOf(this.acqPrefs_.getBoolean(
					"acqChannelDoZStack" + i, true));
			double zOffset = this.acqPrefs_.getDouble("acqChannelZOffset" + i,0.0D);
			ContrastSettings s8 = new ContrastSettings();
			s8.min = acqPrefs_.getDouble("acqChannel8ContrastMin" + i,defaultChannel.contrast8_.min);
			s8.max = acqPrefs_.getDouble("acqChannel8ContrastMax" + i,defaultChannel.contrast8_.max);
			ContrastSettings s16 = new ContrastSettings();
			s16.min = acqPrefs_.getDouble("acqChannel16ContrastMin" + i,defaultChannel.contrast16_.min);
			s16.max = acqPrefs_.getDouble("acqChannel16ContrastMax" + i,defaultChannel.contrast16_.max);
			int r = acqPrefs_.getInt("acqChannelColorR" + i,defaultChannel.color_.getRed());
			int g = acqPrefs_.getInt("acqChannelColorG" + i,defaultChannel.color_.getGreen());
			int b = acqPrefs_.getInt("acqChannelColorB" + i,defaultChannel.color_.getBlue());
			int skip = acqPrefs_.getInt("acqSkip" + i,defaultChannel.skipFactorFrame_);
			Color c = new Color(r, g, b);
			_acqEng.addChannel(name, exp, doZStack, zOffset, s8, s16, skip, c, use);
		}

		int columnCount = 6;
		columnWidth_ = new int[columnCount];
		columnOrder_ = new int[columnCount];
		for (int k = 0; k < columnCount; k++) {
			columnWidth_[k] = acqPrefs_.getInt("column_width" + k, 77);
			columnOrder_[k] = acqPrefs_.getInt("column_order" + k, k);
		}

		this.disableGUItoSettings_ = false;
	}

	public synchronized void saveAcqSettings() {
		try {
			this.acqPrefs_.clear();
		} catch (BackingStoreException e) {
			ReportingUtils.showError(e);
		}

		applySettings();

		acqPrefs_.putBoolean("enableMultiFrame",_acqEng.isFramesSettingEnabled());
		acqPrefs_.putBoolean("enableMultiChannels",_acqEng.isChannelsSettingEnabled());
		acqPrefs_.putInt("acqNumframes", _acqEng.getNumFrames());
		acqPrefs_.putDouble("acqInterval",_acqEng.getFrameIntervalMs());
		acqPrefs_.putInt("acqTimeInit",timeUnitCombo_.getSelectedIndex());
		acqPrefs_.putDouble("acqZbottom", _acqEng.getSliceZBottomUm());
		acqPrefs_.putDouble("acqZtop", _acqEng.getZTopUm());
		acqPrefs_.putDouble("acqZstep", _acqEng.getSliceZStepUm());
		acqPrefs_.putBoolean("enableSliceSettings",_acqEng.isZSliceSettingEnabled());
		acqPrefs_.putBoolean("enableMultiPosition",_acqEng.isMultiPositionEnabled());
		acqPrefs_.putInt("acqZValues", zVals_);
		acqPrefs_.putBoolean("acqSaveFiles", savePanel_.isSelected());
		acqPrefs_.put("acqDirName", nameField_.getText());
		acqPrefs_.put("acqRootName", rootField_.getText());

		acqPrefs_.putInt("sliceMode", _acqEng.getSliceMode());
		acqPrefs_.putInt("acqDisplayMode", _acqEng.getDisplayMode());
		acqPrefs_.putInt("positionMode", _acqEng.getPositionMode());
		acqPrefs_.putBoolean("autofocus_enabled",_acqEng.isAutoFocusEnabled());
		acqPrefs_.putInt("autofocusSkipInterval",_acqEng.getAfSkipInterval());
		acqPrefs_.putBoolean("acqChannelsKeepShutterOpen",_acqEng.isShutterOpenForChannels());
		acqPrefs_.putBoolean("acqStackKeepShutterOpen",_acqEng.isShutterOpenForStack());

		acqPrefs_.put("acqChannelGroup", _acqEng.getChannelGroup());
		ArrayList<ChannelSpec> channels = _acqEng.getChannels();
		acqPrefs_.putInt("acqNumchannels", channels.size());
		for (int i = 0; i < channels.size(); i++) {
			ChannelSpec channel = (ChannelSpec) channels.get(i);
			acqPrefs_.put("acqChannelName" + i, channel.config_);
			acqPrefs_.putDouble("acqChannelExp" + i, channel.exposure_);
			acqPrefs_.putBoolean("acqChannelDoZStack" + i,channel.doZStack_.booleanValue());
			acqPrefs_.putDouble("acqChannelZOffset" + i, channel.zOffset_);
			acqPrefs_.putDouble("acqChannel8ContrastMin" + i,channel.contrast8_.min);
			acqPrefs_.putDouble("acqChannel8ContrastMax" + i,channel.contrast8_.max);
			acqPrefs_.putDouble("acqChannel16ContrastMin" + i,channel.contrast16_.min);
			acqPrefs_.putDouble("acqChannel16ContrastMax" + i,channel.contrast16_.max);
			acqPrefs_.putInt("acqChannelColorR" + i,channel.color_.getRed());
			acqPrefs_.putInt("acqChannelColorG" + i,channel.color_.getGreen());
			acqPrefs_.putInt("acqChannelColorB" + i,channel.color_.getBlue());
			acqPrefs_.putInt("acqSkip" + i, channel.skipFactorFrame_);
		}

		for (int k = 0; k < model_.getColumnCount(); k++) {
			acqPrefs_.putInt("column_width" + k,findTableColumn(channelTable_, k).getWidth());
			acqPrefs_.putInt("column_order" + k,channelTable_.convertColumnIndexToView(k));
		}
		try {
			acqPrefs_.flush();
		} catch (BackingStoreException ex) {
			ReportingUtils.logError(ex);
		}
	}

	public TableColumn findTableColumn(JTable table, int columnModelIndex) {
		Enumeration<TableColumn> e = table.getColumnModel().getColumns();
		while (e.hasMoreElements()) {
			TableColumn col = (TableColumn) e.nextElement();
			if (col.getModelIndex() == columnModelIndex) {
				return col;
			}
		}
		return null;
	}

	protected void enableZSliceControls(boolean state) {
		this.zBottom_.setEnabled(state);
		this.zTop_.setEnabled(state);
		this.zStep_.setEnabled(state);
		this.zValCombo_.setEnabled(state);
	}

	protected void setRootDirectory() {
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(1);
		fc.setCurrentDirectory(new File(this._acqEng.getRootName()));
		int retVal = fc.showOpenDialog(this);
		if (retVal == 0) {
			rootField_.setText(fc.getSelectedFile().getAbsolutePath());
			_acqEng.setRootName(fc.getSelectedFile().getAbsolutePath());
		}
	}

	protected void setTopPosition() {
		double z = _acqEng.getCurrentZPos();
		this.zTop_.setText(NumberUtils.doubleToDisplayString(z));
		applySettings();

		this.summaryTextArea_.setText(this._acqEng.getVerboseSummary());
	}

	protected void setBottomPosition() {
		double z = this._acqEng.getCurrentZPos();
		this.zBottom_.setText(NumberUtils.doubleToDisplayString(z));
		applySettings();

		this.summaryTextArea_.setText(this._acqEng.getVerboseSummary());
	}

	protected void loadAcqSettingsFromFile() {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new AcqFileFilter());
		if (null != this.prefs_) {
			this.acqDir_ = this.prefs_.get("dir", null);
		}
		if (this.acqDir_ != null) {
			fc.setCurrentDirectory(new File(this.acqDir_));
		}
		int retVal = fc.showOpenDialog(this);
		if (retVal == 0) {
			this.acqFile_ = fc.getSelectedFile();
			try {
				FileInputStream in = new FileInputStream(this.acqFile_);
				acqPrefs_.clear();
				Preferences.importPreferences(in);
				loadAcqSettings();
				updateGUIContents();
				in.close();
				acqDir_ = acqFile_.getParent();
				prefs_.put("dir", acqDir_);
			} catch (Exception e) {
				ReportingUtils.showError(e);
				return;
			}
		}
	}

	public void loadAcqSettingsFromFile(String path) {
		this.acqFile_ = new File(path);
		try {
			FileInputStream in = new FileInputStream(this.acqFile_);
			this.acqPrefs_.clear();
			Preferences.importPreferences(in);
			loadAcqSettings();
			updateGUIContents();
			in.close();
			this.acqDir_ = this.acqFile_.getParent();
			if (this.acqDir_ != null)
				this.prefs_.put("dir", this.acqDir_);
		} catch (Exception e) {
			ReportingUtils.showError(e);
			return;
		}
	}

	protected boolean saveAsAcqSettingsToFile() {
		saveAcqSettings();
		JFileChooser fc = new JFileChooser();
		boolean saveFile = true;
		if (this.acqPrefs_ == null)
			return false;
		do {
			if (this.acqFile_ == null) {
				this.acqFile_ = new File("MMAcquistion.xml");
			}

			fc.setSelectedFile(this.acqFile_);
			int retVal = fc.showSaveDialog(this);
			if (retVal == 0) {
				this.acqFile_ = fc.getSelectedFile();

				if (this.acqFile_.exists()) {
					int sel = JOptionPane.showConfirmDialog(this, "Overwrite "
							+ this.acqFile_.getName(), "File Save", 0);

					if (sel == 0)
						saveFile = true;
					else
						saveFile = false;
				}
			} else {
				return false;
			}
		} while (!saveFile);
		try {
			FileOutputStream os = new FileOutputStream(this.acqFile_);
			this.acqPrefs_.exportNode(os);
		} catch (FileNotFoundException e) {
			ReportingUtils.showError(e);
			return false;
		} catch (IOException e) {
			ReportingUtils.showError(e);
			return false;
		} catch (BackingStoreException e) {
			ReportingUtils.showError(e);
			return false;
		}
		return true;
	}

	public void runAcquisition() {
		if (_acqEng.isAcquisitionRunning()) {
			JOptionPane
					.showMessageDialog(this,
							"Cannot start acquisition: previous acquisition still in progress.");
			return;
		}

		if (_acqEng.isFramesSettingEnabled()) {
			double timeNeeded;
			if (_acqEng.isZSliceSettingEnabled()) {
				timeNeeded = _acqEng.getNumSlices();
			}
			else timeNeeded = 1;
			if (_acqEng.isChannelsSettingEnabled()) 
			{
				double timeNeededForChannels=1;
				for (ChannelSpec chan : _acqEng.getChannels()) {
					timeNeededForChannels+=chan.exposure_;
				}
				timeNeeded*=timeNeededForChannels;
			}
			else {
				try {
					timeNeeded *= _core.getExposure();
				} catch (Exception e) {}
			}
			double timeWanted = Double.MAX_VALUE;
			try {
				timeWanted = Double.valueOf(""+interval_.getValue());
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(this, "Interval not a number.");
				return;
			}
			switch(timeUnitCombo_.getSelectedIndex()) {
				case 2 :
					timeWanted*=60;
				case 1:
					timeWanted*=1000;
					break;
			}
			if (timeNeeded > timeWanted) {
				if (!ConfirmDialog.confirm("Interval too short", "Interval set between two captures is too small compared to the " +
						"actual time needed for a capture." +
						"Do you wish to continue anyway ? (This can lead to weird results)."))
					return;
			}
		}
		
		try {
			applySettings();
			ChannelTableModel model = (ChannelTableModel) this.channelTable_
					.getModel();
			if ((_acqEng.isChannelsSettingEnabled())
					&& (model.duplicateChannels())) {
				JOptionPane
						.showMessageDialog(this,
								"Cannot start acquisition using the same channel twice");
				return;
			}
			_acqEng.acquire();
		} catch (MMException e) {
			ReportingUtils.showError(e);
			return;
		}
	}

	public void runAcquisition(String acqName, String acqRoot) {
		if (_acqEng.isAcquisitionRunning()) {
			JOptionPane.showMessageDialog(this,
							"Unable to start the new acquisition task: previous acquisition still in progress.");
			return;
		}
		
		try {
			applySettings();
			_acqEng.setDirName(acqName);
			_acqEng.setRootName(acqRoot);
			_acqEng.setSaveFiles(true);
			gui_.startAcquisition();
			_acqEng.acquire();
		} catch (MMException e) {
			ReportingUtils.showError(e);
			return;
		} catch (MMScriptException e) {
			e.printStackTrace();
		}
	}

	public boolean isAcquisitionRunning() {
		return this._acqEng.isAcquisitionRunning();
	}

	public static int search(int[] numbers, int key) {
		for (int index = 0; index < numbers.length; index++) {
			if (numbers[index] == key) {
				return index;
			}
		}
		return -1;
	}

	public void updateGUIContents() {
		if (this.disableGUItoSettings_) {
			return;
		}
		disableGUItoSettings_ = true;

		model_.setChannels(_acqEng.getChannels());

		double intervalMs = _acqEng.getFrameIntervalMs();
		interval_.setText(numberFormat_.format(convertMsToTime(intervalMs,
				timeUnitCombo_.getSelectedIndex())));

		zBottom_.setText(NumberUtils.doubleToDisplayString(this._acqEng
				.getSliceZBottomUm()));
		zTop_.setText(NumberUtils.doubleToDisplayString(this._acqEng
				.getZTopUm()));
		zStep_.setText(NumberUtils.doubleToDisplayString(this._acqEng
				.getSliceZStepUm()));

		framesPanel_.setSelected(this._acqEng.isFramesSettingEnabled());
		slicesPanel_.setSelected(this._acqEng.isZSliceSettingEnabled());
		positionsPanel_.setSelected(this._acqEng.isMultiPositionEnabled());
		afPanel_.setSelected(this._acqEng.isAutoFocusEnabled());
		posModeCombo_.setEnabled((positionsPanel_.isSelected())
				&& (framesPanel_.isSelected()));
		sliceModeCombo_.setEnabled((this.slicesPanel_.isSelected())
				&& (channelsPanel_.isSelected()));

		afSkipInterval_.setEnabled(this._acqEng.isAutoFocusEnabled());

		Integer numFrames = new Integer(this._acqEng.getNumFrames());
		Integer afSkipInterval = new Integer(this._acqEng.getAfSkipInterval());
		if (_acqEng.isFramesSettingEnabled()) {
			_numFrames.setValue(numFrames);
		}
		afSkipInterval_.setValue(afSkipInterval);

		enableZSliceControls(this._acqEng.isZSliceSettingEnabled());
		model_.fireTableStructureChanged();

		_channelGroupCombo.setSelectedItem(this._acqEng.getChannelGroup());
		sliceModeCombo_.setSelectedIndex(this._acqEng.getSliceMode());
		try {
			displayModeCombo_.setSelectedIndex(_acqEng.getDisplayMode());
		} catch (IllegalArgumentException e) {
			displayModeCombo_.setSelectedIndex(0);
		}
		if ((framesPanel_.isSelected()) && (positionsPanel_.isSelected()))
			posModeCombo_.setSelectedIndex(this._acqEng.getPositionMode());
		else
			posModeCombo_.setSelectedIndex(1);
		zValCombo_.setSelectedIndex(this.zVals_);
		stackKeepShutterOpenCheckBox_.setSelected(this._acqEng
				.isShutterOpenForStack());
		chanKeepShutterOpenCheckBox_.setSelected(this._acqEng
				.isShutterOpenForChannels());

		channelTable_.setAutoResizeMode(4);

		boolean selected = channelsPanel_.isSelected();
		channelTable_.setEnabled(selected);
		channelTable_.getTableHeader().setForeground(
				selected ? Color.black : Color.gray);

		summaryTextArea_.setText(_acqEng.getVerboseSummary());

		disableGUItoSettings_ = false;
	}

	private void applySettings() {
		if (disableGUItoSettings_) {
			return;
		}
		disableGUItoSettings_ = true;

		AbstractCellEditor ae = (AbstractCellEditor) this.channelTable_
				.getCellEditor();
		if (ae != null) {
			ae.stopCellEditing();
		}
		try {
			double zStep = NumberUtils.displayStringToDouble(this.zStep_
					.getText());
			if (Math.abs(zStep) < _acqEng.getMinZStepUm()) {
				zStep = _acqEng.getMinZStepUm();
			}
			this._acqEng.setSlices(
					NumberUtils.displayStringToDouble(zBottom_.getText()),
					NumberUtils.displayStringToDouble(zTop_.getText()), zStep,
					zVals_ != 0);
			_acqEng.enableZSliceSetting(slicesPanel_.isSelected());
			_acqEng.enableMultiPosition(positionsPanel_.isSelected());
			if (channelsPanel_.isSelected()) {
				_acqEng.setSliceMode(((SliceMode) sliceModeCombo_
						.getSelectedItem()).getID());
			} else if (slicesPanel_.isSelected()) {
				_acqEng.setSliceMode(1);
			}
			_acqEng.setDisplayMode(((DisplayMode) this.displayModeCombo_
					.getSelectedItem()).getID());
			_acqEng.setPositionMode(posModeCombo_.getSelectedIndex());
			_acqEng.enableChannelsSetting(channelsPanel_.isSelected());
			_acqEng.setChannels(((ChannelTableModel) this.channelTable_
					.getModel()).getChannels());
			_acqEng.enableFramesSetting(framesPanel_.isSelected());
			_acqEng.setFrames(
					((Integer) this._numFrames.getValue()).intValue(),
					convertTimeToMs(NumberUtils.displayStringToDouble(interval_
							.getText()), this.timeUnitCombo_.getSelectedIndex()));

			_acqEng.setAfSkipInterval(NumberUtils
					.displayStringToInt(afSkipInterval_.getValue().toString()));
			_acqEng.keepShutterOpenForChannels(this.chanKeepShutterOpenCheckBox_
					.isSelected());
			_acqEng.keepShutterOpenForStack(this.stackKeepShutterOpenCheckBox_
					.isSelected());
		} catch (ParseException p) {
			ReportingUtils.showError(p);
		}

		_acqEng.setSaveFiles(this.savePanel_.isSelected());
		_acqEng.setDirName(this.nameField_.getText());
		_acqEng.setRootName(this.rootField_.getText());

		_acqEng.setComment(this.commentTextArea_.getText());

		_acqEng.enableAutoFocus(this.afPanel_.isSelected());

		_acqEng.setParameterPreferences(this.acqPrefs_);
		disableGUItoSettings_ = false;
		updateGUIContents();
	}

	@SuppressWarnings("unused")
	private void saveSettings() {
		Rectangle r = getBounds();

		if (this.prefs_ != null) {
			this.prefs_.putInt("x", r.x);
			this.prefs_.putInt("y", r.y);
		}
	}

	private double convertTimeToMs(double interval, int units) {
		if (units == 1)
			return interval * 1000.0D;
		if (units == 2)
			return interval * 60.0D * 1000.0D;
		if (units == 0) {
			return interval;
		}
		ReportingUtils
				.showError("Unknown units supplied for acquisition interval!");
		return interval;
	}

	private double convertMsToTime(double intervalMs, int units) {
		if (units == 1)
			return intervalMs / 1000.0D;
		if (units == 2)
			return intervalMs / 60000.0D;
		if (units == 0) {
			return intervalMs;
		}
		ReportingUtils
				.showError("Unknown units supplied for acquisition interval!");
		return intervalMs;
	}

	private void zValCalcChanged() {
		if (this.zValCombo_.getSelectedIndex() == 0) {
			this.setTopButton_.setEnabled(false);
			this.setBottomButton_.setEnabled(false);
		} else {
			this.setTopButton_.setEnabled(true);
			this.setBottomButton_.setEnabled(true);
		}

		if (this.zVals_ == this.zValCombo_.getSelectedIndex()) {
			return;
		}
		this.zVals_ = this.zValCombo_.getSelectedIndex();
		double zBottomUm;
		double zTopUm;
		try {
			zBottomUm = NumberUtils.displayStringToDouble(this.zBottom_
					.getText());
			zTopUm = NumberUtils.displayStringToDouble(this.zTop_.getText());
		} catch (ParseException e) {
			ReportingUtils.logError(e);
			return;
		}

		double curZ = this._acqEng.getCurrentZPos();
		double newBottom;
		double newTop;
		if (this.zVals_ == 0) {
			this.setTopButton_.setEnabled(false);
			this.setBottomButton_.setEnabled(false);

			newTop = zTopUm - curZ;
			newBottom = zBottomUm - curZ;
		} else {
			this.setTopButton_.setEnabled(true);
			this.setBottomButton_.setEnabled(true);

			newTop = zTopUm + curZ;
			newBottom = zBottomUm + curZ;
		}
		this.zBottom_.setText(NumberUtils.doubleToDisplayString(newBottom));
		this.zTop_.setText(NumberUtils.doubleToDisplayString(newTop));
	}

	public void setBackgroundStyle(String style) {

		repaint();
	}

	public class CheckBoxPanel extends AcqControlDlg.ComponentTitledPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		JCheckBox checkBox;

		CheckBoxPanel(String title) {
			super();
			titleComponent = new JCheckBox(title);
			checkBox = (JCheckBox) titleComponent;
			compTitledBorder = new ComponentTitledBorder(checkBox, this,
					BorderFactory.createEtchedBorder());
			setBorder(compTitledBorder);
			borderSet_ = true;
			final CheckBoxPanel thisPanel = this;
			checkBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					boolean enable = checkBox.isSelected();
					thisPanel.setChildrenEnabled(enable);
				}
			});
		}

		public void setChildrenEnabled(boolean enabled) {
			Component[] comp = getComponents();
			for (int i = 0; i < comp.length; i++)
				comp[i].setEnabled(enabled);
		}

		public boolean isSelected() {
			return this.checkBox.isSelected();
		}

		public void setSelected(boolean selected) {
			this.checkBox.setSelected(selected);
			setChildrenEnabled(selected);
		}

		public void addActionListener(ActionListener actionListener) {
			this.checkBox.addActionListener(actionListener);
		}

		public void removeActionListeners() {
			for (ActionListener l : this.checkBox.getActionListeners())
				this.checkBox.removeActionListener(l);
		}
	}

	public class LabelPanel extends AcqControlDlg.ComponentTitledPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		LabelPanel(String title) {
			super();
			this.titleComponent = new JLabel(title);
			JLabel label = (JLabel) this.titleComponent;

			label.setOpaque(true);
			label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

			this.compTitledBorder = new ComponentTitledBorder(label, this,
					BorderFactory.createEtchedBorder());
			setBorder(this.compTitledBorder);
			this.borderSet_ = true;
		}
	}

	public class ComponentTitledPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public ComponentTitledBorder compTitledBorder;
		public boolean borderSet_ = false;
		public Component titleComponent;

		public ComponentTitledPanel() {
		}

		public void setBorder(Border border) {
			if ((this.compTitledBorder != null) && (this.borderSet_))
				this.compTitledBorder.setBorder(border);
			else
				super.setBorder(border);
		}

		public Border getBorder() {
			return this.compTitledBorder;
		}

		public void setTitleFont(Font font) {
			this.titleComponent.setFont(font);
		}
	}

	public class ChannelCellRenderer extends JLabel implements
			TableCellRenderer {
		private static final long serialVersionUID = -4328340719459382679L;
		private AcquisitionEngine acqEng_;

		public ChannelCellRenderer(AcquisitionEngine acqEng) {
			this.acqEng_ = acqEng;
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int rowIndex, int colIndex) {
			setEnabled(table.isEnabled());

			AcqControlDlg.ChannelTableModel model = (AcqControlDlg.ChannelTableModel) table
					.getModel();
			ArrayList<ChannelSpec> channels = model.getChannels();
			ChannelSpec channel = channels.get(rowIndex);

			if (hasFocus)
				;
			colIndex = table.convertColumnIndexToModel(colIndex);

			setOpaque(false);
			if (colIndex == 0) {
				setText(channel.config_);
			} else if (colIndex == 1) {
				setText(NumberUtils.doubleToDisplayString(channel.exposure_));
			} else if (colIndex == 2) {
				setText(NumberUtils.doubleToDisplayString(channel.zOffset_));
			} else {
				if (colIndex == 3) {
					JCheckBox check = new JCheckBox("",
							channel.doZStack_.booleanValue());
					check.setEnabled((this.acqEng_.isZSliceSettingEnabled())
							&& (table.isEnabled()));
					if (isSelected) {
						check.setBackground(table.getSelectionBackground());
						check.setOpaque(true);
					} else {
						check.setOpaque(false);
						check.setBackground(table.getBackground());
					}
					return check;
				}
				if (colIndex == 4) {
					setText(Integer.toString(channel.skipFactorFrame_));
				} else if (colIndex == 5) {
					setText("");
					setBackground(channel.color_);
					setOpaque(true);
				}
			}
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setOpaque(true);
			} else {
				setOpaque(false);
				setBackground(table.getBackground());
			}

			return this;
		}

		public void validate() {
		}

		public void revalidate() {
		}

		protected void firePropertyChange(String propertyName, Object oldValue,
				Object newValue) {
		}

		public void firePropertyChange(String propertyName, boolean oldValue,
				boolean newValue) {
		}
	}

	public class ChannelCellEditor extends AbstractCellEditor implements
			TableCellEditor {
		private static final long serialVersionUID = -8374637422965302637L;
		JTextField text_ = new JTextField();
		JComboBox combo_ = new JComboBox();
		JCheckBox checkBox_ = new JCheckBox();
		JLabel colorLabel_ = new JLabel();
		int editCol_ = -1;
		int editRow_ = -1;
		ChannelSpec channel_ = null;

		public ChannelCellEditor() {
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int rowIndex, int colIndex) {
			if (isSelected)
				;
			AcqControlDlg.ChannelTableModel model = (AcqControlDlg.ChannelTableModel) table
					.getModel();
			ArrayList<ChannelSpec> channels = model.getChannels();
			ChannelSpec channel = channels.get(rowIndex);
			this.channel_ = channel;

			colIndex = table.convertColumnIndexToModel(colIndex);

			this.editRow_ = rowIndex;
			this.editCol_ = colIndex;
			if ((colIndex == 1) || (colIndex == 2)) {
				text_.setText(((Double) value).toString());
				return text_;
			}
			if (colIndex == 3) {
				checkBox_.setSelected(((Boolean) value).booleanValue());
				return checkBox_;
			}
			if (colIndex == 4) {
				text_.setText(((Integer) value).toString());
				return text_;
			}
			if (colIndex == 0) {
				combo_.removeAllItems();

				ActionListener[] l = this.combo_.getActionListeners();
				for (int i = 0; i < l.length; i++) {
					combo_.removeActionListener(l[i]);
				}
				combo_.removeAllItems();

				String[] configs = model.getAvailableChannels();
				for (int i = 0; i < configs.length; i++) {
					combo_.addItem(configs[i]);
				}
				combo_.setSelectedItem(channel.config_);
				channel.color_ = new Color(
						AcqControlDlg.this.colorPrefs_.getInt("Color_"
								+ AcqControlDlg.this._acqEng.getChannelGroup()
								+ "_" + channel.config_, Color.white.getRGB()));

				combo_.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						channel_.color_ = new Color(colorPrefs_.getInt(
								(new StringBuilder()).append("Color_")
										.append(_acqEng.getChannelGroup())
										.append("_").append(channel_.config_)
										.toString(), Color.white.getRGB()));
						fireEditingStopped();
					}
				});
				return this.combo_;
			}

			return this.colorLabel_;
		}

		public Object getCellEditorValue() {
			String err = null;
			try {
				if (this.editCol_ == 0) {
					this.channel_.color_ = new Color(
							AcqControlDlg.this.colorPrefs_.getInt(
									"Color_"
											+ AcqControlDlg.this._acqEng
													.getChannelGroup() + "_"
											+ this.combo_.getSelectedItem(),
									Color.white.getRGB()));
					return this.combo_.getSelectedItem();
				}
				if ((this.editCol_ == 1) || (this.editCol_ == 2))
					return new Double(
							NumberUtils.displayStringToDouble(this.text_
									.getText()));
				if (this.editCol_ == 3)
					return new Boolean(this.checkBox_.isSelected());
				if (this.editCol_ == 4)
					return new Integer(
							NumberUtils.displayStringToInt(this.text_.getText()));
				if (this.editCol_ == 5) {
					Color c = this.colorLabel_.getBackground();
					return c;
				}
			} catch (ParseException p) {
				ReportingUtils.showError(p);
				err = new String("Internal error: unknown column");
			}
			return err;
		}
	}

	public class ChannelTableModel extends AbstractTableModel implements
			TableModelListener {
		private static final long serialVersionUID = 3290621191844925827L;
		private ArrayList<ChannelSpec> channels_;
		private AcquisitionEngine acqEng_;
		public final String[] COLUMN_NAMES = { "Configuration", "Exposure",
				"Z-offset", "Z-stack", "Skip Fr.", "Color" };

		public ChannelTableModel(AcquisitionEngine eng) {
			this.acqEng_ = eng;
			addTableModelListener(this);
		}

		public int getRowCount() {
			if (this.channels_ == null) {
				return 0;
			}
			return this.channels_.size();
		}

		public int getColumnCount() {
			return this.COLUMN_NAMES.length;
		}

		public String getColumnName(int columnIndex) {
			return this.COLUMN_NAMES[columnIndex];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if ((this.channels_ != null) && (rowIndex < this.channels_.size())) {
				if (columnIndex == 0)
					return this.channels_.get(rowIndex).config_;
				if (columnIndex == 1)
					return new Double(this.channels_.get(rowIndex).exposure_);
				if (columnIndex == 2)
					return new Double(this.channels_.get(rowIndex).zOffset_);
				if (columnIndex == 3)
					return new Boolean(
							this.channels_.get(rowIndex).doZStack_
									.booleanValue());
				if (columnIndex == 4)
					return new Integer(
							this.channels_.get(rowIndex).skipFactorFrame_);
				if (columnIndex == 5) {
					return this.channels_.get(rowIndex).color_;
				}
			}
			return null;
		}

		public Class<? extends Object> getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public void setValueAt(Object value, int row, int col) {
			if ((row >= this.channels_.size()) || (value == null)) {
				return;
			}

			ChannelSpec channel = this.channels_.get(row);

			if (col == 0)
				channel.config_ = value.toString();
			else if (col == 1)
				channel.exposure_ = ((Double) value).doubleValue();
			else if (col == 2)
				channel.zOffset_ = ((Double) value).doubleValue();
			else if (col == 3)
				channel.doZStack_ = ((Boolean) value);
			else if (col == 4)
				channel.skipFactorFrame_ = ((Integer) value).intValue();
			else if (col == 5) {
				channel.color_ = ((Color) value);
			}

			this.acqEng_.setChannel(row, channel);
			AcqControlDlg.this.repaint();
		}

		public boolean isCellEditable(int nRow, int nCol) {
			return (nCol != 3) || (this.acqEng_.isZSliceSettingEnabled());
		}

		public void tableChanged(TableModelEvent e) {
			int row = e.getFirstRow();
			if (row < 0) {
				return;
			}
			int col = e.getColumn();
			if (col < 0) {
				return;
			}
			ChannelSpec channel = this.channels_.get(row);
			TableModel model = (TableModel) e.getSource();
			if (col == 5) {
				Color color = (Color) model.getValueAt(row, col);
				AcqControlDlg.this.colorPrefs_.putInt(
						"Color_" + this.acqEng_.getChannelGroup() + "_"
								+ channel.config_, color.getRGB());
			}
		}

		public void setChannels(ArrayList<ChannelSpec> ch) {
			this.channels_ = ch;
		}

		public ArrayList<ChannelSpec> getChannels() {
			return channels_;
		}

		public void addNewChannel() {
			ChannelSpec channel = new ChannelSpec();
			channel.config_ = "";
			if (this.acqEng_.getChannelConfigs().length > 0) {
				for (String config : this.acqEng_.getChannelConfigs()) {
					boolean unique = true;
					for (ChannelSpec chan : this.channels_) {
						if (config.contentEquals(chan.config_))
							unique = false;
					}
					if (unique) {
						channel.config_ = config;
						break;
					}
				}
				if (channel.config_.length() == 0) {
					ReportingUtils
							.showMessage("No more channels are available\nin this channel group.");
				} else {
					channel.color_ = new Color(
							AcqControlDlg.this.colorPrefs_.getInt("Color_"
									+ this.acqEng_.getChannelGroup() + "_"
									+ channel.config_, Color.white.getRGB()));
					this.channels_.add(channel);
				}
			}
		}

		public void removeChannel(int chIndex) {
			if ((chIndex >= 0) && (chIndex < this.channels_.size()))
				this.channels_.remove(chIndex);
		}

		public int rowDown(int rowIdx) {
			if ((rowIdx >= 0) && (rowIdx < this.channels_.size() - 1)) {
				ChannelSpec channel = this.channels_.get(rowIdx);
				this.channels_.remove(rowIdx);
				this.channels_.add(rowIdx + 1, channel);
				return rowIdx + 1;
			}
			return rowIdx;
		}

		public int rowUp(int rowIdx) {
			if ((rowIdx >= 1) && (rowIdx < this.channels_.size())) {
				ChannelSpec channel = this.channels_.get(rowIdx);
				this.channels_.remove(rowIdx);
				this.channels_.add(rowIdx - 1, channel);
				return rowIdx - 1;
			}
			return rowIdx;
		}

		public String[] getAvailableChannels() {
			return this.acqEng_.getChannelConfigs();
		}

		public void cleanUpConfigurationList() {
			for (Iterator<ChannelSpec> it = this.channels_.iterator(); it
					.hasNext();) {
				String config = it.next().config_;
				if ((!config.contentEquals(""))
						&& (!this.acqEng_.isConfigAvailable(config))) {
					it.remove();
				}
			}
			fireTableStructureChanged();
		}

		public boolean duplicateChannels() {
			for (int i = 0; i < this.channels_.size() - 1; i++) {
				for (int j = i + 1; j < this.channels_.size(); j++) {
					if (this.channels_.get(i).config_.equals(this.channels_
							.get(j).config_)) {
						return true;
					}
				}
			}
			return false;
		}
	}

	private class AcqFileFilter extends FileFilter {
		private final String EXT_BSH;
		private final String DESCRIPTION;

		public AcqFileFilter() {
			this.EXT_BSH = new String("xml");
			this.DESCRIPTION = new String("XML files (*.xml)");
		}

		public boolean accept(File f) {
			if (f.isDirectory()) {
				return true;
			}

			return this.EXT_BSH.equals(getExtension(f));
		}

		public String getDescription() {
			return this.DESCRIPTION;
		}

		private String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if ((i > 0) && (i < s.length() - 1)) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}
	}
}