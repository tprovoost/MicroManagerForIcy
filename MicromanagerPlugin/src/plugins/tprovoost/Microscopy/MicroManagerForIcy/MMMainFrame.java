package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.common.MenuCallback;
import icy.common.listener.AcceptListener;
import icy.gui.component.IcyLogo;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.IcyFrame;
import icy.gui.frame.IcyFrameAdapter;
import icy.gui.frame.IcyFrameEvent;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.main.MainFrame;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUTBand;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.preferences.IcyPreferences;
import icy.preferences.XMLPreferences;
import icy.preferences.XMLPreferences.XMLPreferencesRoot;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import ij.gui.ImageWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.MMCoreJ;
import mmcorej.MMEventCallback;
import mmcorej.StrVector;
import mmcorej.TaggedImage;

import org.json.JSONObject;
import org.micromanager.AcqControlDlg;
import org.micromanager.CalibrationListDlg;
import org.micromanager.ConfigGroupPad;
import org.micromanager.PositionListDlg;
import org.micromanager.PropertyEditor;
import org.micromanager.acquisition.AcquisitionManager;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.AcquisitionEngine;
import org.micromanager.api.Autofocus;
import org.micromanager.api.IAcquisitionEngine2010;
import org.micromanager.api.ImageCache;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;
import org.micromanager.conf2.ConfiguratorDlg2;
import org.micromanager.conf2.MMConfigFileException;
import org.micromanager.conf2.MicroscopeModel;
import org.micromanager.navigation.PositionList;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.ReportingUtils;
import org.micromanager.utils.StateItem;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper.AcquisitionWrapperEngineIcy;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper.ConfigButtonsPanel;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack.ColorEditor;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack.ColorRenderer;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack.JTableEvolvedModel;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack.SliderEditor;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack.SliderRenderer;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.MicroscopePainterPreferences;

/**
 * Main frame for Micro Manager Plugin (Singleton pattern)
 * 
 * @author Thomas Provoost
 */
public class MMMainFrame extends IcyFrame implements ScriptInterface {

	// ------------------
	// CORE OF MMAINFRAME
	// ------------------
	/** Singleton pattern for this window */
	static MMMainFrame _singleton;
	/** Reference to core */
	private static MicroscopeCore mCore = null;
	/** Is the MMMainFrame instanced ? */
	private static boolean instanced = false;
	/** Is the MMMainFrame currently instancing ? */
	private static boolean instancing = false;
	/** AutofocusManager for multi-d acquisition */
	private AutofocusManager _afMgr;
	/** Position list for multi-d acquisition */
	private PositionList _posList;
	/** Position list dialog for multi-d acquisition */
	private PositionListDlg posListDlg_;
	/** Unused at the moment */
	private Component saveConfigButton_;
	/** Property Editor */
	PropertyEditor editor;
	/** File path of the config file. */
	private String _sysConfigFile;
	/** Camera Name */
	private String _camera_label;
	/** Vector of shutters */
	private StrVector _shutters;
	/** List of all plugins added to the main plugin via addPlugin method. */
	private ArrayList<MicroscopePlugin> _list_plugin = new ArrayList<MicroscopePlugin>();
	/** List of all objects that asked for continuous acquisition */
	private ArrayList<Object> _plugin_contAcq_list = new ArrayList<Object>();
	/** List of all progresses bars added to the main plugin. */
	private ArrayList<RunningProgress> _list_progress = new ArrayList<MMMainFrame.RunningProgress>();
	/** Used to show a bar when a continuous acquisition is currently running. */
	private RunningProgress continuousProgress = new RunningProgress();
	/**
	 * This boolean tells if the configuration is loaded. isConfigLoaded is true
	 * when a configuration file is being loaded. It is false when over.
	 * */
	private boolean _isConfigLoaded;
	/** Has the config changed or not. */
	private boolean _configChanged_ = false;
	/** Uses or not the actual interval on camera. */
	boolean _usesActualIntervalms;
	/** Used when the application asks if can exit or not. */
	boolean _pluginListEmpty = true;
	/** Used to know if the application can exit or not. */
	AcceptListener acceptListener;
	JTable painterTable;
	AdvancedConfigurationDialog advancedDlg;

	// ------------
	// PREFERENCES
	// --------------
	/** String for Preferences */
	public static final String NODE_NAME = "MicroManagerForIcy";
	/** Preferences root */
	public XMLPreferences _root;
	/** Preferences on a file */
	public XMLPreferences _prefs;
	/** Constant value of the preference concerning exposure */
	private static final String PREF_EXPOSURE = "exposure";
	/** Constant value of the preference concerning shutter */
	private static final String PREF_SHUTTER = "shutter";
	/** Constant value of the preference concerning absolute histogram or not */
	private static final String PREF_ABS_HIST = "absolutehist";
	/** Constant value of the preference concerning bit depth */
	private static final String PREF_BITDEPTH = "bitdepth";

	// -------
	// GUI
	// ------
	private JPanel _mainPanel;
	private JTabbedPane _tabbedPanel;
	// CAMERA SETTINGS PART
	/** Container for camera settings for layout purpose. */
	private JPanel _panelCameraSettingsContainer;
	/** Container of all camera settings */
	private JPanel _panel_cameraSettings;
	/** Text Field containing exposure. */
	private JTextField _txtExposure;
	/** ComboBox containing current value of binning. */
	private JComboBox _combo_binning;
	/** ComboBox containing current shutter. */
	private JComboBox _combo_shutters;
	/** Check box concerning the utilization of absolute histogram. */
	private JCheckBox _cbAbsoluteHisto;
	/** ComboBox concerning the bit depth of the absolute histogram. */
	private JComboBox _comboBitDepth;
	/** Size of a single icon in the menu. */
	private static final int MENU_ICON_SIZE = 20;
	/**
	 * Contains the int value of the shortcut depending on the current platform.
	 */
	private static final int SHORTCUTKEY_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private static final String PREFS_FB_GROUP = "filterBlock";
	private static final String PREFS_OT_GROUP = "objectiveTurret";

	// CONFIG PART
	/** Container for configuration. */
	private JPanel _panelConfig;
	/** Configuration panel. */
	private ConfigGroupPad _groupPad;
	/** Buttons for editing the configuration panel. */
	private ConfigButtonsPanel _groupButtonsPanel;

	// ACQUISITION PART
	/** Container for the right part of interface */
	private JPanel _panelAcquisitions;

	// COLOR SETTINGS
	private JPanel _panelColorChooser;
	private MicroscopePainterPreferences painterPreferences;

	// PROGRESS FRAME
	/**
	 * IcyFrame containing the progress bar. This frame is shown while the
	 * configuration file is being loaded.
	 * */
	private IcyFrame _progressFrame;
	/** Progress Bar of the configuration file loading. */
	private JProgressBar _progressBar;
	private EventCallBackManager callback;
	private AcquisitionManager acqMgr;
	private AcquisitionEngine engine_;

