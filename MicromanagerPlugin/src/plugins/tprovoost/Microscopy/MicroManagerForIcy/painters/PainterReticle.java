package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.canvas.IcyCanvas;
import icy.roi.ROI;
import icy.sequence.Sequence;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class PainterReticle extends MicroscopePainter {

	boolean drawNormal = false;

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		super.paint(g, sequence, canvas);
		int w = sequence.getWidth();
		int h = sequence.getHeight();
		int sizew = (int) (ROI.canvasToImageLogDeltaX(canvas, 20));
		float strokew = (float) ROI.canvasToImageDeltaX(canvas, 2);
		
		// Draw the shadow
		g.setColor(prefs.getColor("Shadow"));
		g.setStroke(new BasicStroke(strokew));
		g.drawLine(w / 2 - sizew / 2 - 1, h / 2 + 1, (w / 2) + sizew / 2 - 1, h / 2 + 1);
		g.drawLine(w / 2 + 1, h / 2 - sizew / 2 - 1, w / 2 + 1, h / 2 + sizew / 2 - 1);

		// draw the reticle
		g.setColor(prefs.getColor("Reticle"));
		g.drawLine(w / 2 - sizew / 2, h / 2, (w / 2) + sizew / 2, h / 2);
		g.drawLine(w / 2, h / 2 - sizew / 2, w / 2, h / 2 + sizew / 2);
	}
}