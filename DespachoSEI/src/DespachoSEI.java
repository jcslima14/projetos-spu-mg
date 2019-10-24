import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import framework.JPAUtils;
import framework.MyUtils;

@SuppressWarnings("serial")
public class DespachoSEI extends JFrame {

	public DespachoSEI() {
		super("Respostas do SEI");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);

		EntityManager conexao = obterConexaoEM();

		JMenuItem sbmProcessoRecebido = new JMenuItem("Processamento de Solicitação de Análise Recebida");
		JMenuItem sbmSolicitacaoAnalise = new JMenuItem("Solicitação de Análise");
		JMenuItem sbmSolicitacaoResposta = new JMenuItem("Resposta à Solicitação de Análise");
		JMenuItem sbmSolicitacaoAnaliseMenu = new JMenu("Solicitação de Análise") {{ add(sbmSolicitacaoAnalise); add(sbmSolicitacaoResposta); add(sbmProcessoRecebido); }};
		JMenuItem sbmMunicipio = new JMenuItem("Município");
		JMenuItem sbmMunicipioTipoResposta = new JMenuItem("Tipo de Resposta por Município");
		JMenuItem sbmMunicipioMenu = new JMenu("Município") {{ add(sbmMunicipio); add(sbmMunicipioTipoResposta); }};
		JMenuItem sbmAssinante = new JMenuItem("Assinante");
		JMenuItem sbmAssinanteTipoResposta = new JMenuItem("Tipo de Resposta por Assinante");
		JMenuItem sbmAssinanteMenu = new JMenu("Assinante") {{ add(sbmAssinante); add(sbmAssinanteTipoResposta); }};
		JMenuItem sbmDestino = new JMenuItem("Destino");
		JMenuItem sbmTipoResposta = new JMenuItem("Tipo de Resposta");
		JMenuItem sbmTipoImovel = new JMenuItem("Tipo de Imóvel");
		JMenuItem sbmTipoProcesso = new JMenuItem("Tipo de Processo");
		JMenuItem sbmOrigem = new JMenuItem("Origem");
		JMenuItem sbmInclusaoRespostaSEI = new JMenuItem("Inclusão de Respostas no SEI");
		JMenuItem sbmRespostaSapiens = new JMenuItem("Resposta a Processos do Sapiens");
		JMenuItem sbmImportacaoPlanilha = new JMenuItem("Importação de Planiliha");
		JMenuItem sbmImpressaoRespostas = new JMenuItem("Impressão de Respostas");
		JMenuItem sbmRecepcaoProcessos = new JMenuItem("Recepção de Processos do Sapiens");
		JMenuItem sbmRespostaSPUNet = new JMenuItem("Resposta a Processos do SPUNet");
		JMenuItem sbmParametro = new JMenuItem("Parâmetros");
		JMenuItem sbmExecucaoScript = new JMenuItem("Execução de Scripts");
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
				ExecucaoScript janela = new ExecucaoScript("Execução de Scripts", conexao);
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
				ParametroCadastro janela = new ParametroCadastro("Parâmetros", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmOrigem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OrigemCadastro janela = new OrigemCadastro("Origem de Solicitação", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmProcessoRecebido.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ProcessoRecebidoCadastro janela = new ProcessoRecebidoCadastro("Processamento de Solicitação de Análise Recebida", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmSolicitacaoResposta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DespachoCadastro janela = new DespachoCadastro("Resposta à Solicitação de Análise", conexao);
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
				InclusaoDespachoSEI geracaoRespostaSEI = new InclusaoDespachoSEI("Geração de Respostas no SEI", conexao);
				desktop.add(geracaoRespostaSEI);
				geracaoRespostaSEI.abrirJanela();
			}
		});
		
		sbmImpressaoRespostas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImpressaoDespacho janela = new ImpressaoDespacho("Impressão de Respostas", conexao);
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
		
		sbmSolicitacaoAnalise.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SolicitacaoAnaliseConsulta janela = new SolicitacaoAnaliseConsulta("Solicitação de Análise", conexao, desktop);
				janela.abrirJanela();
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
				MunicipioTipoRespostaCadastro janela = new MunicipioTipoRespostaCadastro("Tipo de Resposta x Município", conexao);
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

