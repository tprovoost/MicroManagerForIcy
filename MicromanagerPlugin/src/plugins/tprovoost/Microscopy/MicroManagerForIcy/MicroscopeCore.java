package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.main.MainFrame;
import icy.main.Icy;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.MathTools;

public class MicroscopeCore extends CMMCore {

	/** Singleton pattern: reference to the current object */
	private static MicroscopeCore core;
	/**
	 * This boolean is used to know if the core is already loaded. When true,
	 * the configuration was loaded without any issue, and the core is ready to
	 * be used.
	 */
	private static boolean isCoreReady = false;
	/** Reference to the current group representing the filter blocks */
	private String currentFilterBlockGroup = null;
	/** Reference to the current group representing the objective turret */
	private String currentObjectiveTurretGroup = null;
	/**
	 * The key is represented by the label of the objective, the value is
	 * represented by the magnification of the objective.
	 */
	private HashMap<String, Double> hashmapMagnification = new HashMap<String, Double>();

	/**
	 * Singleton Pattern
	 * 
	 * @return Returns the only instance of CoreSingleton. Creates it if
	 *         necessary.
	 */
	public static MicroscopeCore getCore() {
		if (core == null) {
			try {
				core = new MicroscopeCore();
			} catch (UnsatisfiedLinkError e1) {
			} catch (NoClassDefFoundError e2) {
			}
		}
		return core;
	}

	/**
	 * Singleton Pattern: This method will set the Micro-Manager Core to a null
	 * value.<br/>
	 * <br/>
	 * <b>Using this method is highly discouraged.</b>
	 */
	public static void releaseCore() {
		core = null;
	}

	@Override
	public synchronized boolean isSequenceRunning() {
		return super.isSequenceRunning();
	}

	@Override
	public void reset() throws Exception {
		isCoreReady = false;
		super.reset();
	}

	@Override
	public void loadSystemConfiguration(String filename) throws Exception {
		isCoreReady = false;
		super.loadSystemConfiguration(filename);
		isCoreReady = true;
	}

	/**
	 * Is the system currently being loaded ?
	 * 
	 * @return : Returns true if loadind, return false otherwise
	 */
	public static boolean isReady() {
		return isCoreReady;
	}

	/**
	 * Wait for exposure. If exposure = 400 ms, this method will wait for 400
	 * ms.
	 * 
	 * @throws Exception
	 *             : Throws an exception if the exposure cannot be recovered
	 */
	public void waitForExposure() throws Exception {
		MathTools.waitFor((long) core.getExposure());
	}

	/**
	 * Returns the current filter block group.
	 * 
	 * @return A string or null if the current filter block group was not set.
	 */
	public String getCurrentFilterBlockGroup() {
		return currentFilterBlockGroup;
	}

	/**
	 * Set the filter block group. This value is used to get all the labels of
	 * the filter groups.
	 * 
	 * @param currentFilterBlockGroup
	 * @see #getCurrentFilterBlockGroup(), {@link #getFilterBlocksLabels()}
	 */
	public void setCurrentFilterBlockGroup(String currentFilterBlockGroup) {
		this.currentFilterBlockGroup = currentFilterBlockGroup;
	}

	/**
	 * This method will return every filter block existing with the current
	 * filter block device.
	 * 
	 * @return An array list containing the labels.
	 * @see #getFilterBlocksLabels(String)
	 */
	public ArrayList<String> getFilterBlocksLabels() {
		return getFilterBlocksLabels(currentFilterBlockGroup);
	}

	/**
	 * This method will return every filter block existing with the group
	 * <code>filterBlockGroup</code>.
	 * 
	 * @param filterBlockGroup
	 *            : FilterBlock from which you want the labels
	 * @return An ArrayList containing every label for the filter blocks.
	 */
	public ArrayList<String> getFilterBlocksLabels(String filterBlockGroup) {
		ArrayList<String> toReturn = new ArrayList<String>();
		for (String s : core.getAvailableConfigs(filterBlockGroup).toArray())
			toReturn.add(s);
		return toReturn;
	}

