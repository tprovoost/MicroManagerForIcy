package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import org.micromanager.acquisition.DefaultTaggedImagePipeline;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.api.AAcquisitionWrapperEngine;
import org.micromanager.api.AcquisitionDisplay;
import org.micromanager.utils.ReportingUtils;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeSequence;

public class AcquisitionWrapperEngineIcy extends AAcquisitionWrapperEngine {

	MicroscopeSequence sequence;
	
	@Override
	public String runPipeline(SequenceSettings acquisitionSettings) {
		try {
	         MyTaggedImagePipeline taggedImagePipeline = new MyTaggedImagePipeline(
	                 getPipeline(),
	                 acquisitionSettings,
	                 taggedImageProcessors_,
	                 gui_,
	                 acquisitionSettings.save);
	         summaryMetadata_ = taggedImagePipeline.summaryMetadata_;
	         imageCache_ = taggedImagePipeline.imageCache_;
	         sequence = taggedImagePipeline.sequence;
	         return taggedImagePipeline.acqName_;
	      } catch (Throwable ex) {
	         ReportingUtils.showError(ex);
	         return null;
	      }
	}

	@Override
	public AcquisitionDisplay getDisplay() {
		return sequence;
	}

}