	private EntityManager obterConexaoEM() {
		EntityManager conexao = null;
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("DespachoSEI");
			conexao = emf.createEntityManager();
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

	private void criarTabelas(EntityManager conexao) throws Exception {
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

	private void criarTabelaAssinante(EntityManager conexao) throws Exception {
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
	
			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaAssinanteTipoResposta(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "assinantetiporesposta")) {
			String sql = "CREATE TABLE assinantetiporesposta " + 
						 "(" + 
						 "  assinantetiporespostaid integer primary key autoincrement not null," + 
						 "  assinanteid integer not null," + 
						 "  tiporespostaid integer," + 
						 "  blocoassinatura varchar NOT NULL" + 
						 ")"; 
	
			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaSolicitacao(EntityManager conexao) throws Exception {
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

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaSolicitacaoResposta(EntityManager conexao) throws Exception {
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

			JPAUtils.executeUpdate(conexao, sql);
			JPAUtils.executeUpdate(conexao, "CREATE INDEX ix_solicitacaoresposta_001 ON solicitacaoresposta (solicitacaoid)");
		}
	}

	private void criarTabelaDestino(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "destino")) {
			String sql = "CREATE TABLE destino" + 
						 "(" + 
						 "  destinoid integer primary key autoincrement not null," +
						 "  abreviacao varchar NOT NULL," + 
						 "  artigo varchar NOT NULL," + 
						 "  descricao varchar NOT NULL," + 
						 "  usarcartorio boolean NOT NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaTipoResposta(EntityManager conexao) throws Exception {
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

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaTipoImovel(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipoimovel")) {
			String sql = "CREATE TABLE tipoimovel" + 
						 "(" + 
						 "  tipoimovelid integer primary key autoincrement not null," + 
						 "  descricao varchar NOT NULL" + 
						 ")"; 
	
			JPAUtils.executeUpdate(conexao, sql);
		}
		
		preencherTabelaTipoImovel(conexao);
	}

	private void preencherTabelaTipoImovel(EntityManager conexao) throws Exception {
		DespachoServico despachoServico = new DespachoServico(conexao);
		for (TipoImovel tipoImovel : TipoImovel.TIPOS_IMOVEIS) {
			if (despachoServico.obterTipoImovel(tipoImovel.getTipoImovelId(), null).size() == 0) {
				JPAUtils.persistir(conexao, tipoImovel);
			}
		}
	}

	private void criarTabelaTipoProcesso(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "tipoprocesso")) {
			String sql = "CREATE TABLE tipoprocesso (" + 
						 "  tipoprocessoid integer primary key autoincrement not null," + 
						 "  descricao varchar not null" + 
						 ")"; 
	
			JPAUtils.executeUpdate(conexao, sql);
		}
		
		preencherTabelaTipoProcesso(conexao);
	}

	private void preencherTabelaTipoProcesso(EntityManager conexao) throws Exception {
		DespachoServico despachoServico = new DespachoServico(conexao);
		for (TipoProcesso tipoProcesso : TipoProcesso.TIPOS_PROCESSO) {
			if (despachoServico.obterTipoProcesso(tipoProcesso.getTipoProcessoId(), null).size() == 0) {
				JPAUtils.persistir(conexao, tipoProcesso);
			}
		}
	}

	private void criarTabelaMunicipio(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "municipio")) {
			String sql = "CREATE TABLE municipio " + 
						 "(" + 
						 "  municipioid integer primary key autoincrement not null," + 
						 "  nome varchar NOT NULL," + 
						 "  municipioidcomarca integer," +
						 "  destinoid integer," +
						 "  tiporespostaid integer" +
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaMunicipioTipoResposta(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "municipiotiporesposta")) {
			String sql = "CREATE TABLE municipiotiporesposta " + 
						 "(" + 
						 "  municipiotiporespostaid integer primary key autoincrement not null," + 
						 "  municipioid integer not null," + 
						 "  origemid integer not null," +
						 "  tiporespostaid integer not null" +
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaSolicitacaoEnvio(EntityManager conexao) throws Exception {
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

			JPAUtils.executeUpdate(conexao, sql);
			JPAUtils.executeUpdate(conexao, "CREATE INDEX ix_solicitacaoenvio_001 ON solicitacaoenvio (solicitacaoid)");
		}
	}

	private void criarTabelaParametro(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "parametro")) {
			String sql = "CREATE TABLE parametro " + 
						 "(" + 
						 "  parametroid integer primary key not null," + 
						 "  descricao varchar NOT NULL," + 
						 "  conteudo varchar NOT NULL," +
						 "  ativo boolean NOT NULL" +
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
		
		preencherTabelaParametro(conexao);
	}

	private void preencherTabelaParametro(EntityManager conexao) throws Exception {
		DespachoServico despachoServico = new DespachoServico(conexao);
		for (Entry<Integer, String[]> parametro : Parametro.DESCRICOES.entrySet()) {
			if (despachoServico.obterParametro(parametro.getKey(), null).size() == 0) {
				JPAUtils.persistir(conexao, new Parametro(parametro.getKey(), parametro.getValue()[0], parametro.getValue()[1], true));
			}
		}
	}

	private void criarTabelaOrigem(EntityManager conexao) throws Exception {
		if (!MyUtils.tabelaExiste(conexao, "origem")) {
			String sql = "CREATE TABLE origem " + 
						 "(" + 
						 "  origemid integer primary key not null," + 
						 "  descricao varchar NOT NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}

		preencherTabelaOrigem(conexao);
	}

	private void preencherTabelaOrigem(EntityManager conexao) throws Exception {
		DespachoServico despachoServico = new DespachoServico(conexao);
		for (Origem origem : Origem.ORIGENS) {
			if (despachoServico.obterOrigem(origem.getOrigemId(), null).size() == 0) {
				JPAUtils.persistir(conexao, origem);
			}
		}
	}
}
