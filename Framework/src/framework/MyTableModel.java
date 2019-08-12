package framework;
import java.util.Vector;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class MyTableModel extends DefaultTableModel {
	public MyTableModel(Vector<Object> columNames, Vector<Vector<Object>> data) {
		this.setDataVector(data, columNames);
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (this.getRowCount() > 0) {
			if (this.getValueAt(0, columnIndex) instanceof Boolean)
				return Boolean.class;
		}
	    return super.getColumnClass(columnIndex);
	}
	
	@Override
    public boolean isCellEditable(int row, int column) {
		if (column != 0) {
			return false;
		} else {
			return true;
		}
    }
}
