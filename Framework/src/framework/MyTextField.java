package framework;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class MyTextField extends JTextField implements PropriedadesEdicao {
	private boolean inclusao = false;
	
	private boolean edicao = false;
	
	private boolean exclusao = false;

	public MyTextField() {
		super();
	}
	
	public MyTextField(String caption) {
		super(caption);
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
