package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.canvas.IcyCanvas;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.painter.Painter;
import icy.preferences.IcyPreferences;
import icy.preferences.XMLPreferences;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.MicroscopePainter;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.painters.PainterInfoConfig;

public class LivePainter extends MicroscopePainter {

	MicroscopeCore mCore = MicroscopeCore.getCore();
	Point2D lastImagePoint;

	XMLPreferences prefs = IcyPreferences.pluginsRoot().node("LivePainter");
	boolean considerPxSizeCfg;

	public LivePainter(LiveSequence s) {
		considerPxSizeCfg = prefs.getBoolean("considerPxSizeCfg", true);
		StageMover.setInvertX(prefs.getBoolean("invertX", false));
		StageMover.setInvertY(prefs.getBoolean("invertY", false));
		for (Painter pa : s.getPainters()) {
			if (pa instanceof PainterInfoConfig) {
				((PainterInfoConfig) pa).putData("helpDrag", "Alt + drag: move the stage");
				((PainterInfoConfig) pa).putData("helpPress", "Ctrl + Shift + click: move to point");
				setInvertX((PainterInfoConfig) pa);
				setInvertY((PainterInfoConfig) pa);
			}
		}

	}

	private void setInvertX(PainterInfoConfig pif) {
		if (StageMover.isInvertX())
			pif.putData("helpInvertX", "x: inverted (Alt + X to change)");
		else
			pif.putData("helpInvertX", "x: normal (Alt + X to change)");
	}

	private void setInvertY(PainterInfoConfig pif) {
		if (StageMover.isInvertY())
			pif.putData("helpInvertY", "y: inverted (Alt + Y to change)");
		else
			pif.putData("helpInvertY", "y: normal (Alt + Y to change)");
	}

	@Override
	public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_X) {
			StageMover.setInvertX(StageMover.isInvertX());
			for (Painter pa : canvas.getSequence().getPainters()) {
				if (pa instanceof PainterInfoConfig) {
					setInvertX((PainterInfoConfig) pa);
				}
			}
		} else if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_Y) {
			StageMover.setInvertY(StageMover.isInvertY());
			for (Painter pa : canvas.getSequence().getPainters()) {
				if (pa instanceof PainterInfoConfig) {
					setInvertY((PainterInfoConfig) pa);
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (e.isAltDown()) {
			lastImagePoint = imagePoint;
			e.consume();
		}
		if (e.isShiftDown() && e.isControlDown()) {
			if (mCore.getAvailablePixelSizeConfigs().size() > 0)
				try {
					StageMover.moveToPoint(canvas.getSequence(), imagePoint.getX(), imagePoint.getY());
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			else
				MessageDialog.showDialog("PixelSize configuration", "No pixel size configuration has been found. The pixel size "
						+ "represents the number of pixels per µm, and is often used for high precision when moving the stage. <br/>"
						+ "It is highly advised to run the Calibrator Manager to get the best value.");
		}
	}

	@Override
	public void mouseReleased(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		lastImagePoint = null;
	}

	@Override
	public void mouseDrag(MouseEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (e.isAltDown()) {
			boolean pxSizeConfigured = mCore.getAvailablePixelSizeConfigs().size() > 0;
			if (!considerPxSizeCfg || (considerPxSizeCfg && pxSizeConfigured)) {
				if (lastImagePoint != null) {
					// movement
					Point2D vect = new Point2D.Double(imagePoint.getX() - lastImagePoint.getX(), imagePoint.getY() - lastImagePoint.getY());
					double pxsize;
					if (pxSizeConfigured)
						pxsize = mCore.getPixelSizeUm();
					else
						pxsize = 1;
					try {
						StageMover.moveXYRelative(vect.getX() * pxsize, vect.getY() * pxsize);
						e.consume();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			} else {
				if (ConfirmDialog.confirm("PixelSize configuration", "No pixel size configuration was found. The pixel size "
						+ "represents the number of pixels per µm, and is often used for high precision when moving the stage. <br/>"
						+ "It is highly advised to run the Calibrator Manager to get the best value and set it in Micro-Manager For Icy before continuing."
						+ "<br/>However, you can use the drag without a pixel size configuration, but the speed of the stage will "
						+ "not be according to neither your current objective nor your camera.<br/> " + "Do you want to use the drag without the pixel size configuration ?")) {
					considerPxSizeCfg = true;
					if (ConfirmDialog.confirm("Save", "Do you want to always be able to use the drag option, without being noticed ?"))
						prefs.putBoolean("considerPxSizeCfg", considerPxSizeCfg);
				}
			}
		}
	}
}
