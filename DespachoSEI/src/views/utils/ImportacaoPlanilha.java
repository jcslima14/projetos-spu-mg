package views.utils;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import javax.persistence.EntityManager;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
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
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JTextField txtLinhaInicial = new JTextField(10);
	private JLabel lblLinhaInicial = new JLabel("Linha inicial:", JLabel.TRAILING) {{ setLabelFor(txtLinhaInicial); }};
	private JTextField txtLinhaFinal = new JTextField(10);
	private JLabel lblLinhaFinal = new JLabel("Linha final:", JLabel.TRAILING) {{ setLabelFor(txtLinhaFinal); }};
	private MyComboBox cbbAssinante = new MyComboBox();
	private MyLabel lblAssinante = new MyLabel("Assinado por");
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private DespachoServico despachoServico;

	public ImportacaoPlanilha(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));

		this.conexao = conexao;
		despachoServico = new DespachoServico(this.conexao);

		despachoServico.preencherOpcoesAssinante(cbbAssinante, null, false, true);

		JPanel painelArquivo = new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }};
		JPanel painelLinhas = new JPanel() {{ setLayout(new SpringLayout()); add(lblLinhaInicial); add(txtLinhaInicial); add(lblLinhaFinal); add(txtLinhaFinal); add(lblAssinante); add(cbbAssinante); }};

		SpringUtilities.makeCompactGrid(painelLinhas, 3, 2, 6, 6, 6, 6);
		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblNomeArquivo);
		painelDados.add(painelLinhas);
		painelDados.add(new JPanel());
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeCompactGrid(painelDados,
	            espacoEmDisco == null ? 3 : 4, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(areaDeRolagem, BorderLayout.CENTER);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = null;
				if (lblNomeArquivo.getText().equals("")) {
					mensagemErro = "Para iniciar o processamento � necess�rio selecionar um arquivo Excel para processar.";
				}
				try {
					int linhaInicial = Integer.parseInt(txtLinhaInicial.getText());
					int linhaFinal = Integer.parseInt(txtLinhaFinal.getText());
					if (linhaInicial > linhaFinal) {
						mensagemErro = "A linha inicial a ser processada deve ser menor ou igual � linha final.";
					}
				} catch (Exception ex) {
					mensagemErro = "Erro ao verificar as linhas de in�cio e fim de processamento. Informe somente d�gitos nestes campos.";
				}

				if (mensagemErro != null) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				try {
					logArea.setText("");
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								importarArquivo(lblNomeArquivo.getText(), Integer.parseInt(txtLinhaInicial.getText()), Integer.parseInt(txtLinhaFinal.getText()));;
							} catch (Exception e) {
								MyUtils.appendLogArea(logArea, "Erro ao importar a planilha de dados: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
								e.printStackTrace();
							}
						}

						private String stackTraceToString(Exception e) {
							StringWriter sw = new StringWriter();
							e.printStackTrace(new PrintWriter(sw));
							return sw.toString();
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		btnAbrirArquivo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String diretorioPadrao = despachoServico.obterConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO);
				if (diretorioPadrao != null && !diretorioPadrao.trim().equals("")) {
					File dirPadrao = new File(diretorioPadrao);
					if (dirPadrao.exists()) {
						filArquivo.setCurrentDirectory(dirPadrao);
					}
				}
				Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(ImportacaoPlanilha.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filArquivo.getSelectedFile().exists()) {
						lblNomeArquivo.setText(filArquivo.getSelectedFile().getAbsolutePath());
						if (!diretorioPadrao.equals(filArquivo.getSelectedFile().getParent())) {
							despachoServico.salvarConteudoParametro(Parametro.PASTA_PLANILHA_IMPORTACAO, filArquivo.getSelectedFile().getParent());
						}
					}
				}
			}
		});
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void importarArquivo(String arquivo, int linhaInicial, int linhaFinal) throws Exception {
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
				msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Origem do processo n�o identificada";
			}

			// verifica se o n�mero do processo foi informado corretamente
			if (numeroProcesso.length() <= 1) {
				numeroProcesso = "-";
			} else {
				if (origem != null && origem.getOrigemId().equals(Origem.SAPIENS_ID)) {
					if (numeroProcesso.length() != 17 && numeroProcesso.length() != 20) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O n�mero do processo parece estar errado (tamanho diferente de 1, 17 ou 20 caracteres)";
					}
				} else if (origem != null && origem.getOrigemId().equals(Origem.SPUNET_ID)) {
					if (numeroProcesso.length() != 17) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O n�mero do processo parece estar errado (tamanho diferente de 1 ou 17 caracteres)";
					}
				}
			}

			// verifica se o n�mero de atendimento est� formatado corretamente
			if (chaveBusca.length() != 0 && chaveBusca.length() != 11) {
				msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "O n�mero do atendimento parece estar errado (tamanho diferente de 11 caracteres)";
			}

			MyUtils.appendLogArea(logArea, "Linha Processada: " + (l+1));
			MyUtils.appendLogArea(logArea, "N� Processo: " + numeroProcesso + " (" + numeroProcessoOriginal + ")");
			MyUtils.appendLogArea(logArea, "Autor......: " + autor);

			// se o status do registro (conte�do da coluna 16 da linha) n�o estiver vazio, ignora o processamento e retorna ao usu�rio
			if (statusAtual.equals("")) {
				TipoImovel tipoImovel = endereco.trim().toLowerCase().replace("�", "o").contains("imovel rural") ? TipoImovel.RURAL : TipoImovel.URBANO;

				// ajusta o tipo de resposta para consulta a �rg�o ambiental (ICMBio, IBAMA, MMA)
				if (tipoResposta.trim().equalsIgnoreCase("consultar �rg�o ambiental")) {
					observacao = observacao.replaceFirst("APAF ", "");
				}

				Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(null, nomeMunicipio));
				if (municipio == null) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Munic�pio n�o encontrado";
				} else {
					if (municipio.getMunicipioComarca() == null) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Munic�pio n�o possui comarca";
					}
				}

				Destino destino = null;
				// se n�o foi encontrado o destino no cadastro (PU ou PSUs), indica que se trata de processo extrajudicial
				if (!origemProcesso.equalsIgnoreCase("judicial")) {
					tipoResposta = "extra judicial " + (tipoProcesso.getTipoProcessoId().equals(TipoProcesso.FISICO_ID) ? "f�sico " : "") + tipoResposta;
					cartorio = origemProcesso;

					if (origemProcesso.toLowerCase().trim().startsWith("defensoria")) {
						origemProcesso = "Defensoria P�blica";
					} else if (origemProcesso.toLowerCase().trim().startsWith("serventia")) {
						origemProcesso = "Serventia de Registro de Im�veis";
					} else {
						origemProcesso = "Cart�rio da Comarca";
					}
					destino = MyUtils.entidade(despachoServico.obterDestino(null, null, origemProcesso, null, null, null));
				} else {
					if (municipio != null) destino = municipio.getDestino();
					cartorio = null;
				}

				if (destino == null) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Destino n�o encontrado";
				}

				TipoResposta tpResposta = MyUtils.entidade(despachoServico.obterTipoResposta(null, tipoResposta.toLowerCase(), null));
				if (tpResposta == null) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Tipo de Resposta n�o encontrado";
				}

				if (tipoImovel.getTipoImovelId().equals(TipoImovel.RURAL_ID) && endereco.trim().toLowerCase().replace("�", "o").equalsIgnoreCase("imovel rural")) {
					endereco = "";
				} else if (tipoImovel.getTipoImovelId().equals(TipoImovel.RURAL_ID)) {
					endereco = endereco.replaceFirst("im�vel ", "").replaceFirst("imovel ", "").replaceFirst("Im�vel ", "").replaceFirst("Imovel ", "");
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

					// se a chave de busca foi informada na planilha, atualiza a que estiver na solicita��o
					if (!chaveBusca.equals("")) solicitacao.setChaveBusca(chaveBusca);

					// se for origem SPUNet e o n�mero do processo SEI tiver sido informado, grava o n�mero do processo como n�mero do processo SEI interno onde ser�o gravados os despachos futuros
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
					msgRetorno = "Autom�tico pelo sistema";
				} else {
					msgRetorno = "Manual: " + msgRetorno;
				}
			} else {
				msgRetorno = "Linha parece j� ter sido processada: " + statusAtual.replace("Linha parece j� ter sido processada: ", "");
			}
			MyUtils.appendLogArea(logArea, msgRetorno);
			MyUtils.appendLogArea(logArea, "------------------------------------------------------------------------------------");
			linha.createCell(18).setCellValue(msgRetorno);
		}
		MyUtils.appendLogArea(logArea, "Fim de leitura do arquivo!");
		File fileOutput = new File(filArquivo.getSelectedFile().getAbsolutePath() + "_temp");
		FileOutputStream fos = new FileOutputStream(fileOutput);
		wb.write(fos);
		fos.flush();
		fos.close();
		wb.close();
		fileInput.delete();
		fileOutput.renameTo(fileInput);
	}
}