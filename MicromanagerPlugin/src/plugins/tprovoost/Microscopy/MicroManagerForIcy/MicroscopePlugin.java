package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.plugin.abstract_.PluginActionable;
import icy.system.thread.ThreadUtil;

import org.micromanager.utils.StateItem;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame.EventCallBackManager;

/**
 * This is the class to inherit in order to create a Microscope Plugin. Instead
 * of implementing compute(), you will have to implement the start() method.
 * That way, you will have access to the main interface and the core, and your
 * plugin will automatically wait for the Micro-Manager For Icy to be running.<br/>
 * <p>
 * <b>Example: </b>
 * 
 * <pre>
 * {@code public class MyMicroscopePlugin extends MicroscopePlugin {
 * 
 *     public void start() {
 *         // Acquisition of the first image to set up the settings
 *         MicroscopeImage imgFirst = ImageGetter.snapImage(MicroscopeCore.getCore());
 * 
 *         // Tests if null. In this case, displays a message error and quits the plugin.
 *         if (imgFirst != null) {
 *             Icy.addSequence(new Sequence(imgFirst));
 *         } 
 *     }
 *     
 *     public void notifyConfigAboutToChange(StateItem item) {
 *        // Do nothing
 *     }
 * 
 *     public void notifyConfigChanged(StateItem item) throws Exception {
 *         // Do nothing
 *     }
 * 
 *     public void MainGUIClosed() {
 *         // Do nothing
 *     }
 * }
 * </pre>
 * 
 * </p>
 * 
 * @author Thomas Provoost
 * @see #start()
 */
public abstract class MicroscopePlugin extends PluginActionable
{
    /**
     * This is a reference to the MMMainFrame gui.
     */
    protected MMMainFrame mainGui = null;
    /**
     * Reference to the core. It will be initialized only when the GUI is
     * initialized.
     */
    protected MicroscopeCore mCore = null;

    /**
     * Constructor. Only get the instance of the main interface.
     */
    protected MicroscopePlugin()
    {
        mainGui = MMMainFrame.getInstance();
    }

    @Override
    public void run()
    {
        if (!MMMainFrame.isInstancing() && !MMMainFrame.instanced())
            return;

        // This uses a thread in order to wait for MicroManagerForIcy to be
        // ready.
        ThreadUtil.bgRun(new Runnable()
        {
            @Override
            public void run()
            {
                // waiting until instanced
                while (!MMMainFrame.instanced())
                {
                    if (!MMMainFrame.isInstancing())
                        return;
                    ThreadUtil.sleep(10);
                }
                ThreadUtil.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // if the GUI was loading, it is null.
                        // we have to get it again.
                        if (mainGui == null)
                            mainGui = MMMainFrame.getInstance();
                        mCore = MicroscopeCore.getCore();
                        start();
                    }
                });
            }
        });
    }

    /**
     * This method should be used if a plugin is about to change anything on the
     * current configuration. That way, all other plugins running will be
     * notified.
     */
    protected void notifyMainGuiConfigAboutToChange()
    {
        mainGui.notifyConfigAboutToChange(null);
    }

    /**
     * This method should be used when the plugin changed something on the
     * current configuration. That way, all other plugins running will be
     * notified.
     */
    protected void notifyMainGuiConfigChanged()
    {
        mainGui.notifyConfigChanged(null);
        mainGui.configChanged();
    }

    /**
     * This method is the Microscope Plugin "compute()" equivalent. It is called
     * only <b>after</b> Micro-Manager For Icy is launched.
     */
    public abstract void start();

    /**
     * Called before the configuration is changed via GUI.
     * 
     * @param item
     * @throws Exception
     */
    public void notifyConfigAboutToChange(StateItem item) throws Exception
    {
    }

    /**
     * Called after configuration is changed.
     * 
     * @param item
     * @throws Exception
     * @see notifyConfigAboutToChange()
     */
    public void notifyConfigChanged(StateItem item) throws Exception
    {
    }

    /**
     * Called when main gui is closed
     */
    public void MainGUIClosed()
    {
    }

    /**
     * Dispatched from {@link EventCallBackManager#onPropertyChanged(String, String, String)}.
     * 
     * @param s
     * @param s1
     * @param s2
     */
    public void onPropertyChanged(String s, String s1, String s2)
    {
    }

    /**
     * Dispatched from {@link EventCallBackManager#onPixelSizeChanged(double)}.
     * 
     * @param d
     */
    public void onPixelSizeChanged(double d)
    {
    }

    /**
     * Dispatched from {@link EventCallBackManager#onConfigGroupChanged(String, String)}.
     * 
     * @param s
     * @param s1
     */
    public void onConfigGroupChanged(String s, String s1)
    {
    }

    /**
     * Dispatched from {@link EventCallBackManager#onPropertiesChanged()}.
     */
    public void onPropertiesChanged()
    {
    }
}
