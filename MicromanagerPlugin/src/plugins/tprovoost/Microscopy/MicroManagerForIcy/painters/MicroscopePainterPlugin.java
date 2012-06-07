package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.plugin.abstract_.PluginActionable;
import icy.plugin.interface_.PluginDaemon;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.MicroscopePainter;

public abstract class MicroscopePainterPlugin extends PluginActionable implements PluginDaemon {
	
	public abstract MicroscopePainter getPainter();
	
}
