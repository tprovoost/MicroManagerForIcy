package plugins.tprovoost.Microscopy.MicroManagerForIcy.Tools.JTablePack;

import javax.swing.table.AbstractTableModel;

public class JTableEvolvedModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8162458350601913411L;
	private Object [][] data;
	private String[] columnNames;
	
	public JTableEvolvedModel(String[] columnNames, Object [][] data) {
		this.columnNames = columnNames;
		this.data = data;
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.length;
	}
	
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	@Override
	public Class<?> getColumnClass(int col) {
		return getValueAt(0, col).getClass();
	}
	
	@Override
	public boolean isCellEditable(int row, int col) {
		if (col < 1)
			return false;
		return true;
	}

	@Override
	public Object getValueAt(int row, int col) {
        return data[row][col];
    }
	
	@Override
	public void setValueAt(Object value, int row, int col) {
		 data[row][col] = value;
         fireTableCellUpdated(row, col);
	}
}
