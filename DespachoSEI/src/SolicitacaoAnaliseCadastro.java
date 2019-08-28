import java.beans.PropertyVetoException;
import java.sql.Connection;

import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;

import framework.CadastroController;

@SuppressWarnings("serial")
public class SolicitacaoAnaliseCadastro extends JInternalFrame {

	private JTabbedPane tabs = new JTabbedPane();
	
	public SolicitacaoAnaliseCadastro(Connection conexao, DespachoServico despachoServico, Solicitacao solicitacao, SolicitacaoAnaliseConsulta solicitacaoAnaliseConsulta) {
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);
		setSize(1000, 500);

		CadastroController painelSolicitacao = new SolicitacaoCadastro(conexao, despachoServico, solicitacao, solicitacaoAnaliseConsulta, this);

		tabs.addTab("Solicita��o", painelSolicitacao);
		tabs.addTab("Recep��o",  ((SolicitacaoCadastro) painelSolicitacao).getSolicitacaoEnvioCadastro());
		tabs.addTab("Resposta", ((SolicitacaoCadastro) painelSolicitacao).getSolicitacaoRespostaCadastro());

		painelSolicitacao.doEditarRegistro();

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
