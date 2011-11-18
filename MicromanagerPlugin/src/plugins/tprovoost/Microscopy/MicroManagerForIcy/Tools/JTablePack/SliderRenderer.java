package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack;

import java.awt.Component;

import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

public class SliderRenderer extends JSlider implements TableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -67864008719282299L;

	public SliderRenderer() {
		super(SwingConstants.HORIZONTAL);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object slider, boolean isSelected, boolean hasFocus, int row, int column) {
		setValue(((JSlider)slider).getValue());
		return this;
	}
}
