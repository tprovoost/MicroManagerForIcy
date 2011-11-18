package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools;

import icy.gui.frame.progress.AnnounceFrame;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeCore;
import plugins.tprovoost.Microscopy.MicroManagerForIcy.MicroscopeImage;

public class ImageGetter {

	/**
	 * This variable is only used in the case an error is too often recursively
	 * repeated.
	 */
	private static int retry = 0;

	/**
	 * Use this method to snap an image. If an acquisition is running, will wait
	 * for exposure.
	 * 
	 * @param core
	 *            : reference to the actual core
	 * @return Returns a MicroscopeImage snapped picture. Returns null if an
	 *         error occurs.
	 */
	synchronized public static MicroscopeImage snapImage(MicroscopeCore core) {
		if (core == null) {
			System.out.println("Core nil");
			return null;
		}
		try {
			/* Core Function */
			if (core.getBytesPerPixel() == 1 || core.getBytesPerPixel() == 2) {
				core.snapImage();
				short[] img = Array1DUtil.arrayToShortArray(core.getImage(), false);
				int width = (int) core.getImageWidth();
				int height = (int) core.getImageHeight();
				if (width == 0 || height == 0)
					return null;
				MicroscopeImage toReturn = new MicroscopeImage(width, height, 1, DataType.USHORT);
				toReturn.setDataXYAsShort(0, img);
				return toReturn;
			} else {
				System.out.println("Dont' know how to handle images with " + core.getBytesPerPixel() + " byte pixels.");
				return null;
			}
		} catch (Exception e) {
			if (e.toString().contains("Camera image buffer read failed")) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
				}
				++retry;
				if (retry > 3) {
					retry = 0;
					return null;
				}
				return snapImage(core);
			} else if (e.toString().contains("This operation can not be executed while sequence acquisition is runnning")
					|| e.toString().contains("Camera Busy.  Stop camera activity first")) {
				try {
					core.waitForDevice(core.getCameraDevice());
					core.waitForExposure();
					core.waitForImageSynchro();
				} catch (Exception e1) {
				}
				return getImageFromLive(core);
			} else {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Use this method to only get the data of a snapped image. Uses short.
	 * 
	 * @param core
	 * @return Returns data in int. Returns null if an error occurs.
	 */
	synchronized public static short[] snapImageToShort(MicroscopeCore core) {
		if (core == null)
			return null;
		try {
			core.snapImage();
			if (core.getBytesPerPixel() == 1 || core.getBytesPerPixel() == 2) {
				return Array1DUtil.arrayToShortArray(core.getImage(), false);
			} else {
				System.out.println("Dont' know how to handle images with " + core.getBytesPerPixel() + " byte pixels.");
				return null;
			}
		} catch (Exception e) {
			if (e.toString().contains("Camera image buffer read failed")) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
				}
				++retry;
				if (retry > 3) {
					retry = 0;
					return null;
				}
				return snapImageToShort(core);
			} else if (e.toString().contains("This operation can not be executed while sequence acquisition is runnning")
					|| e.toString().contains("Camera Busy.  Stop camera activity first")) {
				try {
					core.waitForDevice(core.getCameraDevice());
					core.waitForExposure();
					core.waitForImageSynchro();
				} catch (Exception e1) {
				}
				return getImageFromLiveToShort(core);
			} else {
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Use this method to get the last image of the acquisition sequence. If
	 * sequence is running, will not wait for exposure.
	 * 
	 * @param core
	 *            : reference to actual core
	 * @return Returns a data short[] image. Returns null if an error occurs
	 *         (mostly when acquisition sequence has not yet been run).
	 */
	public static short[] getImageFromLiveToShort(MicroscopeCore core) {
		if (core == null)
			return null;
		try {
			while (core.getRemainingImageCount() == 0)
				Thread.sleep(10);
			if (core.getBytesPerPixel() == 1) {
				return Array1DUtil.arrayToShortArray(core.getLastImage(), false);
			} else if (core.getBytesPerPixel() == 2) {
				return (short[]) core.getLastImage();
			} else {
				System.out.println("Dont' know how to handle images with " + core.getBytesPerPixel() + " byte pixels.");
				return null;
			}
		} catch (Exception e) {
			if (e.toString().contains("circular buffer")) {
				++retry;
				if (retry > 3) {
					retry = 0;
					return null;
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				return getImageFromLiveToShort(core);
			} else {
				new AnnounceFrame("Error while capturing image.");
				e.printStackTrace();
				return null;
			}
		}
	}

	/**
	 * Use this method to get the last image of the acquisition sequence
	 * 
	 * @param core
	 *            : reference to actual core
	 * @return Returns a MicroscopeImage. Returns null if an error occurs
	 *         (mostly when acquisition sequence has not yet been run).
	 */
	public static MicroscopeImage getImageFromLive(MicroscopeCore core) {
		if (core == null)
			return null;
		try {
			short[] img;
			try {
				while (core.getRemainingImageCount() == 0)
					Thread.sleep(10);
				if (core.getBytesPerPixel() == 1 || core.getBytesPerPixel() == 2) {
					if (core.getBytesPerPixel() == 1)
						img = Array1DUtil.arrayToShortArray(core.getLastImage(), false);
					else
						img = (short[]) core.getLastImage();
					int width = (int) core.getImageWidth();
					int height = (int) core.getImageHeight();
					MicroscopeImage toReturn = new MicroscopeImage(width, height, 1, DataType.USHORT);
					toReturn.setDataXYAsShort(0, img);
					return toReturn;
				} else {
					System.out.println("Dont' know how to handle images with " + core.getBytesPerPixel() + " byte pixels.");
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e1) {
			new AnnounceFrame("Error while capturing image.");
			e1.printStackTrace();
			return null;
		}
	}
}
