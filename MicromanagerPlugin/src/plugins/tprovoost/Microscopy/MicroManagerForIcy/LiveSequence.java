package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.system.thread.ThreadUtil;

import java.util.ArrayList;


public class LiveSequence extends MicroscopeSequence {

	/** Current listeners */
	protected ArrayList<LiveListener> _listeners;
	/** Used to prevent double updating */
	protected boolean updating = false;
	
	public LiveSequence(String s, MicroscopeImage buffer) {
		super(s, buffer);
		_listeners = new ArrayList<LiveListener>();
	}
	
	public LiveSequence(MicroscopeImage buffer) {
		super(buffer);
		_listeners = new ArrayList<LiveListener>();
	}
	
	public LiveSequence() {
		_listeners = new ArrayList<LiveListener>();
	}
	
	synchronized protected void setUpdating(boolean b) {
		updating = b;
	}
	
	/** Add listener to the liveframe */
	public void addLiveListener(LiveListener listener) {
		_listeners.add(listener);
	}	
	
	/** Removes listener from the liveframe */
	public void removeLiveListener(LiveListener listener) {
		_listeners.remove(listener);
	}
	
	synchronized public void notifyListeners() throws Exception {
		if (_listeners.isEmpty())
			return;
		ArrayList<LiveListener> cloned = new ArrayList<LiveListener>(_listeners);
		for (int i = 0 ; i < cloned.size(); ++i) {
			final LiveListener l = cloned.get(i);
			ThreadUtil.bgRun(new Runnable() {
				@Override
				public void run() {
					l.updated(LiveSequence.this);
				}
			});
		}
	}
}
