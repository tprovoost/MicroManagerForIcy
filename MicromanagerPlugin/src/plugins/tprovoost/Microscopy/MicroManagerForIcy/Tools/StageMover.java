package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools;

import icy.preferences.XMLPreferences;
import icy.roi.ROI2D;
import icy.sequence.Sequence;

import java.util.ArrayList;

import mmcorej.CMMCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;

public class StageMover {

	private static ArrayList<StageListener> _listeners = new ArrayList<StageListener>();
	private static boolean invertX = false;
	private static boolean invertY = false;
	private static boolean invertZ = false;
	private static boolean switchXY = false;

	private static XMLPreferences prefs;
	/** Constant value of the invert x for stage movement */
	private static final String INVERTX = "invertx";
	/** Constant value of the invert y for stage movement */
	private static final String INVERTY = "inverty";
	/** Constant value of the invert z for stage movement */
	private static final String INVERTZ = "invertz";
	/** Constant value of the switch between x and y for stage movement */
	private static final String SWITCHXY = "switchxy";

	public static void loadPreferences(XMLPreferences pref) {
		prefs = pref;
		invertX = prefs.getBoolean(INVERTX, false);
		invertY = prefs.getBoolean(INVERTY, false);
		invertZ = prefs.getBoolean(INVERTZ, false);
		switchXY = prefs.getBoolean(SWITCHXY, false);
	}

	/**
	 * Add a listener to the Stage Mover. The Stage Mover will update the
	 * listeners with the new values of the XY stage and focus device (x,y,z).
	 * 
	 * @param sl
	 *            : Listener object to be added.
	 * @see
	 */
	public static void addListener(StageListener sl) {
		_listeners.add(sl);
	}

	/**
	 * Add a listener to the Stage Mover. he stage will update the listeners
	 * with the new values.
	 * 
	 * @param sl
	 *            : Listener object to be removed.
	 * @see
	 */
	public static void removeListener(StageListener sl) {
		_listeners.remove(sl);
	}