	/**
	 * Return the current Filter Block Label
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getCurrentFilterBLockLabel() throws Exception {
		return core.getCurrentConfig(currentFilterBlockGroup);
	}

	/**
	 * Return the current Filter Block Label for the given filter block group
	 * 
	 * @param group
	 * @return
	 * @throws Exception
	 */
	public String getFilterBlockLabel(String group) throws Exception {
		return core.getCurrentConfig(group);
	}

	/**
	 * Set the current filter block on the <code>filterBlockLabel</code>.
	 * 
	 * @param filterBlockLabel
	 *            : name of the filter block wanted
	 * @throws Exception
	 */
	public void setFilterBlock(String filterBlockLabel) throws Exception {
		setFilterBlock(currentFilterBlockGroup, filterBlockLabel);
	}

	/**
	 * Set the <code>filterBlockDevice</code> on the
	 * <code>filterBlockLabel</code>.
	 * 
	 * @param filterBlockGroup
	 *            : device on which the filter block changes
	 * @param filterBlockLabel
	 *            : name of the filter block wanted
	 * @throws Exception
	 */
	public void setFilterBlock(String filterBlockGroup, String filterBlockLabel) throws Exception {
		core.setConfig(filterBlockGroup, filterBlockLabel);
	}

	/**
	 * Returns an {@link HashMap} containing all the different magnifications.
	 * The string represents the label and the integer the value.
	 * 
	 * @return
	 */
	public HashMap<String, Double> getAvailableMagnifications() {
		return hashmapMagnification;
	}

	/**
	 * Returns an array of double containing all the different magnifications.
	 * 
	 * @return
	 */
	public ArrayList<Double> getAvailableMagnificationValues() {
		return new ArrayList<Double>(hashmapMagnification.values());
	}

	/**
	 * Returns an array list of Strings containing all the different labels of
	 * magnification.
	 * 
	 * @return
	 */
	public ArrayList<String> getAvailableObjectives() {
		return new ArrayList<String>(hashmapMagnification.keySet());
	}
	
	/**
	 * Return the current Objective
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getCurrentObjective() throws Exception {
		return core.getCurrentConfig(currentObjectiveTurretGroup);
	}

	/**
	 * Set the objective turret to the objective named <code>label</code>.
	 * 
	 * @param label
	 *            : name of the objective.
	 * @throws Exception
	 *             an exception is raised if the label is unknown. this can
	 *             happen when the objective turret config has not yet been
	 *             configured.
	 * @see #getAvailableObjectives()
	 */
	public void setObjective(String label) throws Exception {
		core.setConfig(getCurrentObjectiveTurretGroup(), label);
	}

	/**
	 * Set the objective turret to the objective named <code>label</code>.<br/>
	 * <b>Warning:</b> will choose the first label automatically if the value
	 * corresponds to multiple labels. In this case, please use
	 * {@link #setObjective(String)} or {@link #setMagnifications(HashMap)}
	 * instead.
	 * 
	 * @param value
	 *            : value of the objective.
	 * @throws Exception
	 *             an exception is raised if the value is unknown. This can
	 *             happen when the objective turret config has not yet been
	 *             configured.
	 */
	public void setMagnification(double value) throws Exception {
		core.setConfig(getCurrentObjectiveTurretGroup(), getLabelsCorresponding(value).get(0));
	}

	/**
	 * Set the {@link HashMap} of the magnifications directly into the core.
	 * 
	 * @param hashmapMagnification
	 */
	public void setMagnifications(HashMap<String, Double> hashmapMagnification) {
		this.hashmapMagnification = hashmapMagnification;
	}

	/**
	 * Get the current zoom magnification.
	 * 
	 * @return
	 * @throws Exception
	 *             : An exception is raised if the current configuration is
	 *             unknown. This can happen when the objective turret config has
	 *             not yet been configured.
	 */
	public double getMagnification() throws Exception {
		String config = core.getCurrentConfig(getCurrentObjectiveTurretGroup());
		if (hashmapMagnification.containsKey(config))
			return hashmapMagnification.get(config);
		throw new Exception("The config was not found in the hashmap used to manipulate the" + "objectives. Please re-set the hashmap.");
	}

