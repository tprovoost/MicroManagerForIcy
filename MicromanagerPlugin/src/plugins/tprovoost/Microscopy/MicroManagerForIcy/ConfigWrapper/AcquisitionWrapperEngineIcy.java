package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.api.AcquisitionDisplay;
import org.micromanager.utils.ReportingUtils;

public class AcquisitionWrapperEngineIcy extends AcquisitionWrapperEngine {

    private SequenceCacheListener listener;
    private boolean display = true;

    @Override
    public void setDisplayMode(int mode) {
	if (mode == 0)
	    display = false;
	else
	    display = true;
    }

    @Override
    public String runAcquisition(SequenceSettings acquisitionSettings) {
	try {
	    MyTaggedImagePipeline taggedImagePipeline = new MyTaggedImagePipeline(getAcquisitionEngine2010(), acquisitionSettings, taggedImageProcessors_,
		    gui_, acquisitionSettings.save, display);
	    summaryMetadata_ = taggedImagePipeline.summaryMetadata_;
	    imageCache_ = taggedImagePipeline.imageCache_;
	    listener = taggedImagePipeline.listener;
	    return taggedImagePipeline.acqName_;
	} catch (Throwable ex) {
	    ReportingUtils.showError(ex);
	    return null;
	}
    }

    public AcquisitionDisplay getDisplay() {
	return listener;
    }
}