	/**
	 * Moves the stage on the Z-Axis.
	 * 
	 * @param position
	 *            : position (in µm)
	 * @throws Exception
	 */
	public static void moveZAbsolute(double position) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		String namez = core.getFocusDevice();
		if (namez == "")
			return;
		core.setPosition(namez, position);
	}

	/**
	 * Moves the stage on the Z-axis relative to current position.
	 * 
	 * @param mCore
	 *            : CoreSingleton.
	 * @param movement
	 *            : movement (in µm)
	 * @throws Exception
	 */
	public static void moveZRelative(double movement) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		String namez = core.getFocusDevice();
		if (namez == "")
			return;
		if (invertZ)
			core.setRelativePosition(namez, -movement);
		else
			core.setRelativePosition(namez, movement);
	}

	/**
	 * 
	 * Moves the stage on the X and Y axes relative to actual position.
	 * 
	 * @param movX
	 *            : movement on X-Axis (in µm)
	 * @param movY
	 *            : movement on Y-Axis (in µm)
	 * @throws Exception
	 */
	public static void moveXYRelative(double movX, double movY) throws Exception {
		moveXYRelative(movX, movY, false);
	}

	/**
	 * 
	 * Moves the stage on the X and Y axes relative to actual position.
	 * 
	 * @param movX
	 *            : movement on X-Axis (in µm)
	 * @param invertX
	 *            : should invert X Axis or not
	 * @param movY
	 *            : movement on Y-Axis (in µm)
	 * @param invertY
	 *            : should invert Y Axis or not
	 * @throws Exception
	 */
	public static void moveXYRelative(double movX, double movY, boolean waitStage) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		String device = core.getXYStageDevice();
		if (device == null || device == "")
			return;
		int invXModifier = invertX ? -1 : 1;
		int invYModifier = invertY ? -1 : 1;
		if (switchXY)
			core.setRelativeXYPosition(device, movY * invYModifier, movX * invXModifier);
		else
			core.setRelativeXYPosition(device, movX * invXModifier, movY * invYModifier);
		if (waitStage) {
			while (movX > 10 && movY > 10 && isXYStageMoving(100, 1)) {
				Thread.yield();
			}
		}
	}

	/**
	 * Move the stage on the X and Y axes to the absolute position given by posX
	 * and posY. <br/>
	 * <b>Warning:</b> this method will not wait for the stage to be in
	 * position.
	 * 
	 * @param posX
	 *            : x position wanted
	 * @param posY
	 *            : y position wanted
	 * @throws Exception
	 */
	public static void moveXYAbsolute(double posX, double posY) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		String device = core.getXYStageDevice();
		if (device == null || device == "")
			return;
		core.setXYPosition(device, posX, posY);
	}

	/**
	 * Wait for the focus device. The stage will move only if the device is not
	 * busy.
	 * 
	 * @param mCore
	 *            : CoreSingleton
	 * @throws Exception
	 */
	public static void waitForFocusDevice() throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		core.waitForDevice(core.getFocusDevice());
	}

	/**
	 * Wait for the XY Stage. This method is based on Micro-Manager's
	 * {@link CMMCore#waitForDevice(String)} method, so it will be the subject
	 * of all the drawbacks relative to the use of drivers.
	 * 
	 * @throws Exception
	 */
	public static void waitForXYStage() throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		core.waitForDevice(core.getXYStageDevice());
	}

	/**
	 * 
	 * @return Returns if focus device is busy or not.
	 * @throws Exception
	 */
	public static boolean isFocusDeviceBusy() throws Exception {
		if (!MicroscopeCore.isReady())
			return true;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return true;
		return core.deviceBusy(core.getFocusDevice());
	}

	/**
	 * This method will return if the XY Stage is Busy. <br/>
	 * <br/>
	 * <b>Warning:</b> It is based on {@link CMMCore#deviceBusy(String)}, which
	 * highly depends on the hardware you are using and your configuration file. <br/>
	 * Indeed, some adapters can send a simple timeout instead of a real value.
	 * 
	 * @return Returns if XY Stage is busy or not.
	 * @throws Exception
	 *             : An exception is raised if the XY stage was not found or
	 * 
	 */
	public static boolean isXYStageBusy() throws Exception {
		if (!MicroscopeCore.isReady())
			return true;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return true;
		return core.deviceBusy(core.getXYStageDevice());
	}

	/**
	 * Stops the XY Stage Movement.
	 * 
	 * @throws Exception
	 */
	public static void stopXYStage() throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		core.stop(core.getXYStageDevice());
	}

	/**
	 * This method will move the stage to the ROI wanted in the Sequence. <b>Be
	 * careful, this method should not be used if the pixelSize configuration is
	 * not accurate.</b> <br/>
	 * The stage will move only if the device is not busy.
	 * 
	 * @param s
	 *            : Sequence with the ROIs wanted to focus on. Can be the Live
	 *            Video.
	 * @param ROIidx
	 *            : Index of the ROI user want to focus on.
	 * @param invertX
	 *            : Invert X-Axis or not.
	 * @param invertY
	 *            : Invert Y-Axis or not.
	 * @throws Exception
	 */
	public static void moveStageToROI(Sequence s, ROI2D roi) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		if (isXYStageBusy())
			return;
		if (roi == null)
			return;
		double pxsize = core.getPixelSizeUm();
		double vectx = roi.getBounds().getCenterX() - s.getBounds().getCenterX();

		// Y coordinates are inverted in the sequence
		double vecty = -(roi.getBounds().getCenterY() - s.getBounds().getCenterY());
		StageMover.moveXYRelative(vectx * pxsize, vecty * pxsize);
	}

	/**
	 * Move the stage to a specific point <b>in the sequence</b>. The movement
	 * will be relative to the actual position of the stage, and relative to the
	 * center in the sequence.
	 * 
	 * @param s
	 *            : Sequence used to calculate the center and the movement to
	 *            the coordinates given.
	 * @param x
	 *            : x value of the point we want to go to.
	 * @param y
	 *            : y value of the point we want to go to.
	 * @param invertX
	 *            : should x-axis be inverted ?
	 * @param invertY
	 *            : should y-axis be inverted ?
	 * @throws Exception
	 */
	public static void moveToPoint(Sequence s, double x, double y) throws Exception {
		if (!MicroscopeCore.isReady())
			return;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core == null)
			return;
		if (isXYStageBusy())
			return;
		double pxsize = core.getPixelSizeUm();
		double vectx = x - s.getBounds().getCenterX();
		double vecty = y - s.getBounds().getCenterY(); // Y coordinates are
														// inverted in the
														// sequence
		StageMover.moveXYRelative(vectx * pxsize, vecty * pxsize);
	}

	/**
	 * This method returns the coordinates x and y of the current stage device
	 * and z coordinate of focus device. If there was an error with any
	 * coordinate, the value of the coordinate will be equal to
	 * <i>Double.NaN</i>.
	 * 
	 * @return Returns a table containing the coordinates x,y and z of the stage
	 *         respectively.
	 */
	public static double[] getXYZ() throws Exception {
		double[] toReturn = { Double.NaN, Double.NaN, Double.NaN };
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core != null) {
			String nameXY = core.getXYStageDevice();
			if (nameXY != null && nameXY != "") {
				toReturn[0] = core.getXPosition(nameXY);
				toReturn[1] = core.getYPosition(nameXY);
			}

			String nameZ = core.getFocusDevice();
			if (nameZ != null && nameZ != "") {
				toReturn[2] = core.getPosition(nameZ);
			}
		}
		return toReturn;
	}

	/**
	 * This method returns the coordinates x of the current stage device If
	 * there was an error, the value of the coordinate will be equal to
	 * <i>Double.NaN</i>.
	 * 
	 * @return Returns the coordinates x of the current stage device.
	 */
	public static double getX() throws Exception {
		double toReturn = Double.NaN;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core != null) {
			String nameXY = core.getXYStageDevice();
			if (nameXY != null && nameXY != "") {
				toReturn = core.getXPosition(nameXY);
			}
		}
		return toReturn;
	}

	/**
	 * This method returns the coordinates y of the current stage device If
	 * there was an error, the value of the coordinate will be equal to
	 * <i>Double.NaN</i>.
	 * 
	 * @return Returns the coordinates y of the current stage device.
	 */
	public static double getY() throws Exception {
		double toReturn = Double.NaN;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core != null) {
			String nameXY = core.getXYStageDevice();
			if (nameXY != null && nameXY != "") {
				toReturn = core.getYPosition(nameXY);
			}
		}
		return toReturn;
	}

	/**
	 * This method returns the coordinates z of the current focus device If
	 * there was an error, the value of the coordinate will be equal to
	 * <i>Double.NaN</i>.
	 * 
	 * @return Returns the coordinates z of the current focus device.
	 */
	public static double getZ() throws Exception {
		double toReturn = Double.NaN;
		MicroscopeCore core = MicroscopeCore.getCore();
		if (core != null) {
			String nameZ = core.getFocusDevice();
			if (nameZ != null && nameZ != "") {
				toReturn = core.getPosition(nameZ);
			}
		}
		return toReturn;
	}

	/**
	 * This method verifies during the measure time if there was a movement
	 * greater than the threshold.<br/>
	 * <br/>
	 * During measureTime, as many as possible values of x and y are captured.
	 * When measureTime is over, the highest difference in x and y is compared
	 * to the thresholdMovement value.
	 * 
	 * @param measureTime
	 *            : value in milliseconds to wait for
	 * @param thresholdMovement
	 *            : value to exceed for a movement to be noticed (unit depending
	 *            on the stage, but should be in µm)
	 * @return Returns true if stage is moving.
	 */
	public static boolean isXYStageMoving(long measureTime, double thresholdMovement) {
		double firstX = Double.NaN;
		double firstY = Double.NaN;
		double minX, minY;
		double maxX, maxY;
		// Up to 10 successive errors in a row.
		for (int i = 0; i < 10; ++i) {
			try {
				firstX = getX();
				firstY = getY();
				break;
			} catch (Exception e) {
			}
		}
		if (firstX == Double.NaN || firstY == Double.NaN) {
			System.out.println("isXYStageMoving: Stage error");
			return false;
		}
		// beginning of the verification
		minX = maxX = firstX;
		minY = maxY = firstY;
		long workUntil = System.nanoTime() + measureTime * 1000;
		while (System.nanoTime() < workUntil) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double x = 0;
			double y = 0;
			// Up to 10 successive errors in a row.
			for (int i = 0; i < 10; ++i) {
				try {
					x = getX();
					y = getY();
					break;
				} catch (Exception e) {
				}
			}
			if (x < minX)
				minX = x;
			else if (x > maxX)
				maxX = x;
			if (y < minY)
				minY = y;
			else if (y > maxY)
				minY = y;
		}
		if (maxX - minX >= thresholdMovement || maxY - minY >= thresholdMovement)
			return true;
		else
			return false;
	}

	/**
	 * This method verifies during the measure time if there was a movement
	 * greater than the threshold.
	 * 
	 * @param measureTime
	 *            : value in milliseconds
	 * @param thresholdMovement
	 *            : value compared to unit of stage (should be in µm)
	 */
	public static boolean isZDeviceMoving(long measureTime, double thresholdMovement) {
		double firstZ = Double.NaN;
		double minZ, maxZ;
		// Up to 10 successive errors in a row.
		for (int i = 0; i < 10; ++i) {
			try {
				firstZ = getZ();
				break;
			} catch (Exception e) {
			}
		}
		if (firstZ == Double.NaN) {
			System.out.println("isZDeviceMoving: device error");
			return false;
		}
		// beginning of the verification
		minZ = maxZ = firstZ;
		long workUntil = System.nanoTime() + measureTime * 1000;
		while (System.nanoTime() < workUntil) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			double z = 0;
			// Up to 10 successive errors in a row.
			for (int i = 0; i < 10; ++i) {
				try {
					z = getX();
					break;
				} catch (Exception e) {
				}
			}
			if (z < minZ)
				minZ = z;
			else if (z > maxZ)
				maxZ = z;
		}
		if (maxZ - minZ >= thresholdMovement)
			return true;
		else
			return false;
	}

	public static void setInvertX(boolean invertX) {
		StageMover.invertX = invertX;
		prefs.putBoolean(INVERTX, invertX);
	}

	public static void setInvertY(boolean invertY) {
		StageMover.invertY = invertY;
		prefs.putBoolean(INVERTY, invertY);
	}

	public static void setInvertZ(boolean invertZ) {
		StageMover.invertZ = invertZ;
		prefs.putBoolean(INVERTZ, invertZ);
	}

	public static boolean isInvertX() {
		return invertX;
	}

	public static boolean isInvertY() {
		return invertY;
	}

	public static boolean isInvertZ() {
		return invertZ;
	}

	public static void setSwitchXY(boolean switchXY) {
		StageMover.switchXY = switchXY;
		prefs.putBoolean(SWITCHXY, switchXY);
	}

	public static boolean isSwitchXY() {
		return switchXY;
	}

	public static void onStagePositionChanged(String s, double z) {
		for (StageListener l : _listeners)
			l.onStagePositionChanged(s, z);
	}

	public static void onStagePositionChangedRelative(String s, double z) {
		for (StageListener l : _listeners)
			l.onStagePositionChangedRelative(s, z);
	}

	public static void onXYStagePositionChanged(String s, double d, double d1) {
		for (StageListener l : _listeners)
			l.onXYStagePositionChanged(s, d, d1);
	};

	public static void onXYStagePositionChangedRelative(String s, double d, double d1) {
		for (StageListener l : _listeners)
			l.onXYStagePositionChangedRelative(s, d, d1);
	}
}