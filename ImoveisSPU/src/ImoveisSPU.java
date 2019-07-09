import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

@SuppressWarnings("serial")
public class ImoveisSPU extends JFrame {

	public ImoveisSPU() {
		super("Imóveis SPU");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);
		
		EntityManager conexao = obterConexaoEM();

		JMenuItem sbmImportacaoPlanilha = new JMenuItem("Importação de Planiliha");
		JMenuItem sbmConsolidacaoInformacoes = new JMenuItem("Consolidação de Informações dos Municípios");
		JMenuItem sbmMunicipioCorrecao = new JMenuItem("Correção de Nome de Município");
		JMenuItem sbmTipoPlanilha = new JMenuItem("Tipo de Planilha");
		JMenuItem sbmEstruturaPlanilha = new JMenuItem("Estrutura da Planilha");
		JMenu mnuCadastro = new JMenu("Cadastro") {{ add(sbmMunicipioCorrecao); addSeparator(); add(sbmTipoPlanilha); add(sbmEstruturaPlanilha); }};
		JMenu mnuRelatorio = new JMenu("Relatório");
		JMenu mnuProcessamento = new JMenu("Processamento") {{ add(sbmImportacaoPlanilha); add(sbmConsolidacaoInformacoes); }};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuCadastro); add(mnuRelatorio); add(mnuProcessamento); }};

		sbmMunicipioCorrecao.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MunicipioCorretoCadastro janela = new MunicipioCorretoCadastro("Correção de Nome de Município", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmTipoPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TipoPlanilhaCadastro janela = new TipoPlanilhaCadastro("Tipo de Planilha", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmEstruturaPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EstruturaPlanilhaCadastro janela = new EstruturaPlanilhaCadastro("Estrutura da Planilha", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmImportacaoPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ImportacaoPlanilha janela = new ImportacaoPlanilha("Importação de Planilha de Dados", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmConsolidacaoInformacoes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConsolidacaoInformacoes janela = new ConsolidacaoInformacoes("Consolidação de Informações dos Municípios", conexao);
				desktop.add(janela);
				janela.abrirJanela();
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

	public static void main(String args[]) {
		ImoveisSPU app = new ImoveisSPU();
		app.setSize(1000, 500);
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// app.pack(); 
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 

	private void criarTabelas(EntityManager conexao) throws Exception {
		criarTabelaMunicipio(conexao);
		criarTabelaMunicipioCorrecao(conexao);
		criarTabelaCidi(conexao);
		criarTabelaCidiAlienacao(conexao);
		criarTabelaCidiDevolucao(conexao);
		criarTabelaCidiPoligono(conexao);
		criarTabelaSpiunet(conexao);
		criarTabelaSiapa(conexao);
		criarTabelaRIPPoligono(conexao);
		criarTabelaTipoPlanilha(conexao);
		criarTabelaEstruturaPlanilha(conexao);
	}

	private void criarTabelaCidi(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "cidi")) {
			String sql = "CREATE TABLE cidi " + 
						 "(" + 
						 "  cidiid integer primary key autoincrement not null," + 
						 "  ur varchar NOT NULL," + 
						 "  nbp varchar NOT NULL," + 
						 "  parcela varchar NOT NULL," + 
						 "  descricao varchar NOT NULL," + 
						 "  cod_trecho varchar NOT NULL," + 
						 "  trecho_ini varchar NOT NULL," + 
						 "  trecho_fim varchar NOT NULL," + 
						 "  logradouro varchar NOT NULL," + 
						 "  complemento varchar NOT NULL," + 
						 "  municipio varchar NOT NULL," + 
						 "  cep varchar NOT NULL," + 
						 "  uf varchar NOT NULL," + 
						 "  area varchar NOT NULL," + 
						 "  nprocesso varchar NOT NULL," + 
						 "  termo_transf varchar NOT NULL," + 
						 "  termo_ano varchar NOT NULL," + 
						 "  situacao19 varchar NOT NULL" + 
						 ")"; 
	
			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaCidiAlienacao(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "cidialienacao")) {
			String sql = "CREATE TABLE cidialienacao " + 
						 "(" + 
						 "  cidialienacaoid integer primary key autoincrement not null," + 
						 "  processo varchar NOT NULL," + 
						 "  sarp varchar NOT NULL," + 
						 "  ur varchar NOT NULL," + 
						 "  municipio varchar NOT NULL," + 
						 "  endereco varchar NOT NULL," + 
						 "  assunto varchar NOT NULL," + 
						 "  interessado varchar NOT NULL," + 
						 "  cod_bp varchar NOT NULL," + 
						 "  num_pcl_bp varchar NOT NULL" + 
						 ")"; 
	
			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaCidiDevolucao(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "cididevolucao")) {
			String sql = "CREATE TABLE cididevolucao " + 
						 "(" + 
						 "  cididevolucaoid integer primary key autoincrement not null," + 
						 "  numero varchar NOT NULL," + 
						 "  tt varchar NOT NULL," + 
						 "  municipio varchar NOT NULL," + 
						 "  nbp varchar NOT NULL," + 
						 "  oficio_spu varchar NOT NULL," + 
						 "  data varchar NOT NULL," + 
						 "  nprocesso varchar NOT NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaSpiunet(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "spiunet")) {
			String sql = "CREATE TABLE spiunet " + 
						 "(" + 
						 "  spiunetid integer primary key autoincrement not null," + 
						 "  municipio varchar NOT NULL," + 
						 "  ripimovel varchar NOT NULL," + 
						 "  riputilizacao varchar NOT NULL," + 
						 "  regimeutilizacao varchar NOT NULL," + 
						 "  tipoproprietario varchar NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaSiapa(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "siapa")) {
			String sql = "CREATE TABLE siapa " + 
						 "(" + 
						 "  siapaid integer primary key autoincrement not null," + 
						 "  municipio varchar NOT NULL," + 
						 "  ripimovel varchar NOT NULL," + 
						 "  conceituacao varchar NOT NULL," + 
						 "  classeimovel varchar NOT NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaMunicipio(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "municipio")) {
			String sql = "CREATE TABLE municipio " + 
						 "(" + 
						 "  municipioid integer primary key autoincrement not null," + 
						 "  municipio varchar NOT NULL," + 
						 "  ciditotal integer," +
						 "  cidialienado integer," +
						 "  cidialienadonaopago integer," +
						 "  cidibaixa integer," +
						 "  cidipoligono integer," +
						 "  spiunettotal integer," +
						 "  spiunetalugado integer," +
						 "  spiunetpoligono integer," +
						 "  siapatotal integer," +
						 "  siapapoligono integer" +
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaMunicipioCorrecao(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "municipiocorrecao")) {
			String sql = "CREATE TABLE municipiocorrecao " + 
						 "(" + 
						 "  municipiocorrecaoid integer primary key autoincrement not null," + 
						 "  nomeincorreto varchar NOT NULL," + 
						 "  nomecorreto varchar NULL" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaTipoPlanilha(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "tipoplanilha")) {
			String sql = "CREATE TABLE tipoplanilha " + 
						 "(" + 
						 "  tipoplanilhaid integer primary key autoincrement not null," + 
						 "  descricao varchar not null," + 
						 "  linhacabecalho integer not null" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaEstruturaPlanilha(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "estruturaplanilha")) {
			String sql = "CREATE TABLE estruturaplanilha " + 
						 "(" + 
						 "  estruturaplanilhaid integer primary key autoincrement not null," + 
						 "  tipoplanilhaid integer not null," + 
						 "  nomecampo varchar not null," + 
						 "  nomecoluna varchar not null," + 
						 "  obrigatorio boolean not null" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaCidiPoligono(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "cidipoligono")) {
			String sql = "CREATE TABLE cidipoligono " + 
						 "(" + 
						 "  cidipoligonoid integer primary key autoincrement not null," + 
						 "  descricao integer not null," + 
						 "  nome varchar not null," + 
						 "  nbp varchar not null" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private void criarTabelaRIPPoligono(EntityManager conexao) throws SQLException {
		if (!MyUtils.tabelaExiste(conexao, "rippoligono")) {
			String sql = "CREATE TABLE rippoligono " + 
						 "(" + 
						 "  rippoligonoid integer primary key autoincrement not null," + 
						 "  descricao integer not null," + 
						 "  nome varchar not null," + 
						 "  rip varchar not null" + 
						 ")"; 

			JPAUtils.executeUpdate(conexao, sql);
		}
	}

	private EntityManager obterConexaoEM() {
		EntityManager conexao = null;
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("ImoveisSPU");
			conexao = emf.createEntityManager();
			criarTabelas(conexao);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados. \n \n" + e1.getMessage());
			e1.printStackTrace();
		}
		
		return conexao;
	}
}