	/**
	 * Get the labels corresponding to a specific value.
	 * 
	 * @param value
	 * @return an Empty ArrayList if value not found. Return the labels
	 *         otherwise.
	 */
	public ArrayList<String> getLabelsCorresponding(double value) {
		if (!hashmapMagnification.containsValue(value))
			return new ArrayList<String>();
		ArrayList<String> list = new ArrayList<String>(1);
		for (String s : hashmapMagnification.keySet()) {
			if (hashmapMagnification.get(s) != null && hashmapMagnification.get(s) == value)
				list.add(s);
		}
		return list;
	}

	/**
	 * Returns the current objective turret configuration used.
	 * 
	 * @return Value of the current objective turret configuration. Be careful:
	 *         the string can be null.
	 */
	public String getCurrentObjectiveTurretGroup() {
		return currentObjectiveTurretGroup;
	}

	/**
	 * Set the current objective turret group and open a Dialog to configure the
	 * different magnifications.
	 */
	public void setCurrentObjectiveTurretGroup(String currentObjectiveTurretGroup) {
		this.currentObjectiveTurretGroup = currentObjectiveTurretGroup;
		MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
		StrVector vector = core.getAvailableConfigs(currentObjectiveTurretGroup);
		ArrayList<String> objectives = new ArrayList<String>();
		for (String s : vector) {
			objectives.add(s);
		}
		ObjectivesDialog dialog = new ObjectivesDialog(mainFrame, objectives);
		dialog.pack();
		dialog.setLocationRelativeTo(mainFrame);
		dialog.setVisible(true);
		hashmapMagnification = dialog.getResult();
	}

	/**
	 * Set both current objective turret and its magnifications at the same
	 * time. Be careful, only one magnification hashmap is used at a time.
	 * 
	 * @param currentObjectiveTurretGroup
	 * @param hashmapMagnification
	 */
	public void setCurrentObjectiveTurretGroup(String currentObjectiveTurretGroup, HashMap<String, Double> hashmapMagnification) {
		this.currentObjectiveTurretGroup = currentObjectiveTurretGroup;
		this.hashmapMagnification = hashmapMagnification;
	}

	private class ObjectivesDialog extends JDialog implements KeyListener {

		/** */
		private static final long serialVersionUID = 1L;
		HashMap<String, Double> result = new HashMap<String, Double>();

		public ObjectivesDialog(MainFrame frame, final ArrayList<String> objectives) {
			super(frame, "Objectives Configuration", true);

			JPanel panelObjectives = new JPanel();
			panelObjectives.setLayout(new GridLayout(objectives.size(), 2));
			for (final String s : objectives) {
				panelObjectives.add(new JLabel(s));
				final JTextField tf = new JTextField(" ");
				tf.addKeyListener(this);
				tf.addCaretListener(new CaretListener() {

					@Override
					public void caretUpdate(CaretEvent e) {
						try {
							double value = Double.parseDouble(tf.getText());
							result.put(s, value);
						} catch (NumberFormatException e1) {
							result.remove(s);
						}
					}
				});
				panelObjectives.add(tf);
			}
			panelObjectives.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

			JButton btnOk = new JButton("OK");
			btnOk.setSize(30, 20);
			btnOk.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			JPanel panelButton = new JPanel();
			panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
			panelButton.add(Box.createHorizontalGlue());
			panelButton.add(btnOk);
			panelButton.add(Box.createHorizontalGlue());

			JPanel mainPanel = new JPanel(new BorderLayout());
			String text = "<html>You currently have " + objectives.size() + " objectives at your disposal. Please enter the real <br/>"
					+ "magnification for each objective label. Leave a blank if you don't want to<br/>" + "use an objective.</html>";
			mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
			mainPanel.add(new JLabel(text), BorderLayout.NORTH);
			mainPanel.add(panelObjectives, BorderLayout.CENTER);
			mainPanel.add(panelButton, BorderLayout.SOUTH);
			setLayout(new BorderLayout());
			add(mainPanel, BorderLayout.CENTER);
			addKeyListener(this);
		}

		public HashMap<String, Double> getResult() {
			return result;
		}

		@Override
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();
			if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_ENTER)
				setVisible(false);
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}

		@Override
		public void keyTyped(KeyEvent e) {
		}
	}
}
