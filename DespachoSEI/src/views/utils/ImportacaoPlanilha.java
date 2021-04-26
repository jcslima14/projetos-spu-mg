package views.utils;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.enums.NivelMensagem;
import framework.utils.MyUtils;
import model.Assinante;
import model.Destino;
import model.Municipio;
import model.Origem;
import model.Parametro;
import model.Solicitacao;
import model.SolicitacaoEnvio;
import model.SolicitacaoResposta;
import model.TipoImovel;
import model.TipoProcesso;
import model.TipoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class ImportacaoPlanilha extends JInternalFrame {

	private EntityManager conexao;
	private JFileChooser filArquivo = MyUtils.obterJFileChooser("Planilhas Excel (xlsx, xls)", "xls", "xlsx");
	private JButton btnAbrirArquivo = MyUtils.obterBotao("Escolher Arquivo", "/icons/040-folder.png", SwingConstants.LEFT, null);
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JTextField txtLinhaInicial = new JTextField(10);
	private JLabel lblLinhaInicial = new JLabel("Linha inicial:", JLabel.TRAILING) {{ setLabelFor(txtLinhaInicial); }};
	private JTextField txtLinhaFinal = new JTextField(10);
	private JLabel lblLinhaFinal = new JLabel("Linha final:", JLabel.TRAILING) {{ setLabelFor(txtLinhaFinal); }};
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JButton btnProcessar = MyUtils.obterBotao("Processar", "/icons/011-settings-1.png", SwingConstants.LEFT, 10);
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};
	private DespachoServico despachoServico;

	public ImportacaoPlanilha(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		txtLinhaInicial.setMinimumSize(new Dimension(128, 26));
		txtLinhaFinal.setMinimumSize(new Dimension(128, 26));

		setLayout(new GridBagLayout());
		this.add(btnAbrirArquivo, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 0; anchor = GridBagConstraints.LINE_END; fill = GridBagConstraints.HORIZONTAL; }});
		this.add(lblNomeArquivo, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 0; gridx = 1; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; }});
		this.add(lblLinhaInicial, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		this.add(txtLinhaInicial, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 1; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		this.add(lblLinhaFinal, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		this.add(txtLinhaFinal, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 2; gridx = 1; anchor = GridBagConstraints.LINE_START; }});
		this.add(lblAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 0; anchor = GridBagConstraints.LINE_END; }});
		this.add(cbbAssinante, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 3; gridx = 1; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; weightx = 0.5; }});
		this.add(btnProcessar, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 4; gridx = 0; anchor = GridBagConstraints.LINE_START; fill = GridBagConstraints.HORIZONTAL; }});
		this.add(areaDeRolagem, new GridBagConstraints() {{ insets = new Insets(5, 5, 5, 5); gridy = 5; gridx = 0; anchor = GridBagConstraints.FIRST_LINE_START; fill = GridBagConstraints.BOTH; gridwidth = 2; weightx = 1.0; weighty = 1.0; }});

		this.conexao = conexao;
		despachoServico = new DespachoServico(this.conexao);

		despachoServico.preencherOpcoesAssinante(cbbAssinante, null, false, true);

		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				validarDadosEntrada();
				importarArquivo(lblNomeArquivo.getText(), Integer.parseInt(txtLinhaInicial.getText()), Integer.parseInt(txtLinhaFinal.getText()));;
			}
		}));

		btnAbrirArquivo.addActionListener(MyUtils.openFileDialogWindow(despachoServico.obterConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO),
				filArquivo, lblNomeArquivo, ImportacaoPlanilha.this, new Runnable() {
					@Override
					public void run() {
						if (!despachoServico.obterConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO).equals(filArquivo.getSelectedFile().getParent())) {
							despachoServico.salvarConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO, filArquivo.getSelectedFile().getParent());
						}
					}
				}));
	}

	public void validarDadosEntrada() {
		if (lblNomeArquivo.getText().equals("")) {
			throw new RuntimeException("Para iniciar o processamento é necessário selecionar um arquivo Excel para processar.");
		}
		
		try {
			int linhaInicial = Integer.parseInt(txtLinhaInicial.getText());
			int linhaFinal = Integer.parseInt(txtLinhaFinal.getText());
			if (linhaInicial > linhaFinal) {
				throw new RuntimeException("A linha inicial a ser processada deve ser menor ou igual à linha final.");
			}
		} catch (Exception ex) {
			throw new RuntimeException("Erro ao verificar as linhas de início e fim de processamento. Informe somente dígitos nestes campos.");
		}
	}
	
	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void importarArquivo(String arquivo, int linhaInicial, int linhaFinal) throws RuntimeException {
		try {
			File fileInput = new File(arquivo);
			Workbook wb = WorkbookFactory.create(fileInput);
			Sheet planilha = wb.getSheetAt(0);
			for (int l = linhaInicial - 1; l < linhaFinal; l++) {
				Row linha = planilha.getRow(l);
				String msgRetorno = "";
				TipoProcesso tipoProcesso = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(1))).trim().toLowerCase().startsWith("f") ? TipoProcesso.FISICO : TipoProcesso.ELETRONICO;
				String numeroProcessoOriginal = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(2))).trim();
				String numeroProcesso = numeroProcessoOriginal.replaceAll("\\D+", "").trim();
				String autor = MyUtils.obterValorCelula(linha.getCell(3)).trim();
				String cartorio = "";
				String endereco = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(5))).trim();
				String nomeMunicipio = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(6))).trim();
				String coordenada = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(7))).trim();
				coordenada = (coordenada.trim().length() <= 1 ? "" : coordenada);
				String area = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(8))).trim();
				area = (area.trim().length() <= 1 ? "" : area);
				String origemProcesso = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(14))).trim();
				String chaveBusca = (tipoProcesso.getTipoProcessoId().equals(TipoProcesso.FISICO_ID) || origemProcesso.equalsIgnoreCase("judicial") ? "" : MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(4))).toUpperCase().replaceAll("[^A-Z0-9]", "").trim());
				String tipoResposta = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(15))).trim();
				String observacao = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha.getCell(17))).trim();
				observacao = (observacao.trim().length() <= 1 ? "" : observacao);
				String statusAtual = "";
				NivelMensagem nivelMensagem = NivelMensagem.DESTAQUE_NEGRITO;
				if (linha.getCell(18) != null) {
					statusAtual = (new DataFormatter()).formatCellValue(linha.getCell(18));
				}
	
				// define a origem do processo
				Origem origem = null;
				if (origemProcesso.equalsIgnoreCase("judicial")) {
					origem = Origem.SAPIENS;
				} else if (!origemProcesso.equals("")) {
					origem = Origem.SPUNET;
				} else {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Origem do processo não identificada";
				}
	
				// verifica se o número do processo foi informado corretamente
				if (numeroProcesso.length() <= 1) {
					numeroProcesso = "-";
				} else {
					if (origem != null && origem.getOrigemId().equals(Origem.SAPIENS_ID)) {
						if (numeroProcesso.length() != 17 && numeroProcesso.length() != 20) {
							msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O número do processo parece estar errado (tamanho diferente de 1, 17 ou 20 caracteres)";
						}
					} else if (origem != null && origem.getOrigemId().equals(Origem.SPUNET_ID)) {
						if (numeroProcesso.length() != 17) {
							msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O número do processo parece estar errado (tamanho diferente de 1 ou 17 caracteres)";
						}
					}
				}
	
				// verifica se o número de atendimento está formatado corretamente
				if (chaveBusca.length() != 0 && chaveBusca.length() != 11) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O número do atendimento parece estar errado (tamanho diferente de 11 caracteres)";
				}
	
				MyUtils.appendLogArea(logArea, "Linha Processada: " + (l+1));
				MyUtils.appendLogArea(logArea, "Nº Processo: " + numeroProcesso + " (" + numeroProcessoOriginal + ")");
				MyUtils.appendLogArea(logArea, "Autor......: " + autor);
	
				// se o status do registro (conteúdo da coluna 16 da linha) não estiver vazio, ignora o processamento e retorna ao usuário
				if (statusAtual.equals("")) {
					TipoImovel tipoImovel = endereco.trim().toLowerCase().replace("ó", "o").contains("imovel rural") ? TipoImovel.RURAL : TipoImovel.URBANO;
	
					// ajusta o tipo de resposta para consulta a órgão ambiental (ICMBio, IBAMA, MMA)
					if (tipoResposta.trim().equalsIgnoreCase("consultar órgão ambiental")) {
						observacao = observacao.replaceFirst("APAF ", "");
					}
	
					Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(null, nomeMunicipio));
					if (municipio == null) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Município não encontrado";
					} else {
						if (municipio.getMunicipioComarca() == null) {
							msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Município não possui comarca";
						}
					}
	
					Destino destino = null;
					// se não foi encontrado o destino no cadastro (PU ou PSUs), indica que se trata de processo extrajudicial
					if (!origemProcesso.equalsIgnoreCase("judicial")) {
						tipoResposta = "extra judicial " + (tipoProcesso.getTipoProcessoId().equals(TipoProcesso.FISICO_ID) ? "físico " : "") + tipoResposta;
						cartorio = origemProcesso;
	
						if (origemProcesso.toLowerCase().trim().startsWith("defensoria")) {
							origemProcesso = "Defensoria Pública";
						} else if (origemProcesso.toLowerCase().trim().startsWith("serventia")) {
							origemProcesso = "Serventia de Registro de Imóveis";
						} else {
							origemProcesso = "Cartório da Comarca";
						}
						destino = MyUtils.entidade(despachoServico.obterDestino(null, null, origemProcesso, null, null, null));
					} else {
						if (municipio != null) destino = municipio.getDestino();
						cartorio = null;
					}
	
					if (destino == null) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Destino não encontrado";
					}
	
					TipoResposta tpResposta = MyUtils.entidade(despachoServico.obterTipoResposta(null, tipoResposta.toLowerCase(), null));
					if (tpResposta == null) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Tipo de Resposta não encontrado";
					}
	
					if (tipoImovel.getTipoImovelId().equals(TipoImovel.RURAL_ID) && endereco.trim().toLowerCase().replace("ó", "o").equalsIgnoreCase("imovel rural")) {
						endereco = "";
					} else if (tipoImovel.getTipoImovelId().equals(TipoImovel.RURAL_ID)) {
						endereco = endereco.replaceFirst("imóvel ", "").replaceFirst("imovel ", "").replaceFirst("Imóvel ", "").replaceFirst("Imovel ", "");
					}
	
					if (msgRetorno.equals("")) {
						Solicitacao solicitacao;
						SolicitacaoEnvio envio = null;
	
						if (numeroProcesso.equals("-")) {
							solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, origem, tipoProcesso, null, null, autor, municipio, cartorio, endereco, area));
						} else {
							solicitacao = MyUtils.entidade(despachoServico.obterSolicitacao(null, origem, tipoProcesso, numeroProcesso, null));
						}
	
						if (solicitacao == null) {
							solicitacao = new Solicitacao();
							envio = new SolicitacaoEnvio(null, null, MyUtils.formatarData(new Date(), "yyyy-MM-dd HH:mm:ss"), null, true, null);
						}
	
						solicitacao.setOrigem(origem);
						solicitacao.setTipoProcesso(tipoProcesso);
						solicitacao.setNumeroProcesso(numeroProcesso);
						solicitacao.setChaveBusca(MyUtils.emptyStringIfNull(solicitacao.getChaveBusca()).trim());
						solicitacao.setAutor(autor);
						solicitacao.setMunicipio(municipio);
						solicitacao.setDestino(destino);
						solicitacao.setCartorio(cartorio);
						solicitacao.setTipoImovel(tipoImovel);
						solicitacao.setEndereco(endereco);
						solicitacao.setCoordenada(coordenada);
						solicitacao.setArea(area);
						solicitacao.setArquivosAnexados(origem.getOrigemId().equals(Origem.SAPIENS_ID) ? false : true);
	
						// se a chave de busca foi informada na planilha, atualiza a que estiver na solicitação
						if (!chaveBusca.equals("")) solicitacao.setChaveBusca(chaveBusca);
	
						// se for origem SPUNet e o número do processo SEI tiver sido informado, grava o número do processo como número do processo SEI interno onde serão gravados os despachos futuros
						if (origem.getOrigemId().equals(Origem.SPUNET_ID) && numeroProcesso.length() == 17) {
							solicitacao.setNumeroProcessoSEI(numeroProcessoOriginal);
						}
						
						solicitacao = despachoServico.salvarSolicitacao(solicitacao);
	
						if (envio != null) {
							envio.setSolicitacao(solicitacao);
							despachoServico.salvarSolicitacaoEnvio(envio);
						}
	
						// busca uma solicitacao pendente, se existir
						SolicitacaoResposta resposta = MyUtils.entidade(despachoServico.obterSolicitacaoRespostaPendente(solicitacao));

						if (resposta == null) {
							resposta = new SolicitacaoResposta();
						}
	
						resposta.setSolicitacao(solicitacao);
						resposta.setTipoResposta(tpResposta);
						resposta.setObservacao(observacao);
						resposta.setAssinante(new Assinante(MyUtils.idItemSelecionado(cbbAssinante)));
						resposta.setRespostaImpressa(false);
						resposta.setRespostaNoBlocoAssinatura(false);
	
						despachoServico.salvarSolicitacaoResposta(resposta);
						msgRetorno = "Automático pelo sistema";
					} else {
						msgRetorno = "Manual: " + msgRetorno;
						nivelMensagem = NivelMensagem.ERRO;
					}
				} else {
					msgRetorno = "Linha parece já ter sido processada: " + statusAtual.replace("Linha parece já ter sido processada: ", "");
					nivelMensagem = NivelMensagem.ALERTA;
				}
				MyUtils.appendLogArea(logArea, msgRetorno, nivelMensagem);
				MyUtils.appendLogArea(logArea, "------------------------------------------------------------------------------------");
				linha.createCell(18).setCellValue(msgRetorno);
			}
			MyUtils.appendLogArea(logArea, "Fim de leitura do arquivo!", NivelMensagem.OK);
			File fileOutput = new File(filArquivo.getSelectedFile().getAbsolutePath() + "_temp");
			FileOutputStream fos = new FileOutputStream(fileOutput);
			wb.write(fos);
			fos.flush();
			fos.close();
			wb.close();
			fileInput.delete();
			fileOutput.renameTo(fileInput);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
