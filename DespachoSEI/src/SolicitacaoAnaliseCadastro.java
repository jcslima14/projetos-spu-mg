import java.beans.PropertyVetoException;
import java.sql.Connection;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import framework.CadastroController;

@SuppressWarnings("serial")
public class SolicitacaoAnaliseCadastro extends JInternalFrame {

	private JTabbedPane tabs = new JTabbedPane();
	
	public SolicitacaoAnaliseCadastro(String tituloJanela, Connection conexao, DespachoServico despachoServico, Solicitacao solicitacao, SolicitacaoAnaliseConsulta solicitacaoCadastro) {
		super(tituloJanela);

		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setSize(1000, 500);

		CadastroController painelSolicitacao = new SolicitacaoCadastro(despachoServico, solicitacao, solicitacaoCadastro);
		painelSolicitacao.doEditarRegistro();
		JPanel painelSolicitacaoEnvio = new SolicitacaoEnvioCadastro(conexao, despachoServico, solicitacao);
		JPanel painelSolicitacaoResposta = new SolicitacaoRespostaCadastro(conexao, despachoServico, solicitacao);
		
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
