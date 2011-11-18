package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.micromanager.utils.SliderPanel;
import org.micromanager.utils.SortFunctionObjects;
import org.micromanager.utils.StateItem;

// Referenced classes of package org.micromanager.utils:
//            SliderPanel, StateItem, SortFunctionObjects

public class StatePresetCellEditor extends AbstractCellEditor implements TableCellEditor {

	private static final long serialVersionUID = 1L;
	JTextField text_;
	JComboBox combo_;
	StateItem item_;
	SliderPanel slider_;
	
	public StatePresetCellEditor() {
		text_ = new JTextField();
		combo_ = new JComboBox();
		slider_ = new SliderPanel();
		slider_.addEditActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
		slider_.addSliderMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				fireEditingStopped();
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {
		if (!isSelected);
		ConfigGroupPad.StateTableData data = (ConfigGroupPad.StateTableData) table.getModel();
		item_ = data.getPropertyItem(rowIndex);
		if (item_.allowed.length == 0) {
			text_.setText((String) value);
			return text_;
		}
		if (item_.allowed.length == 1 && item_.singleProp) {
			if (item_.hasLimits) {
				if (item_.isInteger())
					slider_.setLimits((int) item_.lowerLimit, (int) item_.upperLimit);
				else
					slider_.setLimits(item_.lowerLimit, item_.upperLimit);
				slider_.setText((String) value);
				return slider_;
			}
			if (item_.singlePropAllowed != null && item_.singlePropAllowed.length > 0) {
				setComboBox(item_.allowed);
				return combo_;
			} else {
				text_.setText((String) value);
				return text_;
			}
		}
		if (1 < item_.allowed.length) {
			boolean allNumeric2 = true;
			int k = 0;
			do {
				if (k >= item_.allowed.length)
					break;
				if (!Character.isDigit(item_.allowed[k].charAt(0))) {
					allNumeric2 = false;
					break;
				}
				k++;
			} while (true);
			if (allNumeric2)
				Arrays.sort(item_.allowed, new SortFunctionObjects.NumericPrefixStringComp());
			else
				Arrays.sort(item_.allowed);
		}
		setComboBox(item_.allowed);
		return combo_;
	}

	private void setComboBox(String allowed[]) {
		ActionListener l[] = combo_.getActionListeners();
		for (int i = 0; i < l.length; i++)
			combo_.removeActionListener(l[i]);

		combo_.removeAllItems();
		for (int i = 0; i < allowed.length; i++)
			combo_.addItem(allowed[i]);

		combo_.removeAllItems();
		for (int i = 0; i < allowed.length; i++)
			combo_.addItem(allowed[i]);

		combo_.setSelectedItem(item_.config);
		combo_.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}

	public Object getCellEditorValue() {
		if (item_.allowed.length == 1) {
			if (item_.singleProp && item_.hasLimits)
				return slider_.getText();
			if (item_.singlePropAllowed != null && item_.singlePropAllowed.length == 0)
				return text_.getText();
			else
				return combo_.getSelectedItem();
		} else {
			return combo_.getSelectedItem();
		}
	}
}