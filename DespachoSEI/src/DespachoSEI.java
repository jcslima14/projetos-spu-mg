import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map.Entry;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import framework.MyUtils;

@SuppressWarnings("serial")
public class DespachoSEI extends JFrame {

	public DespachoSEI() {
		super("Respostas do SEI");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);

		Connection conexao = obterConexao();

		JMenuItem sbmProcessoRecebido = new JMenuItem("Processamento de Solicita��o de An�lise Recebida");
		JMenuItem sbmSolicitacaoAnalise = new JMenuItem("Solicita��o de An�lise");
		JMenuItem sbmSolicitacaoResposta = new JMenuItem("Resposta � Solicita��o de An�lise");
		JMenuItem sbmSolicitacaoAnaliseMenu = new JMenu("Solicita��o de An�lise") {{ add(sbmSolicitacaoAnalise); add(sbmSolicitacaoResposta); add(sbmProcessoRecebido); }};
		JMenuItem sbmMunicipio = new JMenuItem("Munic�pio");
		JMenuItem sbmMunicipioTipoResposta = new JMenuItem("Tipo de Resposta por Munic�pio");
		JMenuItem sbmMunicipioMenu = new JMenu("Munic�pio") {{ add(sbmMunicipio); add(sbmMunicipioTipoResposta); }};
		JMenuItem sbmAssinante = new JMenuItem("Assinante");
		JMenuItem sbmAssinanteTipoResposta = new JMenuItem("Tipo de Resposta por Assinante");
		JMenuItem sbmAssinanteMenu = new JMenu("Assinante") {{ add(sbmAssinante); add(sbmAssinanteTipoResposta); }};
		JMenuItem sbmDestino = new JMenuItem("Destino");
		JMenuItem sbmTipoResposta = new JMenuItem("Tipo de Resposta");
		JMenuItem sbmTipoImovel = new JMenuItem("Tipo de Im�vel");
		JMenuItem sbmTipoProcesso = new JMenuItem("Tipo de Processo");
		JMenuItem sbmOrigem = new JMenuItem("Origem");
		JMenuItem sbmInclusaoRespostaSEI = new JMenuItem("Inclus�o de Respostas no SEI");
		JMenuItem sbmRespostaSapiens = new JMenuItem("Resposta a Processos do Sapiens");
		JMenuItem sbmImportacaoPlanilha = new JMenuItem("Importa��o de Planiliha");
		JMenuItem sbmImpressaoRespostas = new JMenuItem("Impress�o de Respostas");
		JMenuItem sbmRecepcaoProcessos = new JMenuItem("Recep��o de Processos do Sapiens");
		JMenuItem sbmRespostaSPUNet = new JMenuItem("Resposta a Processos do SPUNet");
		JMenuItem sbmParametro = new JMenuItem("Par�metros");
		JMenuItem sbmExecucaoScript = new JMenuItem("Execu��o de Scripts");
		JMenuItem sbmRespostaProcesso = new JMenu("Resposta a Processos") {{ add(sbmRespostaSapiens); add(sbmRespostaSPUNet); }};
		JMenu mnuCadastro = new JMenu("Cadastro") {{ add(sbmSolicitacaoAnaliseMenu); addSeparator();
													 add(sbmDestino); add(sbmAssinanteMenu); add(sbmMunicipioMenu); addSeparator(); 
													 add(sbmTipoResposta); add(sbmTipoProcesso); add(sbmTipoImovel); add(sbmOrigem); add(sbmParametro); 
												  }};
		JMenu mnuProcessamento = new JMenu("Processamento") {{ add(sbmImportacaoPlanilha); addSeparator(); 
															   add(sbmRecepcaoProcessos); add(sbmInclusaoRespostaSEI); add(sbmImpressaoRespostas); add(sbmRespostaProcesso); 
															}};
		JMenu mnuFerramenta = new JMenu("Ferramentas") {{ add(sbmExecucaoScript); }};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuCadastro); add(mnuProcessamento); add(mnuFerramenta); }};

		sbmExecucaoScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExecucaoScript janela = new ExecucaoScript("Execu��o de Scripts", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmRespostaSPUNet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RespostaSPUNet janela = new RespostaSPUNet("Resposta a Processos do SPUNet", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmParametro.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ParametroCadastro janela = new ParametroCadastro("Par�metros", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmOrigem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OrigemCadastro janela = new OrigemCadastro("Origem de Solicita��o", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmProcessoRecebido.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProcessoRecebidoCadastro janela = new ProcessoRecebidoCadastro("Processamento de Solicita��o de An�lise Recebida", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmSolicitacaoResposta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DespachoCadastro janela = new DespachoCadastro("Resposta � Solicita��o de An�lise", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmRecepcaoProcessos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RecepcaoProcesso janela = new RecepcaoProcesso("Recep��o de Processos do Sapiens", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmRespostaSapiens.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RespostaProcesso cargaSEI = new RespostaProcesso("Resposta a Processos no Sapiens", conexao);
				desktop.add(cargaSEI);
				cargaSEI.abrirJanela();
			}
		});

		sbmInclusaoRespostaSEI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				InclusaoDespachoSEI geracaoRespostaSEI = new InclusaoDespachoSEI("Gera��o de Respostas no SEI", conexao);
				desktop.add(geracaoRespostaSEI);
				geracaoRespostaSEI.abrirJanela();
			}
		});
		
		sbmImpressaoRespostas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImpressaoDespacho janela = new ImpressaoDespacho("Impress�o de Respostas", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmImportacaoPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportacaoPlanilha importacaoPlanilha = new ImportacaoPlanilha("Importa��o de Planilha de Dados", conexao);
				desktop.add(importacaoPlanilha);
				importacaoPlanilha.abrirJanela();
			}
		});
		
		sbmSolicitacaoAnalise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SolicitacaoAnaliseConsulta janela = new SolicitacaoAnaliseConsulta("Solicita��o de An�lise", conexao, desktop);
				janela.abrirJanela();
			}
		});
		
		sbmTipoImovel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoImovelCadastro tipoImovel = new TipoImovelCadastro("Tipo de Im�vel", conexao);
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
		
		sbmAssinanteTipoResposta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AssinanteTipoDespachoCadastro janela = new AssinanteTipoDespachoCadastro("Tipo de Resposta x Assinante", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmMunicipioTipoResposta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MunicipioTipoRespostaCadastro janela = new MunicipioTipoRespostaCadastro("Tipo de Resposta x Munic�pio", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmMunicipio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MunicipioCadastro municipio = new MunicipioCadastro("Munic�pio", conexao);
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
		
		sbmTipoResposta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoRespostaCadastro tipoResposta = new TipoRespostaCadastro("Tipo de Resposta", conexao);
				desktop.add(tipoResposta);
				tipoResposta.abrirJanela();
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
//			conexao = DriverManager.getConnection("jdbc:sqlite:L:\\DIVERSOS\\Ferramentas SPU\\Despacho SEI\\DespachoSEI.db");
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
		criarTabelaAssinanteTipoResposta(conexao);
		criarTabelaSolicitacao(conexao);
		criarTabelaSolicitacaoEnvio(conexao);
		criarTabelaSolicitacaoResposta(conexao);
		criarTabelaDestino(conexao);
		criarTabelaTipoResposta(conexao);
		criarTabelaTipoImovel(conexao);
		criarTabelaTipoProcesso(conexao);
		criarTabelaMunicipio(conexao);
		criarTabelaMunicipioTipoResposta(conexao);
		criarTabelaParametro(conexao);
		criarTabelaOrigem(conexao);
	}

	private void criarTabelaAssinante(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "assinante")) {
			String sql = "CREATE TABLE assinante " + 
						 "(" + 
						 "  assinanteid integer primary key autoincrement not null," + 
						 "  nome varchar NOT NULL," + 
						 "  ativo boolean NOT NULL," + 
						 "  cargo varchar NOT NULL," + 
						 "  setor varchar NOT NULL," + 
						 "  superior boolean NOT NULL," + 
						 "  numeroprocessosei varchar NOT NULL," + 
						 "  blocoassinatura varchar NOT NULL," + 
						 "  pastaarquivoprocesso varchar NOT NULL" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaAssinanteTipoResposta(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "assinantetiporesposta")) {
			String sql = "CREATE TABLE assinantetiporesposta " + 
						 "(" + 
						 "  assinantetiporespostaid integer primary key autoincrement not null," + 
						 "  assinanteid integer not null," + 
						 "  tiporespostaid integer," + 
						 "  blocoassinatura varchar NOT NULL" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaSolicitacao(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "solicitacao")) {
			String sql = "CREATE TABLE solicitacao " + 
						 "(" + 
						 "  solicitacaoid integer primary key autoincrement not null," +
						 "  origemid integer NOT NULL," +
						 "  tipoprocessoid integer not null," + 
						 "  numeroprocesso varchar NOT NULL," + 
						 "  chavebusca varchar NOT NULL," + 
						 "  autor varchar NOT NULL," + 
						 "  municipioid integer," +
						 "  destinoid integer," + 
						 "  cartorio varchar," + 
						 "  tipoimovelid integer," + 
						 "  endereco varchar," + 
						 "  coordenada varchar," + 
						 "  area varchar," + 
						 "  numeroprocessosei varchar," + 
						 "  arquivosanexados boolean" + 
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaSolicitacaoResposta(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "solicitacaoresposta")) {
			String sql = "CREATE TABLE solicitacaoresposta " + 
						 "(" +
						 "  solicitacaorespostaid integer primary key autoincrement not null," +
						 "  solicitacaoid integer not null," +
						 "  tiporespostaid integer null," + 
						 "  observacao varchar null," + 
						 "  assinanteid integer not null," + 
						 "  assinanteidsuperior integer," + 
						 "  numerodocumentosei varchar," + 
						 "  datahoraresposta datetime," + 
						 "  numeroprocessosei varchar," + 
						 "  respostaimpressa boolean not null," + 
						 "  datahoraimpressao datetime," + 
						 "  blocoassinatura varchar," + 
						 "  respostanoblocoassinatura boolean not null" + 
						 ")";

			MyUtils.execute(conexao, sql);
			MyUtils.execute(conexao, "CREATE INDEX ix_solicitacaoresposta_001 ON solicitacaoresposta (solicitacaoid)");
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
						 "  usarcartorio boolean NOT NULL" + 
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaTipoResposta(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tiporesposta")) {
			String sql = "CREATE TABLE tiporesposta" + 
						 "(" + 
						 "  tiporespostaid integer primary key autoincrement not null," + 
						 "  descricao varchar NOT NULL," + 
						 "  tipodocumento varchar NOT NULL," + 
						 "  numerodocumentomodelo varchar NOT NULL," + 
						 "  gerarprocessoindividual boolean NOT NULL," + 
						 "  unidadeaberturaprocesso varchar," + 
						 "  tipoprocesso varchar," + 
						 "  imprimirresposta boolean not null," +
						 "  quantidadeassinaturas integer," +
						 "  origemid integer," +
						 "  respostaspunet varchar," + 
						 "  complementospunet varchar" + 
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
			
			preencherTabelaTipoImovel(conexao);
		}
	}

	private void preencherTabelaTipoImovel(Connection conexao) throws Exception {
		for (TipoImovel tipoImovel : TipoImovel.TIPOS_IMOVEIS) {
			MyUtils.execute(conexao, "insert into tipoimovel (tipoimovelid, descricao) values (" + tipoImovel.getTipoImovelId() + ", '" + tipoImovel.getDescricao() + "')");
		}
	}

	private void criarTabelaTipoProcesso(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipoprocesso")) {
			String sql = "CREATE TABLE tipoprocesso (" + 
						 "  tipoprocessoid integer primary key autoincrement not null," + 
						 "  descricao varchar not null" + 
						 ")"; 
	
			MyUtils.execute(conexao, sql);
			
			preencherTabelaTipoProcesso(conexao);
		}
	}

	private void preencherTabelaTipoProcesso(Connection conexao) throws Exception {
		for (TipoProcesso tipoProcesso : TipoProcesso.TIPOS_PROCESSO) {
			MyUtils.execute(conexao, "insert into tipoprocesso (tipoprocessoid, descricao) values (" + tipoProcesso.getTipoProcessoId() + ", '" + tipoProcesso.getDescricao() + "')");
		}
	}

	private void criarTabelaMunicipio(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "municipio")) {
			String sql = "CREATE TABLE municipio " + 
						 "(" + 
						 "  municipioid integer primary key autoincrement not null," + 
						 "  nome varchar NOT NULL," + 
						 "  municipioidcomarca integer," +
						 "  destinoid integer," +
						 "  tiporespostaid integer" +
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaMunicipioTipoResposta(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "municipiotiporesposta")) {
			String sql = "CREATE TABLE municipiotiporesposta " + 
						 "(" + 
						 "  municipiotiporespostaid integer primary key autoincrement not null," + 
						 "  municipioid integer not null," + 
						 "  origemid integer not null," +
						 "  tiporespostaid integer not null" +
						 ")"; 

			MyUtils.execute(conexao, sql);
		}
	}

	private void criarTabelaSolicitacaoEnvio(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "solicitacaoenvio")) {
			String sql = "CREATE TABLE solicitacaoenvio " + 
						 "(" +
						 "  solicitacaoenvioid integer primary key autoincrement not null," +
						 "  solicitacaoid integer not null," +
						 "  datahoramovimentacao integer NOT NULL," +
						 "  resultadodownload varchar," + 
						 "  arquivosprocessados boolean," +
						 "  resultadoprocessamento varchar" + 
						 ")";

			MyUtils.execute(conexao, sql);
			MyUtils.execute(conexao, "CREATE INDEX ix_solicitacaoenvio_001 ON solicitacaoenvio (solicitacaoid)");
		}
	}

	private void criarTabelaParametro(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "parametro")) {
			String sql = "CREATE TABLE parametro " + 
						 "(" + 
						 "  parametroid integer primary key not null," + 
						 "  descricao varchar NOT NULL," + 
						 "  conteudo varchar NOT NULL," +
						 "  ativo boolean NOT NULL" +
						 ")"; 

			MyUtils.execute(conexao, sql);
			
			preencherTabelaParametro(conexao);
		}
	}

	private void preencherTabelaParametro(Connection conexao) throws Exception {
		for (Entry<Integer, String[]> parametro : Parametro.DESCRICOES.entrySet()) {
			MyUtils.execute(conexao, "insert into parametro (parametroid, descricao, conteudo, ativo) values (" + parametro.getKey() + ", '" + parametro.getValue()[0] + "', '" + parametro.getValue()[1] + "', true)");
		}
	}

	private void criarTabelaOrigem(Connection conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "origem")) {
			String sql = "CREATE TABLE origem " + 
						 "(" + 
						 "  origemid integer primary key not null," + 
						 "  descricao varchar NOT NULL" + 
						 ")"; 

			MyUtils.execute(conexao, sql);

			preencherTabelaOrigem(conexao);
		}
	}

	private void preencherTabelaOrigem(Connection conexao) throws Exception {
		for (Origem origem : Origem.ORIGENS) {
			MyUtils.execute(conexao, "insert into origem (origemid, descricao) values (" + origem.getOrigemId() + ", '" + origem.getDescricao() + "')");
		}
	}
}
