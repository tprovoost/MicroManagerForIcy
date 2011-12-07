package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.util.StringUtil;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.Calendar;
import java.util.Hashtable;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeImage;

public class PainterInfoConfig extends MicroscopePainter {

	private Hashtable<String,String> dataToDisplay = new Hashtable<String,String>();
	private String exposure = null;
	private String timeCapture = null;
	private boolean displayHelp = true;

	@Override
	public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas) {
		if (!displayHelp || canvas instanceof Canvas3D)
			return;
		int w = sequence.getWidth();
		int h = sequence.getHeight();

		IcyBufferedImage img = canvas.getCurrentImage();
		if (img instanceof MicroscopeImage) {
			MicroscopeImage Mimg = (MicroscopeImage) img;
			double exp = Mimg.getExposure();
			if (exp != -1) {
				exposure = "Exposure: " + StringUtil.toString(Mimg.getExposure(), 2) + " ms";
				dataToDisplay.put("exposure", exposure);
			}
			long time = Mimg.getTimeCapture();
			if (time != 0) {
				Calendar calendar = Calendar.getInstance();
				timeCapture = "Date: " + calendar.get(Calendar.MONTH) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + calendar.get(Calendar.YEAR) + "-"
						+ calendar.get(Calendar.HOUR_OF_DAY) + "_" + calendar.get(Calendar.MINUTE) + "_" + calendar.get(Calendar.SECOND) ;//+ ;
				dataToDisplay.put("timeCapture", timeCapture);
			}
			
		}
		
		putData("help", "Alt + h: toggle help");

		// set the font
		Font f;
		if (w > h)
			f = g.getFont().deriveFont(12F * h / 512);
		else
			f = g.getFont().deriveFont(12F * w / 512);
		g.setFont(f);
		int fh = g.getFontMetrics().getHeight();

		// ------------------------------
		// Calculate the size of the box
		// ------------------------------
		int nbComponents = 0;
		int largestComponentWidth = -1;
		for (String s : dataToDisplay.values()) {
			++nbComponents;
			int wh = g.getFontMetrics().charsWidth(s.toCharArray(), 0, s.length());
			if (wh > largestComponentWidth)
				largestComponentWidth = wh;
		}

		// --------------------------
		// Fill the box with the data
		// --------------------------
		if (nbComponents == 0)
			return;
		g.setColor(prefs.getColor("Text Background"));
		g.fillRect(0, 0, largestComponentWidth + fh * 2, fh * nbComponents + fh / 2);
		g.setColor(prefs.getColor("Text"));
		int i = 1;
		for (String s : dataToDisplay.values()) {
			g.drawString(s, fh, fh * i);
			++i;
		}
	}

	public void putData(String key, String value) {
		dataToDisplay.put(key, value);
	}
	
	public void removeData(String key) {
		dataToDisplay.remove(key);
	}
	
	@Override
	public void keyPressed(KeyEvent e, Point2D imagePoint, IcyCanvas canvas) {
		if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_H) {
			displayHelp = !displayHelp;
		}
	}
}