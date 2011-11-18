/**
 * 
 */
package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.Plugin;
import icy.plugin.interface_.PluginImageAnalysis;

/**
 * @author Thomas Provoost
 * 
 */
public class MicromanagerPlugin extends Plugin implements PluginImageAnalysis {

	/** Reference to the actual core */

	@Override
	public void compute() {
		MMMainFrame frame = MMMainFrame.getInstance();
		if (frame != null)
			frame.setVisible(true);
		else {
			MessageDialog.showDialog("Error while initializing the microscope.\n"
					+ "Please check if all devices are correctly turned on and recognized by the computer and\n"
					+ "quit Micro Manager or any program using the microscope and/or the camera.");
			MMMainFrame.dispose();
		}
	}

}
