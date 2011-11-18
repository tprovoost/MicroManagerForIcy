package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.micromanager.utils.MMPropertyTableModel;
import org.micromanager.utils.PropertyItem;

public class PropertyNameCellRenderer implements TableCellRenderer {
	PropertyItem item_;

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int column) {
		MMPropertyTableModel data = (MMPropertyTableModel) table.getModel();
		this.item_ = data.getPropertyItem(rowIndex);
		JLabel lab = new JLabel();
		lab.setOpaque(true);
		lab.setHorizontalAlignment(2);
		lab.setText((String) value);

		if (this.item_.readOnly) {
			lab.setBackground(Color.LIGHT_GRAY);
		} else {
			lab.setBackground(Color.WHITE);
		}
		return lab;
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