package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.micromanager.utils.MMPropertyTableModel;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.SliderPanel;

public class PropertyValueCellRenderer implements TableCellRenderer {
	PropertyItem item_;
	private boolean disableExcluded_;

	public PropertyValueCellRenderer(boolean disableExcluded) {
		this.disableExcluded_ = disableExcluded;
	}

	public PropertyValueCellRenderer() {
		this(false);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int rowIndex, int colIndex) {
		MMPropertyTableModel data = (MMPropertyTableModel) table.getModel();
		this.item_ = data.getPropertyItem(rowIndex);

		JLabel lab = new JLabel();
		lab.setOpaque(true);
		lab.setHorizontalAlignment(2);
		Component comp;
		if (this.item_.hasRange) {
			SliderPanel slider = new SliderPanel();
			if (this.item_.isInteger())
				slider.setLimits((int) this.item_.lowerLimit, (int) this.item_.upperLimit);
			else {
				slider.setLimits(this.item_.lowerLimit, this.item_.upperLimit);
			}
			slider.setText((String) value);
			slider.setToolTipText(this.item_.value);
			comp = slider;
		} else {
			lab.setText(this.item_.value);
			comp = lab;
		}

		if (this.disableExcluded_) {
			comp.setEnabled(this.item_.confInclude);
		}
		if (this.item_.readOnly) {
			comp.setBackground(Color.LIGHT_GRAY);
		} else {
			comp.setBackground(Color.WHITE);
		}

		return comp;
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