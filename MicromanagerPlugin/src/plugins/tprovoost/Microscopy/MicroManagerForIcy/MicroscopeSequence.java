package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;

import org.micromanager.api.AcquisitionDisplay;

public class MicroscopeSequence extends Sequence implements AcquisitionDisplay
{

    // PainterCoordinates coordinates = new PainterCoordinates();
    // PainterReticle reticle = new PainterReticle();
    // PainterInfoConfig infoConfig = new PainterInfoConfig();
    MicroscopeCore core = MicroscopeCore.getCore();

    public MicroscopeSequence()
    {
        // setPainters();
        double pxSize = core.getPixelSizeUm();
        if (pxSize != 0)
        {
            setPixelSizeX(pxSize);
            setPixelSizeY(pxSize);
        }
    }

    public MicroscopeSequence(IcyBufferedImage buffer)
    {
        super(buffer);
        // setPainters();
        double pxSize = core.getPixelSizeUm();
        if (pxSize != 0)
        {
            setPixelSizeX(pxSize);
            setPixelSizeY(pxSize);
        }
    }

    public MicroscopeSequence(String string, IcyBufferedImage buffer)
    {
        super(string, buffer);
        // setPainters();
        double pxSize = core.getPixelSizeUm();
        if (pxSize != 0)
        {
            setPixelSizeX(pxSize);
            setPixelSizeY(pxSize);
        }
    }

    // private void setPainters() {
    // addPainter(coordinates);
    // addPainter(reticle);
    // addPainter(infoConfig);
    // }

}
