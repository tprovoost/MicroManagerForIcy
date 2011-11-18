package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.component.IcyLogo;
import icy.main.Icy;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.micromanager.conf.ConfiguratorDlg;

public class LoadFrame extends JDialog implements KeyListener, ContainerListener {

	/**
	 * Default Serial ID
	 */
	private static final long serialVersionUID = 1L;

	// Preference keys for this package
	private static final String FILE = "cfgfile";
	private static final String NB_FILES = "nbfiles";
	private static final String X_POS = "xpos";
	private static final String Y_POS = "ypos";
	private static final String WIDTH = "width";
	private static final String HEIGHT = "height";
	private static final String DISP_RIGHT = "disp_right";
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 300;

	// Class variables
	private Preferences _prefs;
	private String sysConfigFile;
	private DefaultListModel _CFGFiles;
	private File _actualfile = null;
	private int _retval;
	private int _actual_file_nb_devices;
	private int _actual_file_nb_groups;
	private int _actual_file_nb_presets;
	private boolean _isRightDisplayed = false;

	// ---------------
	// UI Variables
	// --------------
	private IcyLogo _panel_top;
	private JPanel _panel_main;
	private JPanel _panel_buttons;

	// File choser panel
	private JPanel _panel_files;
	private JList _list_files;
	private JScrollPane _scroll_files;

	// Device List Panel
	private JPanel _panel_devices;
	private JTextArea _list_devices;
	private JScrollPane _scroll_devices;

	// Config Panel
	private JPanel _panel_configs;
	private JTextArea _list_configs;
	private JScrollPane _scroll_configs;

	// Buttons
	private JButton _btn_open_selected;
	private JButton _btn_select_file;
	private JButton _btn_remove;
	private JButton _btn_more;
	private JButton _btn_wizard;

	// resume windows
	private JPanel _panel_resume;
	private JTextArea _list_resume;

