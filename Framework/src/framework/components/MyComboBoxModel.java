package framework.components;
import javax.swing.DefaultComboBoxModel;

import framework.models.ComboBoxItem;

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
