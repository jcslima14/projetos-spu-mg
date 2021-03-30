package views.robo;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import org.openqa.selenium.WebElement;

import framework.enums.NivelMensagem;
import framework.exceptions.MyException;
import framework.exceptions.MyValidationException;
import framework.services.SapiensService;
import framework.utils.MyUtils;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.SolicitacaoEnvio;
import model.TipoProcesso;
import services.DespachoServico;

@SuppressWarnings("serial")
public class RecepcaoProcessoSapiens extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private boolean receberProcessoSemArquivo = false;

	public RecepcaoProcessoSapiens(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.despachoServico = new DespachoServico(conexao);

		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = MyUtils.obterBotao("Processar", "resources/icons/011-settings-1.png", SwingConstants.LEFT, 10); 
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);
		JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);

		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}

		setLayout(new GridBagLayout());
		add(lblUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(cbbNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(chkExibirNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 0; anchor = GridBagConstraints.LINE_START; gridwidth = 2; }});
		add(botaoProcessar, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 4; gridx = 0; anchor = GridBagConstraints.LINE_START; }});
		add(areaDeRolagem, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 5; gridx = 0; anchor = GridBagConstraints.FIRST_LINE_START; gridwidth = 2; fill = GridBagConstraints.BOTH; weightx = 1; weighty = 1; }});

		botaoProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			
			@Override
			public void run() {
				recepcionarProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), cbbNavegador.getSelectedItem().toString());
			}
		}));
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void recepcionarProcessosSapiens(JTextPane logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws RuntimeException {
		String pastaDeDownload;
		try {
			pastaDeDownload = MyUtils.entidade(despachoServico.obterParametro(Parametro.PASTA_DOWNLOAD_SAPIENS, null)).getConteudo();
		} catch (Exception e1) {
			throw new RuntimeException(e1.getMessage());
		}
		if (!MyUtils.arquivoExiste(pastaDeDownload)) {
			throw new MyValidationException("A pasta para download dos arquivos não existe: " + pastaDeDownload);
		}

		try {
			despachoServico.salvarConteudoParametro(Parametro.DEFAULT_BROWSER, navegador);
			boolean baixarTodoProcessoSapiens = despachoServico.obterConteudoParametro(Parametro.BAIXAR_TODO_PROCESSO_SAPIENS, "Não").trim().equalsIgnoreCase("sim");
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
			receberProcessoSemArquivo = despachoServico.obterConteudoParametro(Parametro.RECEBER_PROCESSO_SEM_ARQUIVO).equalsIgnoreCase("Sim");
			SapiensService sapiensService = new SapiensService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS), exibirNavegador, pastaDeDownload);
			sapiensService.login(usuario, senha);
			sapiensService.clicarAbaOficios();
	
	        int pagina = 0;
	
	        while (true) {
	        	WebElement tabela = sapiensService.obterTabelaProcessos();
	        	List<WebElement> linhas = sapiensService.obterProcessos(tabela);
	
		        // obtem a lista de processos a ser lida
		        MyUtils.appendLogArea(logArea, "Página: " + ++pagina + " - Processos encontrados: " + linhas.size(), NivelMensagem.DESTAQUE_NEGRITO);
	
		        int nLinha = 0;
	
		        for (WebElement linha : linhas) {
		        	String resultadoDownload = "";
		        	String[] info = sapiensService.obterInformacoesProcesso(linha, tabela);
		        	String nup = info[0];
		        	String numeroProcessoJudicial = MyUtils.emptyStringIfNull(info[1]);
		        	String especie = info[2];
		        	String dataRemessa = info[3];
		        	String autor = null;
	
		        	MyUtils.appendLogArea(logArea, ++nLinha + ") NUP: " + nup + " (" + nup.replaceAll("\\D+", "") + ") - Processo Judicial: " + numeroProcessoJudicial + " (" + numeroProcessoJudicial.replaceAll("\\D+", "") + ")");
		        	nup = nup.replaceAll("\\D+", "");
		        	numeroProcessoJudicial = numeroProcessoJudicial.equals("") ? nup : numeroProcessoJudicial.replaceAll("\\D+", "");
	
		        	if (processoJaRecebido(logArea, numeroProcessoJudicial, dataRemessa)) {
		        		continue;
		        	}
	
		        	sapiensService.abrirProcesso(linha);
		        	autor = sapiensService.obterNomeAutorProcesso();
		        	try {
		        		sapiensService.baixarProcesso(linha, baixarTodoProcessoSapiens, pastaDeDownload, especie, nup, numeroProcessoJudicial, dataRemessa);
		        	} catch (MyException e) {
		        		resultadoDownload = e.getMessage();
		        	}
	        		sapiensService.fecharJanelaAtual();
	
	    			if (resultadoDownload.equals("") || receberProcessoSemArquivo) {
	    				receberProcessoSapiens(numeroProcessoJudicial, nup, autor, dataRemessa, resultadoDownload);
	    			} else {
	    				MyUtils.appendLogArea(logArea, resultadoDownload + "\n" + "O processo não foi recebido porque ocorreu um erro no download do arquivo. O processo será avaliado novamente na próxima recepção de processos do Sapiens", NivelMensagem.ERRO);
	    			}
		        }
	
		        if (!sapiensService.proximaPaginaProcessos()) {
		        	break;
		        }
	        }
	
			MyUtils.appendLogArea(logArea, "Fim do processamento...", NivelMensagem.OK);
			sapiensService.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean processoJaRecebido(JTextPane logArea, String numeroProcesso, String dataHoraMovimentacao) throws Exception {
		List<SolicitacaoEnvio> processos = despachoServico.obterSolicitacaoEnvio(null, null, Origem.SAPIENS, numeroProcesso, MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss"), null, false);
		if (processos == null || processos.isEmpty()) {
			MyUtils.appendLogArea(logArea, "Processo ainda não recebido...", NivelMensagem.DESTAQUE_ITALICO);
			return false;
		} else {
			return true;
		}
	}

	private void receberProcessoSapiens(String numeroProcesso, String chaveBusca, String autor, String dataHoraMovimentacao, String resultadoDownload) throws Exception {
		String dataHoraFormatada = MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss");
		Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, null));

		// se a solicitação já existe, atualiza o nome do autor; se não, cria uma nova solicitação que será gravada
		if (solicitacao != null) {
			solicitacao.setAutor(autor);
		} else {
			solicitacao = new Solicitacao(Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, chaveBusca, autor);
		}

		solicitacao = despachoServico.salvarSolicitacao(solicitacao);
		despachoServico.salvarSolicitacaoEnvio(new SolicitacaoEnvio(null, solicitacao, dataHoraFormatada, resultadoDownload, false, null));
	}
}
