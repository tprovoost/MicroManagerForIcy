package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import ij.gui.ImageWindow;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.geom.Point2D.Double;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

import org.json.JSONObject;
import org.micromanager.AcqControlDlg;
import org.micromanager.PositionListDlg;
import org.micromanager.acquisition.MMAcquisition;
import org.micromanager.api.AcquisitionEngine;
import org.micromanager.api.Autofocus;
import org.micromanager.api.ImageCache;
import org.micromanager.api.MMListenerInterface;
import org.micromanager.api.ScriptInterface;
import org.micromanager.navigation.PositionList;
import org.micromanager.utils.AutofocusManager;
import org.micromanager.utils.ContrastSettings;
import org.micromanager.utils.MMScriptException;

import plugins.tprovoost.Microscopy.MicroManagerForIcy.MMMainFrame;

/**
 * Avoid the implementation of a huge number of useless methods.
 * @author thomasprovoost
 *
 */
public class FakeScriptInterfacer implements ScriptInterface {

	MMMainFrame mainGui;
	
	public FakeScriptInterfacer(MMMainFrame mainGui) {
		this.mainGui = mainGui;
	}
	
	@Override
	public void sleep(long l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void message(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearMessageWindow() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void refreshGUI() {
		// TODO Auto-generated method stub

	}

	@Override
	public void snapSingleImage() {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k, int l, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k, int l, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void openAcquisition(String s, String s1, int i, int j, int k, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public String createAcquisition(JSONObject jsonobject, boolean flag) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getUniqueAcquisitionName(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCurrentAlbum() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addToAlbum(TaggedImage taggedimage) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeSimpleAcquisition(String s, int i, int j, int k, int l, int i1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeAcquisition(String s, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void initializeAcquisition(String s, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean acquisitionExists(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAllAcquisitions() {
		// TODO Auto-generated method stub

	}

	@Override
	public MMAcquisition getCurrentAcquisition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getAcquisitionNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MMAcquisition getAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void snapAndAddImage(String s, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void snapAndAddImage(String s, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, Object obj, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l, boolean flag) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addImage(String s, TaggedImage taggedimage, int i, int j, int k, int l, boolean flag, boolean flag1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public int getAcquisitionImageWidth(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAcquisitionImageHeight(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAcquisitionImageBitDepth(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAcquisitionImageByteDepth(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAcquisitionMultiCamNumChannels(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setAcquisitionProperty(String s, String s1, String s2) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquisitionSystemState(String s, JSONObject jsonobject) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAcquisitionSummary(String s, JSONObject jsonobject) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setImageProperty(String s, int i, int j, int k, String s1, String s2) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition(int i, String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void runBurstAcquisition(int i) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadBurstAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public String runAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String runAcqusition(String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String runAcquisition(String s, String s1) throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void loadAcquisition(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPositionList(PositionList positionlist) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public PositionList getPositionList() throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setChannelColor(String s, int i, Color color) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChannelName(String s, int i, String s1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setChannelContrast(String s, int i, int j, int k) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContrastBasedOnFrame(String s, int i, int j) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAcquisitionImage5D(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeAcquisitionWindow(String s) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public Double getXYStagePosition() throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStagePosition(double d) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRelativeStagePosition(double d) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setXYStagePosition(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRelativeXYStagePosition(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getXYStageName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setXYOrigin(double d, double d1) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveConfigPresets() {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageWindow getImageWin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageWindow getSnapLiveWin() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installPlugin(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installPlugin(String s, String s1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String installAutofocusPlugin(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CMMCore getMMCore() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Autofocus getAutofocus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void showAutofocusDialog() {
		// TODO Auto-generated method stub

	}

	@Override
	public AcquisitionEngine getAcquisitionEngine() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logMessage(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showMessage(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(Exception exception, String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void logError(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(Exception exception, String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(Exception exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showError(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMMListener(MMListenerInterface mmlistenerinterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMMListener(MMListenerInterface mmlistenerinterface) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addMMBackgroundListener(Component component) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeMMBackgroundListener(Component component) {
		// TODO Auto-generated method stub

	}

	@Override
	public Color getBackgroundColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean displayImage(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean displayImage(TaggedImage taggedimage) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLiveModeOn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void enableLiveMode(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public Rectangle getROI() throws MMScriptException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setROI(Rectangle rectangle) throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public ImageCache getAcquisitionImageCache(String s) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void markCurrentPosition() {
		// TODO Auto-generated method stub

	}

	@Override
	public AcqControlDlg getAcqDlg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PositionListDlg getXYPosListDlg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAcquisitionRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean versionLessThan(String s) throws MMScriptException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void logStartupProperties() {
		// TODO Auto-generated method stub

	}

	@Override
	public void makeActive() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean getLiveMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void applyContrastSettings(ContrastSettings contrastsettings, ContrastSettings contrastsettings1) {
		// TODO Auto-generated method stub

	}

	@Override
	public ContrastSettings getContrastSettings() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean is16bit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateGUI(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean displayImageWithStatusLine(Object obj, String s) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void displayStatusLine(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public AutofocusManager getAutofocusManager() {
		return null;
	}

	@Override
	public String getBackgroundStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initializeGUI() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBurstAcquisitionRunning() throws MMScriptException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean okToAcquire() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBackgroundStyle(String s) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConfigChanged(boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void showXYPositionList() {
		mainGui.showXYPositionList();
	}

	@Override
	public void startAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startBurstAcquisition() throws MMScriptException {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopAllActivity() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean updateImage() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void openAcquisitionData(String s, boolean flag) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableRoiButtons(boolean flag) {
		// TODO Auto-generated method stub

	}

}