	public LoadFrame() {
		setTitle("Please choose your configuration file");
		_retval = -1;
		setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowevent) {
				_retval = 1;
				savePrefs();
			}
		});

		Preferences root = Preferences.userNodeForPackage(getClass());
		_prefs = root.node(root.absolutePath() + "/" + "CFGFiles");

		JLabel lbl_files = new JLabel("Files");
		lbl_files.setHorizontalAlignment(SwingConstants.CENTER);
		lbl_files.setFont(lbl_files.getFont().deriveFont(Font.BOLD, 12));

		JLabel lbl_devices = new JLabel("Devices");
		lbl_devices.setFont(lbl_files.getFont().deriveFont(Font.BOLD, 12));
		lbl_devices.setHorizontalAlignment(SwingConstants.CENTER);

		JLabel lbl_config_presets = new JLabel("Config / Main Presets");
		lbl_config_presets.setFont(lbl_files.getFont().deriveFont(Font.BOLD, 12));
		lbl_config_presets.setHorizontalAlignment(SwingConstants.CENTER);

		_CFGFiles = new DefaultListModel();
		_list_files = new JList(_CFGFiles);
		_list_files.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!_list_files.isSelectionEmpty()) {
					sysConfigFile = (String) _list_files.getSelectedValue();
					_actualfile = new File(sysConfigFile);
					try {
						loadFileAttribs();
						_btn_open_selected.setEnabled(true);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		_list_files.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(java.awt.event.MouseEvent mouseevent) {
				if (mouseevent.getClickCount() >= 2)
					_btn_open_selected.doClick();
			}
		});
		_list_files.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		_list_files.setLayoutOrientation(JList.VERTICAL);
		_list_files.setToolTipText("All files added. Select a file and click \"Open File\" to open it.");
		_scroll_files = new JScrollPane(_list_files);
		_panel_files = new JPanel();
		_panel_files.setLayout(new BorderLayout());
		_panel_files.add(lbl_files, BorderLayout.NORTH);
		_panel_files.add(_scroll_files);

		_list_resume = new JTextArea("Microscope :\nNb Devices :\nNb Groups : \nNb Presets : ");
		_list_resume.setEditable(false);
		_list_resume.setToolTipText("Basic information on the current file.");

		_list_devices = new JTextArea();
		_list_devices.setEditable(false);
		_list_devices.setToolTipText("Devices in the current file.");
		_scroll_devices = new JScrollPane(_list_devices);
		_panel_devices = new JPanel();
		_panel_devices.setLayout(new BorderLayout());
		_panel_devices.add(lbl_devices, BorderLayout.NORTH);
		_panel_devices.add(_scroll_devices);

		_list_configs = new JTextArea();
		_list_configs.setEditable(false);
		_list_configs.setToolTipText("Configurations and Presets for the current file.");
		_scroll_configs = new JScrollPane(_list_configs);
		_panel_configs = new JPanel();
		_panel_configs.setLayout(new BorderLayout());
		_panel_configs.add(lbl_config_presets, BorderLayout.NORTH);
		_panel_configs.add(_scroll_configs);

		_panel_main = new JPanel();
		_panel_buttons = new JPanel();

		loadPrefs();

		_btn_open_selected = new JButton("Open File");
		_btn_open_selected.setToolTipText("Open currently selected file.");
		_btn_open_selected.setEnabled(false);
		_btn_open_selected.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_retval = 0;
				savePrefs();
				setVisible(false);
			}
		});
		_btn_select_file = new JButton("+");
		_btn_select_file.setToolTipText("Add a new file to the list.");
		_btn_select_file.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadConfig();
			}
		});
		_btn_remove = new JButton("-");
		_btn_remove.setToolTipText("Remove current file from the list.");
		_btn_remove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!_list_files.isSelectionEmpty())
					_CFGFiles.remove(_list_files.getSelectedIndex());
				savePrefs();
			}
		});
		_btn_wizard = new JButton("Wizard");
		_btn_wizard.setToolTipText("Run the Configuration Wizard Tool. Disabled.");
		_btn_wizard.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ConfiguratorDlg configurator = new ConfiguratorDlg(MicroscopeCore.getCore(), "");
				configurator.setVisible(true);
				String res = configurator.getFileName();
				if (res != "")
					sysConfigFile = res;
				loadFile();
			}
		});
		_btn_wizard.setEnabled(false);
		if (!_isRightDisplayed) {
			_btn_more = new JButton("More...");
			_btn_more.setToolTipText("Expand panel for advanced information");
		} else {
			_btn_more = new JButton("Less...");
			_btn_more.setToolTipText("Shorten panel for basic information");
		}
		_btn_more.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				_isRightDisplayed = !_isRightDisplayed;
				if (_isRightDisplayed) {
					_btn_more.setText("Less...");
					_btn_more.setToolTipText("Shorten panel for basic information");
				} else {
					_btn_more.setText("More...");
					_btn_more.setToolTipText("Expand panel for advanced information");
				}
				updateGUI();
			}
		});
		_panel_main.setLayout(new GridLayout(1, 3));
		_panel_main.add(_panel_files);
		_panel_main.add(_panel_devices);
		_panel_main.add(_panel_configs);
		_panel_buttons.setLayout(new BoxLayout(_panel_buttons, BoxLayout.X_AXIS));
		_panel_buttons.add(Box.createHorizontalGlue());
		_panel_buttons.add(_btn_open_selected);
		_panel_buttons.add(_btn_select_file);
		_panel_buttons.add(_btn_remove);
		_panel_buttons.add(_btn_wizard);
		_panel_buttons.add(_btn_more);

		_panel_top = new IcyLogo("Configuration : Choose your file");
		_panel_top.setPreferredSize(new Dimension(0, 80));

		JLabel _lbl_resume = new JLabel("Resume : ");
		_lbl_resume.setHorizontalAlignment(SwingConstants.CENTER);

		_panel_resume = new JPanel();
		_panel_resume.setLayout(new BoxLayout(_panel_resume, BoxLayout.Y_AXIS));
		_panel_resume.add(_lbl_resume);
		_panel_resume.add(_list_resume);

		setLayout(new BorderLayout());
		add(_panel_top, BorderLayout.NORTH);
		add(_panel_main, BorderLayout.CENTER);
		add(_panel_buttons, BorderLayout.SOUTH);
		addKeyAndContainerListenerRecursively(this);
		updateGUI();
	}

	public int showDialog() {
		setVisible(true);
		removeAll();
		return _retval;
	}

	String getPath() {
		return sysConfigFile;
	}

	private void savePrefs() {
		_prefs.putInt(NB_FILES, _CFGFiles.getSize());
		for (int i = 0; i < _CFGFiles.getSize(); ++i) {
			_prefs.put(FILE + i, _CFGFiles.getElementAt(i).toString());
		}
		_prefs.putInt(X_POS, getX());
		_prefs.putInt(Y_POS, getY());
		_prefs.putInt(WIDTH, getWidth());
		_prefs.putInt(HEIGHT, getHeight());
		_prefs.putBoolean(DISP_RIGHT, _isRightDisplayed);
	}

	private void loadPrefs() {
		int _nbFiles = 0;
		if (!_CFGFiles.isEmpty())
			_CFGFiles.removeAllElements();

		_nbFiles = _prefs.getInt(NB_FILES, _nbFiles);
		for (int i = 0; i < _nbFiles; ++i) {
			String tmp = "none";
			tmp = _prefs.get(FILE + i, "none");
			if (tmp != "none") {
				_CFGFiles.addElement(tmp);
			}
		}
		int pos_x = Toolkit.getDefaultToolkit().getScreenSize().width / 2;
		int pos_y = Toolkit.getDefaultToolkit().getScreenSize().height / 2;
		int width = _prefs.getInt(WIDTH, DEFAULT_WIDTH);
		int heigth = _prefs.getInt(HEIGHT, DEFAULT_HEIGHT);
		pos_x = _prefs.getInt(X_POS, pos_x);
		pos_y = _prefs.getInt(Y_POS, pos_y);
		setLocation(pos_x, pos_y);
		setSize(width, heigth);

		boolean displayright = false;
		_isRightDisplayed = _prefs.getBoolean(DISP_RIGHT, displayright);
	}

	private void loadConfig() {
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileNameExtensionFilter("Configuration Files (.cfg)", "cfg"));
		int returnVal = fc.showDialog(Icy.getMainInterface().getFrame(), "Launch Configuration");
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			sysConfigFile = fc.getSelectedFile().getAbsolutePath();
			loadFile();
		}
	}

	private void loadFile() {
		if (!_CFGFiles.contains(sysConfigFile))
			_CFGFiles.addElement(sysConfigFile);
		else
			_list_files.setSelectedValue(sysConfigFile, true);
		savePrefs();
		repaint();
	}

	private void loadFileAttribs() throws IOException {
		_actual_file_nb_devices = 0;
		_actual_file_nb_groups = 0;
		_actual_file_nb_presets = 0;

		String slist_resume = "Microscope : ";
		String slist_devices = "";
		String slist_configs = "";

		BufferedReader in = new BufferedReader(new FileReader(_actualfile));
		if (_actualfile != null) {
			String actual_line = "";
			while ((actual_line = in.readLine()) != null) {
				if (actual_line.isEmpty())
					continue;
				if (actual_line.charAt(0) == '#') {
					if (actual_line.contains("Group:")) {
						slist_configs = slist_configs + actual_line.substring(9) + "\n";
						++_actual_file_nb_groups;

					} else if (actual_line.contains("Preset:")) {
						slist_configs = slist_configs + "   " + actual_line.substring(10) + "\n";
						++_actual_file_nb_presets;
					}
				} else {
					if (actual_line.startsWith("Device")) {
						actual_line = actual_line.substring(7);
						int coma_index;
						while ((coma_index = actual_line.indexOf(',')) != -1) {
							actual_line = actual_line.substring(coma_index + 1);
						}
						if (isAMicoscope(actual_line)) {
							slist_resume += actual_line;
						}
						slist_devices = slist_devices + actual_line + "\n";
						++_actual_file_nb_devices;
					}
				}
			}
		}
		in.close();
		_list_configs.getCaret().moveDot(0);
		slist_resume = slist_resume + "\nNb Devices : " + _actual_file_nb_devices;
		slist_resume = slist_resume + "\nNb Groups : " + _actual_file_nb_groups;
		slist_resume = slist_resume + "\nNb Presets : " + _actual_file_nb_presets;
		_list_devices.setText(slist_devices);
		_list_configs.setText(slist_configs);
		_list_resume.setText(slist_resume);
		_list_devices.setCaretPosition(0);
		_list_configs.setCaretPosition(0);
	}

	public boolean isAMicoscope(String s) {
		if (s.contains("TIScope"))
			return true;
		if (s.contains("Scope"))
			return true;
		if (s.contains("DCam"))
			return true;
		return false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			_btn_open_selected.doClick();
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			_retval = 1;
			dispose();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void componentAdded(ContainerEvent e) {
		addKeyAndContainerListenerRecursively(e.getChild());
	}

	@Override
	public void componentRemoved(ContainerEvent e) {
		removeKeyAndContainerListenerRecursively(e.getChild());
	}

	/**
	 * Adds recursively the listeners onto the components.
	 * 
	 * @param c
	 *            : Container
	 */
	private void addKeyAndContainerListenerRecursively(Component c) {
		// Add KeyListener to the Component passed as an argument
		c.addKeyListener(this);
		// Check if the Component is a Container
		if (c instanceof Container) {
			// Component c is a Container. The following cast is safe.
			Container cont = (Container) c;
			// Add ContainerListener to the Container.
			cont.addContainerListener(this);
			// Get the Container's array of children Components.
			Component[] children = cont.getComponents();
			// For every child repeat the above operation.
			for (int i = 0; i < children.length; i++) {
				addKeyAndContainerListenerRecursively(children[i]);
			}
		}
	}

	/**
	 * Removes recursively the listeners onto the components.
	 * 
	 * @param c
	 *            : Container
	 */
	private void removeKeyAndContainerListenerRecursively(Component c) {
		c.removeKeyListener(this);
		if (c instanceof Container) {
			Container cont = (Container) c;
			cont.removeContainerListener(this);
			Component[] children = cont.getComponents();
			for (int i = 0; i < children.length; i++) {
				removeKeyAndContainerListenerRecursively(children[i]);
			}
		}
	}

	public void updateGUI() {
		_panel_main.removeAll();
		if (_isRightDisplayed) {
			_panel_main.setLayout(new GridLayout(1, 4));
			_panel_main.add(_panel_files);
			_panel_main.add(_panel_resume);
			_panel_main.add(_panel_devices);
			_panel_main.add(_panel_configs);
			Rectangle bounds = getBounds();
			setBounds(bounds.x, bounds.y, bounds.width + 200, bounds.height);
		} else {
			_panel_main.setLayout(new GridLayout(1, 2));
			_panel_main.add(_panel_files);
			_panel_main.add(_panel_resume);

			Rectangle bounds = getBounds();
			if (bounds.width > 500)
				setBounds(bounds.x, bounds.y, bounds.width - 200, bounds.height);
		}
		validate();
		repaint();
	}
}
