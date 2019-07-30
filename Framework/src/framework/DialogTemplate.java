package framework;
import java.awt.BorderLayout;
import java.awt.Dialog;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

@SuppressWarnings("serial")
public class DialogTemplate extends JDialog {

	private JPanel pnlAreaCentral;
	private JPanel pnlBotoes = null;

	public void setPnlAreaCentral(JPanel pnlAreaCentral) {
		this.pnlAreaCentral = pnlAreaCentral;
	}

	public void setPnlBotoes(JPanel pnlBotoes) {
		this.pnlBotoes = pnlBotoes;
	}

	public DialogTemplate() {
		super();
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
	}

	public DialogTemplate(String tituloJanela) {
		super();
		this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
		setTitle(tituloJanela);
	}

	public void inicializar() {
		JScrollPane areaRolavel = new JScrollPane(pnlAreaCentral);
		areaRolavel.setVisible(true);
		add(areaRolavel, BorderLayout.CENTER);

		if (pnlBotoes != null) {
			add(pnlBotoes, BorderLayout.SOUTH);
		}
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
}
