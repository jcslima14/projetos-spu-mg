import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ImportacaoPlanilha extends JInternalFrame {

	private Connection conexao;
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

	public ImportacaoPlanilha(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));

		this.conexao = conexao;
		despachoServico = new DespachoServico(this.conexao);

		opcoesAssinante();

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
//		painelDados.add(lblLinhaInicial);
//		painelDados.add(txtLinhaInicial);
//		painelDados.add(painelLinhaFinal);
//		painelDados.add(new JPanel());
//		painelDados.add(lblLinhaFinal);
//		painelDados.add(txtLinhaFinal);
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
					mensagemErro = "Para iniciar o processamento é necessário selecionar um arquivo Excel para processar.";
				}
				try {
					int linhaInicial = Integer.parseInt(txtLinhaInicial.getText());
					int linhaFinal = Integer.parseInt(txtLinhaFinal.getText());
					if (linhaInicial > linhaFinal) {
						mensagemErro = "A linha inicial a ser processada deve ser menor ou igual à linha final.";
					}
				} catch (Exception ex) {
					mensagemErro = "Erro ao verificar as linhas de início e fim de processamento. Informe somente dígitos nestes campos.";
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
			String sData = MyUtils.formatarData(new Date(), "yyyy-MM-dd");
			if (linha.getCell(0).getCellTypeEnum().equals(CellType.NUMERIC)) {
				if (DateUtil.isCellDateFormatted(linha.getCell(0))) {
					DataFormatter df = new DataFormatter();
					sData = df.formatCellValue(linha.getCell(0));
					SimpleDateFormat f1 = new SimpleDateFormat("dd/MM/yy");
					sData = MyUtils.formatarData(f1.parse(sData), "yyyy-MM-dd");
				}
			}
			String tipoProcesso = (MyUtils.obterValorCelula(linha.getCell(1)).trim().toLowerCase().startsWith("f") ? "Físico" : "Eletrônico");
			String numeroProcesso = MyUtils.obterValorCelula(linha.getCell(2));
			String autor = MyUtils.obterValorCelula(linha.getCell(3));
			String cartorio = "";
			String endereco = MyUtils.obterValorCelula(linha.getCell(5));
			String municipio = MyUtils.obterValorCelula(linha.getCell(6));
			String coordenada = MyUtils.obterValorCelula(linha.getCell(7));
			coordenada = (coordenada.trim().length() <= 1 ? "" : coordenada);
			String area = MyUtils.obterValorCelula(linha.getCell(8));
			area = (area.trim().length() <= 1 ? "" : area);
			String origemProcesso = MyUtils.obterValorCelula(linha.getCell(14));
			String tipoResposta = MyUtils.obterValorCelula(linha.getCell(15));
			String observacao = MyUtils.obterValorCelula(linha.getCell(17));
			observacao = (observacao.trim().length() <= 1 ? "" : observacao);
			String statusAtual = "";
			if (linha.getCell(18) != null) {
				statusAtual = (new DataFormatter()).formatCellValue(linha.getCell(18));
			}
			String msgRetorno = "";

			MyUtils.appendLogArea(logArea, "Linha Processada: " + (l+1));
			MyUtils.appendLogArea(logArea, "Nº Processo: " + numeroProcesso);
			MyUtils.appendLogArea(logArea, "Autor......: " + autor);

			// se o status do registro (conteúdo da coluna 16 da linha) não estiver vazio, ignora o processamento e retorna ao usuário
			if (statusAtual.equals("")) {
				String tipoImovel = endereco.trim().toLowerCase().replace("ó", "o").contains("imovel rural") ? "Rural" : "Urbano";

				// ajusta o tipo de resposta para consulta a órgão ambiental (ICMBio, IBAMA, MMA)
				if (tipoResposta.trim().equalsIgnoreCase("consultar icmbio") || 
					tipoResposta.trim().equalsIgnoreCase("consultar icmbio/mma") ||
					tipoResposta.trim().equalsIgnoreCase("consultar mma/ibama")) {
					tipoResposta = "consultar órgão ambiental";
					observacao = observacao.replaceFirst("APAF ", "");
				}

				List<Municipio> municipios = despachoServico.obterMunicipio(true, null, municipio);
				if (municipios.isEmpty()) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Município não encontrado";
				} else {
					if (municipios.iterator().next().getMunicipioComarca() == null) {
						msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Município não possui comarca";
					}
				}

				List<Destino> destinos = null;
				// se não foi encontrado o destino no cadastro (PU ou PSUs), indica que se trata de processo extrajudicial
				if (!origemProcesso.equalsIgnoreCase("judicial")) {
					tipoResposta = "extra judicial " + tipoResposta;
					cartorio = origemProcesso;

					if (origemProcesso.toLowerCase().trim().startsWith("defensoria")) {
						origemProcesso = "Defensoria Pública";
					} else if (origemProcesso.toLowerCase().trim().startsWith("serventia")) {
						origemProcesso = "Serventia de Registro de Imóveis";
					} else {
						origemProcesso = "Cartório da Comarca";
					}
					destinos = despachoServico.obterDestino(null, null, origemProcesso, null, null, null);
				} else {
					destinos = despachoServico.obterDestino(null, null, null, null, municipio, null);
					if (municipios != null && !municipios.isEmpty() && municipios.iterator().next().getMunicipioComarca() != null) cartorio = municipios.iterator().next().getMunicipioComarca().getNome();
				}

				if (destinos == null || destinos.isEmpty()) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Destino não encontrado";
				}

				TipoResposta tpResposta = MyUtils.entidade(despachoServico.obterTipoResposta(null, tipoResposta.toLowerCase()));
				if (tpResposta == null) {
					msgRetorno += (msgRetorno.equalsIgnoreCase("") ? "" : " / ") + "Tipo de Resposta não encontrado";
				}

				if (tipoImovel.equalsIgnoreCase("rural") && endereco.trim().toLowerCase().replace("ó", "o").equalsIgnoreCase("imovel rural")) {
					endereco = "";
				} else if (tipoImovel.equalsIgnoreCase("rural")) {
					endereco = endereco.replaceFirst("imóvel ", "").replaceFirst("imovel ", "").replaceFirst("Imóvel ", "").replaceFirst("Imovel ", "");
				}

				if (msgRetorno.equals("")) {
					if (!(tipoResposta.startsWith("Extra judicial") && tipoProcesso.equals("Eletrônico"))) {
						Origem origem = Origem.SAPIENS;
						if (origemProcesso.equalsIgnoreCase("judicial")) {
							
						}

						Solicitacao solicitacao = new Solicitacao(null, origem, despachoServico.obterTipoProcesso(null, tipoProcesso.toLowerCase()).iterator().next(), numeroProcesso, autor, municipios.iterator().next(), destinos.iterator().next(),
								cartorio, despachoServico.obterTipoImovel(null, tipoImovel).iterator().next(), endereco, coordenada, area, null, false);

						solicitacao = despachoServico.salvarSolicitacao(solicitacao);
						
						SolicitacaoResposta resposta = new SolicitacaoResposta(
								null, 
								solicitacao, 
								tpResposta, 
								observacao, 
								new Assinante(MyUtils.idItemSelecionado(cbbAssinante)), 
								null, 
								null, 
								null, 
								null, 
								false,
								null, 
								null, 
								false);

						despachoServico.salvarSolicitacaoResposta(resposta);
						msgRetorno = "Automático pelo sistema";
					} else {
						msgRetorno = "Extrajudicial eletrônico, já feito pelo analista";
					}
				} else {
					msgRetorno = "Manual: " + msgRetorno;
				}
			} else {
				msgRetorno = "Linha parece já ter sido processada: " + statusAtual.replace("Linha parece já ter sido processada: ", "");
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

	private void opcoesAssinante() {
		cbbAssinante.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbAssinante, "select assinanteid, nome from assinante where superior = false order by nome");
	}
}
