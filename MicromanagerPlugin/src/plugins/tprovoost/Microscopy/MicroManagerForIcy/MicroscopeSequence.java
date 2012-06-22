package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import java.nio.channels.Channel;
import java.util.ArrayList;
import jxl.biff.NumFormatRecordsException;

import mmcorej.TaggedImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.api.AcquisitionDisplay;
import org.micromanager.api.ImageCacheListener;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

public class MicroscopeSequence extends Sequence implements AcquisitionDisplay, ImageCacheListener {

	// PainterCoordinates coordinates = new PainterCoordinates();
	// PainterReticle reticle = new PainterReticle();
	// PainterInfoConfig infoConfig = new PainterInfoConfig();
	MicroscopeCore core = MicroscopeCore.getCore();

	public MicroscopeSequence() {
		if (core.getPixelSizeUm() > 0) {
			setPixelSizeX(core.getPixelSizeUm() * 0.001);
			setPixelSizeY(getPixelSizeX());
		}
	}

	public MicroscopeSequence(JSONObject summaryMetadata) {
		if (core.getPixelSizeUm() > 0) {
			setPixelSizeX(core.getPixelSizeUm() * 0.001);
			setPixelSizeY(getPixelSizeX());
		}
		try {
			int width = MDUtils.getWidth(summaryMetadata);
			int height = MDUtils.getHeight(summaryMetadata);
			int numChannels = MDUtils.getNumChannels(summaryMetadata);
			int numSlices = MDUtils.getNumSlices(summaryMetadata);
			int numFrames = MDUtils.getNumFrames(summaryMetadata);
			int ijtype = MDUtils.getNumberOfComponents(summaryMetadata);
			int bitDepth = MDUtils.getBitDepth(summaryMetadata);
			for (int t = 0; t < numFrames; ++t) {
				if (t != 0)
					addVolumetricImage();
				for (int z = 0 ; z < numSlices; ++z) {
					IcyBufferedImage img;
					if (bitDepth == 1)
						img = new IcyBufferedImage(width, height, numChannels, DataType.UBYTE);
					else if (bitDepth == 2)
						img = new IcyBufferedImage(width, height, numChannels, DataType.USHORT);
					else
						throw new MMScriptException("Unknown bit depth");				
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MMScriptException e) {
			e.printStackTrace();
		}

	}

	public MicroscopeSequence(IcyBufferedImage buffer) {
		super(buffer);
		// setPainters();
		if (core.getPixelSizeUm() > 0) {
			setPixelSizeX(core.getPixelSizeUm() * 0.001);
			setPixelSizeY(getPixelSizeX());
		}
	}

	public MicroscopeSequence(String string, IcyBufferedImage buffer) {
		super(string, buffer);
		// setPainters();
		if (core.getPixelSizeUm() > 0) {
			setPixelSizeX(core.getPixelSizeUm() * 0.001);
			setPixelSizeY(getPixelSizeX());
		}
	}

	@Override
	public void imageReceived(TaggedImage taggedImage) {
		addImage(taggedImage);
	}

	@Override
	public void imagingFinished(String path) {

	}

	private void addImage(TaggedImage taggedImage) {
		try {
			JSONObject tags = null;
			if (taggedImage != null) {
				tags = taggedImage.tags;
			}
			if (tags == null) {
				return;
			}
			int frame = MDUtils.getFrameIndex(tags);
			int ch = MDUtils.getChannelIndex(tags);
			int slice = MDUtils.getSliceIndex(tags);
			int position = MDUtils.getPositionIndex(tags);
			IcyBufferedImage img = getImage(frame, slice);
			img.setDataXY(ch, taggedImage);
			dataChanged();
		} catch (Exception e) {
			ReportingUtils.logError(e);
		}
	}

	// private void setPainters() {
	// addPainter(coordinates);
	// addPainter(reticle);
	// addPainter(infoConfig);
	// }

}
