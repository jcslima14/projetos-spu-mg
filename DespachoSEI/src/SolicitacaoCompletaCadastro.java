import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import framework.CadastroController;

@SuppressWarnings("serial")
public class SolicitacaoCompletaCadastro extends JInternalFrame {

	private JTabbedPane tabs = new JTabbedPane();
	
	private DespachoServico despachoServico;
	private Solicitacao solicitacao;

	public SolicitacaoCompletaCadastro(String tituloJanela, DespachoServico despachoServico, Solicitacao solicitacao) {
		super(tituloJanela);

		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setSize(1000, 500);

		this.despachoServico = despachoServico;
		this.solicitacao = solicitacao;

		CadastroController painelSolicitacao = new SolicitacaoEntidadeCadastro(this.despachoServico, this.solicitacao);
		painelSolicitacao.doEditarRegistro();
		JPanel painelSolicitacaoEnvio = new JPanel();
		JPanel painelSolicitacaoResposta = new JPanel();
		
		tabs.addTab("Solicitação", painelSolicitacao);
		tabs.addTab("Recepção", painelSolicitacaoEnvio);
		tabs.addTab("Resposta", painelSolicitacaoResposta);

		this.add(tabs);
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			this.setMaximum(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		this.setVisible(true);
		this.show();
	}
}
