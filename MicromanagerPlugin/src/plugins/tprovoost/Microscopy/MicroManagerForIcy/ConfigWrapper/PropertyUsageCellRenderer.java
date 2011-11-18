package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.micromanager.utils.PropertyItem;

public class PropertyUsageCellRenderer implements TableCellRenderer {
	PropertyItem item_;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int column) {
		PropertyTableData data = (PropertyTableData) table.getModel();
		this.item_ = data.getPropertyItem(rowIndex);

		JCheckBox cb = new JCheckBox();
		cb.setSelected(this.item_.confInclude);

		if (this.item_.readOnly)
			cb.setEnabled(false);
		return cb;
	}

	public void validate() {
	}

	public void revalidate() {
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
	}
}
