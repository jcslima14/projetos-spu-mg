package framework.models;
public class ComboBoxItem {
	private Integer intId;
	
	private String stringId;
	
	private String caption;

	public ComboBoxItem(Integer intId, String stringId, String caption) {
		this.intId = intId;
		this.stringId = stringId;
		this.caption = caption;
	}
	
	public Integer getIntId() {
		return intId;
	}

	public void setIntId(Integer intId) {
		this.intId = intId;
	}

	public String getStringId() {
		return stringId;
	}

	public void setStringId(String stringId) {
		this.stringId = stringId;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

    public String toString() {
        return this.caption;
    }
}
