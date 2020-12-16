package views.robo;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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

import org.openqa.selenium.WebElement;

import framework.MyException;
import framework.services.SapiensService;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
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
	private JTextArea logArea = new JTextArea(30, 100);
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
							recepcionarProcessosSapiens(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), navegador);
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
	
	private void recepcionarProcessosSapiens(JTextArea logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws Exception {
		String pastaDeDownload = MyUtils.entidade(despachoServico.obterParametro(Parametro.PASTA_DOWNLOAD_SAPIENS, null)).getConteudo();
		if (!MyUtils.arquivoExiste(pastaDeDownload)) {
			JOptionPane.showMessageDialog(null, "A pasta para download dos arquivos não existe: " + pastaDeDownload);
			return;
		}
		boolean baixarTodoProcessoSapiens = despachoServico.obterConteudoParametro(Parametro.BAIXAR_TODO_PROCESSO_SAPIENS, "Não").trim().equalsIgnoreCase("sim");
		MyUtils.appendLogArea(logArea, "Iniciando o navegador web...");
		receberProcessoSemArquivo = despachoServico.obterConteudoParametro(Parametro.RECEBER_PROCESSO_SEM_ARQUIVO).equalsIgnoreCase("Sim");
		SapiensService sapiensService = new SapiensService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SAPIENS), exibirNavegador, pastaDeDownload);
		sapiensService.login(usuario, senha);
		sapiensService.clicarAbaOficios();

        int pagina = 0;

        while (true) {
        	WebElement tabela = sapiensService.obterTabelaProcessos();
        	List<WebElement> linhas = sapiensService.obterProcessos(tabela);

	        // obtem a lista de processos a ser lida
	        MyUtils.appendLogArea(logArea, "Página: " + ++pagina + " - Processos encontrados: " + linhas.size());

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
    				MyUtils.appendLogArea(logArea, resultadoDownload + "\n" + "O processo não foi recebido porque ocorreu um erro no download do arquivo. O processo será avaliado novamente na próxima recepção de processos do Sapiens");
    			}
	        }

	        if (!sapiensService.proximaPaginaProcessos()) {
	        	break;
	        }
        }

		MyUtils.appendLogArea(logArea, "Fim do processamento...");
		sapiensService.fechaNavegador();
	}
	
	private boolean processoJaRecebido(JTextArea logArea, String numeroProcesso, String dataHoraMovimentacao) throws Exception {
		List<SolicitacaoEnvio> processos = despachoServico.obterSolicitacaoEnvio(null, null, Origem.SAPIENS, numeroProcesso, MyUtils.formatarData(MyUtils.obterData(dataHoraMovimentacao, "dd-MM-yyyy HH:mm"), "yyyy-MM-dd HH:mm:ss"), null, false);
		if (processos == null || processos.isEmpty()) {
			MyUtils.appendLogArea(logArea, "Processo ainda não recebido...");
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
