package views.robo;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.openqa.selenium.By;

import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.enums.NivelMensagem;
import framework.exceptions.MyException;
import framework.services.SEIService;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import model.AssinanteTipoResposta;
import model.Origem;
import model.Parametro;
import model.SolicitacaoResposta;
import model.TipoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class InclusaoDespachoSEI extends JInternalFrame {

	private EntityManager conexao;
	private JTextField txtUsuario = new JTextField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15) {{ setMinimumSize(new Dimension(192, 26)); }};
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JButton btnProcessar = MyUtils.obterBotao("Processar", "resources/icons/011-settings-1.png", SwingConstants.LEFT, 10); 
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};
	private DespachoServico despachoServico;
	private Assinante superior;

	public InclusaoDespachoSEI(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);
		despachoServico.preencherOpcoesAssinante(cbbAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Todos)")); }}, false, true);
		despachoServico.selecionarAssinantePadrao(cbbAssinante);

		setLayout(new GridBagLayout());
		add(lblUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtUsuario, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(txtSenha, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		add(lblAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		add(cbbAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 1; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; }});
		add(btnProcessar, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 0; anchor = GridBagConstraints.LINE_START; }});
		add(areaDeRolagem, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 4; gridx = 0; anchor = GridBagConstraints.FIRST_LINE_START; gridwidth = 2; fill = GridBagConstraints.BOTH; weightx = 1; weighty = 1; }});
		
		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				gerarRespostaSEI(txtUsuario.getText(), new String(txtSenha.getPassword()), MyUtils.idItemSelecionado(cbbAssinante));
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void gerarRespostaSEI(String usuario, String senha, Integer assinanteId) throws RuntimeException {
		try {
			String msgVldPastaAssinante = validarPastaProcessoIndividual(assinanteId);
	        if (!msgVldPastaAssinante.equals("")) {
	        	throw new Exception(msgVldPastaAssinante);
	        }
	        
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);
	
	        // obter os dados do superior assinante
			Iterator<Assinante> assinanteIterator = despachoServico.obterAssinante(null, null, true, true).iterator();
			
			if(!assinanteIterator.hasNext()) {
				throw new Exception("Nenhum assinante superior cadastrado.");
			}
			
			superior = assinanteIterator.next();
	
	        SEIService seiServico = new SEIService("chrome", despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));
	        seiServico.login(usuario, senha, despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));
	        seiServico.selecionarUnidadePadrao(despachoServico.obterConteudoParametro(Parametro.UNIDADE_PADRAO_SEI));
	
			Map<String, List<SolicitacaoResposta>> respostasAGerar = obterRespostasACadastrar(assinanteId);
			for (String unidadeAberturaProcesso : respostasAGerar.keySet()) {
				List<SolicitacaoResposta> respostasDaUnidade = respostasAGerar.get(unidadeAberturaProcesso);
	
				if (!unidadeAberturaProcesso.trim().equals("")) {
					seiServico.selecionarUnidadePadrao(unidadeAberturaProcesso);
				}
	
				for (SolicitacaoResposta respostaAGerar : respostasDaUnidade) {
					// processamento....
					MyUtils.appendLogArea(logArea, "Processo: " + respostaAGerar.getSolicitacao().getNumeroProcesso());
	
					// verifica se há pendências
					if (!respostaAGerar.getPendenciasParaGeracao().trim().equals("")) {
						MyUtils.appendLogArea(logArea, "A resposta possui pendências de informação e não pode ser gerada automaticamente até que sejam resolvidas: \n" + respostaAGerar.getPendenciasParaGeracao(), NivelMensagem.ALERTA);
						continue;
					}
	
					if (respostaAGerar.getTipoResposta().getGerarProcessoIndividual()) {
						List<File> anexos = obterArquivos(respostaAGerar.getAssinante().getPastaArquivoProcesso(), respostaAGerar.getSolicitacao().getNumeroProcesso(), null);
						if (MyUtils.emptyStringIfNull(respostaAGerar.getSolicitacao().getNumeroProcessoSEI()).trim().equalsIgnoreCase("")) {
							if (anexos == null || anexos.size() == 0) {
								MyUtils.appendLogArea(logArea, "Não foi possível gerar o processo individual, pois não foi encontrado nenhum arquivo referente ao processo.", NivelMensagem.ALERTA);
								continue;
							}
		
							gerarProcessoIndividual(seiServico, respostaAGerar, respostaAGerar.getAssinante().getPastaArquivoProcesso());
						}
	
						if (!respostaAGerar.getSolicitacao().getArquivosAnexados()) {
							anexarArquivosProcesso(seiServico, respostaAGerar, anexos);
						}
	
						respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getSolicitacao().getNumeroProcessoSEI());
					} else {
						if (MyUtils.emptyStringIfNull(respostaAGerar.getSolicitacao().getNumeroProcessoSEI()).equals("")) {
							respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getAssinante().getNumeroProcessoSEI());
						} else {
							respostaAGerar.setNumeroProcessoSEI(respostaAGerar.getSolicitacao().getNumeroProcessoSEI());
						}
					}
	
					try {
						seiServico.pesquisarProcesso(respostaAGerar.getNumeroProcessoSEI());
						respostaAGerar.setNumeroDocumentoSEI(seiServico.inserirDocumentoNoProcesso(respostaAGerar.getNumeroProcessoSEI(), respostaAGerar.getTipoResposta().getTipoDocumento(), respostaAGerar.getTipoResposta().getNumeroDocumentoModelo()));
						seiServico.acessarFramePorConteudo(By.xpath("//*[contains(text(), '<autor>')]"));
						seiServico.substituirMarcacaoDocumento(obterMapaSubstituicoes(respostaAGerar, superior));
						seiServico.salvarFecharDocumentoEditado();
						// incluir no bloco de assinatura
						respostaAGerar.setBlocoAssinatura(obterBlocoAssinatura(respostaAGerar.getAssinante(), respostaAGerar.getTipoResposta()));
						seiServico.incluirDocumentoBlocoAssinatura(respostaAGerar.getNumeroDocumentoSEI(), respostaAGerar.getBlocoAssinatura());
					} catch (MyException e) {
						MyUtils.appendLogArea(logArea, e.getMessage(), NivelMensagem.ERRO);
						continue;
					}
					
					// atualiza o número do documento gerado no SEI
					atualizarDocumentoGerado(respostaAGerar, superior);
				} // fim do loop de respostas a gerar por unidade
			} // fim do loop de todas as respostas a gerar
	
			MyUtils.appendLogArea(logArea, "Fim do Processamento...", NivelMensagem.OK);
	
			seiServico.fechaNavegador();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String obterBlocoAssinatura(Assinante assinante, TipoResposta tipoResposta) throws Exception {
		List<AssinanteTipoResposta> confs = despachoServico.obterAssinanteTipoResposta(null, assinante.getAssinanteId(), tipoResposta.getTipoRespostaId());
		if (confs != null && confs.size() > 0) {
			return confs.iterator().next().getBlocoAssinatura();
		} else {
			return assinante.getBlocoAssinatura();
		}
	}

	private void gerarProcessoIndividual(SEIService seiServico, SolicitacaoResposta resposta, String pastaArquivosProcessosIndividuais) throws Exception {
		MyUtils.appendLogArea(logArea, "Gerando processo individual...", NivelMensagem.DESTAQUE_ITALICO);
		String numeroProcesso = seiServico.gerarProcessoIndividual(resposta.getTipoResposta().getTipoProcesso(), resposta.getSolicitacao().getNumeroProcesso());
		MyUtils.appendLogArea(logArea, "Gerado o processo individual nº " + numeroProcesso, NivelMensagem.DESTAQUE_NEGRITO_ITALICO);

		resposta.getSolicitacao().setNumeroProcessoSEI(numeroProcesso);
		atualizarProcessoGerado(resposta);
	}
	
	public void anexarArquivosProcesso(SEIService seiServico, SolicitacaoResposta resposta, List<File> anexos) throws Exception {
		MyUtils.appendLogArea(logArea, "Anexando os arquivos ao processo...", NivelMensagem.DESTAQUE_ITALICO);
		seiServico.anexarArquivosProcesso(resposta.getSolicitacao().getNumeroProcessoSEI(), anexos);
		
		resposta.getSolicitacao().setArquivosAnexados(true);
		atualizarArquivosAnexados(resposta);
	}

	private Map<String, String> obterMapaSubstituicoes(SolicitacaoResposta resposta, Assinante superior) {
		Map<String, String> retorno = new LinkedHashMap<String, String>();
		String numeroProcesso = resposta.getSolicitacao().getNumeroProcesso();
		if (resposta.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) && resposta.getSolicitacao().getNumeroProcesso().length() == 17) {
			// reformata o número do processo para a máscara UUUU.NNNNNN/AAAA-DD
			numeroProcesso = numeroProcesso.substring(0, 5) + "." + numeroProcesso.substring(5, 11) + "/" + numeroProcesso.substring(11, 15) + "-" + numeroProcesso.substring(15); 
		}
		retorno.put("<numero_processo>", numeroProcesso);
		retorno.put("<numero_atendimento>", resposta.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SPUNET_ID) ? MyUtils.emptyStringIfNull(resposta.getSolicitacao().getChaveBusca()) : "");
		retorno.put("<autor>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getAutor()));
		retorno.put("<comarca>", (resposta.getSolicitacao().getMunicipio() == null || resposta.getSolicitacao().getMunicipio().getMunicipioComarca() == null ? "" : resposta.getSolicitacao().getMunicipio().getMunicipioComarca().getNome().toUpperCase()));
		retorno.put("<cartorio>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getCartorio()));
		String destino = resposta.getSolicitacao().getDestino().getUsarCartorio() ? resposta.getSolicitacao().getCartorio() : resposta.getSolicitacao().getDestino().getDescricao();
		retorno.put("<destino_inicial>", resposta.getSolicitacao().getDestino().getArtigo() + " " + destino);
		if (resposta.getSolicitacao().getTipoImovel().getDescricao().equalsIgnoreCase("rural") && !resposta.getSolicitacao().getEndereco().equalsIgnoreCase("")) {
			retorno.put("<tipo_imovel>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getEndereco()));
			retorno.put("<endereco>", "");
		} else {
			retorno.put("<tipo_imovel>", (resposta.getSolicitacao().getTipoImovel() == null ? "" : resposta.getSolicitacao().getTipoImovel().getDescricao().toLowerCase()));
			retorno.put("<endereco>", resposta.getSolicitacao().getEndereco() == null || resposta.getSolicitacao().getEndereco().trim().equals("") ? "" : "localizado na " + resposta.getSolicitacao().getEndereco() + ", ");
		}
		retorno.put("<municipio>", resposta.getSolicitacao().getMunicipio() == null ? "" : resposta.getSolicitacao().getMunicipio().getNome());
		retorno.put("<area>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getArea()).trim().equals("") ? "" : "com área de " + resposta.getSolicitacao().getArea() + ", ");
		retorno.put("<coordenada>", MyUtils.emptyStringIfNull(resposta.getSolicitacao().getCoordenada()).trim().equals("") ? "" : "cuja poligonal possui um dos vértices com coordenada " + resposta.getSolicitacao().getCoordenada() + ", ");
		retorno.put("<destino_final>", resposta.getSolicitacao().getDestino().getArtigo().toLowerCase() + " " + (destino.startsWith("Procuradoria") ? "Procuradoria" : destino));
		retorno.put("<assinante>", resposta.getAssinante().getNome());
		retorno.put("<assinante_cargo>", resposta.getAssinante().getCargo());
		retorno.put("<assinante_setor>", resposta.getAssinante().getSetor());
		retorno.put("<assinante_superior>", superior.getNome());
		retorno.put("<assinante_superior_cargo>", superior.getCargo());
		retorno.put("<assinante_superior_setor>", superior.getSetor());
		retorno.put("<observacao>", MyUtils.emptyStringIfNull(resposta.getObservacao().trim()));
		retorno.put("<data_hoje>", MyUtils.formatarData(new Date(), "dd 'de' MMMM 'de' yyyy").toLowerCase());
		return retorno;
	}

	private void atualizarProcessoGerado(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacao "
				 + "   set numeroprocessosei = '" + resposta.getSolicitacao().getNumeroProcessoSEI() + "' "
				 + "	 , arquivosanexados = false "
				 + " where solicitacaoid = " + resposta.getSolicitacao().getSolicitacaoId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private void atualizarArquivosAnexados(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacao "
				 + "   set arquivosanexados = true "
				 + " where solicitacaoid = " + resposta.getSolicitacao().getSolicitacaoId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private void atualizarDocumentoGerado(SolicitacaoResposta resposta, Assinante superior) throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("update solicitacaoresposta "
				 + "   set numerodocumentosei = '" + resposta.getNumeroDocumentoSEI() + "'"
				 + (MyUtils.isPostgreSQL(conexao) 
					? "	 , datahoraresposta = now() "
					: "	 , datahoraresposta = datetime('now', 'localtime') "
				)
				 + "	 , numeroprocessosei = '" + resposta.getNumeroProcessoSEI() + "' "
				 + "	 , respostaimpressa = " + (resposta.getTipoResposta().getImprimirResposta() ? "false" : "true")
				 + "	 , blocoassinatura = '" + resposta.getBlocoAssinatura() + "' "
				 + "	 , respostanoblocoassinatura = true "
				 + "     , assinanteidsuperior = " + superior.getAssinanteId()
				 + " where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());

		JPAUtils.executeUpdate(conexao, sql.toString());
	}

	private Map<String, List<SolicitacaoResposta>> obterRespostasACadastrar(Integer assinanteId) throws Exception {
		Map<String, List<SolicitacaoResposta>> retorno = new TreeMap<String, List<SolicitacaoResposta>>();
		Assinante assinante = null;
		if (assinanteId != null && assinanteId.intValue() > 0) {
			assinante = new Assinante(assinanteId, null);
		}

		List<SolicitacaoResposta> respostas = despachoServico.obterRespostasAGerar(assinante);
		
		for (SolicitacaoResposta resposta : respostas) {
			String unidadeAberturaProcesso = resposta.getTipoResposta().getUnidadeAberturaProcesso();
			if (unidadeAberturaProcesso == null) unidadeAberturaProcesso = "";
			if (retorno.get(unidadeAberturaProcesso) == null) retorno.put(unidadeAberturaProcesso, new ArrayList<SolicitacaoResposta>());
			retorno.get(unidadeAberturaProcesso).add(resposta);
		}
		
		return retorno;
	}

	private List<File> obterArquivos(String pasta, String filtroNomeArquivo, String filtroExtensao) {
		FilenameFilter filtro = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				boolean atendeNome = true;
				boolean atendeExtensao = true;
				if (filtroNomeArquivo != null && !filtroNomeArquivo.equalsIgnoreCase("")) {
					atendeNome = name.toLowerCase().contains(filtroNomeArquivo.toLowerCase());
				}
				if (filtroExtensao != null && !filtroExtensao.equalsIgnoreCase("")) {
					atendeExtensao = name.toLowerCase().endsWith(filtroExtensao.toLowerCase());
				}
				return atendeNome && atendeExtensao;
			}
		};
		File diretorio = new File(pasta);
		return Arrays.asList(diretorio.listFiles(filtro));
	}
	
	private String validarPastaProcessoIndividual(Integer assinanteId) throws Exception {
		List<TipoResposta> respostas = despachoServico.obterTipoResposta(null, null, Boolean.TRUE);
		if (respostas != null && respostas.size() > 0) {
			List<Assinante> assinantes = despachoServico.obterAssinante(assinanteId == null || assinanteId.intValue() == 0 ? null : assinanteId, null, false, true);
			for (Assinante assinante : assinantes) {
				if (assinante.getPastaArquivoProcesso().equals("") || !MyUtils.arquivoExiste(assinante.getPastaArquivoProcesso())) {
					return "A pasta de arquivos de processos individuais para o assinante " + assinante.getNome() + " não existe ou não está configurada: " + assinante.getPastaArquivoProcesso() + "\nConfigure a pasta para o assinante e tente novamente.";
				}
			}
		}
		return "";
	}
}
