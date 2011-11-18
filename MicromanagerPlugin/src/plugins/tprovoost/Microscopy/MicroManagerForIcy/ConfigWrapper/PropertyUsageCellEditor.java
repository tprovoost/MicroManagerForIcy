package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.micromanager.utils.PropertyItem;

public class PropertyUsageCellEditor extends AbstractCellEditor implements TableCellEditor {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JCheckBox check_ = new JCheckBox();
	PropertyItem item_;

	public PropertyUsageCellEditor() {
		this.check_.setSelected(false);
		this.check_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PropertyUsageCellEditor.this.fireEditingStopped();
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
			int colIndex) {
		PropertyTableData data = (PropertyTableData) table.getModel();
		this.item_ = data.getPropertyItem(rowIndex);
		this.check_.setSelected(this.item_.confInclude);

		return this.check_;
	}

	public Object getCellEditorValue() {
		return Boolean.valueOf(this.check_.isSelected());
	}
}