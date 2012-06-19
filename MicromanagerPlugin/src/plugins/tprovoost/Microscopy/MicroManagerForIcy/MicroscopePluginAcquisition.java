package plugins.tprovoost.Microscopy.MicroManagerForIcy;

/**
 * This class is used for plugins that will show their progress in the GUI as
 * progress bars.
 */
public abstract class MicroscopePluginAcquisition extends MicroscopePlugin {

	public MicroscopePluginAcquisition() {
		super();
	}

	public abstract String getRenderedName();

	/**
	 * Notify GUI that the acquisition of this plugin started.
	 */
	public void notifyAcquisitionStarted(boolean displayChange) {
		if (!MMMainFrame.isInstanced())
			return;
		if (MMMainFrame.isInstancing())
			return;
		if (!mainGui.isConfigLoaded())
			return;
		mainGui.notifyAcquisitionStarting(this, displayChange);
	}

	/**
	 * Notify GUI that the acquisition of this plugin stopped.
	 */
	public void notifyAcquisitionOver() {
		if (!MMMainFrame.isInstanced())
			return;
		if (MMMainFrame.isInstancing())
			return;
		if (!mainGui.isConfigLoaded())
			return;
		mainGui.notifyAcquisitionOver(this);
	}

	/**
	 * Notify GUI that the progress of the acquisition changed.
	 * 
	 * @param progress
	 *            : Percentage of the progress <b>done</b>.
	 */
	public void notifyProgress(int progress) {
		if (!MMMainFrame.isInstanced())
			return;
		if (MMMainFrame.isInstancing())
			return;
		if (!mainGui.isConfigLoaded())
			return;
		mainGui.notifyProgress(this, progress);
	}
}
