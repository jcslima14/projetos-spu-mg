package views.robo;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

import framework.MyException;
import framework.services.SapiensService;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
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
		JLabel lblUsuario = new JLabel("Usuário:");
		JTextField txtUsuario = new JTextField(15);
		lblUsuario.setLabelFor(txtUsuario);

		JLabel lblSenha = new JLabel("Senha:");
		JPasswordField txtSenha = new JPasswordField(15);
		lblSenha.setLabelFor(txtSenha);

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar"); 
		JCheckBox chkExibirNavegador = new JCheckBox("Exibir nevagador", true);

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(lblNavegador);
		painelDados.add(cbbNavegador);
		painelDados.add(chkExibirNavegador);
		painelDados.add(new JPanel());
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
                espacoEmDisco == null ? 5 : 6, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad
		
		add(painelDados, BorderLayout.WEST);
		JTextArea logArea = new JTextArea(30, 100);
		JScrollPane areaDeRolagem = new JScrollPane(logArea);
		add(areaDeRolagem, BorderLayout.SOUTH);

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							String navegador = cbbNavegador.getSelectedItem().toString();
							despachoServico.salvarConteudoParametro(Parametro.DEFAULT_BROWSER, navegador);
							responderProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), navegador);
						} catch (Exception e) {
							MyUtils.appendLogArea(logArea, "Erro ao processar a carga: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e));
							e.printStackTrace();
						}
					}
				}).start();
			} 
		}); 
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void responderProcessosSapiens(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
		Origem sapiens = MyUtils.entidade(despachoServico.obterOrigem(Origem.SAPIENS_ID, null));
        String pastaDespachosSalvos = MyUtils.emptyStringIfNull(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS) + File.separator + sapiens.getDescricao());

        if (pastaDespachosSalvos.equals("") || !MyUtils.arquivoExiste(pastaDespachosSalvos)) {
        	JOptionPane.showMessageDialog(null, "A pasta onde devem estar gravados os arquivos PDF de resposta não está configurada ou não existe: " + pastaDespachosSalvos + ". \nConfigure a origem Sapiens (" + Origem.SAPIENS_ID + ") com o caminho para a pasta onde os arquivos PDF deve estar gravados.");
        	return;
        }

        MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
        SapiensService sapiensService = new SapiensService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS), exibirNavegador);

        sapiensService.login(usuario, senha);
        Map<String, List<Object[]>> mapaArquivos = separarArquivosPorTipoFiltro(MyUtils.obterArquivos(pastaDespachosSalvos));
        
        // inicia o loop para leitura dos arquivos do diretório
        for (String tipoFiltro : mapaArquivos.keySet()) {
        	List<Object[]> listaArquivos = mapaArquivos.get(tipoFiltro);

        	sapiensService.clicarAbaOficios();

        	for (Object[] objArquivo : listaArquivos) {
        		sapiensService.esperarCarregamento(1000, 5, 1, "//div[text() = 'Carregando...']");

        		String chaveBusca = objArquivo[0].toString();
        		File arquivo = (File) objArquivo[1];
	        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
	        	boolean encontrado = false;

	        	MyUtils.appendLogArea(logArea, "Nº do Processo: " + numeroProcesso + " - Arquivo: " + arquivo.getAbsolutePath());

		        // clica no botão de filtro
	        	sapiensService.filtrarProcesso(tipoFiltro, chaveBusca);
	        	try {
	        		encontrado = sapiensService.responderProcesso(tipoFiltro, chaveBusca, numeroProcesso, arquivo);
	        	} catch (MyException e) {
	        		MyUtils.appendLogArea(logArea, e.getMessage());
	        		continue;
	        	}

	        	if (!encontrado) {
					MyUtils.appendLogArea(logArea, "O processo " + numeroProcesso + " não foi encontrado. Pesquisa pelo " + tipoFiltro + ": " + chaveBusca);
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

        	// ao terminar o tipo de filtro, dar um refresh na página para limpar os filtros e reiniciar o processo para o segundo tipo de filtro
        	sapiensService.atualizarPagina();
        }
		
        MyUtils.appendLogArea(logArea, "Fim do processamento...");
    	sapiensService.fechaNavegador();
	}
	
	private Map<String, List<Object[]>> separarArquivosPorTipoFiltro(List<File> arquivos) throws Exception {
		Map<String, List<Object[]>> retorno = new LinkedHashMap<String, List<Object[]>>();

		for (File arquivo : arquivos) {
        	String numeroProcesso = arquivo.getName().toLowerCase().replace(".pdf", "");
        	String chaveBusca = numeroProcesso;

        	// tenta obter o número da chave de busca para o número do processo lido do nome do arquivo
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
