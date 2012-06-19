package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools;

public interface StageListener {

	public void onStagePositionChanged(String zStage, double z);

    public void onStagePositionChangedRelative(String zStage, double z);

    public void onXYStagePositionChanged(String XYStage, double x, double y);

    public void onXYStagePositionChangedRelative(String XYStage, double x, double y);

}