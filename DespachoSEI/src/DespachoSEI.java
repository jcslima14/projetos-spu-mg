import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class DespachoSEI extends JFrame {

	public DespachoSEI() {
		super("Despachos do SEI");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);

		Connection conexao = obterConexao();

		JMenuItem sbmDespacho = new JMenuItem("Despacho");
		JMenuItem sbmMunicipio = new JMenuItem("Município");
		JMenuItem sbmAssinante = new JMenuItem("Assinante");
		JMenuItem sbmAssinanteTipoDespacho = new JMenuItem("Tipo de Despacho por Assinante");
		JMenuItem sbmAssinanteMenu = new JMenu("Assinante") {{ add(sbmAssinante); add(sbmAssinanteTipoDespacho); }};
		JMenuItem sbmDestino = new JMenuItem("Destino");
		JMenuItem sbmTipoDespacho = new JMenuItem("Tipo de Despacho");
		JMenuItem sbmTipoImovel = new JMenuItem("Tipo de Imóvel");
		JMenuItem sbmTipoProcesso = new JMenuItem("Tipo de Processo");
		JMenuItem sbmInclusaoDespachoSEI = new JMenuItem("Inclusão de Despachos no SEI");
		JMenuItem sbmRespostaProcesso = new JMenuItem("Resposta a Processos");
		JMenuItem sbmImportacaoPlanilha = new JMenuItem("Importação de Planiliha");
		JMenuItem sbmImpressaoDespachos = new JMenuItem("Impressão de Despachos");
		JMenuItem sbmRecepcaoProcessos = new JMenuItem("Recepção de Processos do Sapiens");
		JMenuItem sbmProcessoRecebido = new JMenuItem("Processos Recebidos do Sapiens");
		JMenuItem sbmInclusaoSPUNet = new JMenuItem("Inclusão de Geometadados no SPUNet");
		JMenuItem sbmParametro = new JMenuItem("Parâmetros");
		JMenu mnuCadastro = new JMenu("Cadastro") {{ add(sbmDespacho); addSeparator();
													 add(sbmDestino); add(sbmAssinanteMenu); add(sbmMunicipio); addSeparator(); 
													 add(sbmTipoDespacho); add(sbmTipoProcesso); add(sbmTipoImovel); add(sbmParametro); addSeparator();
													 add(sbmProcessoRecebido); }};
		JMenu mnuProcessamento = new JMenu("Processamento") {{ add(sbmImportacaoPlanilha); addSeparator(); 
															   add(sbmRecepcaoProcessos); add(sbmInclusaoDespachoSEI); add(sbmImpressaoDespachos); add(sbmRespostaProcesso); 
//															   addSeparator(); add(sbmInclusaoSPUNet);  
															}};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuCadastro); add(mnuProcessamento); }};

		sbmInclusaoSPUNet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				InclusaoSPUNet janela = new InclusaoSPUNet("Inclusão no SPUNet", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmParametro.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ParametroCadastro janela = new ParametroCadastro("Parâmetros", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmProcessoRecebido.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProcessoRecebidoCadastro janela = new ProcessoRecebidoCadastro("Processos Recebidos do Sapiens", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmRecepcaoProcessos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RecepcaoProcesso janela = new RecepcaoProcesso("Recepção de Processos do Sapiens", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmRespostaProcesso.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RespostaProcesso cargaSEI = new RespostaProcesso("Resposta a Processos no Sapiens", conexao);
				desktop.add(cargaSEI);
				cargaSEI.abrirJanela();
			}
		});

		sbmInclusaoDespachoSEI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				InclusaoDespachoSEI geracaoDespachoSEI = new InclusaoDespachoSEI("Geração de Despachos no SEI", conexao);
				desktop.add(geracaoDespachoSEI);
				geracaoDespachoSEI.abrirJanela();
			}
		});
		
		sbmImpressaoDespachos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImpressaoDespacho janela = new ImpressaoDespacho("Impressão de Despachos", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmImportacaoPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportacaoPlanilha importacaoPlanilha = new ImportacaoPlanilha("Importação de Planilha de Dados", conexao);
				desktop.add(importacaoPlanilha);
				importacaoPlanilha.abrirJanela();
			}
		});
		
		sbmDespacho.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DespachoCadastro despacho = new DespachoCadastro("Despacho", conexao);
				desktop.add(despacho);
				despacho.abrirJanela();
			}
		});
		
		sbmTipoImovel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoImovelCadastro tipoImovel = new TipoImovelCadastro("Tipo de Imóvel", conexao);
				desktop.add(tipoImovel);
				tipoImovel.abrirJanela();
			}
		});
		
		sbmAssinante.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AssinanteCadastro assinante = new AssinanteCadastro("Assinante", conexao);
				desktop.add(assinante);
				assinante.abrirJanela();
			}
		});
		
		sbmAssinanteTipoDespacho.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AssinanteTipoDespachoCadastro janela = new AssinanteTipoDespachoCadastro("Tipo de Despacho x Assinante", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmMunicipio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MunicipioCadastro municipio = new MunicipioCadastro("Município", conexao);
				desktop.add(municipio);
				municipio.abrirJanela();
			}
		});
		
		sbmDestino.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DestinoCadastro destino = new DestinoCadastro("Destino", conexao);
				desktop.add(destino);
				destino.abrirJanela();
			}
		});
		
		sbmTipoProcesso.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoProcessoCadastro tipoProcesso = new TipoProcessoCadastro("Tipo de Processo", conexao);
				desktop.add(tipoProcesso);
				tipoProcesso.abrirJanela();
			}
		});
		
		sbmTipoDespacho.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoDespachoCadastro tipoDespacho = new TipoDespachoCadastro("Tipo de Despacho", conexao);
				desktop.add(tipoDespacho);
				tipoDespacho.abrirJanela();
			}
		});
		
		this.setJMenuBar(barraMenu);

		// exit the JVM when the window is closed
		this.addWindowStateListener(new WindowAdapter() { 
			public void windowClosed(WindowEvent e) { 
				System.exit(0); 
			} 
		});
    }

	private Connection obterConexao() {
		Connection conexao = null;

		try {
			conexao = DriverManager.getConnection("jdbc:sqlite:DespachoSEI.db");
			criarTabelas(conexao);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados. \n \n" + e1.getMessage());
			e1.printStackTrace();
		}

		return conexao;
	}

	public static void main(String args[]) {
		DespachoSEI app = new DespachoSEI();
		app.setSize(1000, 500);
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// app.pack(); 
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 

	private void criarTabelas(Connection conexao) throws Exception {
		criarTabelaAssinante(conexao);
		criarTabelaAssinanteTipoDespacho(conexao);
		criarTabelaDespacho(conexao);
		criarTabelaDestino(conexao);
		criarTabelaTipoDespacho(conexao);
		criarTabelaTipoImovel(conexao);
		criarTabelaTipoProcesso(conexao);
		criarTabelaMunicipio(conexao);
		criarTabelaProcessoRecebido(conexao);
		criarTabelaParametro(conexao);
	}

	private void criarTabelaAssinante(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "assinante")) {
			String sql = "CREATE TABLE assinante " + 
						 "(" + 
						 "  assinanteid integer primary key autoincrement not null," + 
						 "  nome varchar NOT NULL," + 
						 "  cargo varchar NOT NULL," + 
						 "  setor varchar NOT NULL," + 
						 "  superior boolean NOT NULL," + 
						 "  numeroprocesso varchar NOT NULL," + 
						 "  blocoassinatura varchar NOT NULL" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaAssinanteTipoDespacho(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "assinantetipodespacho")) {
			String sql = "CREATE TABLE assinantetipodespacho " + 
						 "(" + 
						 "  assinantetipodespachoid integer primary key autoincrement not null," + 
						 "  assinanteid integer not null," + 
						 "  tipodespachoid integer," + 
						 "  blocoassinatura varchar NOT NULL" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaDespacho(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "despacho")) {
			String sql = "CREATE TABLE despacho" + 
						 "(" + 
						 "  despachoid integer primary key autoincrement not null," + 
						 "  datadespacho date NOT NULL," + 
						 "  tipoprocessoid integer not null," + 
						 "  numeroprocesso varchar NOT NULL," + 
						 "  autor varchar NOT NULL," + 
						 "  comarca varchar NOT NULL," + 
						 "  tipoimovelid integer NOT NULL," + 
						 "  endereco varchar," + 
						 "  municipio varchar NOT NULL," + 
						 "  coordenada varchar," + 
						 "  area varchar," + 
						 "  tipodespachoid integer," + 
						 "  assinanteid integer," + 
						 "  destinoid integer," + 
						 "  observacao varchar," + 
						 "  numerodocumentosei varchar," + 
						 "  datahoradespacho datetime," + 
						 "  numeroprocessosei varchar," + 
						 "  arquivosanexados boolean not null," + 
						 "  despachoimpresso boolean," + 
						 "  datahoraimpressao datetime," + 
						 "  blocoassinatura varchar," + 
						 "  despachonoblocoassinatura boolean" + 
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaDestino(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "destino")) {
			String sql = "CREATE TABLE destino" + 
						 "(" + 
						 "  destinoid integer primary key autoincrement not null," +
						 "  abreviacao varchar NOT NULL," + 
						 "  artigo varchar NOT NULL," + 
						 "  descricao varchar NOT NULL," + 
						 "  usarcomarca boolean NOT NULL," + 
						 "  caminhodespachos varchar NOT NULL" + 
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaTipoDespacho(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipodespacho")) {
			String sql = "CREATE TABLE tipodespacho" + 
						 "(" + 
						 "  tipodespachoid integer primary key autoincrement not null," + 
						 "  descricao varchar NOT NULL," + 
						 "  numerodocumentosei varchar NOT NULL," + 
						 "  gerarprocessoindividual boolean NOT NULL," + 
						 "  unidadeaberturaprocesso varchar," + 
						 "  tipoprocesso varchar" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaTipoImovel(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipoimovel")) {
			String sql = "CREATE TABLE tipoimovel" + 
						 "(" + 
						 "  tipoimovelid integer primary key autoincrement not null," + 
						 "  descricao varchar NOT NULL" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaTipoProcesso(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipoprocesso")) {
			String sql = "CREATE TABLE tipoprocesso (" + 
						 "  tipoprocessoid integer primary key autoincrement not null," + 
						 "  descricao varchar not null" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaMunicipio(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "municipio")) {
			String sql = "CREATE TABLE municipio " + 
						 "(" + 
						 "  municipioid integer primary key autoincrement not null," + 
						 "  nome varchar NOT NULL," + 
						 "  municipioidcomarca integer," +
						 "  destinoid integer" +
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaProcessoRecebido(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "processorecebido")) {
			String sql = "CREATE TABLE processorecebido " + 
						 "(" + 
						 "  processorecebidoid integer primary key autoincrement not null," + 
						 "  numerounico varchar NOT NULL," + 
						 "  datahoramovimentacao integer NOT NULL," +
						 "  municipioid integer," +
						 "  resultadodownload varchar," + 
						 "  arquivosprocessados boolean," +
						 "  resultadoprocessamento varchar" + 
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaParametro(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "parametro")) {
			String sql = "CREATE TABLE parametro " + 
						 "(" + 
						 "  parametroid integer primary key not null," + 
						 "  descricao varchar NOT NULL," + 
						 "  conteudo varchar NOT NULL" +
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}
}
