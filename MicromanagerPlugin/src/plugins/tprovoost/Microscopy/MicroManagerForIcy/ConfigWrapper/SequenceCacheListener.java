package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.type.DataType;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import loci.formats.ome.OMEXMLMetadataImpl;
import mmcorej.TaggedImage;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;
import org.micromanager.acquisition.SequenceSettings;
import org.micromanager.api.ImageCacheListener;
import org.micromanager.navigation.MultiStagePosition;
import org.micromanager.utils.ChannelSpec;
import org.micromanager.utils.MDUtils;
import org.micromanager.utils.MMScriptException;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopePluginAcquisition;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;

/**
 * @author thomasprovoost
 */
public class SequenceCacheListener implements ImageCacheListener
{

    private static final String POS_X = "XPositionUm";
    private static final String POS_Y = "YPositionUm";
    private static final String POS_Z = "ZPositionUm";

    private Object result = null;
    private int nbImages;
    private int currentImg;

    private SequenceSettings settings;
    private JSONObject summaryMetadata;

    boolean firstDone = false;
    private int planeIdx = -1;

    private MicroscopePluginAcquisition pluginAcquisition = new MicroscopePluginAcquisition()
    {

        @Override
        public void start()
        {
        }

        @Override
        public String getRenderedName()
        {
            return "Acquisition Engine";
        }
    };

