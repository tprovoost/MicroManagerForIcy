package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.util.Iterator;
import java.util.Vector;

import mmcorej.CMMCore;
import mmcorej.DeviceType;
import mmcorej.StrVector;

import org.micromanager.api.Autofocus;
import org.micromanager.utils.MMException;
import org.micromanager.utils.ReportingUtils;

public class AutofocusManager {

	public AutofocusManager(CMMCore core) {
		afs_ = new Vector<CoreAutofocus>();
		afPluginClassNames_ = new Vector<String>();
		currentAfDevice_ = null;
		_core = core;
	}

	public void selectDevice(String name) throws MMException {
		for (Iterator<CoreAutofocus> i$ = afs_.iterator(); i$.hasNext();) {
			Autofocus af = (Autofocus) i$.next();
			if (af.getDeviceName().equals(name)) {
				currentAfDevice_ = af;
				return;
			}
		}

		throw new MMException((new StringBuilder()).append(name).append(" not loaded.").toString());
	}

	public void setAFPluginClassName(String className) {
		if (!afPluginClassNames_.contains(className))
			afPluginClassNames_.add(className);
	}

	public Autofocus getDevice() {
		return currentAfDevice_;
	}

	public void refresh() throws MMException {
		afs_.clear();
		StrVector afDevs = _core.getLoadedDevicesOfType(DeviceType.AutoFocusDevice);
		for (int i = 0; (long) i < afDevs.size(); i++) {
			CoreAutofocus caf = new CoreAutofocus();
			try {
				_core.setAutoFocusDevice(afDevs.get(i));
				caf.setCore(_core);
				if (caf.getDeviceName().length() == 0)
					continue;
				afs_.add(caf);
				if (currentAfDevice_ == null)
					currentAfDevice_ = caf;
			} catch (Exception e) {
				ReportingUtils.showError(e);
			}
		}
		boolean found = false;
		Iterator<CoreAutofocus> i$ = afs_.iterator();
		do {
			if (!i$.hasNext())
				break;
			Autofocus af = (Autofocus) i$.next();
			if (af.getDeviceName().equals(currentAfDevice_.getDeviceName())) {
				found = true;
				currentAfDevice_ = af;
			}
		} while (true);
		if (!found && afs_.size() > 0)
			currentAfDevice_ = (Autofocus) afs_.get(0);
		if (afDlg_ != null)
			afDlg_.rebuild();
	}

	public void showOptionsDialog() {
		if (afDlg_ == null)
			afDlg_ = new AutofocusPropertyEditor(this);
		afDlg_.setVisible(true);
		if (currentAfDevice_ != null) {
			currentAfDevice_.applySettings();
			currentAfDevice_.saveSettings();
		}
	}

	public void closeOptionsDialog() {
		if (afDlg_ != null)
			afDlg_.cleanup();
	}

	public String[] getAfDevices() {
		String afDevs[] = new String[afs_.size()];
		int count = 0;
		for (Iterator<CoreAutofocus> i$ = afs_.iterator(); i$.hasNext();) {
			Autofocus af = (Autofocus) i$.next();
			afDevs[count++] = af.getDeviceName();
		}

		return afDevs;
	}

	public boolean hasDevice(String dev) {
		for (Iterator<CoreAutofocus> i$ = afs_.iterator(); i$.hasNext();) {
			Autofocus af = (Autofocus) i$.next();
			if (af.getDeviceName().equals(dev))
				return true;
		}

		return false;
	}

	private CMMCore _core;
	private Vector<CoreAutofocus> afs_;
	private Vector<String> afPluginClassNames_;
	private Autofocus currentAfDevice_;
	private AutofocusPropertyEditor afDlg_;
}