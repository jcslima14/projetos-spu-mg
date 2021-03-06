package framework.components;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JButton;

import framework.models.PropriedadesEdicao;

@SuppressWarnings("serial")
public class MyButton extends JButton implements PropriedadesEdicao {
	private boolean inclusao = false;
	
	private boolean edicao = false;
	
	private boolean exclusao = false;

	public MyButton() {
		super();
	}

	public MyButton(String caption) {
		super(caption);
	}

	public MyButton(String caption, Icon icon) {
		super(caption, icon);
		this.setMargin(new Insets(5, 5, 5, 5));
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
