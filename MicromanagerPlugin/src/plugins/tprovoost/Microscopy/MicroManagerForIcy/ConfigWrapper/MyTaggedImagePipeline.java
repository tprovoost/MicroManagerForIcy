/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.main.Icy;
import icy.sequence.Sequence;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import mmcorej.TaggedImage;

import org.json.JSONObject;
import org.micromanager.acquisition.LiveAcq;
import org.micromanager.acquisition.MMImageCache;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.acquisition.TaggedImageStorageRam;
import org.micromanager.api.DataProcessor;
import org.micromanager.api.IAcquisitionEngine2010;
import org.micromanager.api.ImageCache;
import org.micromanager.api.ScriptInterface;
import org.micromanager.navigation.MultiStagePosition;
import org.micromanager.utils.MMScriptException;

/**
 * This is the default setup for the acquisition engine pipeline.
 * We create a default display,
 * a DataProcessor<TaggedImage> queue,
 * and a default saving mechanism and connect them to the acqEngine.
 * Alternate setups are possible.
 */
public class MyTaggedImagePipeline
{

    final String acqName_;
    final JSONObject summaryMetadata_;
    final ImageCache imageCache_;
    final SequenceCacheListener listener;

    /*
     * This class creates the default sequence of modules
     * that digest a TaggedImage. They are
     * AcquisitionEngine2010 -> ProcessorStack -> LiveAcq -> ImageCache
     * -> Sequence
     * Other kinds of pipelines can be set up in this way.
     */
    public MyTaggedImagePipeline(IAcquisitionEngine2010 acqEngine, SequenceSettings sequenceSettings,
            List<DataProcessor<TaggedImage>> imageProcessors, ScriptInterface gui, boolean diskCached, boolean display)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, MMScriptException
    {

        // Start up the acquisition engine

        BlockingQueue<TaggedImage> taggedImageQueue = acqEngine.run(sequenceSettings);
        summaryMetadata_ = acqEngine.getSummaryMetadata();

        // Create the default display
        acqName_ = getClass().getName();
        listener = new SequenceCacheListener(sequenceSettings, summaryMetadata_);
        Object resultingData = listener.getResultingData();
        if (display)
        {
            Calendar calendar = Calendar.getInstance();
            String sequence_name = "MultiD - " + calendar.get(Calendar.MONTH) + "_"
                    + calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.YEAR) + " - "
                    + calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_"
                    + calendar.get(Calendar.SECOND);
            if (resultingData instanceof Sequence)
            {
                Sequence res = (Sequence) resultingData;
                res.setName(sequence_name);
                Icy.getMainInterface().addSequence(res);
            }
            else
            {
                Sequence[] sequences = (Sequence[]) resultingData;
                for (int i = 0; i < sequences.length; ++i)
                {
                    Sequence s = sequences[i];
                    MultiStagePosition pos = sequenceSettings.positions.get(i);
                    int x = (int) (100 * pos.getX()) / 100;
                    int y = (int) (100 * pos.getY()) / 100;
                    int z = (int) (100 * pos.getZ()) / 100;
                    s.setName(sequence_name + " - " + x + "_" + y + "_" + z);
                    Icy.getMainInterface().addSequence(s);
                }
            }
        }
        imageCache_ = new MMImageCache(new TaggedImageStorageRam(summaryMetadata_));
        imageCache_.addImageCacheListener(listener);

        // Start pumping images into the ImageCache
        LiveAcq liveAcq = new LiveAcq(taggedImageQueue, imageCache_);
        liveAcq.start();
    }

}
