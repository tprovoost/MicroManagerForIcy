package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import icy.gui.util.LookAndFeelUtil;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;

import org.micromanager.utils.StateItem;

// Referenced classes of package org.micromanager.utils:
//            PropertyValueCellRenderer, StateItem

public class StateGroupCellRenderer extends PropertyValueCellRenderer {

	public StateGroupCellRenderer() {
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int colIndex) {
		ConfigGroupPad.StateTableData data = (ConfigGroupPad.StateTableData) table.getModel();
		stateItem_ = data.getPropertyItem(rowIndex);
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setFont(new Font("Arial", 1, 11));
		label.setText((String) value);
		label.setToolTipText(stateItem_.descr);
		label.setHorizontalAlignment(2);
		Component comp = label;
		Color c = LookAndFeelUtil.getBackground(comp);
		float[] hsb = new float[3];
		hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
		if (hsb[2] > 0.5f) {
			if (isSelected)
				comp.setBackground(c.darker());
			else
				comp.setBackground(c);
		} else {
			if (isSelected)
				comp.setBackground(c.brighter());
			else
				comp.setBackground(c);
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
