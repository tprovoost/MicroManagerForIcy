package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeImage;

public class PainterCoordinates extends MicroscopePainter {

	protected String xValue = "0.0000 µm";
	protected String yValue = "0.0000 µm";
	protected String zValue = "0.0000 µm";

	public PainterCoordinates() {
		MicroscopeCore core = MicroscopeCore.getCore();
		String nameXY = core.getXYStageDevice();
		String nameZ = core.getFocusDevice();
		if (nameXY != null && nameXY != "") {
			try {
				xValue = StringUtil.toString(core.getXPosition(nameXY), 4);
			} catch (Exception e) {
			}
			try {
				yValue = StringUtil.toString(core.getYPosition(nameXY), 4);
			} catch (Exception e) {
			}
		}
		if (nameZ != null && nameZ != "") {
			try {
				zValue = StringUtil.toString(core.getPosition(nameZ), 4);
			} catch (Exception e) {
			}
		}
	}

	public PainterCoordinates(double x, double y, double z) {
		xValue = StringUtil.toString(-x, 4);
		yValue = StringUtil.toString(-y, 4);
		zValue = StringUtil.toString(-z, 4);
	}

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		super.paint(g, sequence, canvas);
		if ( canvas instanceof Canvas3D)
			return;
		if (xValue.contains("0.0000") && yValue.contains("0.0000") && zValue.contains("0.0000"))
			return;
		int w = sequence.getWidth();
		int h = sequence.getHeight();

		IcyBufferedImage img = canvas.getCurrentImage();
		if (img instanceof MicroscopeImage) {
			MicroscopeImage Mimg = (MicroscopeImage) img;
			xValue = StringUtil.toString(Mimg.getX(), 4) + " µm";
			yValue = StringUtil.toString(Mimg.getY(), 4) + " µm";
			zValue = StringUtil.toString(Mimg.getZ(), 4) + " µm";
		}

		// SETUP OF THE TEXT
		String toDisplay = "Stage:  x: " + xValue + "  y: " + yValue + "  z: " + zValue;
		Font f;
		if (w > h)
			f = g.getFont().deriveFont(14F * h / 512);
		else
			f = g.getFont().deriveFont(14F * w / 512);
		g.setFont(f);
		int fh = g.getFontMetrics().getHeight();
		int wh = g.getFontMetrics().charsWidth(toDisplay.toCharArray(), 0, toDisplay.length());
		// DRAW
		g.setColor(prefs.getColor("Text Background"));
		g.fillRect(0, h - fh, wh + fh * 2, fh);
		g.setStroke(new BasicStroke((float) ROI.canvasToImageLogDeltaX(canvas, 4)));
		g.setColor(prefs.getColor("Borders"));
		g.setStroke(new BasicStroke(1));
		g.drawRect(0, h - fh, wh + fh * 2, fh);
		g.setColor(prefs.getColor("Text"));
		g.drawString(toDisplay, fh, h - fh / 4);
	}
}
