package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;

import org.micromanager.api.AcquisitionDisplay;

public class MicroscopeSequence extends Sequence implements AcquisitionDisplay {

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


	// private void setPainters() {
	// addPainter(coordinates);
	// addPainter(reticle);
	// addPainter(infoConfig);
	// }

}
