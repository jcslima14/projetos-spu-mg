import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
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

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class ImportacaoPlanilha extends JInternalFrame {

	private EntityManager conexao;
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextArea logArea = new JTextArea(30, 100);
	private JScrollPane areaDeRolagem = new JScrollPane(logArea);
	private SPUNetServico cadastroServico;
	private JTextField txtPlanilha = new JTextField("0");
	private JLabel lblPlanilha = new JLabel("ID Inicial:") {{ setLabelFor(txtPlanilha); }};
	private List<String> municipios = null;

	public ImportacaoPlanilha(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));

		this.conexao = conexao;
		cadastroServico = new SPUNetServico(this.conexao);

		JPanel painelArquivo = new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }};

		String espacoEmDisco = MyUtils.verificacaoDeEspacoEmDisco(20);
		
		if (espacoEmDisco != null) {
			painelDados.add(new JLabel("<html><font color='red'>" + espacoEmDisco + "</font></html>"));
			painelDados.add(new JPanel());
		}
		painelDados.add(painelArquivo);
		painelDados.add(lblNomeArquivo);
		painelDados.add(lblPlanilha); 
		painelDados.add(txtPlanilha); 
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
								importarArquivo(lblNomeArquivo.getText());;
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
				// Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				// detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(ImportacaoPlanilha.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filArquivo.getSelectedFile().exists()) {
						lblNomeArquivo.setText(filArquivo.getSelectedFile().getAbsolutePath());
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

	private void importarArquivo(String arquivo) throws Exception {
		File fileInput = new File(arquivo);
		Workbook wb = WorkbookFactory.create(fileInput);
		Sheet planilha = wb.getSheetAt(Integer.parseInt(txtPlanilha.getText()));
		DataFormatter df = new DataFormatter();

		for (int l = 2; l <= planilha.getLastRowNum(); l++) {
			Row linha = planilha.getRow(l);
			String msgRetorno = (l+1) + ": ";
			String situacao = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 0)).trim();

			if (situacao.trim().equalsIgnoreCase("ok")) {
				Geoinformacao geo = new Geoinformacao();
				geo.setCadastrado(false);
				geo.setIdentFormatoProdutoCDG(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 1)).trim());
				geo.setIdentProdutoCDG(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 2)).trim());
				geo.setIdentTituloProduto(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 3)).trim());
				geo.setIdentDataCriacao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 4, df)).trim());
				geo.setIdentDataDigitalizacao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 5, df)).trim());
				geo.setIdentResumo(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 6)).trim());
				geo.setIdentStatus(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 7)).trim());
				geo.setIdentInstituicao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 8)).trim());
				geo.setIdentFuncao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 9)).trim());
				geo.setSisrefDatum(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 10)).trim());
				geo.setSisrefProjecao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 11)).trim());
				geo.setSisrefObservacao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 12)).trim());
				geo.setIdentcdgTipoReprEspacial(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 13)).trim());
				geo.setIdentcdgEscala(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 14, df)).trim());
				geo.setIdentcdgIdioma(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 16)).trim());
				geo.setIdentcdgCategoria(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 17)).trim());
				geo.setIdentcdgUF(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 18)).trim());
				geo.setIdentcdgMunicipio(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 19)).trim());
				geo.setIdentcdgDatum(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 20)).trim());
				geo.setQualidadeNivel(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 21)).trim());
				geo.setQualidadeLinhagem(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 22)).trim());
				geo.setDistribuicaoFormato(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 23)).trim());
				geo.setDistribuicaoInstituicao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 24)).trim());
				geo.setDistribuicaoFuncao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 25)).trim());
				geo.setMetadadoIdioma(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 26)).trim());
				geo.setMetadadoInstituicao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 27)).trim());
				geo.setMetadadoFuncao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 28)).trim());
				geo.setInfadicTipoArticulacao(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 29)).trim());
				geo.setInfadicCamadaInf(MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 30)).trim());

				String msgValidacao = validar(geo);

				if (msgValidacao.equals("")) {
					try {
						JPAUtils.persistir(conexao, geo);
						msgRetorno += "título '" + geo.getIdentTituloProduto() + "' cadastrado com sucesso!";
					} catch (Exception e) {
						msgRetorno += "erro ao cadastrar o título '" + geo.getIdentTituloProduto() + "' (" + e.getMessage() + ")";
					}
				} else {
					msgRetorno += msgValidacao;
				}
			} else {
				msgRetorno += "registro não está com indicador de ok";
			}

			// if (msgRetorno.trim().length() > 20) {
			if (!msgRetorno.contains("já está cadastrado na base de dados para catalogação")) {
				MyUtils.appendLogArea(logArea, msgRetorno);
			 }
		}
		MyUtils.appendLogArea(logArea, "------------------------------------------------------------------------------------");
		MyUtils.appendLogArea(logArea, "Fim de leitura do arquivo!");
		wb.close();
	}
	
	private String validar(Geoinformacao geo) throws Exception {
		Geoinformacao geoCadastrada = MyUtils.entidade(cadastroServico.obterGeoinformacao(null, null, geo.getIdentTituloProduto())); 
		
		if (geoCadastrada != null) {
			return "título '" + geo.getIdentTituloProduto() + "' já está cadastrado na base de dados para catalogação";
		}

		if (!getMunicipios().contains(geo.getIdentcdgMunicipio())) {
			return "título '" + geo.getIdentTituloProduto() + "' município de " + geo.getIdentcdgMunicipio() + " não está cadastrado";
		}
		
		if (geo.getQualidadeNivel().trim().equals("")) {
			return "título '" + geo.getIdentTituloProduto() + "' qualidade/nível não foi informado";
		}

		try {
			MyUtils.obterData(geo.getIdentDataCriacao(), "dd/MM/yyyy");
		} catch (Exception e) {
			return "título '" + geo.getIdentTituloProduto() + "' data de criação inválida: " + geo.getIdentDataCriacao();
		}

		try {
			MyUtils.obterData(geo.getIdentDataDigitalizacao(), "dd/MM/yyyy");
		} catch (Exception e) {
			return "título '" + geo.getIdentTituloProduto() + "' data de digitalização inválida: " + geo.getIdentDataDigitalizacao();
		}

		return "";
	}
	
	private List<String> getMunicipios() throws Exception {
		if (municipios == null) {
			municipios = new ArrayList<String>();
			for (Municipio municipio : cadastroServico.obterMunicipio(null, null)) {
				municipios.add(municipio.getNome());
			}
		}

		return municipios;
	}
}
