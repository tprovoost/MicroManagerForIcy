package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.image.IcyBufferedImage;
import icy.type.DataType;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.StageMover;

public class MicroscopeImage extends IcyBufferedImage {

	private double x = 0;
	private double y = 0;
	private double z = 0;
	private double exposure = -1;
	private long timeCapture = 0;
		
	public MicroscopeImage(int width, int height, int numComponents, DataType dataType) {
		super(width, height, numComponents, dataType);
		try {
			setXYZ(StageMover.getXYZ());
		} catch (Exception e1) {
		}
		try {
			exposure = MicroscopeCore.getCore().getExposure();
		} catch (Exception e) {
		}
		timeCapture = System.nanoTime();
	}
	
	/**
	 * @return the x
	 */
	public double getX() {
		return x;
	}

	/**
	 * @param z the z to set
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public double getY() {
		return y;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * @return the z
	 */
	public double getZ() {
		return z;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(double z) {
		this.z = z;
	}
	
	/** Set all coordinates at once */
	public void setXYZ(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	/** Set all coordinates at once */
	public void setXYZ(double [] coordinates) {
		x = coordinates[0];
		y = coordinates[1];
		z = coordinates[2];
	}

	public double getExposure() {
		return exposure;
	}

	public void setExposure(double exposure) {
		this.exposure = exposure;
	}

	public long getTimeCapture() {
		return timeCapture;
	}

	public void setTimeCapture(long timeCapture) {
		this.timeCapture = timeCapture;
	}
	
}
