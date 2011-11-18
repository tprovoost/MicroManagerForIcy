package plugins.tprovoost.Microscopy.MicroManagerForIcy.ConfigWrapper;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.micromanager.utils.MMPropertyTableModel;
import org.micromanager.utils.PropertyItem;
import org.micromanager.utils.SliderPanel;

public class PropertyValueCellEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long serialVersionUID = 1L;
	JTextField text_ = new JTextField();
	JComboBox combo_ = new JComboBox();
	SliderPanel slider_ = new SliderPanel();
	PropertyItem item_;
	public boolean disableExcluded_;

	public PropertyValueCellEditor() {
		this(false);
	}

	public PropertyValueCellEditor(boolean disableExcluded) {
		this.disableExcluded_ = disableExcluded;

		this.combo_.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PropertyValueCellEditor.this.fireEditingStopped();
			}
		});
		this.slider_.addEditActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PropertyValueCellEditor.this.fireEditingStopped();
			}
		});
		this.slider_.addSliderMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				PropertyValueCellEditor.this.fireEditingStopped();
			}
		});
		this.text_.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10)
					PropertyValueCellEditor.this.fireEditingStopped();
			}
		});
		this.text_.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
			}
		});
	}

	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex,
			int colIndex) {
		MMPropertyTableModel data = (MMPropertyTableModel) table.getModel();
		this.item_ = data.getPropertyItem(rowIndex);

		if ((this.item_.confInclude) || (!this.disableExcluded_)) {
			if (this.item_.allowed.length == 0) {
				if (this.item_.hasRange) {
					if (this.item_.isInteger())
						this.slider_.setLimits((int) this.item_.lowerLimit, (int) this.item_.upperLimit);
					else {
						this.slider_.setLimits(this.item_.lowerLimit, this.item_.upperLimit);
					}
					this.slider_.setText((String) value);
					return this.slider_;
				}
				this.text_.setText((String) value);
				return this.text_;
			}

			ActionListener[] l = this.combo_.getActionListeners();
			for (int i = 0; i < l.length; i++) {
				this.combo_.removeActionListener(l[i]);
			}
			this.combo_.removeAllItems();
			for (int i = 0; i < this.item_.allowed.length; i++) {
				this.combo_.addItem(this.item_.allowed[i]);
			}
			this.combo_.setSelectedItem(this.item_.value);

			this.combo_.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					PropertyValueCellEditor.this.fireEditingStopped();
				}
			});
			return this.combo_;
		}

		return null;
	}

	public Object getCellEditorValue() {
		if (this.item_.allowed.length == 0) {
			if (this.item_.hasRange) {
				return this.slider_.getText();
			}
			return this.text_.getText();
		}

		return this.combo_.getSelectedItem();
	}
}