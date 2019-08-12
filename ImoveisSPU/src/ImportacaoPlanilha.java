import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.ComboBoxItem;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyTextField;
import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ImportacaoPlanilha extends JInternalFrame {

	private Map<String, Map<String, EstruturaPlanilha>> mapaEstruturaPlanilha;
	private EntityManager conexao;
	private ImoveisServico imoveisServico;
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(300, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:") {{ setLabelFor(filArquivo); }};
	private MyComboBox cbbTipoPlanilha = new MyComboBox();
	private JLabel lblTipoPlanilha = new JLabel("Tipo de Planilha:") {{ setLabelFor(cbbTipoPlanilha); }};
	private MyTextField txtLinhaCabecalho = new MyTextField();
	private JLabel lblLinhaCabecalho = new JLabel("Linha do Cabeçalho:") {{ setLabelFor(txtLinhaCabecalho); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private Properties propriedades = MyUtils.obterPropriedades("imoveisspu.properties");

	public ImportacaoPlanilha(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));

		this.conexao = conexao;
		imoveisServico = new ImoveisServico(conexao);

		try {
			opcoesTipoPlanilha();
			inicializarMapaEstrutura();
		} catch (Exception e) {
			e.printStackTrace();
		}

		JPanel painelArquivo = new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }};
		JPanel painelLinhas = new JPanel() {{ setLayout(new SpringLayout()); add(lblTipoPlanilha); add(cbbTipoPlanilha); add(lblLinhaCabecalho); add(txtLinhaCabecalho); }};

		SpringUtilities.makeCompactGrid(painelLinhas, 2, 2, 6, 6, 6, 6);

		painelDados.add(painelArquivo);
		painelDados.add(lblNomeArquivo);
		painelDados.add(painelLinhas);
		painelDados.add(new JPanel());
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeCompactGrid(painelDados,
	            3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		add(areaDeRolagem, BorderLayout.CENTER);

		cbbTipoPlanilha.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					alterarLinhaCabecalho(((ComboBoxItem) e.getItem()).getIntId());
				}
			}
		});

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = null;
				if (lblNomeArquivo.getText().equals("")) {
					mensagemErro = "Para iniciar o processamento é necessário selecionar um arquivo Excel para processar.";
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
								importarArquivo(lblNomeArquivo.getText(), ((ComboBoxItem) cbbTipoPlanilha.getSelectedItem()).getCaption());
							} catch (Exception e) {
								appendLogArea(logArea, "Erro ao importar a planilha de dados: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
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
				String diretorioPadrao = propriedades.getProperty("importacao_planilha_default_path");
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
						propriedades.put("importacao_planilha_default_path", filArquivo.getSelectedFile().getParent());
						MyUtils.salvarPropriedades(propriedades, "imoveisspu.properties");
					}
				}
			}
		});
	}


	private void alterarLinhaCabecalho(Integer tipoPlanilhaId) {
		try {
			TipoPlanilha tipoPlanilha = imoveisServico.obterTipoPlanilha(tipoPlanilhaId, null).iterator().next();
			txtLinhaCabecalho.setText(tipoPlanilha.getLinhaCabecalho().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void opcoesTipoPlanilha() throws Exception {
		cbbTipoPlanilha.setModel(new MyComboBoxModel());
		List<TipoPlanilha> tiposPlanilha = imoveisServico.obterTipoPlanilha(null, null);
		MyUtils.insereOpcoesComboBox(cbbTipoPlanilha, tiposPlanilha);
		alterarLinhaCabecalho(tiposPlanilha.iterator().next().getTipoPlanilhaId());
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void importarArquivo(String arquivo, String tipoPlanilha) throws Exception {
		int linhaCabecalho = Integer.parseInt(txtLinhaCabecalho.getText());
		if (tipoPlanilha.equalsIgnoreCase("cidi")) {
			importarPlanilhaCidi(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("cidi - alienação")) {
			importarPlanilhaCidiAlienacao(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("cidi - devolução")) {
			importarPlanilhaCidiDevolucao(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("spiunet - utilização")) {
			importarPlanilhaSpiunetUtilizacao(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("spiunet - imóvel")) {
			importarPlanilhaSpiunetImovel(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("siapa")) {
			importarPlanilhaSiapa(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("cidi - polígonos")) {
			importarPlanilhaCidiPoligono(arquivo, tipoPlanilha, linhaCabecalho);
		} else if (tipoPlanilha.equalsIgnoreCase("spiunet/siapa - polígonos")) {
			importarPlanilhaRIPPoligono(arquivo, tipoPlanilha, linhaCabecalho);
		}
	}
	
	private void importarPlanilhaCidi(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosCidi();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);
				if (linha == null || linha.getCell(0) == null || df.formatCellValue(linha.getCell(0)) == null || df.formatCellValue(linha.getCell(0)).trim().equals("")) {
					break;
				}
	
				String ur = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("ur").getNumeroColuna(), df);
				String nbp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nbp").getNumeroColuna()).replace(".0", "");
				String parcela = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("parcela").getNumeroColuna(), df);
				String descricao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("descricao").getNumeroColuna(), df);
				String cod_trecho = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("cod_trecho").getNumeroColuna(), df);
				String trecho_ini = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("trecho_ini").getNumeroColuna(), df);
				String trecho_fim = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("trecho_fim").getNumeroColuna(), df);
				String logradouro = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("logradouro").getNumeroColuna(), df);
				String complemento = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("complemento").getNumeroColuna(), df);
				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String cep = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("cep").getNumeroColuna(), df);
				String uf = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("uf").getNumeroColuna(), df).trim().toUpperCase();
				String area = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("area").getNumeroColuna(), df);
				String nprocesso = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nprocesso").getNumeroColuna(), df);
				String termo_transf = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("termo_transf").getNumeroColuna(), df);
				String termo_ano = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("termo_ano").getNumeroColuna(), df);
				String situacao19 = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("situacao19").getNumeroColuna(), df);
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - NBP: " + nbp + "-" + parcela + " - Município: " + municipio);
	
				gravarRegistroCidi(ur, nbp, parcela, descricao, cod_trecho, trecho_ini, trecho_fim, logradouro, complemento, municipio, cep, uf, area, nprocesso, termo_transf, termo_ano, situacao19);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosCidi() throws Exception {
		String sql = "delete from cidi";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroCidi(String ur, String nbp, String parcela, String descricao, String cod_trecho,
			String trecho_ini, String trecho_fim, String logradouro, String complemento, String municipio, String cep,
			String uf, String area, String nprocesso, String termo_transf, String termo_ano, String situacao19) throws Exception {
		String sql = "insert into cidi (ur, nbp, parcela, descricao, cod_trecho, trecho_ini, trecho_fim, logradouro, complemento, municipio, cep, uf, area, nprocesso, termo_transf, termo_ano, situacao19) values (" + 
					 "'" + ur + "', " +
					 "'" + nbp + "', " +
					 "'" + parcela + "', " +
					 "'" + descricao.replaceAll("'", "''") + "', " +
					 "'" + cod_trecho + "', " +
					 "'" + trecho_ini.replaceAll("'", "''") + "', " +
					 "'" + trecho_fim.replaceAll("'", "''") + "', " +
					 "'" + logradouro.replaceAll("'", "''") + "', " +
					 "'" + complemento.replaceAll("'", "''") + "', " +
					 "'" + municipio.replaceAll("'", "''") + "', " +
					 "'" + cep + "', " +
					 "'" + uf + "', " +
					 "'" + area + "', " +
					 "'" + nprocesso + "', " +
					 "'" + termo_transf + "', " +
					 "'" + termo_ano + "', " +
					 "'" + situacao19.replaceAll("'", "''") + "') ";

		gravaMunicipioIncorreto(conexao, municipio);
		JPAUtils.executeUpdate(conexao, sql);
	}
	
	private void importarPlanilhaCidiAlienacao(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosCidiAlienacao();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);
				if (linha == null || linha.getCell(0) == null || df.formatCellValue(linha.getCell(0)) == null || df.formatCellValue(linha.getCell(0)).trim().equals("")) {
					break;
				}
	
				String processo = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("processo").getNumeroColuna(), df);
				String sarp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("sarp").getNumeroColuna(), df);
				String ur = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("ur").getNumeroColuna(), df);
				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String endereco = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("endereco").getNumeroColuna(), df);
				String assunto = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("assunto").getNumeroColuna(), df);
				String interessado = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("interessado").getNumeroColuna(), df);
				String cod_bp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("cod_bp").getNumeroColuna()).replace(".0", "");
				String num_pcl_bp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("num_pcl_bp").getNumeroColuna(), df);
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - NBP: " + cod_bp + "-" + num_pcl_bp + " - Município: " + municipio);
	
				gravarRegistroCidiAlienacao(processo, sarp, ur, municipio, endereco, assunto, interessado, cod_bp, num_pcl_bp);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosCidiAlienacao() throws Exception {
		String sql = "delete from cidialienacao";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroCidiAlienacao(String processo, String sarp, String ur, String municipio, String endereco, String assunto, String interessado, String cod_bp, String num_pcl_bp) throws Exception {
		String sql = "insert into cidialienacao (processo, sarp, ur, municipio, endereco, assunto, interessado, cod_bp, num_pcl_bp) values (" + 
					 "'" + processo + "', " +
					 "'" + sarp + "', " +
					 "'" + ur + "', " +
					 "'" + municipio.replaceAll("'", "''") + "', " +
					 "'" + endereco.replaceAll("'", "''") + "', " +
					 "'" + assunto.replaceAll("'", "''") + "', " +
					 "'" + interessado.replaceAll("'", "''") + "', " +
					 "'" + cod_bp + "', " +
					 "'" + num_pcl_bp + "') ";

		gravaMunicipioIncorreto(conexao, municipio);
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void importarPlanilhaCidiDevolucao(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosCidiDevolucao();
		
		String lNumero = "";
		String lProcesso = "";

		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);
				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}
	
				String numero = df.formatCellValue(linha.getCell(0));
				if (numero == null || numero.trim().equals("")) {
					numero = lNumero;
				} else {
					lNumero = numero;
				}
				String tt = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("tt").getNumeroColuna(), df);
				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String nbp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nbp").getNumeroColuna()).replace(".0", "");
				String oficio_spu = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("oficio_spu").getNumeroColuna(), df);
				String data = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("data").getNumeroColuna(), df);
				String processo = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("processo").getNumeroColuna());
				if (processo == null || processo.trim().equals("")) {
					processo = lProcesso;
				} else {
					lProcesso = processo;
				}
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - NBP: " + nbp + " - Município: " + municipio);
	
				gravarRegistroCidiDevolucao(numero, tt, municipio, nbp, oficio_spu, data, processo);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosCidiDevolucao() throws Exception {
		String sql = "delete from cididevolucao";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroCidiDevolucao(String numero, String tt, String municipio, String nbp, String oficio_spu, String data, String nprocesso) throws Exception {
		String sql = "insert into cididevolucao (numero, tt, municipio, nbp, oficio_spu, data, nprocesso) values (" + 
					 "'" + numero + "', " +
					 "'" + tt + "', " +
					 "'" + municipio.replaceAll("'", "''") + "', " +
					 "'" + nbp + "', " +
					 "'" + oficio_spu + "', " +
					 "'" + data + "', " +
					 "'" + nprocesso + "') ";

		gravaMunicipioIncorreto(conexao, municipio);
		JPAUtils.executeUpdate(conexao, sql);
	}
	
	private void importarPlanilhaSpiunetUtilizacao(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosSpiunet();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; linhaCabecalho <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);
				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}
	
				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String ripImovel = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("ripimovel").getNumeroColuna()).replace(".0", "");
				String ripUtilizacao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("riputilizacao").getNumeroColuna()).replace(".0", "");
				String regimeUtilizacao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("regimeutilizacao").getNumeroColuna()).replace(".0", "");
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - RIP Imóvel: " + ripImovel + " - Município: " + municipio);
	
				gravarRegistroSpiunetUtilizacao(municipio, ripImovel, ripUtilizacao, regimeUtilizacao);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosSpiunet() throws Exception {
		String sql = "delete from spiunet";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroSpiunetUtilizacao(String municipio, String ripImovel, String ripUtilizacao, String regimeUtilizacao) throws Exception {
		String sql = "insert into spiunet (municipio, ripimovel, riputilizacao, regimeutilizacao) values (" + 
					 "'" + municipio.replaceAll("'", "''") + "', " +
					 "'" + ripImovel + "', " +
					 "'" + ripUtilizacao + "', " +
					 "'" + regimeUtilizacao + "') ";

		gravaMunicipioIncorreto(conexao, municipio);
		JPAUtils.executeUpdate(conexao, sql);
	}
	
	private void importarPlanilhaSpiunetImovel(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);

		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);
				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}
	
				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String ripImovel = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("ripimovel").getNumeroColuna()).replace(".0", "");
				String tipoProprietario = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("tipoproprietario").getNumeroColuna()).replace(".0", "");
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - RIP Imóvel: " + ripImovel + " - Município: " + municipio);
	
				gravarRegistroSpiunetImovel(ripImovel, tipoProprietario);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void gravarRegistroSpiunetImovel(String ripImovel, String tipoProprietario) throws Exception {
		String sql = "update spiunet set tipoproprietario = '" + tipoProprietario + "' where ripimovel = '" + ripImovel + "'"; 
		JPAUtils.executeUpdate(conexao, sql);
	}
	
	private void importarPlanilhaSiapa(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosSiapa();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);

				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}

				String municipio = MyUtils.retiraAcento(MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("municipio").getNumeroColuna(), df).trim().toUpperCase());
				String ripImovel = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("ripimovel").getNumeroColuna()).replace(".0", "");
				String conceituacao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("conceituacao").getNumeroColuna());
				String classeImovel = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("classeimovel").getNumeroColuna());
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - RIP Imóvel: " + ripImovel + " - Município: " + municipio);
	
				gravarRegistroSiapa(municipio, ripImovel, conceituacao, classeImovel);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosSiapa() throws Exception {
		String sql = "delete from siapa";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroSiapa(String municipio, String ripImovel, String conceituacao, String classeImovel) throws Exception {
		String sql = "insert into siapa (municipio, ripimovel, conceituacao, classeimovel) values (" + 
					 "'" + municipio.replaceAll("'", "''") + "', " +
					 "'" + ripImovel + "', " +
					 "'" + conceituacao + "', " +
					 "'" + classeImovel + "') ";

		gravaMunicipioIncorreto(conexao, municipio);
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void importarPlanilhaCidiPoligono(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosCidiPoligono();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);

				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}

				String descricao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("descricao").getNumeroColuna(), df).trim();
				String nome = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nome").getNumeroColuna());
				String nbp = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nbp").getNumeroColuna()).replace(".0", "");
	
				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - NBP: " + nbp);
	
				gravarRegistroCidiPoligono(descricao, nome, nbp);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosCidiPoligono() throws Exception {
		String sql = "delete from cidipoligono";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroCidiPoligono(String descricao, String nome, String nbp) throws Exception {
		String sql = "insert into cidipoligono (descricao, nome, nbp) values (" + 
					 "'" + descricao.replaceAll("'", "''") + "', " +
					 "'" + nome.replaceAll("'", "''") + "', " +
					 "'" + nbp + "') ";

		JPAUtils.executeUpdate(conexao, sql);
	}

	private void importarPlanilhaRIPPoligono(String arquivo, String tipoPlanilha, int linhaCabecalho) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		DataFormatter df = new DataFormatter();
		Sheet planilha = wb.getSheetAt(0);
		
		apagarDadosRIPPoligono();
		
		Row dadosCabecalho = planilha.getRow(linhaCabecalho - 1);
		String msgAjuste = ajustarEstruturaPlanilha(tipoPlanilha, dadosCabecalho);
		if (msgAjuste == null) {
			for (int l = linhaCabecalho; l <= planilha.getLastRowNum(); l++) {
				Row linha = planilha.getRow(l);

				if (linha == null || linha.getCell(2) == null || df.formatCellValue(linha.getCell(2)) == null || df.formatCellValue(linha.getCell(2)).trim().equals("")) {
					break;
				}

				String descricao = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("descricao").getNumeroColuna(), df).trim();
				String nome = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("nome").getNumeroColuna());
				String rip = MyUtils.obterValorCelula(linha, mapaEstruturaPlanilha.get(tipoPlanilha).get("rip").getNumeroColuna()).replace(".0", "");

				appendLogArea(logArea, "Linha Processada: " + (l+1) + " - RIP: " + rip);

				gravarRegistroRIPPoligono(descricao, nome, rip);
			}
		} else {
			appendLogArea(logArea, "Não foi possível processar o arquivo. Algumas informações obrigatórias não estão presentes na planilha fornecida: \n \n" + msgAjuste);
		}
		appendLogArea(logArea, "------------------------------------------------------------------------------------");
		appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}

	private void apagarDadosRIPPoligono() throws Exception {
		String sql = "delete from rippoligono";
		JPAUtils.executeUpdate(conexao, sql);
	}

	private void gravarRegistroRIPPoligono(String descricao, String nome, String rip) throws Exception {
		String sql = "insert into rippoligono (descricao, nome, rip) values (" + 
					 "'" + descricao.replaceAll("'", "''") + "', " +
					 "'" + nome.replaceAll("'", "''") + "', " +
					 "'" + rip + "') ";

		JPAUtils.executeUpdate(conexao, sql);
	}

	private void appendLogArea(JTextArea logArea, String msg) {
		System.out.println(msg);
		logArea.append(msg + "\n");
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private void gravaMunicipioIncorreto(EntityManager conexao, String municipio) {
		String sql = "insert into municipiocorrecao (nomeincorreto, nomecorreto) " + 
					 "select '" + municipio.replaceAll("'", "''") + "', ''" +
					 " where not exists (select 1 from municipiocorrecao where nomeincorreto = '" + municipio.replaceAll("'", "''") + "')" +
					 "   and not exists (select 1 from municipio where municipio = '" + municipio.replaceAll("'", "''") + "')";

		JPAUtils.executeUpdate(conexao, sql);
	}

	private void inicializarMapaEstrutura() throws Exception {
		mapaEstruturaPlanilha = new LinkedHashMap<String, Map<String, EstruturaPlanilha>>();
		
		for (TipoPlanilha tipoPlanilha : imoveisServico.obterTipoPlanilha(null, null)) {
			mapaEstruturaPlanilha.put(tipoPlanilha.getDescricao(), new LinkedHashMap<String, EstruturaPlanilha>());
			
			for (EstruturaPlanilha estruturaPlanilha : imoveisServico.obterEstruturaPlanilha(null, tipoPlanilha.getDescricao(), null)) {
				mapaEstruturaPlanilha.get(tipoPlanilha.getDescricao()).put(estruturaPlanilha.getNomeCampo(), estruturaPlanilha.clone());
			}
		}
	}

	private String ajustarEstruturaPlanilha(String tipoPlanilha, Row dadosCabecalho) {
		String retorno = "";
		try {
			for (EstruturaPlanilha estruturaPlanilha : mapaEstruturaPlanilha.get(tipoPlanilha).values()) {
				estruturaPlanilha.setNumeroColuna(-1);
				for (int c = dadosCabecalho.getFirstCellNum(); c < dadosCabecalho.getLastCellNum(); c++) {
					Cell celula = dadosCabecalho.getCell(c);
					if (celula != null) {
						if (MyUtils.retiraAcento(estruturaPlanilha.getNomeColuna().toUpperCase()).equalsIgnoreCase(MyUtils.retiraAcento(celula.getStringCellValue().toUpperCase()))) {
							estruturaPlanilha.setNumeroColuna(c);
							break;
						}
					}
				}
				if (estruturaPlanilha.getNumeroColuna() == -1 && estruturaPlanilha.getObrigatorio()) {
					retorno += "Coluna na Planilha: " + estruturaPlanilha.getNomeColuna() + " - Campo na Tabela: " + estruturaPlanilha.getNomeCampo() + "\n";
				}
			}
		} catch (Exception e) {
			retorno = "ATENÇÃO! Ocorreu um erro ao tentar obter os dados do cabeçalho da planilha. Verifique se a planilha que você selecionou corresponde à fonte de dados que você deseja processar.";
		}
		return (retorno.equals("") ? null : retorno);
	}
}