    public SequenceCacheListener(SequenceSettings settings, JSONObject summaryMetadata)
    {
        this.settings = settings;
        this.summaryMetadata = summaryMetadata;
        pluginAcquisition.notifyAcquisitionStarted(true);
        try
        {
            int nbPositions = MDUtils.getNumPositions(summaryMetadata);
            int width = MDUtils.getWidth(summaryMetadata);
            int height = MDUtils.getHeight(summaryMetadata);
            int numChannels = MDUtils.getNumChannels(summaryMetadata);
            int numSlices = MDUtils.getNumSlices(summaryMetadata);
            int numFrames = MDUtils.getNumFrames(summaryMetadata);
            int bitDepth = MDUtils.getBitDepth(summaryMetadata);
            double pxSize = summaryMetadata.getDouble("PixelSize_um");
            double zStep = summaryMetadata.getDouble("z-step_um");
            zStep = zStep == 0.0d ? 1 : zStep;

            nbImages = numChannels * numSlices * numFrames * nbPositions;
            currentImg = 0;
            if (nbPositions <= 1)
            {
                Sequence s = new Sequence();
                OMEXMLMetadataImpl metadata = s.getMetadata();
                try
                {
                    double[] position = StageMover.getXYZ();
                    metadata.setStageLabelX(position[0], 0);
                    metadata.setStageLabelY(position[1], 0);
                    metadata.setStageLabelZ(position[2], 0);
                }
                catch (Exception e)
                {
                }
                if (numChannels == 1)
                {
                    try
                    {
                        metadata.setChannelName(MicroscopeCore.getCore().getCurrentFilterBLockLabel(), 0, 0);
                    }
                    catch (Exception e)
                    {
                    }
                }
                else
                {
                    for (int i = 0; i < numChannels; ++i)
                    {
                        ChannelSpec channel = settings.channels.get(i);
                        metadata.setChannelName(channel.config_, 0, i);
                    }
                }
                s.setPixelSizeX(pxSize);
                s.setPixelSizeY(pxSize);
                s.setPixelSizeZ(zStep);
                for (int t = 0; t < numFrames; ++t)
                {
                    if (t != 0)
                        s.addVolumetricImage();
                    for (int z = 0; z < numSlices; ++z)
                    {
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
                for (int c = 0; c < settings.channels.size(); ++c)
                {
                    s.getColorModel().getColormap(c).setARGBControlPoint(0, Color.BLACK);
                    s.getColorModel().getColormap(c).setARGBControlPoint(255, settings.channels.get(c).color_);
                }
                result = s;
            }
            else
            {
                ArrayList<Sequence> sequences = new ArrayList<Sequence>();
                for (int i = 0; i < nbPositions; ++i)
                {
                    Sequence s = new Sequence();
                    MultiStagePosition pos = settings.positions.get(i);
                    s.getMetadata().setStageLabelX(pos.getX(), 0);
                    s.getMetadata().setStageLabelX(pos.getY(), 0);
                    s.getMetadata().setStageLabelX(pos.getZ(), 0);
                    s.setPixelSizeX(pxSize);
                    s.setPixelSizeY(pxSize);
                    s.setPixelSizeZ(zStep);
                    for (int t = 0; t < numFrames; ++t)
                    {
                        if (t != 0)
                            s.addVolumetricImage();
                        for (int z = 0; z < numSlices; ++z)
                        {
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
                    for (int c = 0; c < settings.channels.size(); ++c)
                    {
                        s.getColorModel().getColormap(c).setARGB(0, Color.BLACK);
                        s.getColorModel().getColormap(c).setARGB(255, settings.channels.get(c).color_);
                    }
                    sequences.add(s);
                }
                result = sequences.toArray(new Sequence[0]);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (MMScriptException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void imageReceived(TaggedImage taggedImage)
    {
        try
        {
            addImage(taggedImage);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        updateProgress();
    }

    private void updateProgress()
    {
        currentImg++;
        pluginAcquisition.notifyProgress((int) (1d * currentImg / nbImages * 100));
    }

    @Override
    public void imagingFinished(String path)
    {
        result = null;
        pluginAcquisition.notifyAcquisitionOver();
    }

    public Object getResultingData()
    {
        return result;
    }

    private void addImage(TaggedImage taggedImage) throws JSONException
    {
        JSONObject tags = null;
        if (taggedImage != null)
        {
            tags = taggedImage.tags;
        }
        if (tags == null)
        {
            return;
        }
        int frame = MDUtils.getFrameIndex(tags);
        int ch = MDUtils.getChannelIndex(tags);
        int slice = MDUtils.getSliceIndex(tags);
        int position = MDUtils.getPositionIndex(tags);
        Sequence s;
        if (result instanceof Sequence[])
        {
            s = ((Sequence[]) result)[position];
        }
        else
        {
            s = (Sequence) result;
        }
        IcyBufferedImage img = s.getImage(frame, slice);
        img.setDataXY(ch, taggedImage.pix);
        s.setImage(frame, slice, img);
        planeIdx++;

        OMEXMLMetadataImpl metadata = s.getMetadata();
        // Position of the plane
        int numFrames = MDUtils.getNumFrames(summaryMetadata);
        int slices = MDUtils.getNumSlices(summaryMetadata);

        double[] defaultPos;
        try
        {
            defaultPos = StageMover.getXYZ();
        }
        catch (Exception e1)
        {
            defaultPos = new double[] {0d, 0d, 0d};
        }
        double xPos = defaultPos[0];
        double yPos = defaultPos[1];
        double zPos = defaultPos[2];
        xPos = taggedImage.tags.has(POS_X) ? (Double) taggedImage.tags.get("XPositionUm") : xPos;
        yPos = taggedImage.tags.has(POS_Y) ? (Double) taggedImage.tags.get("YPositionUm") : yPos;
        zPos = taggedImage.tags.has(POS_Z) ? (Double) taggedImage.tags.get("ZPositionUm") : zPos;

        metadata.setPlanePositionX(xPos, 0, planeIdx);
        metadata.setPlanePositionY(yPos, 0, planeIdx);
        metadata.setPlanePositionZ(zPos, 0, planeIdx);
        metadata.setPlaneTheT(NonNegativeInteger.valueOf("" + frame), 0, planeIdx);
        metadata.setPlaneTheZ(NonNegativeInteger.valueOf("" + slice), 0, planeIdx);

        int numChannels = MDUtils.getNumChannels(summaryMetadata);
        double exposure = 0;
        if (numChannels <= 1)
            try
            {
                exposure = MicroscopeCore.getCore().getExposure();
            }
            catch (Exception e)
            {
            }
        else
            exposure = settings.channels.get(ch).exposure_;

        metadata.setPlaneExposureTime(exposure, 0, planeIdx);
        metadata.setPlaneTheC(NonNegativeInteger.valueOf("" + ch), 0, planeIdx);

        // Date
        Date date;
        try
        {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse((String) taggedImage.tags.get("Time"));
            metadata.setImageAcquisitionDate(new Timestamp(date), 0);
        }
        catch (ParseException e)
        {
        }
    }
}
