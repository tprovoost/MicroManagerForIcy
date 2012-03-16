package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.canvas.Layer;
import icy.sequence.Sequence;

import java.awt.BasicStroke;
import java.awt.Graphics2D;

public class PainterReticle extends MicroscopePainter {

	boolean drawNormal = false;

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		
		// VERIFICATIONS
		if ( canvas instanceof Canvas3D)
			return;
		Layer layer = canvas.getLayer(this);
		if (layer != null && !layer.getName().equals("Reticle"))
			layer.setName("Reticle");

		// VARIABLES
		int w = sequence.getWidth();
		int h = sequence.getHeight();
		int sizew = (int) (canvas.canvasToImageLogDeltaX(20));
		float strokew = (float) canvas.canvasToImageDeltaX(2);
		
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
