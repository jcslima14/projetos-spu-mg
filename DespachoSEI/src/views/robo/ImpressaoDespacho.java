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

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.enums.NivelMensagem;
import framework.exceptions.MyException;
import framework.services.SEIService;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import model.Origem;
import model.Parametro;
import model.SolicitacaoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class ImpressaoDespacho extends JInternalFrame {

	private EntityManager conexao;
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JTextField txtUsuario = new JTextField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
	private JLabel lblUsuario = new JLabel("Usu�rio:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JComboBox<String> cbbNavegador = new JComboBox<String>();
	private JLabel lblNavegador = new JLabel("Navegador:") {{ setLabelFor(cbbNavegador); }};
	private JButton btnProcessar = MyUtils.obterBotao("Processar", "/icons/011-settings-1.png", SwingConstants.LEFT, 10);
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};
	private DespachoServico despachoServico;

	public ImpressaoDespacho(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		cbbNavegador.addItem("Chrome");
		cbbNavegador.addItem("Firefox");
		cbbNavegador.setSelectedItem(despachoServico.obterConteudoParametro(Parametro.DEFAULT_BROWSER, "Firefox"));

		despachoServico.preencherOpcoesAssinante(cbbAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Todos)")); }}, false, null);

		setLayout(new GridBagLayout());
		add(lblAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(cbbAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 1; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; }});
		add(lblUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(cbbNavegador, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(btnProcessar, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 4; gridx = 0; anchor = GridBagConstraints.LINE_START; }});
		add(areaDeRolagem, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 5; gridx = 0; anchor = GridBagConstraints.FIRST_LINE_START; gridwidth = 2; fill = GridBagConstraints.BOTH; weightx = 1; weighty = 1; }});

		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				imprimirRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), MyUtils.idItemSelecionado(cbbAssinante), cbbNavegador.getSelectedItem().toString());;
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void imprimirRespostaSEI(String usuario, String senha, Integer assinanteId, String navegador) throws RuntimeException {
		try {
	        String pastaRespostasImpressas = despachoServico.obterConteudoParametro(Parametro.PASTA_DESPACHOS_SALVOS);
	        String msgValidacao = validarPastas(pastaRespostasImpressas);
	        if (!msgValidacao.equals("")) {
	        	JOptionPane.showMessageDialog(null, msgValidacao);
	        	return;
	        }
	
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
			
			SEIService seiServico = new SEIService(navegador, despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI), true, pastaRespostasImpressas);
			seiServico.login(usuario, senha, despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));
			seiServico.selecionarUnidadePadrao(despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));
	
			Map<String, List<SolicitacaoResposta>> respostasAImprimir = obterRespostasAProcessar(1, assinanteId);
			for (String numeroProcessoSEI : respostasAImprimir.keySet()) {
				// pesquisa o n�mero do processo
				seiServico.pesquisarProcesso(numeroProcessoSEI);
				seiServico.acessarPaginaImpressaoDocumentos();
	
				for (SolicitacaoResposta respostaAImprimir : respostasAImprimir.get(numeroProcessoSEI)) {
					String numeroProcesso = respostaAImprimir.getSolicitacao().getNumeroProcesso();
					String nomeArquivo;
					if (respostaAImprimir.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) && !respostaAImprimir.getSolicitacao().getChaveBusca().equals("")) {
						nomeArquivo = respostaAImprimir.getSolicitacao().getChaveBusca() + "-" + respostaAImprimir.getNumeroDocumentoSEI();
					} else {
						nomeArquivo = numeroProcesso;
					}
					String numeroDocumentoSEI = respostaAImprimir.getNumeroDocumentoSEI();
	
					MyUtils.appendLogArea(logArea, "Processo: " + numeroProcesso + " - N� Documento SEI: " + numeroDocumentoSEI);
	
					String pastaDestino = pastaRespostasImpressas + File.separator + respostaAImprimir.getSolicitacao().getOrigem().getDescricao();
					try {
						seiServico.imprimirDocumento(numeroProcesso, numeroProcessoSEI, numeroDocumentoSEI, respostaAImprimir.getTipoResposta().getQuantidadeAssinaturas(), pastaRespostasImpressas, pastaDestino, nomeArquivo);
					} catch (MyException e) {
						MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ALERTA);
						continue;
					}
	
					// atualiza o indicativo de que o documento foi impresso
					atualizarRespostaImpressa(respostaAImprimir, pastaDestino + File.separator + nomeArquivo + ".pdf");
				} // fim do loop de leitura das respostas de cada processo
			} // fim do loop de diferentes processos com documentos a serem impressos
	
			// in�cio da retirada dos documentos do bloco de assinatura
			MyUtils.appendLogArea(logArea, "Preparando para retirar os documentos dos blocos de assinatura...", NivelMensagem.DESTAQUE_NEGRITO_ITALICO);
	
			Map<String, List<SolicitacaoResposta>> blocosDeAssinatura = obterRespostasAProcessar(2, assinanteId);
			for (String blocoAssinatura : blocosDeAssinatura.keySet()) {
				List<SolicitacaoResposta> respostasRetiradas = new ArrayList<SolicitacaoResposta>();
				MyUtils.appendLogArea(logArea, "Preparando para retirar "  + blocosDeAssinatura.get(blocoAssinatura).size() + " documentos do bloco de assinatura " + blocoAssinatura, NivelMensagem.DESTAQUE_ITALICO);
				
				try {
					seiServico.acessarBlocoAssinatura(blocoAssinatura);
				} catch (MyException e) {
					MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ERRO);
					continue;
				}
				
				for (SolicitacaoResposta respostaARetirar : blocosDeAssinatura.get(blocoAssinatura)) {
					try {
						MyUtils.appendLogArea(logArea, "Marcando para retirada o documento " + respostaARetirar.getNumeroDocumentoSEI());
						seiServico.marcarDocumentoParaRetiradaBlocoAssinatura(respostaARetirar.getNumeroDocumentoSEI());
					} catch (MyException e) {
						MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ALERTA);
						continue;
					}
	
					respostasRetiradas.add(respostaARetirar);
				}
	
				if (respostasRetiradas.size() > 0) {
					MyUtils.appendLogArea(logArea, "Retirando os documentos marcados...", NivelMensagem.DESTAQUE_ITALICO);
					seiServico.confirmarRetiradaDocumentosBlocoAssinatura();
	
					MyUtils.appendLogArea(logArea, "Atualizando a situa��o dos documentos retirados...", NivelMensagem.DESTAQUE_ITALICO);
					for (SolicitacaoResposta respostaRetirada : respostasRetiradas) {
						atualizarRespostaRetiradaBlocoAssinatura(respostaRetirada);
					}
				}
			}
			
			MyUtils.appendLogArea(logArea, "Fim do Processamento...", NivelMensagem.OK);
	
	        seiServico.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void atualizarRespostaImpressa(SolicitacaoResposta resposta, String nomeArquivo) throws Exception {
		if (MyUtils.arquivoExiste(nomeArquivo)) {
			StringBuilder sql = new StringBuilder("");
			sql.append("update solicitacaoresposta "
					 + "   set respostaimpressa = true "
					 + (MyUtils.isPostgreSQL(conexao)
							 ? "	 , datahoraimpressao = now() "
							 : "	 , datahoraimpressao = datetime('now', 'localtime') "
					)
					 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());
	
			JPAUtils.executeUpdate(conexao, sql.toString());
		} else {
			throw new Exception("O arquivo " + nomeArquivo + " n�o foi gerado corretamente.");
		}
	}

	private void atualizarRespostaRetiradaBlocoAssinatura(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacaoresposta "
				 + "   set respostanoblocoassinatura = false "
				 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	// m�todo para obter documentos: tipos de filtro: 1 - respostas n�o impressas; 2 - documentos de resposta a serem retirados do bloco de assinatura
	private Map<String, List<SolicitacaoResposta>> obterRespostasAProcessar(int tipoFiltro, Integer assinanteId) throws Exception {
		Map<String, List<SolicitacaoResposta>> retorno = new LinkedHashMap<String, List<SolicitacaoResposta>>();
		Boolean respostaImpressa = null;
		Boolean respostaNoBlocoAssinatura = null;
		boolean pendentesImpressao = false;
		boolean pendentesRetiraBloco = false;
		Assinante assinante = (assinanteId.equals(0) ? null : new Assinante(assinanteId));

		if (tipoFiltro == 1) {
			respostaImpressa = false;
			pendentesImpressao = true;
		} else {
			respostaImpressa = true;
			respostaNoBlocoAssinatura = true;
			pendentesRetiraBloco = true;
		}

		List<SolicitacaoResposta> respostas = despachoServico.obterRespostasAImprimir(respostaImpressa, respostaNoBlocoAssinatura, assinante, true, pendentesImpressao, pendentesRetiraBloco);

		for (SolicitacaoResposta resposta : respostas) {
			// se for filtro 2 (processos a retirar do bloco de assinatura), certifica-se de pegar somente os registros que j� foram impressos; se n�o foram, desconsidera o registro
			String chave = (tipoFiltro == 1 ? resposta.getNumeroProcessoSEI() : resposta.getBlocoAssinatura());
			if (retorno.get(chave) == null) {
				retorno.put(chave, new ArrayList<SolicitacaoResposta>());
			}
			retorno.get(chave).add(resposta);
		}

		return retorno;
	}
	
	private String validarPastas(String pastaDownload) throws Exception {
		String retorno = "";

		if (pastaDownload.equals("") || !MyUtils.arquivoExiste(pastaDownload)) {
			retorno += "A pasta de download dos arquivos n�o est� configurada ou n�o existe: " + pastaDownload + "\n";
			retorno += "Altere o par�metro " + Parametro.PASTA_DESPACHOS_SALVOS + " com o caminho para a pasta onde os arquivos devem ser gerados antes de serem transferidos para a pasta final.";
			return retorno;
		}

		// cria as pastas das origens, caso elas n�o existam
		for (Origem origem : despachoServico.obterOrigem(null, null)) {
			String pastaOrigem = pastaDownload + File.separator + origem.getDescricao();

			if (!MyUtils.arquivoExiste(pastaOrigem)) {
				(new File(pastaOrigem)).mkdir();
			}
		}

		return retorno;
	}
}
