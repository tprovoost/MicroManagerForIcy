package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.sequence.Sequence;

public interface LiveListener {
	/**
	 * Notified when the LiveModeFrame captured a new image.<br/> 
	 * <b>Should be synchronized.</b>
	 */
	void updated(Sequence s);
}
