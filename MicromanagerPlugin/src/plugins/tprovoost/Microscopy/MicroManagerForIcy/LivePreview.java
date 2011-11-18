package plugins.tprovoost.Microscopy.MicroManagerForIcy;

import icy.image.IcyBufferedImage;
import icy.type.DataType;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.ImageGetter;

/**
 * Live View to give a preview of the current render before launching an
 * acquisition.
 * 
 * @author Thomas Provoost
 */
public class LivePreview extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private LivePreviewThread _thread;
	private MicroscopeCore core;
	private IcyBufferedImage _video;
	private MMMainFrame main_gui = MMMainFrame.getInstance();
	private static final int width = 200;
	private static final int height = 200;

	public LivePreview() {
		setPreferredSize(new Dimension(width, height));
		core = MicroscopeCore.getCore();
		_thread = new LivePreviewThread(this);
		_video = new IcyBufferedImage(width, height, 1, DataType.USHORT);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		g2.drawImage(_video, 0, 0, width, height, null);
	}

	/**
	 * @return Returns the status of the thread.<br/>
	 *         Returns false if the thread is null.
	 */
	public boolean waiting() {
		if (_thread == null)
			return false;
		return _thread.please_wait;
	}

	/**
	 * Starts the thread.
	 */
	public void startThread() {
		if (core == null) {
			System.out.println("core null, impossible to start");
			return;
		}
		if (_thread == null) {
			_thread = new LivePreviewThread(this);
		}
		if (_thread.isAlive()) {
			return;
		}
		_thread.start();
		main_gui.continuousAcquisitionNeeded(this);
	}

	public boolean started() {
		return _thread != null;
	}

	/**
	 * Pauses the thread.
	 */
	public void pausePreview() {
		if (_thread != null) {
			// Resume the thread
			synchronized (_thread) {
				_thread.please_wait = true;
			}
			main_gui.continuousAcquisitionReleased(this);
		}
	}

	/**
	 * Resume a previously paused the thread.
	 */
	public void resumePreview() {
		if (_thread != null) {
			// Resume the thread
			synchronized (_thread) {
				_thread.please_wait = false;
				_thread.notify();
			}
			main_gui.continuousAcquisitionNeeded(this);
		}
	}

	/**
	 * Stops the thread. Therefore, the thread is null after this function is
	 * called. That means it won't resume if not started before.
	 */
	public void stopPreview() {
		if (_thread != null && _thread.isAlive()) {
			synchronized (_thread) {
				_thread.interrupt();
				while (!_thread.isInterrupted()) {
					_thread.interrupt();
				}
				try {
					_thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			main_gui.continuousAcquisitionReleased(this);
			_thread = null;
		}
	}

	class LivePreviewThread extends Thread {

		public boolean please_wait;
		LivePreview _owner;
		/** Temporary image for conversion purposes */
		IcyBufferedImage tmp_image;
		private int imwidth = 0;
		private int imheight = 0;

		public LivePreviewThread(LivePreview owner) {
			_owner = owner;
			tmp_image = new IcyBufferedImage((int) core.getImageWidth(), (int) core.getImageHeight(),
					(int) core.getNumberOfComponents(), DataType.USHORT);
		}

		@Override
		public void run() {
			super.run();
			while (true) {
				synchronized (this) {
					while (please_wait) {
						try {
							wait();
						} catch (InterruptedException e) {
							return;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				try {
					try {

					} catch (Exception e2) {
						// can happen because of threads.
						System.out.println("Device Busy.");
					}
					// if necessary, readaptation of image
					if (core.getImageWidth() != imwidth || core.getImageHeight() != imheight) {
						tmp_image = null;
						imwidth = (int) core.getImageWidth();
						imheight = (int) core.getImageHeight();
						_video = new IcyBufferedImage(imwidth, imheight, (int) core.getNumberOfComponents(),
								DataType.USHORT);
					}
					short[] table = null;
					if (core.isSequenceRunning())
						table = ImageGetter.getImageFromLiveToShort(core);
					else
						table = ImageGetter.snapImageToShort(core);
					if (table != null) {
						_video.setDataXYAsShort(0, table);
						table = null;
					}
					_owner.repaint();

					try {
						sleep(200);
					} catch (InterruptedException e) {
						throw new InterruptedException("Sleep interrupted");
					}
					yield();
				} catch (InterruptedException e1) {
					break;
				}
			}
		}

	}
}
