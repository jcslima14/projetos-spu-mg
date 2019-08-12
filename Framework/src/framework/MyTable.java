package framework;
import java.util.List;

import javax.swing.JTable;

@SuppressWarnings("serial")
public class MyTable extends JTable {
	boolean columnsResized = false;

	public MyTable() {
		this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		this.setAutoscrolls(true);
	}

	public void resizeColumns(List<MyTableColumn> columns, boolean onlyFirstTime) {
		// if (onlyFirstTime && columnsResized) return;
		for (int i = 0; i < columns.size(); i++) {
			MyTableColumn column = columns.get(i);
			this.getColumnModel().getColumn(i).setPreferredWidth(column.getWidth());
			this.getColumnModel().getColumn(i).setResizable(column.isResizable());
			this.getColumnModel().getColumn(i).setCellRenderer(column.getCellRenderer());
			if (!column.isResizable()) {
				this.getColumnModel().getColumn(i).setMaxWidth(getWidth());
			}
		}
		columnsResized = true;
	}
}
