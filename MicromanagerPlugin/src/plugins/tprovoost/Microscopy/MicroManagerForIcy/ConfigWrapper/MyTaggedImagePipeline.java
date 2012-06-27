/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.main.Icy;
import icy.sequence.Sequence;

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
import org.micromanager.utils.MMScriptException;

/**
 * This is the default setup for the acquisition engine pipeline.
 * We create a default display,
 * a DataProcessor<TaggedImage> queue,
 * and a default saving mechanism and connect them to the acqEngine.
 * Alternate setups are possible.
 */
public class MyTaggedImagePipeline {

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
	public MyTaggedImagePipeline(
			IAcquisitionEngine2010 acqEngine,
			SequenceSettings sequenceSettings,
			List<DataProcessor<TaggedImage>> imageProcessors,
			ScriptInterface gui,
			boolean diskCached) throws ClassNotFoundException, InstantiationException, IllegalAccessException, MMScriptException {

		// Start up the acquisition engine
		BlockingQueue<TaggedImage> taggedImageQueue = acqEngine.run(sequenceSettings);
		summaryMetadata_ = acqEngine.getSummaryMetadata();

		
		// Create the default display
		acqName_ = getClass().getName();
		listener = new SequenceCacheListener(sequenceSettings, summaryMetadata_);
		Object resultingData = listener.getResultingData();
		if (resultingData instanceof Sequence) {
			Icy.addSequence((Sequence)resultingData);
		} else {
			Sequence[] sequences = (Sequence[]) resultingData;
			for (Sequence s : sequences)
				Icy.addSequence(s);
		}
		imageCache_ = new MMImageCache(new TaggedImageStorageRam(summaryMetadata_));
		imageCache_.addImageCacheListener(listener);

		// Start pumping images into the ImageCache
		LiveAcq liveAcq = new LiveAcq(taggedImageQueue, imageCache_);
		liveAcq.start();
	}

}
