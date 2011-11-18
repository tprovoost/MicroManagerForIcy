package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class SliderEditor extends AbstractCellEditor implements TableCellEditor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4574031691985928116L;
	JSlider slider;
	
	@Override
	public Object getCellEditorValue() {
		return slider;
	}

	@Override
	public Component getTableCellEditorComponent(JTable jtable, Object obj, boolean flag, int i, int j) {
		slider = (JSlider) obj;
		return slider;
	}
	
}
