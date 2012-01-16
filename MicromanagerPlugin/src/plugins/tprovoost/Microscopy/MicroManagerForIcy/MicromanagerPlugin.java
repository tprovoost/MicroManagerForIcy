/**
 * 
 */
package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.PluginActionable;

/**
 * @author Thomas Provoost
 * 
 */
public class MicromanagerPlugin extends PluginActionable {

	/** Reference to the actual core */

	@Override
	public void run() {
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
