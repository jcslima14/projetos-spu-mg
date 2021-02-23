package views.robo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

import framework.enums.NivelMensagem;
import framework.exceptions.MyException;
import framework.services.SEIService;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
import model.Parametro;
import model.ProcessoRestrito;
import services.DespachoServico;

@SuppressWarnings("serial")
public class RestricaoProcessoSEI extends JInternalFrame {

	private EntityManager conexao;
	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};
	private DespachoServico despachoServico;

	public RestricaoProcessoSEI(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		add(areaDeRolagem, BorderLayout.SOUTH);
		
		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				gerarRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()));
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void gerarRespostaSEI(String usuario, String senha) throws RuntimeException {
		try {
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
	
	        SEIService seiServico = new SEIService("chrome", despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));
	        seiServico.login(usuario, senha, despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));
	        seiServico.selecionarUnidadePadrao(despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));
	
			List<ProcessoRestrito> processosARestringir = obterProcessosARestringir();
	
			for (ProcessoRestrito processoARestringir : processosARestringir) {
				String processoSEI = processoARestringir.getProcessoSEI();
				MyUtils.appendLogArea(logArea, "Processo: " + processoSEI + " (" + processoARestringir.getProcessoJudicial() + ")");

				try {
					seiServico.pesquisarProcesso(processoSEI);

					if (seiServico.processoEncontrado(processoSEI)) {
						boolean processoEstaRestrito = seiServico.processoEstaRestrito();
						if (!processoEstaRestrito || processoARestringir.getProcessoReaberto() || processoARestringir.getProcessoAlterado()) {
							boolean fecharProcesso = processoARestringir.getProcessoReaberto();
							
							// se o processo está fechado, reabre-o e sinaliza que ele deve ser concluído ao final da alteração
							if (!seiServico.processoEstaAberto()) {
								seiServico.reabrirProcesso();
								processoARestringir.setProcessoReaberto(true);
								atualizarStatusProcessamento(processoARestringir);
								fecharProcesso = true;
							}

							if (!processoEstaRestrito) {
								seiServico.alterarProcessoParaRestrito();
								processoARestringir.setProcessoAlterado(true);
								atualizarStatusProcessamento(processoARestringir);
							}

							if (fecharProcesso) {
								seiServico.concluirProcesso();
							}

							processoARestringir.setProcessoAlterado(false);
							processoARestringir.setProcessoReaberto(false);
							processoARestringir.setResultadoProcessamento("Processo alterado com sucesso");
						} else {
							processoARestringir.setResultadoProcessamento("Processo já está restrito");
						}
					} else {
						processoARestringir.setResultadoProcessamento("Processo não encontrado");
					}

					MyUtils.appendLogArea(logArea, processoARestringir.getResultadoProcessamento());
					// atualiza as informações do processamento
					atualizarStatusProcessamento(processoARestringir);
				} catch (MyException e) {
					MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ERRO);
				}
			}
	
			MyUtils.appendLogArea(logArea, "Fim do Processamento...", NivelMensagem.OK);
	
			seiServico.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void atualizarStatusProcessamento(ProcessoRestrito processoRestrito) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update processorestrito "
				 + "   set resultadoprocessamento = " + (processoRestrito.getResultadoProcessamento() == null ? "null" : "'" + processoRestrito.getResultadoProcessamento() + "'")
				 + "	 , datahoraprocessamento = " + (processoRestrito.getResultadoProcessamento() == null ? "null" : "datetime('now', 'localtime') ")
				 + "     , processoreaberto = " + (processoRestrito.getProcessoReaberto() ? "true" : "false")
				 + "     , processoalterado = " + (processoRestrito.getProcessoAlterado() ? "true" : "false")
				 + " where processorestritoid = " + processoRestrito.getProcessoRestritoId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private List<ProcessoRestrito> obterProcessosARestringir() throws Exception {
		List<ProcessoRestrito> retorno = despachoServico.obterProcessoRestrito(true);
		
		return retorno;
	}
}
