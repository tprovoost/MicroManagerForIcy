package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.PainterCoordinates;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.PainterInfoConfig;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.PainterReticle;

public class MicroscopeSequence extends Sequence {
	
	PainterCoordinates coordinates = new PainterCoordinates();
	PainterReticle reticle = new PainterReticle();
	PainterInfoConfig infoConfig = new PainterInfoConfig();
	MicroscopeCore core = MicroscopeCore.getCore();
	
	public MicroscopeSequence() {
		setPainters();
		setPixelSizeX(core.getPixelSizeUm()*0.001);
		setPixelSizeY(getPixelSizeX());
	}

	public MicroscopeSequence(IcyBufferedImage buffer) {
		super(buffer);
		setPainters();
		setPixelSizeX(core.getPixelSizeUm()*0.001);
		setPixelSizeY(getPixelSizeX());
	}

	public MicroscopeSequence(String string, IcyBufferedImage buffer) {
		super(string, buffer);
		setPainters();
		setPixelSizeX(core.getPixelSizeUm()*0.001);
		setPixelSizeY(getPixelSizeX());
	}
	
	private void setPainters() {
		addPainter(coordinates);
		addPainter(reticle);
		addPainter(infoConfig);
	}
	
	public PainterInfoConfig getPainterInfoConfig() {
		return infoConfig;
	}
}
