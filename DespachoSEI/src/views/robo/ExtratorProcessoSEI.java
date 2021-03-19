package views.robo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import framework.enums.NivelMensagem;
import framework.services.SEIService;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;
import model.Parametro;
import services.DespachoServico;

@SuppressWarnings("serial")
public class ExtratorProcessoSEI extends JInternalFrame {

	private JTextField txtUsuario = new JTextField(15);
	private JLabel lblUsuario = new JLabel("Usuário:") {{ setLabelFor(txtUsuario); }};
	private JPasswordField txtSenha = new JPasswordField(15);
	private JLabel lblSenha = new JLabel("Senha:") {{ setLabelFor(txtSenha); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JButton btnProcessar = new JButton("Processar"); 
	private JTextPane logArea = MyUtils.obterPainelNotificacoes();
	private JScrollPane areaDeRolagem = new JScrollPane(logArea) {{ getViewport().setPreferredSize(new Dimension(1200, 500)); }};
	private DespachoServico despachoServico;

	public ExtratorProcessoSEI(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		despachoServico = new DespachoServico(conexao);

		painelDados.add(lblUsuario);
		painelDados.add(txtUsuario);
		painelDados.add(lblSenha);
		painelDados.add(txtSenha);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            3, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);
		add(areaDeRolagem, BorderLayout.SOUTH);
		
		btnProcessar.addActionListener(MyUtils.executarProcessoComLog(logArea, new Runnable() {
			@Override
			public void run() {
				extrairProcessosSEI(txtUsuario.getText(), new String(txtSenha.getPassword()));
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void extrairProcessosSEI(String usuario, String senha) throws RuntimeException {
		try {
			MyUtils.appendLogArea(logArea, "Iniciando o navegador web...", NivelMensagem.DESTAQUE_NEGRITO);

	        SEIService seiServico = new SEIService("chrome", despachoServico.obterConteudoParametro(Parametro.ENDERECO_SEI));
	        seiServico.login(usuario, senha, despachoServico.obterConteudoParametro(Parametro.ORGAO_LOGIN_SEI));
	        
	        List<String> unidades = seiServico.obterUnidadesDisponiveis();
	        Map<String, List<Processo>> mapaProcessos = new LinkedHashMap<String, List<Processo>>();

	        for (String unidade : unidades) {
				MyUtils.appendLogArea(logArea, "Núcleo: " + unidade, NivelMensagem.DESTAQUE_NEGRITO);
		        seiServico.selecionarUnidadePadrao(unidade);
		        int pagina = 1;

//		        if (unidade.equals("SPU-MG-NUCIP")) continue;
		        
		        seiServico.clicarVisualizacaoDetalhada();
		        mapaProcessos.put(unidade, new ArrayList<Processo>());

		        do {
			        int contador = 1;

			        MyUtils.appendLogArea(logArea, "Página: " + pagina++, NivelMensagem.DESTAQUE_ITALICO);

			        // executa os botôes de expandir a lista de interessados
			        seiServico.expandirInteressadosListaDetalhadaProcesso();
			        
			        List<WebElement> linhas = seiServico.obterListaProcessoDetalhado();

		        	// percorre a lista de processos extraindo os dados necessários para montar a planilha
		        	for (WebElement linha : linhas) {
		        		Processo processo = obterProcesso(linha);
		        		MyUtils.appendLogArea(logArea, contador++ + ") Processo: " + processo.getNumeroProcesso());
		        		mapaProcessos.get(unidade).add(processo);
		        	}

		        	if (!seiServico.clicouProximaPagina()) {
		        		break;
		        	}
		        } while (true);
	        }

			seiServico.fechaNavegador();

			MyUtils.appendLogArea(logArea, "Exportando os dados para planilha Excel...", NivelMensagem.DESTAQUE_NEGRITO);

			exportarArquivo(mapaProcessos);
			
			MyUtils.appendLogArea(logArea, "Fim do Processamento...", NivelMensagem.OK);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Processo obterProcesso(WebElement linha) {
		Processo processo = new Processo();
		processo.setNumeroProcesso(linha.findElement(By.xpath("./td[3]")).getText());
		processo.setAtribuidoPara(linha.findElement(By.xpath("./td[4]")).getText().replace("(", "").replace(")", ""));
		processo.setTipoProcesso(linha.findElement(By.xpath("./td[5]")).getText());

		String interessado = "";
		
		try {
			List<WebElement> divInteressados = linha.findElements(By.xpath("./td[6]//div[@class = 'divItemCelula']/div[@class = 'divDiamante']/following-sibling::div"));

			for (WebElement divInteressado : divInteressados) {
				if (!interessado.equals("")) interessado += "\n";
				interessado += divInteressado.getText().replaceAll("\\n","").trim();
			}
		} catch (Exception e) {
		}

		processo.setInteressado(interessado);
		
		// encontrar os marcadores
		List<Marcador> marcadores = new ArrayList<Marcador>();
		
		for (WebElement lnkMarcador : linha.findElements(By.xpath("./td[2]/a"))) {
			Marcador marcador = new Marcador();
			String onmouseover = lnkMarcador.getAttribute("onmouseover").replace("return infraTooltipMostrar('", "").replace("');", "");
			String src = lnkMarcador.findElement(By.xpath("./img")).getAttribute("src");
			String[] dica = onmouseover.split("','");
			marcador.setTipo(src.substring(src.lastIndexOf("/") + 1));
			marcador.setTitulo(dica.length > 1 ? dica[1] : "");
			marcador.setDescricao(dica[0]);
			marcadores.add(marcador);
		}

		processo.setMarcadores(marcadores);
		return processo;
	}

	private void exportarArquivo(Map<String, List<Processo>> dados) throws Exception {
		Workbook wb = new XSSFWorkbook();
		
		Font fonteCabecalho = wb.createFont();
		fonteCabecalho.setBold(true);
		CellStyle estiloCabecalho = wb.createCellStyle();
		estiloCabecalho.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		estiloCabecalho.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		estiloCabecalho.setFont(fonteCabecalho);
		CellStyle estiloDadoNormal = wb.createCellStyle();
		estiloDadoNormal.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		estiloDadoNormal.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		estiloDadoNormal.setVerticalAlignment(VerticalAlignment.TOP);
		CellStyle estiloDadoDestaque = wb.createCellStyle();
		estiloDadoDestaque.setVerticalAlignment(VerticalAlignment.TOP);

		for (String unidade : dados.keySet()) {
			wb.createSheet(unidade);
			Sheet planilha = wb.getSheet(unidade);
			int l = -1;
			int c = -1;
			planilha.createRow(++l);
			adicionarCelula(planilha.getRow(l), ++c, "Nº Processo", estiloCabecalho);
			adicionarCelula(planilha.getRow(l), ++c, "Atribuído Para", estiloCabecalho);
			adicionarCelula(planilha.getRow(l), ++c, "Tipo de Processo", estiloCabecalho);
			adicionarCelula(planilha.getRow(l), ++c, "Interessados", estiloCabecalho);
			adicionarCelula(planilha.getRow(l), ++c, "Marcadores", estiloCabecalho);

			for (Processo processo : dados.get(unidade)) {
				c = -1;
				planilha.createRow(++l);
				adicionarCelula(planilha.getRow(l), ++c, processo.getNumeroProcesso(), l % 2 == 0 ? estiloDadoNormal : estiloDadoDestaque);
				adicionarCelula(planilha.getRow(l), ++c, processo.getAtribuidoPara(), l % 2 == 0 ? estiloDadoNormal : estiloDadoDestaque);
				adicionarCelula(planilha.getRow(l), ++c, processo.getTipoProcesso(), l % 2 == 0 ? estiloDadoNormal : estiloDadoDestaque);
				adicionarCelula(planilha.getRow(l), ++c, processo.getInteressado(), l % 2 == 0 ? estiloDadoNormal : estiloDadoDestaque);
				adicionarCelula(planilha.getRow(l), ++c, processo.getMarcadoresTexto(), l % 2 == 0 ? estiloDadoNormal : estiloDadoDestaque);
				planilha.getRow(l).setHeightInPoints((processo.getTamanhoLinhaExcel() * planilha.getDefaultRowHeightInPoints()));

			}
			
			for (int i = 0; i <= 4; i++) {
				planilha.autoSizeColumn(i);
			}
		}

		String nomeArquivo = Paths.get(System.getProperty("user.home"), "Downloads") + File.separator + "Processos Abertos no SEI - " + MyUtils.formatarData(new Date(), "dd-MM-yyyy HH_mm_ss") + ".xlsx";
		FileOutputStream fos = new FileOutputStream(nomeArquivo);
		wb.write(fos);
		wb.close();
		fos.flush();
		fos.close();
	}

	private void adicionarCelula(Row linha, int coluna, String conteudo, CellStyle estilo) {
		Cell celula = linha.createCell(coluna);
		celula.setCellStyle(estilo);
		celula.setCellValue(conteudo);
	}
	
	private class Processo {
		private String numeroProcesso;
		private String atribuidoPara;
		private String tipoProcesso;
		private String interessado;
		private List<Marcador> marcadores;
		
		public String getNumeroProcesso() {
			return numeroProcesso;
		}
		public void setNumeroProcesso(String numeroProcesso) {
			this.numeroProcesso = numeroProcesso;
		}
		public String getAtribuidoPara() {
			return atribuidoPara;
		}
		public void setAtribuidoPara(String atribuidoPara) {
			this.atribuidoPara = atribuidoPara;
		}
		public String getTipoProcesso() {
			return tipoProcesso;
		}
		public void setTipoProcesso(String tipoProcesso) {
			this.tipoProcesso = tipoProcesso;
		}
		public String getInteressado() {
			return interessado;
		}
		public void setInteressado(String interessado) {
			this.interessado = interessado;
		}
		public List<Marcador> getMarcadores() {
			return marcadores;
		}
		public void setMarcadores(List<Marcador> marcadores) {
			this.marcadores = marcadores;
		}
		
		public String getMarcadoresTexto() {
			String retorno = "";
			for (Marcador marcador : getMarcadores()) {
				if (!retorno.equals("")) retorno += "\n";
				retorno += "Tipo: " + marcador.getTipo();
				if (!marcador.getTitulo().trim().equals("")) retorno += "; Título: " + marcador.getTitulo();
				if (!marcador.getDescricao().trim().equals("")) retorno += "; Descrição: " + marcador.getDescricao();
			}
			return retorno;
		}

		public int getTamanhoLinhaExcel() {
			int retorno = 1;
			if (getInteressado().split("\\n").length > retorno) retorno = getInteressado().split("\\n").length;
			if (getMarcadores().size() > retorno) retorno = getMarcadores().size();
			return retorno;
		}
	}

	private class Marcador {
		private String tipo;
		private String titulo;
		private String descricao;
		
		public String getTipo() {
			return tipo;
		}
		public void setTipo(String tipo) {
			this.tipo = tipo;
		}
		public String getTitulo() {
			return titulo;
		}
		public void setTitulo(String titulo) {
			this.titulo = titulo;
		}
		public String getDescricao() {
			return descricao;
		}
		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
	}
}
