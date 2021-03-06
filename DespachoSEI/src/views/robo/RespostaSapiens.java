package views.robo;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import framework.enums.NivelMensagem;
import framework.exceptions.MyException;
import framework.services.SapiensService;
import framework.utils.MyUtils;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.TipoProcesso;
import services.DespachoServico;

@SuppressWarnings("serial")
public class RespostaSapiens extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;
	private boolean moverRespostaNaoEncontrada = false;

	public RespostaSapiens(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);
		moverRespostaNaoEncontrada = despachoServico.obterConteudoParametro(Parametro.MOVER_RESPOSTAS_NAO_ENCONTRADAS).trim().equalsIgnoreCase("sim");
		
		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		// define os objetos da tela
		JLabel lblUsuario = new JLabel("Usu�rio:");
		JTextField txtUsuario = new JTextField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = MyUtils.obterBotao("Processar", "/icons/011-settings-1.png", SwingConstants.LEFT, 10);
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);
		JTextPane logArea = MyUtils.obterPainelNotificacoes();
		JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);

		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}

		setLayout(new GridBagLayout());
		add(lblUsuario, new GridBagConstraints() {{ insets = new Insets(15, 5, 5, 5); gridy = 0; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtUsuario, new GridBagConstraints() {{ insets = new Insets(15, 5, 5, 5); gridy = 0; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(cbbNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(chkExibirNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 0; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; gridwidth = 2; }});
		add(botaoProcessar, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 4; gridx = 0; anchor = GridBagConstraints.LINE_START; }});
		add(areaDeRolagem, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 5; gridx = 0; anchor = GridBagConstraints.FIRST_LINE_START; gridwidth = 2; fill = GridBagConstraints.BOTH; weightx = 1; weighty = 1; }});

		botaoProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				responderProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), cbbNavegador.getSelectedItem().toString());
			}
		}));
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void responderProcessosSapiens(JTextPane logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws RuntimeException {
		try {
			Origem sapiens = MyUtils.entidade(despachoServico.obterOrigem(Origem.SAPIENS_ID, null));
	        String pastaDespachosSalvos = MyUtils.emptyStringIfNull(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS) + File.separator + sapiens.getDescricao());
	
	        if (pastaDespachosSalvos.equals("") || !MyUtils.arquivoExiste(pastaDespachosSalvos)) {
	        	JOptionPane.showMessageDialog(null, "A pasta onde devem estar gravados os arquivos PDF de resposta n�o est� configurada ou n�o existe: " + pastaDespachosSalvos + ". \nConfigure a origem Sapiens (" + Origem.SAPIENS_ID + ") com o caminho para a pasta onde os arquivos PDF deve estar gravados.");
	        	return;
	        }
	
	        despachoServico.salvarConteudoParametro(Parametro.DEFAULT_BROWSER, navegador);
	
	        MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
	        SapiensService sapiensService = new SapiensService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS), exibirNavegador);
	
	        sapiensService.login(usuario, senha);
	        Map<String, List<Object[]>> mapaArquivos = separarArquivosPorTipoFiltro(MyUtils.obterArquivos(pastaDespachosSalvos));
	        
	        // inicia o loop para leitura dos arquivos do diret�rio
	        for (String tipoFiltro : mapaArquivos.keySet()) {
	        	List<Object[]> listaArquivos = mapaArquivos.get(tipoFiltro);
	
	        	sapiensService.clicarAbaOficios();
	
	        	for (Object[] objArquivo : listaArquivos) {
	        		sapiensService.esperarCarregamento(1000, 5, 1, "//div[text() = 'Carregando...']");
	
	        		String chaveBusca = objArquivo[0].toString();
	        		File arquivo = (File) objArquivo[1];
		        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
		        	boolean encontrado = false;
	
		        	MyUtils.appendLogArea(logArea, "N� do Processo: " + numeroProcesso + " - Arquivo: " + arquivo.getAbsolutePath());
	
			        // clica no bot�o de filtro
		        	sapiensService.filtrarProcesso(tipoFiltro, chaveBusca);
		        	try {
		        		encontrado = sapiensService.responderProcesso(tipoFiltro, chaveBusca, numeroProcesso, arquivo);
		        	} catch (MyException e) {
		        		MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ERRO);
		        		continue;
		        	}
	
		        	if (!encontrado) {
						MyUtils.appendLogArea(logArea, "O processo " + numeroProcesso + " n�o foi encontrado. Pesquisa pelo " + tipoFiltro + ": " + chaveBusca, NivelMensagem.ALERTA);
		        	}
	
		        	if (encontrado || moverRespostaNaoEncontrada) {
						// mover o arquivo
		        		String subpastaDestino = encontrado ? "bkp" : "nao_encontrado";
			        	TimeUnit.MILLISECONDS.sleep(50);
				        MyUtils.criarDiretorioBackup(pastaDespachosSalvos, subpastaDestino);
				        String nomeArquivoBkp = pastaDespachosSalvos + File.separator + subpastaDestino + File.separator + arquivo.getName();
	
				        MyUtils.renomearArquivo(arquivo.getAbsolutePath(), nomeArquivoBkp, 30, true);
		        	}
		        }
	
	        	// ao terminar o tipo de filtro, dar um refresh na p�gina para limpar os filtros e reiniciar o processo para o segundo tipo de filtro
	        	sapiensService.atualizarPagina();
	        }
			
	        MyUtils.appendLogArea(logArea, "Fim do processamento...", NivelMensagem.ERRO);
	    	sapiensService.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private Map<String, List<Object[]>> separarArquivosPorTipoFiltro(List<File> arquivos) throws Exception {
		Map<String, List<Object[]>> retorno = new LinkedHashMap<String, List<Object[]>>();

		for (File arquivo : arquivos) {
        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
        	String chaveBusca = numeroProcesso;

        	// tenta obter o n�mero da chave de busca para o n�mero do processo lido do nome do arquivo
        	Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SAPIENS, TipoProcesso.ELETRONICO, numeroProcesso, null));

        	if (solicitacao != null && !solicitacao.getChaveBusca().trim().equals("")) {
        		chaveBusca = solicitacao.getChaveBusca().trim();
        	}

	        // determina o tipo de filtro de busca
        	String textoBotaoBusca = (chaveBusca.length() != 17 ? "Processo Judicial" : "NUP");
        	
        	if (retorno.get(textoBotaoBusca) == null) retorno.put(textoBotaoBusca, new ArrayList<Object[]>());
        	retorno.get(textoBotaoBusca).add(new Object[] { chaveBusca, arquivo });
		}

		return retorno;
	}
}
