package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

import java.awt.Color;
import java.util.ArrayList;

import mmcorej.TaggedImage;

import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.api.ImageCacheListener;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;
import org.micromanager.utils.ReportingUtils;

/**
 * @author thomasprovoost
 */
public class SequenceCacheListener implements ImageCacheListener {

	public Object result;

	public SequenceCacheListener(SequenceSettings settings, JSONObject summaryMetadata) {
		try {
			int nbPositions = MDUtils.getNumPositions(summaryMetadata);
			int width = MDUtils.getWidth(summaryMetadata);
			int height = MDUtils.getHeight(summaryMetadata);
			int numChannels = MDUtils.getNumChannels(summaryMetadata);
			int numSlices = MDUtils.getNumSlices(summaryMetadata);
			int numFrames = MDUtils.getNumFrames(summaryMetadata);
			// int ijtype = MDUtils.getNumberOfComponents(summaryMetadata);
			int bitDepth = MDUtils.getBitDepth(summaryMetadata);

			if (nbPositions <= 1) {
				Sequence s = new Sequence();
				for (int t = 0; t < numFrames; ++t) {
					if (t != 0)
						s.addVolumetricImage();
					for (int z = 0; z < numSlices; ++z) {
						IcyBufferedImage img;
						if (bitDepth == 8)
							img = new IcyBufferedImage(width, height, numChannels, DataType.UBYTE);
						else if (bitDepth == 16)
							img = new IcyBufferedImage(width, height, numChannels, DataType.USHORT);
						else
							throw new MMScriptException("Unknown bit depth");
						s.addImage(img);
					}
				}
				for (int c = 0; c < settings.channels.size(); ++c) {
					s.getColorModel().getColormap(c).setARGBControlPoint(0, Color.BLACK);
					s.getColorModel().getColormap(c).setARGBControlPoint(255, settings.channels.get(c).color_);
				}
				result = s;
			} else {
				ArrayList<Sequence> sequences = new ArrayList<Sequence>();
				for (int i = 0; i < nbPositions; ++i) {
					Sequence s = new Sequence();
					for (int t = 0; t < numFrames; ++t) {
						if (t != 0)
							s.addVolumetricImage();
						for (int z = 0; z < numSlices; ++z) {
							IcyBufferedImage img;
							if (bitDepth == 8)
								img = new IcyBufferedImage(width, height, numChannels, DataType.UBYTE);
							else if (bitDepth == 16)
								img = new IcyBufferedImage(width, height, numChannels, DataType.USHORT);
							else
								throw new MMScriptException("Unknown bit depth");
							s.addImage(img);
						}
					}
					for (int c = 0; c < settings.channels.size(); ++c) {
						s.getColorModel().getColormap(c).setARGB(0, Color.BLACK);
						s.getColorModel().getColormap(c).setARGB(255, settings.channels.get(c).color_);
					}
					sequences.add(s);
				}
				result = sequences.toArray(new Sequence[0]);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (MMScriptException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void imageReceived(TaggedImage taggedImage) {
		addImage(taggedImage);
	}

	@Override
	public void imagingFinished(String path) {

	}

	public Object getResultingData() {
		return result;
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

			System.out.println(position + "/" + frame + "/" + slice + "/" + ch);

			Sequence s;
			if (result instanceof Sequence[]) {
				s = ((Sequence[]) result)[position];
			} else {
				s = (Sequence) result;
			}
			IcyBufferedImage img = s.getImage(frame, slice);
			img.setDataXY(ch, taggedImage.pix);
			s.setImage(frame, slice, img);
		} catch (Exception e) {
			ReportingUtils.logError(e);
		}
	}

}
