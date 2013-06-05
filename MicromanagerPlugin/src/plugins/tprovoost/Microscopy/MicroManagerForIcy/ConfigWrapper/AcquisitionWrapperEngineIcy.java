package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import org.micromanager.acquisition.AcquisitionWrapperEngine;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.utils.ReportingUtils;

public class AcquisitionWrapperEngineIcy extends AcquisitionWrapperEngine
{
    @Override
    public void stop(boolean interrupted)
    {
        super.stop(interrupted);
    }

    @Override
    public void shutdown()
    {
        super.shutdown();
    }
    
    @Override
    public String runAcquisition(SequenceSettings acquisitionSettings)
    {
        try
        {
            IcyTaggedImagePipeline taggedImagePipeline = new IcyTaggedImagePipeline(getAcquisitionEngine2010(),
                    acquisitionSettings, taggedImageProcessors_, gui_, acquisitionSettings.save, !getSaveFiles());
            summaryMetadata_ = taggedImagePipeline.summaryMetadata_;
            imageCache_ = taggedImagePipeline.imageCache_;
            return taggedImagePipeline.acqName_;
        }
        catch (Throwable ex)
        {
            ReportingUtils.showError(ex);
            return null;
        }
    }
}
