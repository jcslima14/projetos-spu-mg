import javax.swing.DefaultComboBoxModel;

@SuppressWarnings("serial")
public class MyComboBoxModel extends DefaultComboBoxModel<ComboBoxItem> {
	public MyComboBoxModel() {
		super();
	}
	
    public MyComboBoxModel(ComboBoxItem[] items) {
        super(items);
    }
 
    @Override
    public ComboBoxItem getSelectedItem() {
        ComboBoxItem selectedItem = (ComboBoxItem) super.getSelectedItem();
 
        return selectedItem;
    }
}
