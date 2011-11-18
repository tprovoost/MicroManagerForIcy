package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.gui.util.LookAndFeelUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.micromanager.utils.SliderPanel;
import org.micromanager.utils.StateItem;

//Referenced classes of package org.micromanager.utils:
//         SliderPanel, StateItem

public class StatePresetCellRenderer implements TableCellRenderer {

	public StatePresetCellRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int colIndex) {
		ConfigGroupPad.StateTableData data = (ConfigGroupPad.StateTableData) table.getModel();
		stateItem_ = data.getPropertyItem(rowIndex);
		Component comp;
		if (stateItem_.hasLimits) {
			SliderPanel slider = new SliderPanel();
			if (stateItem_.isInteger())
				slider.setLimits((int) stateItem_.lowerLimit, (int) stateItem_.upperLimit);
			else
				slider.setLimits(stateItem_.lowerLimit, stateItem_.upperLimit);
			slider.setText((String) value);
			slider.setToolTipText((String) value);
			comp = slider;
		} else {
			JLabel label = new JLabel();
			label.setOpaque(true);
			label.setFont(new Font("Arial", 0, 10));
			label.setText(stateItem_.config.toString());
			label.setToolTipText(stateItem_.descr);
			label.setHorizontalAlignment(2);
			comp = label;
		}
		Color bgColor = LookAndFeelUtil.getBackground(comp);
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), hsb);
		if (hsb[2] > 0.5f) {
			if (isSelected)
				comp.setBackground(bgColor.darker());
			else
				comp.setBackground(bgColor);
		} else {
			if (isSelected)
				comp.setBackground(bgColor.brighter());
			else
				comp.setBackground(bgColor);
		}
		return comp;
	}

	public void validate() {
	}

	public void revalidate() {
	}

	protected void firePropertyChange(String s, Object obj, Object obj1) {
	}

	public void firePropertyChange(String s, boolean flag, boolean flag1) {
	}

	StateItem stateItem_;
}