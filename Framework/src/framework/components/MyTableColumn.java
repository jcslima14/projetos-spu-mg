package framework.components;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

public class MyTableColumn {
	private String caption = "";
	private int width = 100;
	private boolean resizable = true;
	private int alignment = JLabel.LEFT;
	private DefaultTableCellRenderer cellRenderer;
	private boolean renderCheckbox = false;

	public MyTableColumn() {
	}
	
	public MyTableColumn(String caption) {
		this.caption = caption;
	}
	
	public MyTableColumn(String caption, int width) {
		this.caption = caption;
		this.width = width;
	}
	
	public MyTableColumn(String caption, int width, boolean resizable) {
		this.caption = caption;
		this.width = width;
		this.resizable = resizable;
	}
	
	public MyTableColumn(String caption, int width, boolean resizable, int alignment) {
		this.caption = caption;
		this.width = width;
		this.resizable = resizable;
		this.alignment = alignment;
	}
	
	public MyTableColumn(String caption, int width, int alignment) {
		this.caption = caption;
		this.width = width;
		this.alignment = alignment;
	}
	
	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}
	
	public boolean isRenderCheckbox() {
		return renderCheckbox;
	}

	public void setRenderCheckbox(boolean renderCheckbox) {
		this.renderCheckbox = renderCheckbox;
	}

	public DefaultTableCellRenderer getCellRenderer() {
		if (cellRenderer == null && !renderCheckbox) {
			cellRenderer = new DefaultTableCellRenderer();
			cellRenderer.setHorizontalAlignment(alignment);
		}
		return this.cellRenderer;
	}
}
