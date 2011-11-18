package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.main.MainFrame;
import icy.main.Icy;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import mmcorej.CMMCore;
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
	 * Set the objective turret to the objective named <code>label</code>.
	 * 
	 * @param label
	 *            : name of the objective.
	 * @throws Exception
	 *             an exception is raised if the config or the label is unknown.
	 *             The first issue can happen when the objective turret config
	 *             has not yet been configured.
	 */
	public void setMagnification(String label) throws Exception {
		if (!hashmapMagnification.containsKey(label))
			return;
		core.setConfig(getCurrentObjectiveTurretGroup(), label);
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
	public double getCurrentMagnification() throws Exception {
		String config = core.getCurrentConfig(getCurrentObjectiveTurretGroup());
		if (hashmapMagnification.containsKey(config))
			return hashmapMagnification.get(config);
		throw new Exception("The config was not found in the hashmap used to manipulate the" + "objectives. Please re-set the hashmap.");
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
		StrVector vector = core.getAvailableConfigs(currentObjectiveTurretGroup);
		ArrayList<String> objectives = new ArrayList<String>();
		for (String s : vector) {
			objectives.add(s);
		}
		ObjectivesDialog dialog = new ObjectivesDialog(Icy.getMainInterface().getFrame(), objectives);
		dialog.pack();
		dialog.setVisible(true);
		hashmapMagnification = dialog.getResult();
	}

	private class ObjectivesDialog extends JDialog {

		/** */
		private static final long serialVersionUID = 1L;
		HashMap<String, Double> result = new HashMap<String, Double>();

		public ObjectivesDialog(MainFrame frame, final ArrayList<String> objectives) {
			super(frame, true);

			JPanel panelObjectives = new JPanel();
			panelObjectives.setLayout(new GridLayout(objectives.size(), 2));
			for (final String s : objectives) {
				System.out.println("objective found : " + objectives.size());
				panelObjectives.add(new JLabel(s));
				final JTextField tf = new JTextField(" ");
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

			JButton btnOk = new JButton("OK");
			btnOk.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});

			JPanel mainPanel = new JPanel(new BorderLayout());
			String text = "<html>You currently have " + objectives.size() + " objectives at your disposal. Please enter the real <br/>"
					+ "magnification for each objective label. Leave a blank if you don't want to<br/>" + "use an objective.</html>";
			mainPanel.add(new JLabel(text), BorderLayout.NORTH);
			mainPanel.add(panelObjectives, BorderLayout.CENTER);
			mainPanel.add(btnOk, BorderLayout.SOUTH);
			setLayout(new BorderLayout());
			add(mainPanel);
			addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
						setVisible(false);
				}
			});
		}

		public HashMap<String, Double> getResult() {
			return result;
		}
	}
}