	/**
	 * Singleton pattern : private constructor Use getInstance() instead.
	 */
	private MMMainFrame() {
		super(NODE_NAME, false, true, false, true);

		// --------------
		// INITIALIZATION
		// --------------
		_sysConfigFile = "";
		_isConfigLoaded = false;
		_root = IcyPreferences.pluginsRoot().node(NODE_NAME);
		final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

		// --------------
		// PROGRESS FRAME
		// --------------
		_progressFrame = new IcyFrame("", false, false, false, false);
		_progressBar = new JProgressBar();
		_progressBar.setString("Please wait while loading...");
		_progressBar.setStringPainted(true);
		_progressBar.setIndeterminate(true);
		_progressBar.setMinimum(0);
		_progressBar.setMaximum(1000);
		_progressBar.setBounds(50, 50, 100, 30);
		_progressFrame.setSize(300, 100);
		_progressFrame.setResizable(false);
		_progressFrame.add(_progressBar);
		_progressFrame.addToMainDesktopPane();
		loadConfig();
		if (_sysConfigFile == "") {
			return;
		}
		instancing = true;
		ThreadUtil.bgRun(new Runnable() {

			@Override
			public void run() {
				while (!_isConfigLoaded) {
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
				ThreadUtil.invokeLater(new Runnable() {

					@Override
					public void run() {
						// --------------------
						// START INITIALIZATION
						// --------------------
						if (_progressBar != null)
							getContentPane().remove(_progressBar);
						if (mCore == null) {
							close();
							return;
						}

						ReportingUtils.setCore(mCore);
						_afMgr = new AutofocusManager(MMMainFrame.this);
						acqMgr = new AcquisitionManager();
						PositionList posList = new PositionList();
						
						_camera_label = MMCoreJ.getG_Keyword_CameraName();
						if (_camera_label == null)
							_camera_label = "";
						try {
							setPositionList(posList);
						} catch (MMScriptException e1) {
							e1.printStackTrace();
						}
						posListDlg_ = new PositionListDlg(mCore, MMMainFrame.this, _posList, null);
						posListDlg_.setModalityType(ModalityType.APPLICATION_MODAL);

						callback = new EventCallBackManager();
						mCore.registerCallback(callback);

						engine_ = new AcquisitionWrapperEngineIcy();
						engine_.setParentGUI(MMMainFrame.this);
						engine_.setCore(mCore, getAutofocusManager());
						engine_.setPositionList(getPositionList());
						
						setSystemMenuCallback(new MenuCallback() {

							@Override
							public JMenu getMenu() {
								JMenu toReturn = MMMainFrame.this.getDefaultSystemMenu();
								JMenuItem hconfig = new JMenuItem("Configuration Wizard");
								hconfig.setIcon(new IcyIcon("cog", MENU_ICON_SIZE));

								hconfig.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										if (!_pluginListEmpty
												&& !ConfirmDialog
														.confirm("Are you sure ?",
																"<html>Loading the Configuration Wizard will unload all the devices and pause all running acquisitions.</br> Are you sure you want to continue ?</html>"))
											return;
										notifyConfigAboutToChange(null);
										try {
											mCore.unloadAllDevices();
										} catch (Exception e1) {
											e1.printStackTrace();
										}
										String previous_config = _sysConfigFile;
										ConfiguratorDlg2 configurator = new ConfiguratorDlg2(mCore, _sysConfigFile);
										configurator.setVisible(true);
										String res = configurator.getFileName();
										if (_sysConfigFile == "" || _sysConfigFile == res || res == "") {
											_sysConfigFile = previous_config;
										} else {
											_sysConfigFile = res;
											LoadFrame frame = LoadFrame.getInstance();
											frame.loadFile(_sysConfigFile);
											loadConfig(true);
										}
										refreshGUI();
										notifyConfigChanged(null);
									}
								});

								JMenuItem menuPxSizeConfigItem = new JMenuItem("Pixel Size Config");
								menuPxSizeConfigItem.setIcon(new IcyIcon("link", MENU_ICON_SIZE));
								menuPxSizeConfigItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.SHIFT_DOWN_MASK | SHORTCUTKEY_MASK));
								menuPxSizeConfigItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										CalibrationListDlg dlg = new CalibrationListDlg(mCore);
										dlg.setDefaultCloseOperation(2);
										dlg.setParentGUI(MMMainFrame.this);
										dlg.setVisible(true);
										dlg.addWindowListener(new WindowAdapter() {
											@Override
											public void windowClosed(WindowEvent e) {
												super.windowClosed(e);
												notifyConfigChanged(null);
											}
										});
										notifyConfigAboutToChange(null);
									}
								});

								JMenuItem loadConfigItem = new JMenuItem("Load Configuration");
								loadConfigItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.SHIFT_DOWN_MASK | SHORTCUTKEY_MASK));
								loadConfigItem.setIcon(new IcyIcon("folder_open", MENU_ICON_SIZE));
								loadConfigItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										loadConfig();
										initializeGUI();
										refreshGUI();
									}
								});
								JMenuItem saveConfigItem = new JMenuItem("Save Configuration");
								saveConfigItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK | SHORTCUTKEY_MASK));
								saveConfigItem.setIcon(new IcyIcon("save", MENU_ICON_SIZE));
								saveConfigItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										saveConfig();
									}
								});
								JMenuItem advancedConfigItem = new JMenuItem("Advanced Configuration");
								advancedConfigItem.setIcon(new IcyIcon("wrench_plus", MENU_ICON_SIZE));
								advancedConfigItem.addActionListener(new ActionListener() {

									/**
									 */
									@Override
									public void actionPerformed(ActionEvent e) {
										new ToolTipFrame("<html><h3>About Advanced Config</h3><p>Advanced Configuration is a tool "
												+ "in which you fill some data <br/>about your configuration that some "
												+ "plugins may need to access to.<br/> Exemple: the real values of the magnification" + "of your objectives.</p></html>",
												"MM4IcyAdvancedConfig");
										if (advancedDlg == null)
											advancedDlg = new AdvancedConfigurationDialog();
										advancedDlg.setVisible(!advancedDlg.isVisible());
										advancedDlg.setLocationRelativeTo(mainFrame);
									}
								});
								JMenuItem loadPresetConfigItem = new JMenuItem("Load Configuration Presets");
								loadPresetConfigItem.setIcon(new IcyIcon("doc_import", MENU_ICON_SIZE));
								loadPresetConfigItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										notifyConfigAboutToChange(null);
										loadPresets();
										notifyConfigChanged(null);
									}
								});
								JMenuItem savePresetConfigItem = new JMenuItem("Save Configuration Presets");
								savePresetConfigItem.setIcon(new IcyIcon("doc_export", MENU_ICON_SIZE));
								savePresetConfigItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										savePresets();
									}
								});
								JMenuItem aboutItem = new JMenuItem("About");
								aboutItem.setIcon(new IcyIcon("info", MENU_ICON_SIZE));
								aboutItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										final JDialog dialog = new JDialog(mainFrame, "About");
										JPanel panel_container = new JPanel();
										panel_container.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
										JPanel center = new JPanel(new BorderLayout());
										final JLabel value = new JLabel("<html><body>" + "<h2>About</h2><p>Micro-Manager for Icy is being developed by Thomas Provoost."
												+ "<br/>Copyright 2011, Institut Pasteur</p><br/>"
												+ "<p>This plugin is based on Micro-Manager© v1.4.6. which is developed under the following license:<br/>"
												+ "<i>This software is distributed free of charge in the hope that it will be<br/>"
												+ "useful, but WITHOUT ANY WARRANTY; without even the implied<br/>"
												+ "warranty of merchantability or fitness for a particular purpose. In no<br/>"
												+ "event shall the copyright owner or contributors be liable for any direct,<br/>"
												+ "indirect, incidental spacial, examplary, or consequential damages.<br/>"
												+ "Copyright University of California San Francisco, 2007, 2008, 2009,<br/>" + "2010. All rights reserved.</i>" + "</p>"
												+ "</body></html>");
										JLabel link = new JLabel("<html><a href=\"\">For more information, please follow this link.</a></html>");
										link.addMouseListener(new MouseAdapter() {
											@Override
											public void mousePressed(MouseEvent mouseevent) {
												NetworkUtil.openURL("http://valelab.ucsf.edu/~MM/MMwiki/index.php/Micro-Manager");
											}
										});
										value.setSize(new Dimension(50, 18));
										value.setAlignmentX(SwingConstants.HORIZONTAL);
										value.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

										center.add(value, BorderLayout.CENTER);
										center.add(link, BorderLayout.SOUTH);

										JPanel panel_south = new JPanel();
										panel_south.setLayout(new BoxLayout(panel_south, BoxLayout.X_AXIS));
										JButton btn = new JButton("OK");
										btn.addActionListener(new ActionListener() {

											@Override
											public void actionPerformed(ActionEvent actionevent) {
												dialog.dispose();
											}
										});
										panel_south.add(Box.createHorizontalGlue());
										panel_south.add(btn);
										panel_south.add(Box.createHorizontalGlue());

										dialog.setLayout(new BorderLayout());
										panel_container.setLayout(new BorderLayout());
										panel_container.add(center, BorderLayout.CENTER);
										panel_container.add(panel_south, BorderLayout.SOUTH);
										dialog.add(panel_container, BorderLayout.CENTER);
										dialog.setResizable(false);
										dialog.setVisible(true);
										dialog.pack();
										dialog.setLocation((int) mainFrame.getSize().getWidth() / 2 - dialog.getWidth() / 2,
												(int) mainFrame.getSize().getHeight() / 2 - dialog.getHeight() / 2);
										dialog.setLocationRelativeTo(mainFrame);
									}
								});
								JMenuItem propertyBrowserItem = new JMenuItem("Property Browser");
								propertyBrowserItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, SHORTCUTKEY_MASK));
								propertyBrowserItem.setIcon(new IcyIcon("db", MENU_ICON_SIZE));
								propertyBrowserItem.addActionListener(new ActionListener() {

									@Override
									public void actionPerformed(ActionEvent e) {
										editor.setVisible(!editor.isVisible());
									}
								});
								int idx = 0;
								toReturn.insert(hconfig, idx++);
								toReturn.insert(loadConfigItem, idx++);
								toReturn.insert(saveConfigItem, idx++);
								toReturn.insert(advancedConfigItem, idx++);
								toReturn.insertSeparator(idx++);
								toReturn.insert(loadPresetConfigItem, idx++);
								toReturn.insert(savePresetConfigItem, idx++);
								toReturn.insertSeparator(idx++);
								toReturn.insert(propertyBrowserItem, idx++);
								toReturn.insert(menuPxSizeConfigItem, idx++);
								toReturn.insertSeparator(idx++);
								toReturn.insert(aboutItem, idx++);
								return toReturn;
							}
						});

						saveConfigButton_ = new JButton("Save Button");

						// SETUP
						_groupPad = new ConfigGroupPad();
						_groupPad.setParentGUI(MMMainFrame.this);
						_groupPad.setFont(new Font("", 0, 10));
						_groupPad.setCore(mCore);
						_groupPad.setParentGUI(MMMainFrame.this);
						_groupButtonsPanel = new ConfigButtonsPanel();
						_groupButtonsPanel.setCore(mCore);
						_groupButtonsPanel.setGUI(MMMainFrame.this);
						_groupButtonsPanel.setConfigPad(_groupPad);

						// LEFT PART OF INTERFACE
						_panelConfig = new JPanel();
						_panelConfig.setLayout(new BoxLayout(_panelConfig, BoxLayout.Y_AXIS));
						_panelConfig.add(_groupPad, BorderLayout.CENTER);
						_panelConfig.add(_groupButtonsPanel, BorderLayout.SOUTH);
						_panelConfig.setPreferredSize(new Dimension(300, 300));

						// MIDDLE PART OF INTERFACE
						_panel_cameraSettings = new JPanel();
						_panel_cameraSettings.setLayout(new GridLayout(5, 2));
						_panel_cameraSettings.setMinimumSize(new Dimension(100, 200));

						_txtExposure = new JTextField();
						try {
							mCore.setExposure(90.0D);
							_txtExposure.setText(String.valueOf(mCore.getExposure()));
						} catch (Exception e2) {
							_txtExposure.setText("90");
						}
						_txtExposure.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
						_txtExposure.addKeyListener(new KeyAdapter() {
							@Override
							public void keyPressed(KeyEvent keyevent) {
								if (keyevent.getKeyCode() == KeyEvent.VK_ENTER)
									setExposure();
							}
						});
						_panel_cameraSettings.add(new JLabel("Exposure [ms]: "));
						_panel_cameraSettings.add(_txtExposure);

						_combo_binning = new JComboBox();
						_combo_binning.setMaximumRowCount(4);
						_combo_binning.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
						_combo_binning.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								changeBinning();
							}
						});

						_panel_cameraSettings.add(new JLabel("Binning: "));
						_panel_cameraSettings.add(_combo_binning);

						_combo_shutters = new JComboBox();
						_combo_shutters.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								try {
									if (_combo_shutters.getSelectedItem() != null) {
										mCore.setShutterDevice((String) _combo_shutters.getSelectedItem());
										_prefs.put(PREF_SHUTTER, (String) _combo_shutters.getItemAt(_combo_shutters.getSelectedIndex()));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						});
						_combo_shutters.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
						_panel_cameraSettings.add(new JLabel("Shutter : "));
						_panel_cameraSettings.add(_combo_shutters);

						ActionListener action_listener = new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								updateHistogram();
							}
						};

						_cbAbsoluteHisto = new JCheckBox();
						_cbAbsoluteHisto.addActionListener(action_listener);
						_cbAbsoluteHisto.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent actionevent) {
								_comboBitDepth.setEnabled(_cbAbsoluteHisto.isSelected());
								_prefs.putBoolean(PREF_ABS_HIST, _cbAbsoluteHisto.isSelected());
							}
						});
						_panel_cameraSettings.add(new JLabel("Display absolute histogram ?"));
						_panel_cameraSettings.add(_cbAbsoluteHisto);

						_comboBitDepth = new JComboBox(new String[] { "8-bit", "9-bit", "10-bit", "11-bit", "12-bit", "13-bit", "14-bit", "15-bit", "16-bit" });
						_comboBitDepth.addActionListener(action_listener);
						_comboBitDepth.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent actionevent) {
								_prefs.putInt(PREF_BITDEPTH, _comboBitDepth.getSelectedIndex());
							}
						});
						_comboBitDepth.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
						_comboBitDepth.setEnabled(false);
						_panel_cameraSettings.add(new JLabel("Select your bit depth for abs. hitogram: "));
						_panel_cameraSettings.add(_comboBitDepth);

						// Acquisition
						_panelAcquisitions = new JPanel();
						_panelAcquisitions.setLayout(new BoxLayout(_panelAcquisitions, BoxLayout.Y_AXIS));

						// Color settings
						_panelColorChooser = new JPanel();
						_panelColorChooser.setLayout(new BoxLayout(_panelColorChooser, BoxLayout.Y_AXIS));
						painterPreferences = MicroscopePainterPreferences.getInstance();
						painterPreferences.setPreferences(_prefs.node("paintersPreferences"));
						painterPreferences.loadColors();

						HashMap<String, Color> allColors = painterPreferences.getColors();
						String[] allKeys = (String[]) allColors.keySet().toArray(new String[0]);
						String[] columnNames = { "Painter", "Color", "Transparency" };
						Object[][] data = new Object[allKeys.length][3];

						for (int i = 0; i < allKeys.length; ++i) {
							final int actualRow = i;
							String actualKey = allKeys[i].toString();
							data[i][0] = actualKey;
							data[i][1] = allColors.get(actualKey);
							final JSlider slider = new JSlider(0, 255, allColors.get(actualKey).getAlpha());
							slider.addChangeListener(new ChangeListener() {

								@Override
								public void stateChanged(ChangeEvent changeevent) {
									painterTable.setValueAt(slider, actualRow, 2);
								}
							});
							data[i][2] = slider;
						}
						final AbstractTableModel tableModel = new JTableEvolvedModel(columnNames, data);
						painterTable = new JTable(tableModel);
						painterTable.getModel().addTableModelListener(new TableModelListener() {

							@Override
							public void tableChanged(TableModelEvent tablemodelevent) {
								if (tablemodelevent.getType() == TableModelEvent.UPDATE) {
									int row = tablemodelevent.getFirstRow();
									int col = tablemodelevent.getColumn();
									String columnName = tableModel.getColumnName(col);
									String painterName = (String) tableModel.getValueAt(row, 0);
									if (columnName.contains("Color")) {
										// New color value
										int alpha = painterPreferences.getColor(painterName).getAlpha();
										Color coloNew = (Color) tableModel.getValueAt(row, 1);
										painterPreferences.setColor(painterName, new Color(coloNew.getRed(), coloNew.getGreen(), coloNew.getBlue(), alpha));
									} else if (columnName.contains("Transparency")) {
										// New alpha value
										Color c = painterPreferences.getColor(painterName);
										int alphaValue = ((JSlider) tableModel.getValueAt(row, 2)).getValue();
										painterPreferences.setColor(painterName, new Color(c.getRed(), c.getGreen(), c.getBlue(), alphaValue));
									}
									/*
									 * for (int i = 0; i <
									 * tableModel.getRowCount(); ++i) { try {
									 * String painterName = (String)
									 * tableModel.getValueAt(i, 0); Color c =
									 * (Color) tableModel.getValueAt(i, 1); int
									 * alphaValue; if (ASpinnerChanged &&
									 * tablemodelevent.getFirstRow() == i) {
									 * alphaValue = ((JSlider)
									 * tableModel.getValueAt(i, 2)).getValue();
									 * } else { alphaValue =
									 * painterPreferences.getColor
									 * (painterPreferences
									 * .getPainterName(i)).getAlpha(); }
									 * painterPreferences.setColor(painterName,
									 * new Color(c.getRed(), c.getGreen(),
									 * c.getBlue(), alphaValue)); } catch
									 * (Exception e) { System.out.println(
									 * "error with painter table update"); } }
									 */
								}
							}
						});
						painterTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
						painterTable.setFillsViewportHeight(true);

						// Create the scroll pane and add the table to it.
						JScrollPane scrollPane = new JScrollPane(painterTable);

						// Set up renderer and editor for the Favorite Color
						// column.
						painterTable.setDefaultRenderer(Color.class, new ColorRenderer(true));
						painterTable.setDefaultEditor(Color.class, new ColorEditor());

						painterTable.setDefaultRenderer(JSlider.class, new SliderRenderer(0, 255));
						painterTable.setDefaultEditor(JSlider.class, new SliderEditor());

						_panelColorChooser.add(scrollPane);
						_panelColorChooser.add(Box.createVerticalGlue());

						_mainPanel = new JPanel();
						_mainPanel.setLayout(new BorderLayout());

						// EDITOR
						// will refresh the data and verify if any change
						// occurs.
						// editor = new PropertyEditor(MMMainFrame.this);
						// editor.setCore(mCore);
						// editor.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
						// editor.addToMainDesktopPane();
						// editor.refresh();
						// editor.start();

						editor = new PropertyEditor();
						editor.setGui(MMMainFrame.this);
						editor.setCore(mCore);
						editor.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
						// editor.addToMainDesktopPane();
						// editor.refresh();
						// editor.start();

						add(_mainPanel);
						initializeGUI();
						loadPreferences();
						refreshGUI();
						setResizable(true);
						addToMainDesktopPane();
						instanced = true;
						instancing = false;
						_singleton = MMMainFrame.this;
						setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
						addFrameListener(new IcyFrameAdapter() {
							@Override
							public void icyFrameClosing(IcyFrameEvent e) {
								close();
							}
						});
						acceptListener = new AcceptListener() {

							@Override
							public boolean accept(Object source) {
								close();
								return _pluginListEmpty;
							}
						};
						Icy.getMainInterface().addCanExitListener(acceptListener);
					}

				});
			}
		});
	}

	/**
	 * Update the histogram with camera settings.
	 */
	private void updateHistogram() {
		Sequence s = Icy.getMainInterface().getFocusedSequence();
		if (s != null && !s.getDataType_().isFloat()) {
			for (Viewer v : s.getViewers()) {
				for (LUTBand lutband : v.getLut().getLutBands()) {
					if (_cbAbsoluteHisto.isSelected()) {
						s.setAutoUpdateChannelBounds(false);
						double maxvalue = Math.pow(2, _comboBitDepth.getSelectedIndex() + 8);
						lutband.getScaler().setAbsLeftRightIn(0, maxvalue);
					} else {
						s.setAutoUpdateChannelBounds(true);
						lutband.getScaler().setAbsLeftRightIn(lutband.getMin(), lutband.getMax());
					}
				}
			}
		}
	}

	private void loadConfig() {
		loadConfig(false);
	}

	/**
	 * Loads configuration using a JFileChooser Shows a Dialog if a
	 * configuration already exists.
	 */
	private void loadConfig(boolean force) {
		_isConfigLoaded = false;
		if (mCore != null && !force) {
			if (!ConfirmDialog.confirm("Are you sure ", "Do you want to load another configuration ?")) {
				return;
			}
			if (mCore.isSequenceRunning()) {
				try {
					mCore.stopSequenceAcquisition();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
			if (!_list_plugin.isEmpty()) {
				for (MicroscopePlugin p : _list_plugin) {
					try {
						p.notifyConfigAboutToChange(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			try {
				mCore.unloadAllDevices();
				mCore.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!force) {
			LoadFrame loadingFrame = LoadFrame.getInstance();
			int returnVal = loadingFrame.showDialog();
			if (returnVal == 0)
				_sysConfigFile = loadingFrame.getPath();
		}
		setVisible(false);
		_progressFrame.center();
		_progressFrame.setVisible(true);
		_progressFrame.requestFocus();
		ThreadUtil.bgRun(new Runnable() {

			@Override
			public void run() {
				loadCMMCore(_sysConfigFile);
				_progressFrame.setVisible(false);
				if (mCore == null) {
					if (ConfirmDialog.confirm("Error while launching", "Do you want to load another configuration ?")) {
						ThreadUtil.invokeLater(new Runnable() {
							@Override
							public void run() {
								loadConfig();
							}
						});
					} else {
						_isConfigLoaded = false;
						instancing = false;
						instanced = false;
					}
				} else {
					_isConfigLoaded = true;
					_prefs = _root.node(new File(_sysConfigFile).getName());
					System.out.println("Save file: " + _prefs.absolutePath());
					if (_groupButtonsPanel != null)
						initializeGUI();
					setVisible(true);
					if (!_list_plugin.isEmpty()) {
						for (MicroscopePlugin p : _list_plugin) {
							try {
								p.notifyConfigChanged(null);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
					requestFocus();
				}
			}
		});
	}

	/**
	 * load configuration file for the core
	 */
	void loadCMMCore(String path) {
		mCore = MicroscopeCore.getCore();
		if (mCore == null) {
			ThreadUtil.invokeNow(new Runnable() {
				@Override
				public void run() {
					JDialog dialog = createDLLErrorDialog();
					dialog.setVisible(true);
				}
			});
			return;
		}
		/*
		 * core.enableDebugLog(false); core.enableStderrLog(false);
		 * core.shutdownLogging();
		 */
		try {
			mCore.loadSystemConfiguration(path);
			try {
				StrVector pxSizeConfigs = mCore.getAvailablePixelSizeConfigs();
				if (!(pxSizeConfigs.size() >= 1))
					throw new Exception();
			} catch (Exception e) {
				mCore.definePixelSizeConfig("ResDefault");
				mCore.setPixelSizeUm("ResDefault", 1.0);
			}
			ThreadUtil.invokeLater(new Runnable() {

				@Override
				public void run() {
					{
						if (_groupPad != null)
							_groupPad.setCore(mCore);
						if (_groupButtonsPanel != null)
							_groupButtonsPanel.setCore(mCore);
					}
				}
			});
		} catch (Exception e) {

			String res = e.getMessage();
			if (res.contains("file not acessible or corrupted") || res.contains("Unable to load library")) {
				ThreadUtil.invokeNow(new Runnable() {

					@Override
					public void run() {
						JDialog dialog = createDLLErrorDialog();
						dialog.setVisible(true);
					}
				});
			} else {
				ThreadUtil.invokeNow(new Runnable() {

					@Override
					public void run() {
						JOptionPane.showMessageDialog(Icy.getMainInterface().getMainFrame().getRootPane(),
								"Error while initializing the microscope: please check if all devices are correctly turned on and recognized by the computer and"
										+ "quit any program using those devices.");
					}
				});
			}
			mCore = null;
		}
	}

	/**
	 * Load all saved preferences.
	 */
	private void loadPreferences() {
		_txtExposure.setText(_prefs.get(PREF_EXPOSURE, "90"));
		setExposure();
		String shutter = _prefs.get(PREF_SHUTTER, "");
		if (shutter != "") {
			for (int i = 0; i < _combo_shutters.getItemCount(); ++i)
				if (((String) _combo_shutters.getItemAt(i)) == shutter)
					_combo_shutters.setSelectedIndex(i);
		}
		_cbAbsoluteHisto.setSelected(_prefs.getBoolean(PREF_ABS_HIST, false));
		_comboBitDepth.setEnabled(_cbAbsoluteHisto.isSelected());
		_comboBitDepth.setSelectedIndex(_prefs.getInt(PREF_BITDEPTH, 0));
		mCore.setCurrentFilterBlockGroup(_prefs.get(PREFS_FB_GROUP, null));
		String currentObjective = _prefs.get(PREFS_OT_GROUP, null);
		if (currentObjective != null) {
			HashMap<String, Double> hashMag = new HashMap<String, Double>();
			XMLPreferences nodeObjective = _prefs.node(currentObjective);
			for (String key : nodeObjective.keys()) {
				String value = nodeObjective.get(key, null);
				try {
					hashMag.put(key, Double.parseDouble(value));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			mCore.setCurrentObjectiveTurretGroup(currentObjective, hashMag);
		}
		StageMover.loadPreferences(_prefs);
	}

	/**
	 * Create an error dialog concerning DLL issues with Micro-Manager.
	 * 
	 * @return Returns the dialog.
	 */
	private JDialog createDLLErrorDialog() {
		MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
		// Dialog frame to be returned
		final JDialog dialog = new JDialog(mainFrame, "Loading Error", true);

		// main panel of the dialog
		JPanel panel_main = new JPanel();
		panel_main.setLayout(new BorderLayout());

		JLabel lbl_html = new JLabel("<html>" + "<h2>Unable to load library</h2>" + "<br/><b>What happened ?</b>"
				+ "<p>The library is a file used by µManager to interact with the devices. Each device needs a specific file in order <br/>"
				+ "to work properly with the system. If only one file is missing, this error occurs.</p>"
				+ "<b>To avoid getting this problem again, please acknowledge the following steps: </b>"
				+ "<ol><li>Do you have Micro-Manager 1.4 installed ? If not, please install it via the button below.</li>"
				+ "<li>Check the application directory of Icy. You should find a file named:  " + "<ul><li>on Windows: MMCoreJ_wrap</li><li>on Mac: libMMCoreJ_wrap</li></ul>"
				+ "<li>Plus : you should have a file for each of your devices starting with the name:" + "<ul><li>on Windows: mmgr_dal_</li><li>on Mac: libmmgr_dal_</li></ul>"
				+ "<li>If you don't have these files, please copy (not move) them from the µManager application directory<br/>"
				+ "to your Icy application directory.</li></ol></html>");
		panel_main.add(lbl_html, BorderLayout.CENTER);
		panel_main.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		JPanel panel_buttons = new JPanel();
		panel_buttons.setLayout(new BoxLayout(panel_buttons, BoxLayout.X_AXIS));
		panel_buttons.add(Box.createHorizontalGlue());

		JButton btn_link = new JButton();
		btn_link.setText("Download Micro-Manager 1.4.6");
		btn_link.setBackground(Color.WHITE);
		btn_link.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				NetworkUtil.openURL("http://valelab.ucsf.edu/~MM/MMwiki/index.php/Micro-Manager_Version_Archive");
			}
		});
		panel_buttons.add(btn_link);

		JButton btn_ok = new JButton("OK");
		btn_ok.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		panel_buttons.add(btn_ok);
		panel_main.add(panel_buttons, BorderLayout.SOUTH);

		dialog.add(panel_main);
		dialog.pack();

		dialog.setLocation((int) mainFrame.getSize().getWidth() / 2 - dialog.getWidth() / 2, (int) mainFrame.getSize().getHeight() / 2 - dialog.getHeight() / 2);

		return dialog;
	}

	/**
	 * Save the configuration presets. From Micro-Manager.
	 */
	protected void saveConfig() {
		MicroscopeModel model = new MicroscopeModel();
		try {
			model.loadFromFile(_sysConfigFile);
			model.createSetupConfigsFromHardware(mCore);
			model.createResolutionsFromHardware(mCore);
			JFileChooser fc = new JFileChooser();
			boolean saveFile = true;
			File f;
			do {
				fc.setSelectedFile(new File(model.getFileName()));
				int retVal = fc.showSaveDialog(null);
				if (retVal == 0) {
					f = fc.getSelectedFile();

					if (f.exists()) {
						int sel = JOptionPane.showConfirmDialog(null, "Overwrite " + f.getName(), "File Save", 0);

						if (sel == 0)
							saveFile = true;
						else
							saveFile = false;
					}
				} else {
					return;
				}
			} while (!saveFile);

			model.saveToFile(f.getAbsolutePath());
			_sysConfigFile = f.getAbsolutePath();
			_configChanged_ = false;
			setConfigSaveButtonStatus(_configChanged_);
		} catch (MMConfigFileException e) {
			ReportingUtils.showError(e);
		}
	}

	/**
	 * Modify the binning from camera config combo box.
	 */
	private void changeBinning() {
		try {
			notifyConfigAboutToChange(null);
			boolean bWasRunning;
			if (bWasRunning = mCore.isSequenceRunning())
				mCore.stopSequenceAcquisition();

			if (_camera_label.length() > 0) {
				Object item = _combo_binning.getSelectedItem();
				if (item != null) {
					mCore.setProperty(_camera_label, MMCoreJ.getG_Keyword_Binning(), item.toString());
				}
			}
			notifyConfigChanged(null);
			if (bWasRunning)
				mCore.startContinuousSequenceAcquisition(0.0D);
		} catch (Exception e) {
			ReportingUtils.showError(e);
		}
	}

	/**
	 * Set the exposure from the camera config text field value.
	 */
	private void setExposure() {

		notifyConfigAboutToChange(null);
		Double test = Double.valueOf(_txtExposure.getText());
		if (test == null) {
			try {
				mCore.setExposure(10);
			} catch (Exception e) {
			}
		} else {
			try {
				mCore.setExposure(test.doubleValue());
				double exposure = mCore.getExposure();
				_txtExposure.setText("" + exposure);
				_prefs.put(PREF_EXPOSURE, "" + exposure);
			} catch (Exception exp) {
			}
		}
		notifyConfigChanged(null);
	}

	@Override
	public void close() {
		if (!_list_plugin.isEmpty()) {
			if (!ConfirmDialog.confirm("Some plugins are still running. Are you sure you want to close this ?"))
				return;
		}
		if (painterPreferences != null)
			painterPreferences.saveColors();
		_pluginListEmpty = true;
		// if (editor != null)
		// editor.stop();
		onClosed();
		removeFromMainDesktopPane();
		setVisible(false);
		Icy.getMainInterface().getMainFrame().repaint();
		Icy.getMainInterface().removeCanExitListener(acceptListener);
	}

	@Override
	public void onClosed() {
		if (mCore != null) {
			callback.delete();
			String filterBlockGroup = mCore.getCurrentFilterBlockGroup();
			if (filterBlockGroup != null)
				_prefs.put(PREFS_FB_GROUP, filterBlockGroup);
			String objectiveTuretGroup = mCore.getCurrentObjectiveTurretGroup();
			if (objectiveTuretGroup != null) {
				_prefs.put(PREFS_OT_GROUP, objectiveTuretGroup);
				HashMap<String, Double> hashMag = mCore.getAvailableMagnifications();
				if (hashMag != null) {
					XMLPreferences nodeObjectiveTurret = _prefs.node(objectiveTuretGroup);
					for (String currentKey : hashMag.keySet()) {
						nodeObjectiveTurret.put(currentKey, "" + hashMag.get(currentKey));
					}
				}
			}
			StrVector shutters = mCore.getLoadedDevicesOfType(DeviceType.ShutterDevice);
			for (String s : shutters) {
				try {
					mCore.setShutterDevice(s);
					mCore.setShutterOpen(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (mCore.isSequenceRunning())
				try {
					mCore.stopSequenceAcquisition();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			if (_list_plugin != null) {
				for (int i = 0; i < _list_plugin.size(); ++i)
					_list_plugin.get(i).MainGUIClosed();
			}
			try {
				mCore.unloadAllDevices();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mCore = null;
			MicroscopeCore.releaseCore();
		}
		dispose();
		super.onClosed();
	}

	@Override
	public void setSize(Dimension d) {
		super.setSize(d);
	}

	/**
	 * Singleton Pattern
	 * 
	 * @return instance or new instance of the MMMainFrame
	 */
	public static MMMainFrame getInstance() {
		if (!instanced && !instancing)
			return new MMMainFrame();
		return _singleton;
	}

	/**
	 * Singleton pattern This function delete the singleton
	 */
	public static void dispose() {
		instanced = false;
		instancing = false;
		_singleton = null;
	}

	/**
	 * To avoid
	 * 
	 * @return Actual state of MMMainFrame
	 * @deprecated please use {@link #isInstanced()}
	 */
	@Deprecated
	public static boolean instanced() {
		return instanced;
	}
	
	public static boolean isInstanced() {
		return instanced;
	}

	@Override
	public void applyContrastSettings(ContrastSettings contrastsettings, ContrastSettings contrastsettings1) {
	}

	@Override
	public boolean displayImage(Object obj) {
		return false;
	}

	@Override
	public boolean displayImageWithStatusLine(Object obj, String s) {
		return false;
	}

	@Override
	public void displayStatusLine(String s) {
	}

	@Override
	public void enableLiveMode(boolean flag) {
		try {
			if (flag)
				mCore.startContinuousSequenceAcquisition(0.0D);
			else
				mCore.stopSequenceAcquisition();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initialize all the GUI.
	 */
	@Override
	public void initializeGUI() {
		ThreadUtil.invokeNow(new Runnable() {

			@Override
			public void run() {
				_camera_label = mCore.getCameraDevice();
				if (_camera_label.length() > 0) {
					if (_combo_binning.getItemCount() > 0) {
						_combo_binning.removeAllItems();
					}
					StrVector binSizes;
					try {
						binSizes = mCore.getAllowedPropertyValues(_camera_label, MMCoreJ.getG_Keyword_Binning());
					} catch (Exception e1) {
						binSizes = new StrVector();
					}
					ActionListener[] listeners = _combo_binning.getActionListeners();
					for (int i = 0; i < listeners.length; i++) {
						_combo_binning.removeActionListener(listeners[i]);
					}
					for (int i = 0; i < binSizes.size(); i++) {
						_combo_binning.addItem(binSizes.get(i));
					}
					_combo_binning.setMaximumRowCount((int) binSizes.size());
					if (binSizes.size() == 0L)
						_combo_binning.setEditable(true);
					else {
						_combo_binning.setEditable(false);
					}

					for (int i = 0; i < listeners.length; i++) {
						_combo_binning.addActionListener(listeners[i]);
					}
					_combo_binning.setSelectedIndex(0);
				}
				try {
					_shutters = mCore.getLoadedDevicesOfType(DeviceType.ShutterDevice);
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (_shutters != null) {
					String[] items = new String[(int) _shutters.size()];
					for (int i = 0; i < _shutters.size(); i++) {
						items[i] = _shutters.get(i);
					}

					ActionListener[] listeners = _combo_shutters.getActionListeners();
					for (int i = 0; i < listeners.length; i++) {
						_combo_shutters.removeActionListener(listeners[i]);
					}
					if (_combo_shutters.getItemCount() > 0) {
						_combo_shutters.removeAllItems();
					}

					for (int i = 0; i < items.length; i++) {
						_combo_shutters.addItem(items[i]);
					}

					for (int i = 0; i < listeners.length; i++)
						_combo_shutters.addActionListener(listeners[i]);

					String activeShutter = mCore.getShutterDevice();
					if (activeShutter != null)
						_combo_shutters.setSelectedItem(activeShutter);
					else {
						_combo_shutters.setSelectedItem("");
					}
				}

				// ------------
				// GUI DRAW
				// -----------
				_mainPanel.removeAll();
				_panelConfig = new JPanel();
				_panelConfig.setLayout(new BoxLayout(_panelConfig, BoxLayout.Y_AXIS));
				_panelConfig.add(_groupPad, BorderLayout.CENTER);
				_panelConfig.add(_groupButtonsPanel, BorderLayout.SOUTH);
				_panelConfig.setPreferredSize(new Dimension(300, 300));

				_panelCameraSettingsContainer = new JPanel();
				_panelCameraSettingsContainer.setLayout(new BoxLayout(_panelCameraSettingsContainer, BoxLayout.Y_AXIS));
				_panelCameraSettingsContainer.add(_panel_cameraSettings);
				_panelCameraSettingsContainer.add(Box.createVerticalGlue());

				_panelAcquisitions = new JPanel();
				_panelAcquisitions.setLayout(new BoxLayout(_panelAcquisitions, BoxLayout.Y_AXIS));
				_panelAcquisitions.add(new JLabel("No acquisition running."));

				JPanel panelPainterSettingsContainer = new JPanel();
				panelPainterSettingsContainer.setLayout(new GridLayout());
				panelPainterSettingsContainer.add(_panelColorChooser);

				_tabbedPanel = new JTabbedPane();
				_tabbedPanel.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
				_tabbedPanel.add("Configuration", _panelConfig);
				_tabbedPanel.add("Camera Settings", _panelCameraSettingsContainer);
				_tabbedPanel.add("Running Acquisitions", _panelAcquisitions);
				_tabbedPanel.add("Painter Settings", panelPainterSettingsContainer);

				final IcyLogo logo_title = new IcyLogo("Micro-Manager for Icy");
				logo_title.setPreferredSize(new Dimension(200, 80));
				_mainPanel.add(_tabbedPanel, BorderLayout.CENTER);
				_mainPanel.add(logo_title, BorderLayout.NORTH);
				_mainPanel.validate();
			}
		});
	}

	@Override
	public boolean is16bit() {
		return true;
	}

	@Override
	public void makeActive() {
		setVisible(true);
	}

	@Override
	public boolean okToAcquire() {
		return true;
	}

	@Override
	public void refreshGUI() {
		ThreadUtil.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateGUI(false);
			}
		});
	}

	@Override
	public void setBackgroundStyle(String s) {
	}

	/**
	 * Notify all GUI parts that the configuration changed.
	 */
	@Override
	public void setConfigChanged(boolean status) {
		if (!_isConfigLoaded)
			return;
		_configChanged_ = status;
		setConfigSaveButtonStatus(_configChanged_);
		notifyConfigChanged(null);
	}

	protected void setConfigSaveButtonStatus(boolean changed) {
		saveConfigButton_.setEnabled(changed);
	}

	@Override
	public PositionList getPositionList() {
		return _posList;
	}

	@Override
	public void setPositionList(PositionList positionlist) throws MMScriptException {
		_posList = PositionList.newInstance(positionlist);
	}

	@Override
	public void showXYPositionList() {
		if (posListDlg_ == null) {
			posListDlg_ = new PositionListDlg(mCore, MMMainFrame.this, _posList, null);
			posListDlg_.setModalityType(ModalityType.APPLICATION_MODAL);
		}
		posListDlg_.setVisible(true);
	}

	@Override
	public void stopAllActivity() {
	}

	@Override
	public void updateGUI(boolean updateConfigPadStructure) {

		if (updateConfigPadStructure && _groupPad != null) {
			if (_list_progress.size() == 0)
				_groupPad.refreshStructure(false);
		}
		_panelAcquisitions.removeAll();
		if (_list_progress.size() == 0) {
			_panelAcquisitions.add(new JLabel("No acquisition running."));
		} else {
			for (int i = 0; i < _list_progress.size(); ++i) {
				RunningProgress prog = _list_progress.get(i);
				_panelAcquisitions.add(Box.createRigidArea(new Dimension(1, 10)));
				if (prog.valueDisplayed)
					prog.setString(prog.renderedName + " : " + prog.progress + "%");
				else
					prog.setString(prog.renderedName);
				prog.setStringPainted(true);
				prog.setMinimum(0);
				prog.setBounds(50, 50, 100, 30);

				if (prog.plugin != null) {
					prog.setIndeterminate(false);
					prog.setMaximum(100);
					prog.setValue(prog.progress);
				} else {
					prog.setIndeterminate(true);
					prog.setMaximum(1000);
				}
				_panelAcquisitions.add(prog);

			}
		}
		_panelAcquisitions.setSize(300, 300);
		_panelAcquisitions.validate();
		repaint();
	}

	@Override
	public boolean updateImage() {
		return false;
	}

	public void selectConfigGroup(String groupName) {
		_groupPad.setGroup(groupName);
	}

	@Override
	public void addMMBackgroundListener(Component arg0) {
	}

	@Override
	public Color getBackgroundColor() {
		return null;
	}

	@Override
	public void removeMMBackgroundListener(Component arg0) {
	}

	/**
	 * Adds the plugin to the plugin list of MMMainFrame. If it is a plugin
	 * using the acquisition, acquisition is started.
	 * 
	 * @param plugin
	 *            : plugin to be added.
	 * @see #removePlugin(MicroscopePlugin)
	 */
	public void addPlugin(MicroscopePlugin plugin) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		_list_plugin.add(plugin);
		if (_pluginListEmpty)
			_pluginListEmpty = false;
	}

	/**
	 * Removes the plugin from the plugin list of MMMainFrame. If no more plugin
	 * using the acquisition is running, acquisition is stopped.
	 * 
	 * @param plugin
	 *            : plugin to be removed.
	 * @see #addPlugin(MicroscopePlugin)
	 */
	public void removePlugin(MicroscopePlugin plugin) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		if (plugin == null)
			return;
		_list_plugin.remove(plugin);
		if (_list_plugin.isEmpty())
			_pluginListEmpty = true;
	}

	/**
	 * The plugin notifies the GUI that it does need the continuous acquisition
	 * stream. It will start the continuous acquisition if not started already.
	 * 
	 * @param o
	 *            : Object notifying that it needs continuwous acquisition.
	 */
	public void continuousAcquisitionNeeded(Object o) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		if (!mCore.isSequenceRunning()) {
			try {
				mCore.startContinuousSequenceAcquisition(0.0D);
				_list_progress.add(continuousProgress);
				refreshGUI();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		_plugin_contAcq_list.add(o);
	}

	/**
	 * The plugin notifies the GUI that it does not need the continuous
	 * acquisition stream anymore. If it is the only one using the stream, the
	 * continuous acquisition is stopped.
	 * 
	 * @param o
	 *            : Object notifying that it stops using continuous acquisition.
	 * @see #continuousAcquisitionNeeded(Object)
	 */
	public void continuousAcquisitionReleased(Object o) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		_plugin_contAcq_list.remove(o);
		if (_plugin_contAcq_list.isEmpty()) {
			try {
				mCore.stopSequenceAcquisition();
				_list_progress.remove(continuousProgress);
				refreshGUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Refreshes the group pad when a plugin modified the config.
	 */
	public void configChanged() {
		updateGUI(true);
	}

	/**
	 * Starts a progress frame for the plugin in the acquisition panel.
	 * 
	 * @param plugin
	 *            : Plugin calling
	 * @param valueDisplayed
	 *            : displays or not the actual progress text. It is highly
	 *            advised to not forget to use notifyProgress if the boolean is
	 *            true.
	 * @see #notifyAcquisitionOver(MicroscopePluginAcquisition)
	 * @see #notifyProgress(MicroscopePluginAcquisition, int)
	 */
	public void notifyAcquisitionStarting(final MicroscopePluginAcquisition plugin, final boolean valueDisplayed) {
		int i = 0;
		String name = plugin.getRenderedName();
		// we want to add an index to the name if another plugin
		// with this name is already running an acquisition.
		for (RunningProgress runp : _list_progress) {
			if (runp.renderedName.contains(name))
				++i;
		}
		if (i != 0)
			name = name + " (" + (i + 1) + ")";
		final String namefinal = name;
		ThreadUtil.invokeNow(new Runnable() {

			@Override
			public void run() {
				_list_progress.add(new RunningProgress(plugin, namefinal, valueDisplayed));
			}
		});
		refreshGUI();
	}

	/**
	 * Removes the plugin from the acquisition panel.
	 * 
	 * @param plugin
	 * @see #notifyAcquisitionStarting(MicroscopePluginAcquisition, boolean)
	 * @see #notifyProgress(MicroscopePluginAcquisition, int)
	 */
	public void notifyAcquisitionOver(MicroscopePluginAcquisition plugin) {
		if (plugin != null) {
			int idx = indexOfPlugin(plugin);
			if (idx >= 0)
				_list_progress.remove(idx);
			refreshGUI();
		}
	}

	/**
	 * Updates the progress of the acquisition.
	 * 
	 * @param progress
	 *            : percentage of progress of the plugins acquisition.
	 * @see #notifyAcquisitionStarting(MicroscopePluginAcquisition, boolean)
	 * @see #notifyAcquisitionOver(MicroscopePluginAcquisition)
	 */
	public void notifyProgress(MicroscopePluginAcquisition plugin, int progress) {
		int idx = indexOfPlugin(plugin);
		if (idx >= 0)
			_list_progress.get(indexOfPlugin(plugin)).setProgress(progress);
		refreshGUI();
	}

	/**
	 * This method will notify all plugins that the configuration is about to
	 * change, meaning that the acquisition is stopped right after this method
	 * is called. <br/>
	 * Moreover, this method will automatically remove plugins with no more
	 * reference.
	 * 
	 * @param item
	 *            : item which is going to change.
	 */
	public void notifyConfigAboutToChange(StateItem item) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		// editor.pauseThread();
		ArrayList<MicroscopePlugin> todeletelist = new ArrayList<MicroscopePlugin>();
		for (MicroscopePlugin p : _list_plugin) {
			try {
				p.notifyConfigAboutToChange(item);
			} catch (Exception e) {
				// if an error occurs, the plugin is removed from the list.
				e.printStackTrace();
				todeletelist.add(p);
			}
		}
		for (MicroscopePlugin p : todeletelist) {
			_list_plugin.remove(p);
		}
	}

	/**
	 * This method notifies plugins that configuration changed, meaning that
	 * acquisition is now back to normal. <b>Be careful, image size may have
	 * changed.</b> <br/>
	 * Moreover, this method will automatically remove plugins with no more
	 * reference.
	 * 
	 * @param item
	 *            : item changed.
	 */
	public void notifyConfigChanged(final StateItem item) {
		if (!instanced)
			return;
		if (!_isConfigLoaded)
			return;
		if (advancedDlg != null && (advancedDlg.waitingForObjectiveChange || advancedDlg.waitingForBlockChange)) {
			ThreadUtil.bgRun(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
					}
					ThreadUtil.invokeNow(new Runnable() {
						@Override
						public void run() {
							if (item instanceof StateItem) {
								StateItem sitem = (StateItem) item;
								if (advancedDlg.waitingForObjectiveChange) {
									if (ConfirmDialog.confirm("Confirmation", "<html>Are you sure you want to set: <br/><div align=\"center\"><b>" + sitem.group
											+ "</b></div>   as your Objective Turret configuration ?</html>")) {
										mCore.setCurrentObjectiveTurretGroup(sitem.group);
										String res = mCore.getCurrentObjectiveTurretGroup();
										if (res != null)
											advancedDlg.lblCurrentObjectiveTurretGroup.setText("<html><b>" + res + "</b></html>");
									}
									advancedDlg.waitingForObjectiveChange = false;
								} else if (advancedDlg.waitingForBlockChange) {
									if (ConfirmDialog.confirm("Confirmation", "<html>Are you sure you want to set: <br/><div align=\"center\"><b>" + sitem.group
											+ "</b></div>  as your Filter Block configuration ?</html>")) {
										mCore.setCurrentFilterBlockGroup(sitem.group);
										String res = mCore.getCurrentFilterBlockGroup();
										if (res != null)
											advancedDlg.lblCurrentFilterBLockGroup.setText("<html><b>" + res + "</b></html>");
									}
									advancedDlg.waitingForBlockChange = false;
								}
							}
						}
					});
				}
			});
		} else {
			ArrayList<MicroscopePlugin> todeletelist = new ArrayList<MicroscopePlugin>();
			for (MicroscopePlugin p : _list_plugin) {
				try {
					p.notifyConfigChanged(item);
				} catch (Exception e) {
					e.printStackTrace();
					todeletelist.add(p);
				}
			}
			for (MicroscopePlugin p : todeletelist) {
				_list_plugin.remove(p);
			}
			updateGUI(true);
		}
	}

	/**
	 * Returns the index of the plugin.
	 * 
	 * @param p
	 *            : plugin to find
	 * @return Returns the index if existing, -1 if not existing or null.
	 */
	private int indexOfPlugin(MicroscopePluginAcquisition plugin) {
		if (plugin != null) {
			for (int i = 0; i < _list_progress.size(); ++i) {
				if (plugin != null && _list_progress.get(i).plugin == plugin)
					return i;
			}
		}
		return -1;
	}

	/**
	 * The Running Progress class allows the creation of progress bars for
	 * plugins in order to let them show the progress of their acquisition. It
	 * is also used for the Continuous Acquisition bar.
	 * 
	 * @author Thomas Provoost
	 * 
	 */
	private class RunningProgress extends JProgressBar {
		/** generated UID */
		private static final long serialVersionUID = 1L;
		private int progress = 0;
		private String renderedName;
		private MicroscopePluginAcquisition plugin;
		private boolean valueDisplayed;

		/**
		 * 
		 * @param plugin
		 *            : Plugin which call for the RunningProgress
		 * @param pluginName
		 *            : Plugin name.
		 * @param valueDisplayed
		 *            : Display or not the progress as text.
		 */
		public RunningProgress(MicroscopePluginAcquisition plugin, String pluginName, boolean valueDisplayed) {
			this.plugin = plugin;
			this.renderedName = pluginName;
			this.valueDisplayed = valueDisplayed;
		}

		/**
		 * Used only for Continuous Acquisition.
		 */
		public RunningProgress() {
			plugin = null;
			renderedName = "Continuous Acquisition";
			valueDisplayed = false;
		}

		/**
		 * Change the progress status.
		 * 
		 * @param progress
		 *            : new progress value.
		 */
		public void setProgress(int progress) {
			this.progress = progress;
		}

		@SuppressWarnings("unused")
		public int getProgress() {
			return progress;
		}
	}

	/**
	 * @return Returns if this class is being instanced.
	 */
	public static boolean isInstancing() {
		return instancing;
	}

	/**
	 * 
	 * @return Returns if the configuration file is loaded.
	 */
	public boolean isConfigLoaded() {
		return _isConfigLoaded;
	}

	@Override
	public String getBackgroundStyle() {
		return null;
	}

	@Override
	public ContrastSettings getContrastSettings() {
		return null;
	}

	@Override
	public boolean getLiveMode() {
		return false;
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	public String getConfigFile() {
		return _sysConfigFile;
	}

	/**
	 * Used to load XML Files
	 * 
	 * @return Returns null if Dialog exited.
	 */
	private String xmlFileChooser() {
		String filename = null;
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Sate Files (.xml)", "xml"));
		int returnVal = fc.showDialog(Icy.getMainInterface().getMainFrame(), "Save File");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			filename = fc.getSelectedFile().getAbsolutePath();
			if (!filename.endsWith(".xml"))
				filename += ".xml";
		} else {
			filename = null;
		}
		return filename;

	}

	/**
	 * Load the preset settings.
	 */
	private void loadPresets() {
		String filename = xmlFileChooser();
		if (filename == null)
			return;
		XMLPreferencesRoot root = new XMLPreferencesRoot(filename);
		if (root.getPreferences().nodeExists(_sysConfigFile))
			loadXMLFile(root.getPreferences().node(_sysConfigFile));
	}

	/**
	 * Save the preset settings.
	 */
	private void savePresets() {
		String filename = xmlFileChooser();
		if (filename == null)
			return;
		XMLPreferencesRoot root = new XMLPreferencesRoot(filename);
		saveToXML(root.getPreferences().node(_sysConfigFile));
		root.save();
	}

	/**
	 * Save all the properties into an XML file.
	 * 
	 * @param root
	 *            : file and node where data is saved.
	 */
	private void saveToXML(XMLPreferences root) {
		StrVector devices = mCore.getLoadedDevices();
		for (int i = 0; i < devices.size(); i++) {
			XMLPreferences prefs = root.node(devices.get(i));
			StrVector properties;
			try {
				properties = mCore.getDevicePropertyNames(devices.get(i));
			} catch (Exception e) {
				continue;
			}
			for (int j = 0; j < properties.size(); j++) {
				PropertyItem item = new PropertyItem();
				item.readFromCore(mCore, devices.get(i), properties.get(j),false);
				prefs.put(properties.get(j), item.value);
			}
		}
	}

	/**
	 * Load all the properties into a file.
	 * 
	 * @param root
	 *            : file and node where data is saved.
	 */
	private void loadXMLFile(XMLPreferences root) {
		for (XMLPreferences device : root.getChildren()) {
			for (String propName : device.keys()) {
				String value = device.get(propName, "");
				if (value != "") {
					try {
						mCore.setProperty(device.name(), propName, value);
						mCore.waitForSystem();
					} catch (Exception e) {
						continue;
					}
				}
			}
		}
	}

	private class AdvancedConfigurationDialog extends JDialog {

		private boolean waitingForObjectiveChange = false;
		private boolean waitingForBlockChange = false;

		/** */
		private static final long serialVersionUID = 1L;
		JLabel lblCurrentObjectiveTurretGroup;
		JLabel lblCurrentFilterBLockGroup;

		public AdvancedConfigurationDialog() {
			super(Icy.getMainInterface().getMainFrame(), "Advanced Configuration Dialog", false);

			JPanel panelDevices = new JPanel(new GridLayout(2, 3));
			panelDevices.setBorder(BorderFactory.createEmptyBorder(20, 10, 4, 10));

			// OBJECTIVES TURRET
			panelDevices.add(new JLabel("Objectives Turret Config: "));
			lblCurrentObjectiveTurretGroup = new JLabel("");
			String turretGroup = mCore.getCurrentObjectiveTurretGroup();
			if (turretGroup != null)
				lblCurrentObjectiveTurretGroup.setText("<html><b>" + turretGroup + "</b><html>");
			lblCurrentObjectiveTurretGroup.setHorizontalAlignment(SwingConstants.CENTER);
			panelDevices.add(lblCurrentObjectiveTurretGroup);
			JButton btnSetTurretConfig = new JButton("Set");
			btnSetTurretConfig.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					MessageDialog.showDialog("<html>Please modify the preset of the objective turret's group.<br/>" + "<br/><i>Exemple: switch from 10x to 20x.</i></html>");
					if (waitingForBlockChange)
						waitingForBlockChange = false;
					waitingForObjectiveChange = true;
				}
			});
			panelDevices.add(btnSetTurretConfig);

			// FILTER BLOCKS
			panelDevices.add(new JLabel("Filter Blocks Config: "));
			lblCurrentFilterBLockGroup = new JLabel("");
			String filterBlock = mCore.getCurrentFilterBlockGroup();
			if (filterBlock != null)
				lblCurrentFilterBLockGroup.setText("<html><b>" + filterBlock + "</b><html>");
			lblCurrentFilterBLockGroup.setHorizontalAlignment(SwingConstants.CENTER);
			panelDevices.add(lblCurrentFilterBLockGroup);
			JButton btnSetFilterBlock = new JButton("Set");
			btnSetFilterBlock.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					MessageDialog.showDialog("<html>Please modify the preset of the filter block's group.<br/>" + "<br/><i>Exemple: switch from GFP to Texas Red.</i></html>");
					if (waitingForObjectiveChange)
						waitingForObjectiveChange = false;
					waitingForBlockChange = true;
				}
			});
			panelDevices.add(btnSetFilterBlock);

			JPanel panelInversions = new JPanel(new GridLayout(4, 1));
			panelInversions.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
			JPanel panelInvertX = new JPanel();
			panelInvertX.setLayout(new BoxLayout(panelInvertX, BoxLayout.X_AXIS));
			final JCheckBox cboxInvertX = new JCheckBox("Invert X");
			cboxInvertX.setSelected(StageMover.isInvertX());
			cboxInvertX.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StageMover.setInvertX(cboxInvertX.isSelected());
				}
			});
			panelInvertX.add(cboxInvertX);

			JPanel panelInvertY = new JPanel();
			panelInvertY.setLayout(new BoxLayout(panelInvertY, BoxLayout.X_AXIS));
			final JCheckBox cboxInvertY = new JCheckBox("Invert Y");
			cboxInvertY.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StageMover.setInvertY(cboxInvertY.isSelected());
					cboxInvertY.setSelected(StageMover.isInvertY());
				}
			});
			panelInvertY.add(cboxInvertY);

			JPanel panelInvertZ = new JPanel();
			panelInvertZ.setLayout(new BoxLayout(panelInvertZ, BoxLayout.X_AXIS));
			final JCheckBox cboxInvertZ = new JCheckBox("Invert Z");
			cboxInvertZ.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StageMover.setInvertZ(cboxInvertZ.isSelected());
					cboxInvertZ.setSelected(StageMover.isInvertZ());
				}
			});
			panelInvertZ.add(cboxInvertZ);

			JPanel panelSwitchXY = new JPanel();
			panelSwitchXY.setLayout(new BoxLayout(panelSwitchXY, BoxLayout.X_AXIS));
			final JCheckBox cboxSwitchXY = new JCheckBox("Switch X/Y");
			cboxSwitchXY.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					StageMover.setSwitchXY(cboxSwitchXY.isSelected());
					cboxSwitchXY.setSelected(StageMover.isSwitchXY());
				}
			});
			panelSwitchXY.add(cboxSwitchXY);

			panelInversions.add(panelInvertX);
			panelInversions.add(panelInvertY);
			panelInversions.add(panelInvertZ);
			panelInversions.add(panelSwitchXY);

			JPanel mainPanel = new JPanel(new BorderLayout());
			JButton btnClose = new JButton("Close");
			btnClose.setSize(30, 20);
			btnClose.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			JPanel panelClose = new JPanel();
			panelClose.setLayout(new BoxLayout(panelClose, BoxLayout.X_AXIS));
			panelClose.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			panelClose.add(Box.createHorizontalGlue());
			panelClose.add(btnClose);
			panelClose.add(Box.createHorizontalGlue());

			JPanel panelCenter = new JPanel();
			panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.Y_AXIS));
			panelCenter.add(panelDevices);
			panelCenter.add(panelInversions);

			mainPanel.add(panelCenter, BorderLayout.CENTER);
			mainPanel.add(panelClose, BorderLayout.SOUTH);
			setLayout(new BorderLayout());
			add(new IcyLogo("Advanced Configuration Dialog"), BorderLayout.NORTH);
			add(mainPanel, BorderLayout.CENTER);
			pack();
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					int vk = e.getKeyCode();
					if (vk == KeyEvent.VK_ENTER || vk == KeyEvent.VK_ESCAPE) {
						setVisible(false);
					}
				}
			});
		}
	}

	/*
	 * Starting from here, those 99 functions must be implemented according to
	 * the ScriptInterface of Micro-Manager. Only a few are used, such as
	 * getAutofocusManager().
	 */

	@Override
	public void sleep(long l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void message(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearMessageWindow() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void snapSingleImage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices, int nrPositions) throws MMScriptException {
		this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices,
				nrPositions, true, false);
	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices) throws MMScriptException {
		openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0);
	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices, int nrPositions, boolean show)
			throws MMScriptException {
		this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, nrPositions, show, false);
	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices, boolean show)
			throws MMScriptException {
		this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0, show, false);
	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices, int nrPositions, boolean show, boolean virtual)
			throws MMScriptException {
		acqMgr.openAcquisition(name, rootDir, show, virtual);
		MMAcquisition acq = acqMgr.getAcquisition(name);
		acq.setDimensions(nrFrames, nrChannels, nrSlices, nrPositions);
	}

	@Override
	public void openAcquisition(String name, String rootDir, int nrFrames,
			int nrChannels, int nrSlices, boolean show, boolean virtual)
			throws MMScriptException {
		this.openAcquisition(name, rootDir, nrFrames, nrChannels, nrSlices, 0, show, virtual);
	}

	public String createAcquisition(JSONObject summaryMetadata, boolean diskCached) {
		return acqMgr.createAcquisition(summaryMetadata, diskCached, getAcquisitionEngine());
	}

	public void openAcquisitionSnap(String name, String rootDir, boolean show)
			throws MMScriptException {
		/*
		 * MMAcquisition acq = acqMgr.openAcquisitionSnap(name, rootDir, this,
		 * show);
		 * acq.setDimensions(0, 1, 1, 1);
		 * try {
		 * // acq.getAcqData().setPixelSizeUm(core_.getPixelSizeUm());
		 * acq.setProperty(SummaryKeys.IMAGE_PIXEL_SIZE_UM,
		 * String.valueOf(core_.getPixelSizeUm()));
		 * } catch (Exception e) {
		 * ReportingUtils.showError(e);
		 * }
		 */
	}

	@Override
	public void initializeSimpleAcquisition(String name, int width, int height,
			int byteDepth, int bitDepth, int multiCamNumCh) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(name);
		acq.setImagePhysicalDimensions(width, height, byteDepth, bitDepth, multiCamNumCh);
		acq.initializeSimpleAcq();
	}

	@Override
	public void initializeAcquisition(String name, int width, int height,
			int depth) throws MMScriptException {
		initializeAcquisition(name, width, height, depth, 8 * depth);
	}

	@Override
	public void initializeAcquisition(String name, int width, int height,
			int byteDepth, int bitDepth) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(name);
		// number of multi-cam cameras is set to 1 here for backwards
		// compatibility
		// might want to change this later
		acq.setImagePhysicalDimensions(width, height, byteDepth, bitDepth, 1);
		acq.initialize();
	}

	@Override
	public int getAcquisitionImageWidth(String acqName) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(acqName);
		return acq.getWidth();
	}

	@Override
	public int getAcquisitionImageHeight(String acqName) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(acqName);
		return acq.getHeight();
	}

	@Override
	public int getAcquisitionImageBitDepth(String acqName) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(acqName);
		return acq.getBitDepth();
	}

	@Override
	public int getAcquisitionImageByteDepth(String acqName) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(acqName);
		return acq.getByteDepth();
	}

	@Override
	public int getAcquisitionMultiCamNumChannels(String acqName) throws MMScriptException {
		MMAcquisition acq = acqMgr.getAcquisition(acqName);
		return acq.getMultiCameraNumChannels();
	}

	@Override
	public Boolean acquisitionExists(String name) {
		return acqMgr.acquisitionExists(name);
	}

	@Override
	public void closeAcquisition(String name) throws MMScriptException {
		acqMgr.closeAcquisition(name);
	}

	@Override
	public String getUniqueAcquisitionName(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentAlbum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addToAlbum(TaggedImage taggedimage) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAllAcquisitions() {
		// TODO Auto-generated method stub

	}

	@Override
	public MMAcquisition getCurrentAcquisition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAcquisitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MMAcquisition getAcquisition(String name) throws MMScriptException {
		if (acqMgr == null)
			acqMgr = new AcquisitionManager();
		return acqMgr.getAcquisition(name);
	}

	@Override
	public void snapAndAddImage(String s, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void snapAndAddImage(String s, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, Object obj, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquisitionProperty(String s, String s1, String s2) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquisitionSystemState(String s, JSONObject jsonobject) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquisitionSummary(String s, JSONObject jsonobject) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setImageProperty(String s, int i, int j, int k, String s1, String s2) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition(int i, String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition(int i) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadBurstAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public String runAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String runAcqusition(String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String runAcquisition(String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChannelColor(String s, int i, Color color) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChannelName(String s, int i, String s1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChannelContrast(String s, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContrastBasedOnFrame(String s, int i, int j) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAcquisitionImage5D(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAcquisitionWindow(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public Point2D.Double getXYStagePosition() throws MMScriptException {
		try {
			double[] posXYZ = StageMover.getXYZ();
			return new Point2D.Double(posXYZ[0],posXYZ[1]);
		} catch (Exception e) {
			throw new MMScriptException(e.getMessage());
		}
	}

	@Override
	public void setStagePosition(double d) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRelativeStagePosition(double d) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setXYStagePosition(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRelativeXYStagePosition(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getXYStageName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYOrigin(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveConfigPresets() {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageWindow getImageWin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageWindow getSnapLiveWin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installPlugin(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installPlugin(String s, String s1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installAutofocusPlugin(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMCore getMMCore() {
		return mCore;
	}

	@Override
	public Autofocus getAutofocus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showAutofocusDialog() {
		// TODO Auto-generated method stub

	}

	@Override
	public AcquisitionEngine getAcquisitionEngine() {
		return engine_;
	}

	@Override
	public void logMessage(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMessage(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(Exception exception, String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(Exception exception, String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMMListener(MMListenerInterface mmlistenerinterface) {
	}

	@Override
	public void removeMMListener(MMListenerInterface mmlistenerinterface) {
	}

	@Override
	public boolean displayImage(TaggedImage taggedimage) {
		return false;
	}

	@Override
	public boolean isLiveModeOn() {
		return mCore.isSequenceRunning();
	}

	@Override
	public Rectangle getROI() throws MMScriptException {
		return null;
	}

	@Override
	public void setROI(Rectangle rectangle) throws MMScriptException {
	}

	@Override
	public ImageCache getAcquisitionImageCache(String s) {
		return null;
	}

	@Override
	public void markCurrentPosition() {

	}

	@Override
	public AcqControlDlg getAcqDlg() {
		return null;
	}

	@Override
	public PositionListDlg getXYPosListDlg() {
		return posListDlg_;
	}

	@Override
	public boolean isAcquisitionRunning() {
		return mCore != null && mCore.isSequenceRunning();
	}

	@Override
	public boolean versionLessThan(String s) throws MMScriptException {
		return false;
	}

	@Override
	public void logStartupProperties() {
	}

	@Override
	public AutofocusManager getAutofocusManager() {
		return _afMgr;
	}

	@Override
	public boolean isBurstAcquisitionRunning() throws MMScriptException {
		return false;
	}

	@Override
	public void startAcquisition() throws MMScriptException {
	}

	@Override
	public void startBurstAcquisition() throws MMScriptException {
	}

	@Override
	public String openAcquisitionData(String s, boolean flag) {
	    return s;
	}

	@Override
	public void enableRoiButtons(boolean flag) {
	}

	public class EventCallBackManager extends MMEventCallback {

		@Override
		public void onPropertyChanged(String s, String s1, String s2) {
			// handle property changed
		}

		@Override
		public void onPixelSizeChanged(double d) {
			// handle pixel size changed
		}

		@Override
		public void onConfigGroupChanged(String s, String s1) {
			// handle config group changed
			System.out.println(s + " / " + s1);
		}

		@Override
		public void onPropertiesChanged() {
			// handle properties changed
		}

		@Override
		public void onStagePositionChanged(String s, double z) {
			StageMover.onStagePositionChanged(s, z);
		}

		@Override
		public void onStagePositionChangedRelative(String s, double z) {
			StageMover.onStagePositionChangedRelative(s, z);
		}

		public void onXYStagePositionChanged(String s, double d, double d1) {
			StageMover.onXYStagePositionChanged(s, d, d1);
		};

		@Override
		public void onXYStagePositionChangedRelative(String s, double d, double d1) {
			StageMover.onXYStagePositionChangedRelative(s, d, d1);
		}
	}

	@Override
	public void setImageSavingFormat(@SuppressWarnings("rawtypes") Class imageSavingClass) throws MMScriptException {
	}

	@Override
	public boolean getAutoreloadOption() {
		return false;
	}

	@Override
	public boolean isSeriousErrorReported() {
		return false;
	}

	@Override
	public IAcquisitionEngine2010 getAcquisitionEngine2010() {
		IAcquisitionEngine2010 pipeline = null;
		try {
			Class<?> acquisitionEngine2010Class = Class.forName("org.micromanager.AcquisitionEngine2010");
			pipeline = (IAcquisitionEngine2010) acquisitionEngine2010Class.getConstructors()[0].newInstance(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return pipeline;
	}

	@Override
	public void refreshGUIFromCache() {
	}

	@Override
	public String openAcquisitionData(String location, boolean inRAM, boolean show) throws MMScriptException {
	    return null;
	}
}
