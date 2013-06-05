package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import mmcorej.TaggedImage;

import org.json.JSONObject;
import org.micromanager.acquisition.TaggedImageStorageRam;
import org.micromanager.utils.MMException;

public class IcyTaggedImageStorage extends TaggedImageStorageRam
{

    public IcyTaggedImageStorage(JSONObject summaryMetadata)
    {
        super(summaryMetadata);
    }

    @Override
    public void putImage(TaggedImage taggedImage) throws MMException
    {
        super.putImage(taggedImage);
        // JSONObject md = taggedImage.tags;
        // System.out.println(md.toString());
    }
}
