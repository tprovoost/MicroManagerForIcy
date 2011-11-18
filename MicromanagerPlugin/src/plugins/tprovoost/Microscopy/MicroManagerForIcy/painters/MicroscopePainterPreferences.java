package plugins.tprovoost.Microscopy.MicroManagerForIcy.painters;

import icy.preferences.XMLPreferences;

import java.awt.Color;
import java.util.HashMap;

public class MicroscopePainterPreferences {

	private static MicroscopePainterPreferences microscopePrefs = new MicroscopePainterPreferences();
	private HashMap<String, Color> colors = new HashMap<String, Color>();
	private XMLPreferences xmlpreferences = null;

	/**
	 * Singleton pattern: Private constructor
	 */
	private MicroscopePainterPreferences() {
		// some colors are loaded by default
		colors.put("Normal", new Color(30, 255, 255, 100));
		colors.put("Text Background", new Color(0, 0, 0, 75));
		colors.put("Borders", new Color(217, 217, 217, 100));
		colors.put("Text", new Color(250, 253, 12, 100));
		colors.put("Shadow", Color.GRAY);
		colors.put("Reticle", new Color(250, 253, 12, 100));
	}

	public void setPreferences(XMLPreferences root) {
		xmlpreferences = root;
	}

	public void saveColors() {
		if (microscopePrefs == null)
			return;
		for (String s : colors.keySet()) {
			XMLPreferences actualKey = xmlpreferences.node(s);
			Color ctmp = colors.get(s);
			actualKey.putInt("red", ctmp.getRed());
			actualKey.putInt("green", ctmp.getGreen());
			actualKey.putInt("blue", ctmp.getBlue());
			actualKey.putInt("alpha", ctmp.getAlpha());
		}
	}

	/**
	 * Load the colors according to the XML file set previously.
	 * @see #setPreferences(XMLPreferences)
	 */
	public void loadColors() {
		if (microscopePrefs == null)
			return;
		for (XMLPreferences key : xmlpreferences.getChildren()) {
			int r, g, b, a;
			r = key.getInt("red", -1);
			g = key.getInt("green", -1);
			b = key.getInt("blue", -1);
			a = key.getInt("alpha", -1);
			if (r != -1 && g != -1 && b != -1 && a != -1)
				colors.put(key.name(), new Color(r, g, b, a));
		}
	}

	/**
	 * Singleton pattern: get the reference
	 */
	public static MicroscopePainterPreferences getInstance() {
		return microscopePrefs;
	}

	public Color getColor(String colorName) {
		return colors.get(colorName);
	}

	public void setColor(String colorName, Color color) {
		colors.put(colorName, color);
	}

	public HashMap<String, Color> getColors() {
		return colors;
	}

	/**
	 * This method will return the painter name (or key) corresponding to its
	 * index in the keys() method.
	 * 
	 * @param i
	 *            : index of the painter name
	 * @return the painter name
	 */
	public String getPainterName(int i) {
		return colors.keySet().toArray(new String[0])[i];
	}
}
