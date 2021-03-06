package views.robo;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

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
import framework.services.SPUNetService;
import framework.utils.MyUtils;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.SolicitacaoResposta;
import model.TipoProcesso;
import services.DespachoServico;

@SuppressWarnings("serial")
public class RespostaSPUNet extends JInternalFrame {

	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private DespachoServico despachoServico;

	public RespostaSPUNet(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);
		
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
				incluirDadosSPUNet(logArea, txtUsuario.getText(), new String(txtSenha.getPassword()), chkExibirNavegador.isSelected(), cbbNavegador.getSelectedItem().toString());
			}
		}));
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void incluirDadosSPUNet(JTextPane logArea, String usuario, String senha, boolean exibirNavegador, String navegador) throws RuntimeException {
		try {
			Origem spunet = MyUtils.entidade(despachoServico.obterOrigem(Origem.SPUNET_ID, null));
	        String pastaDespachosSalvos = MyUtils.emptyStringIfNull(despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS) + File.separator + spunet.getDescricao());
	        if (pastaDespachosSalvos.equals("") || !MyUtils.arquivoExiste(pastaDespachosSalvos)) {
	        	JOptionPane.showMessageDialog(null, "A pasta onde devem estar gravados os arquivos PDF de resposta n�o est� configurada ou n�o existe: " + pastaDespachosSalvos + ". \nConfigure a origem SPUNet (" + Origem.SPUNET_ID + ") com o caminho para a pasta onde os arquivos PDF deve estar gravados.");
	        	return;
	        }
	
	        despachoServico.salvarConteudoParametro(Parametro.DEFAULT_BROWSER, navegador);
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
			SPUNetService spunetService = new SPUNetService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SPUNET), exibirNavegador);
			
			spunetService.login(usuario, senha);
			spunetService.acessarPaginaTriagem();
	
	        // inicia o loop para leitura dos arquivos do diret�rio
	        for (File arquivo : MyUtils.obterArquivos(pastaDespachosSalvos)) {
		        String nomeArquivo = arquivo.getName().split("\\.")[0];
		        String[] dadosResposta = nomeArquivo.split("\\-");
	        	String numeroAtendimento = dadosResposta[0];
	        	String numeroDocumentoSEI = "0";
	        	if (dadosResposta.length > 1) numeroDocumentoSEI = dadosResposta[1];
	
		        Solicitacao solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, Origem.SPUNET, TipoProcesso.ELETRONICO, null, numeroAtendimento));
		        SolicitacaoResposta resposta = null;
		        if (solicitacao == null) {
		        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + ": n�o foi encontrada a solicita��o para o n� de atendimento " + numeroAtendimento + ". A resposta n�o poder� ser feita automaticamente", NivelMensagem.ALERTA);
		        	continue;
		        } else {
		        	// busca a resposta referente ao arquivo lido
		        	resposta = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(null, solicitacao, null, null, null, null, numeroDocumentoSEI, false, false, false));
		        }
	
		        if (resposta == null) {
		        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + ": n�o foi encontrado o n�mero do documento de resposta na base de dados. A resposta n�o poder� ser feita automaticamente", NivelMensagem.ALERTA);
		        	continue;
		        }
	
		        if (MyUtils.emptyStringIfNull(resposta.getTipoResposta().getRespostaSPUNet()).trim().equals("")) {
		        	MyUtils.appendLogArea(logArea, "Arquivo " + arquivo.getName() + "(" + solicitacao.getNumeroProcesso() + " / " + numeroAtendimento + "): o tipo de resposta n�o est� configurado para qual tipo de resposta deve ser data no SPUNet. Configure a resposta para o SPUNet e tente novamente.", NivelMensagem.ERRO);
		        	continue;
		        }
	
	        	MyUtils.appendLogArea(logArea, "N� do Processo: " + solicitacao.getNumeroProcesso() + " (N� Atendimento: " + numeroAtendimento + ") - Arquivo: " + arquivo.getAbsolutePath());
	        	
	        	try {
	        		spunetService.responderDemanda(numeroAtendimento, resposta.getTipoResposta().getRespostaSPUNet(), resposta.getTipoResposta().getComplementoSPUNet(), arquivo);
	        	} catch (MyException e) {
	        		MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ERRO);
	        		continue;
	        	}
	
		        MyUtils.criarDiretorioBackup(pastaDespachosSalvos, "bkp");
				arquivo.renameTo(new File(pastaDespachosSalvos + File.separator + "bkp" + File.separator + arquivo.getName()));
	        }
	
			MyUtils.appendLogArea(logArea, "Fim do processamento...", NivelMensagem.OK);
	        spunetService.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
