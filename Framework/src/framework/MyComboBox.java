package framework;
import javax.swing.JComboBox;

@SuppressWarnings("serial")
public class MyComboBox extends JComboBox<ComboBoxItem> implements PropriedadesEdicao {
	private boolean inclusao = false;
	
	private boolean edicao = false;
	
	private boolean exclusao = false;

	public MyComboBox() {
		super();
	}
	
	public boolean isInclusao() {
		return inclusao;
	}
	
	public void setInclusao(boolean inclusao) {
		this.inclusao = inclusao;
	}
	
	public boolean isEdicao() {
		return edicao;
	}
	
	public void setEdicao(boolean edicao) {
		this.edicao = edicao;
	}
	
	public boolean isExclusao() {
		return exclusao;
	}
	
	public void setExclusao(boolean exclusao) {
		this.exclusao = exclusao;
	}
}
