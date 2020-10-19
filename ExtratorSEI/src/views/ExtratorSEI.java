package views;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import views.cadastro.GrupoTematicoCadastro;
import views.cadastro.UsuarioGrupoTematicoCadastro;
import views.processamento.AtribuicaoProcesso;
import views.processamento.ExportacaoPlanilha;
import views.processamento.RelatorioTramitacao;
import views.robos.CargaSEISQLite;

@SuppressWarnings("serial")
public class ExtratorSEI extends JFrame {

	private RelatorioTramitacao relatorioTramitacao;
	
	public ExtratorSEI() {
		super("Extrator de Informações do SEI");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);
		
		Connection conexao = obterConexao();

		JMenuItem sbmRelatorio = new JMenuItem("Relatório de Tramitação");
		JMenuItem sbmPlanilhaGUT = new JMenuItem("Planilha GUT");
		JMenuItem sbmCargaSEI = new JMenuItem("Carga de Informações do SEI");
		JMenuItem sbmAtribuicaoProcesso = new JMenuItem("Atribuição de Processos");
		JMenuItem sbmGrupoTematico = new JMenuItem("Grupo Temático");
		JMenuItem sbmUsuarioGrupoTematico = new JMenuItem("Usuário x Grupo Temático");
		JMenu mnuCadastro = new JMenu("Cadastro") {{ add(sbmGrupoTematico); add(sbmUsuarioGrupoTematico); }};
		JMenu mnuAcoes = new JMenu("Ações") {{ add(sbmRelatorio); add(sbmPlanilhaGUT); addSeparator(); add(sbmCargaSEI); add(sbmAtribuicaoProcesso); }};
//		mnuAcoes.add(sbmTesteLayout); 
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuCadastro); add(mnuAcoes); }};

		sbmRelatorio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (relatorioTramitacao == null) {
					relatorioTramitacao = new RelatorioTramitacao();
				}

				relatorioTramitacao.exibirPreview();
			}
		});
		
		sbmUsuarioGrupoTematico.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				UsuarioGrupoTematicoCadastro janela = new UsuarioGrupoTematicoCadastro("Usuário x Grupo Temático", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmGrupoTematico.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GrupoTematicoCadastro janela = new GrupoTematicoCadastro("Grupo Temático", conexao);
				desktop.add(janela);
				janela.abrirJanela();
			}
		});
		
		sbmPlanilhaGUT.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExportacaoPlanilha exportacaoPlanilha = new ExportacaoPlanilha("Exportação de Planilha GUT", conexao);
				desktop.add(exportacaoPlanilha);
				exportacaoPlanilha.abrirJanela();
			}
		});
		
		sbmCargaSEI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CargaSEISQLite cargaSEI = new CargaSEISQLite("Carga de Informações do SEI", conexao);
				desktop.add(cargaSEI);
				cargaSEI.abrirJanela();
			}
		});
		
		sbmAtribuicaoProcesso.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AtribuicaoProcesso janela = new AtribuicaoProcesso("Atribuição Automática de Processos", conexao);
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

	private static void verificarExistenciaBancoDados(Connection conexao) throws SQLException {
		String sql = "select * from sqlite_master where type = 'table' and name = 'parametro'";

		try {
			Statement consulta = conexao.createStatement();
			ResultSet rs = consulta.executeQuery(sql);
			if (!rs.next()) {
				criarTabelas(conexao);
			}
			consulta.close();
		} catch (SQLException e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private Connection obterConexao() {
		Connection conexao = null;

		try {
			conexao = DriverManager.getConnection("jdbc:sqlite:DadosSEI.db");
			verificarExistenciaBancoDados(conexao);
		} catch (SQLException e1) {
			JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados. \n \n" + e1.getMessage());
			e1.printStackTrace();
		}

		return conexao;
	}

	public static void main(String args[]) {
		ExtratorSEI app = new ExtratorSEI();
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		app.pack(); 
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 

	private static void criarTabelas(Connection conexao) throws SQLException {
		criarTabelaParametro(conexao);
		popularTabelaParametro(conexao);
		criarTabelaUsuario(conexao);
		criarTabelaUnidade(conexao);
		criarTabelaProcessoAndamento(conexao);
		criarTabelaProcessoAndamentoComplemento(conexao);
		criarTabelaProcesso(conexao);
		criarTabelaProcessoUnidade(conexao);
		criarTabelaProcessoTramite(conexao);
		criarTabelaProcessoTramiteEnviado(conexao);
		criarTabelaProcessoDocumento(conexao);
		criarTabelaProcessoDocumentoEnviado(conexao);
		criarTabelaProcessoDocumentoAssinado(conexao);
		criarTabelaProcessoMarcador(conexao);
		criarTabelaTipoMarcador(conexao);
		criarTabelaGrupoTematico(conexao);
		criarTabelaUsuarioGrupoTematico(conexao);
	}

	private static void criarTabelaParametro(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE parametro " + 
					 "(" + 
					 "  parametroid integer primary key autoincrement not null," + 
					 "  descricao varchar NOT NULL," + 
					 "  conteudo varchar" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoAndamento(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processoandamento" + 
					 "(" + 
					 "  processoandamentoid integer primary key autoincrement not null," + 
					 "  numeroprocesso varchar NOT NULL," + 
					 "  datahora datetime NOT NULL," + 
					 "  sequencial integer NOT NULL," + 
					 "  unidade varchar NOT NULL," + 
					 "  usuario varchar NOT NULL," + 
					 "  descricao varchar NOT NULL," +
					 "  desconsiderar boolean NOT NULL" +
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processoandamento_01 ON processoandamento (numeroprocesso, sequencial)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoAndamentoComplemento(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processoandamentocomplemento" + 
					 "(" + 
					 "  processoandamentocomplementoid integer primary key autoincrement not null," + 
					 "  numeroprocesso varchar NOT NULL," + 
					 "  sequencial integer NOT NULL," + 
					 "  nomedocumento varchar NOT NULL," + 
					 "  numerodocumentosei varchar NOT NULL" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processoandamentocomplemento_01 ON processoandamentocomplemento (numeroprocesso, sequencial)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcesso(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processo" + 
					 "(" + 
					 "  processoid integer primary key autoincrement not null," + 
					 "  numeroprocesso varchar NOT NULL," + 
					 "  ultimosequencial integer NOT NULL," + 
					 "  descricao varchar NOT NULL," + 
					 "  indicadorandamentoalterado boolean NOT NULL" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processo_01 ON processo (numeroprocesso)";
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processo_02 ON processo (descricao)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoUnidade(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processounidade" + 
					 "(" + 
					 "  processounidadeid integer primary key autoincrement not null," + 
					 "  processoid varchar NOT NULL," + 
					 "  unidadeid varchar NOT NULL," + 
					 "  ultimosequencial integer NOT NULL," +
					 "  datahoraultimaclassificacaotematica datetime" +
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processounidade_01 ON processounidade (processoid, unidadeid)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoTramite(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processotramite" + 
					 "(" + 
					 "  processotramiteid integer primary key autoincrement not null," + 
					 "  processoid integer NOT NULL," + 
					 "  unidadeid integer NOT NULL," + 
					 "  dataentrada datetime NOT NULL," + 
					 "  usuarioidentrada integer NOT NULL," + 
					 "  datasaida datetime," + 
					 "  datainicio datetime NOT NULL," + 
					 "  datafim datetime," + 
					 "  usuarioidsaida integer," + 
					 "  usuarioidatribuido integer" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processotramite_01 ON processotramite (processoid, unidadeid, dataentrada, datainicio)";
		cmd.execute(sql);

		sql = "CREATE INDEX ix_processotramite_02 ON processotramite (datafim)";
		cmd.execute(sql);

		sql = "CREATE INDEX ix_processotramite_03 ON processotramite (datainicio)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoTramiteEnviado(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processotramiteenviado (" + 
					 "  processotramiteenviadoid integer primary key autoincrement not null," + 
					 "  processotramiteid integer not null," + 
					 "  destinosaida text" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processotramiteenviado_01 ON processotramiteenviado (processotramiteid)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoDocumento(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processodocumento" + 
					 "(" + 
					 "  processodocumentoid integer primary key autoincrement not null," + 
					 "  processotramiteid integer not null," + 
					 "  datadocumento datetime NOT NULL," + 
					 "  numerodocumentosei varchar NOT NULL," + 
					 "  tipodocumento varchar" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processodocumento_01 ON processodocumento (processotramiteid, numerodocumentosei)";
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processodocumento_02 ON processodocumento (numerodocumentosei)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoDocumentoAssinado(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processodocumentoassinado" + 
					 "(" + 
					 "  processodocumentoassinadoid integer primary key autoincrement not null," + 
					 "  processodocumentoid integer not null," + 
					 "  usuarioidassinado integer NOT NULL" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processodocumentoassinado_01 ON processodocumentoassinado (processodocumentoid)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoDocumentoEnviado(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processodocumentoenviado" + 
					 "(" + 
					 "  processodocumentoenviadoid integer primary key autoincrement not null," + 
					 "  processodocumentoid integer not null," + 
					 "  datahoraenvio datetime not null," + 
					 "  usuarioidenvio integer NOT NULL," + 
					 "  formaenvio varchar NOT NULL" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_processodocumentoenviado_01 ON processodocumentoenviado (processodocumentoid)";
		cmd.execute(sql);
	}

	private static void criarTabelaUsuario(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE usuario" + 
					 "(" + 
					 "  usuarioid integer primary key autoincrement not null," + 
					 "	cpf varchar NOT NULL," + 
					 "	nome varchar NOT NULL," + 
					 "	primeironome varchar null," + 
					 "	unidadeid integer null" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_usuario_01 ON usuario (cpf)";
		cmd.execute(sql);
	}

	private static void criarTabelaUnidade(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE unidade" + 
					 "(" + 
					 "  unidadeid integer primary key autoincrement not null," + 
					 "  nome varchar NOT NULL" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
		
		sql = "CREATE INDEX ix_unidade_01 ON unidade (nome)";
		cmd.execute(sql);
	}

	private static void criarTabelaProcessoMarcador(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE processomarcador" + 
					 "(" + 
					 "  processomarcadorid integer primary key autoincrement not null," + 
					 "  processoid integer not null," + 
					 "  unidadeid integer not null," + 
					 "  sequencial integer not null," + 
					 "  datahora varchar not null," + 
					 "  usuarioid integer not null," + 
					 "  tipomarcadorid integer not null," + 
					 "  texto varchar not null," + 
					 "  detalhe varchar null," + 
					 "  municipio varchar null," + 
					 "  gravidade integer null," + 
					 "  urgencia integer null," + 
					 "  tendencia integer null" + 
					 ")";

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}

	private static void criarTabelaTipoMarcador(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE tipomarcador" + 
					 "(" + 
					 "  tipomarcadorid integer primary key autoincrement not null," + 
					 "  descricao varchar not null" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}

	private static void criarTabelaGrupoTematico(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE grupotematico" + 
					 "(" + 
					 "  grupotematicoid integer primary key autoincrement not null," + 
					 "  unidadeid integer not null," + 
					 "  descricao varchar not null" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}

	private static void criarTabelaUsuarioGrupoTematico(Connection conexao) throws SQLException {
		String sql = "CREATE TABLE usuariogrupotematico" + 
					 "(" + 
					 "  usuariogrupotematicoid integer primary key autoincrement not null," + 
					 "  usuarioid integer not null," + 
					 "  grupotematicoid integer not null," + 
					 "  ativo boolean not null" + 
					 ")"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}

	private static void popularTabelaParametro(Connection conexao) throws SQLException {
		String sql = "INSERT INTO parametro (descricao, conteudo) values (" + 
					 "'Data da última carga'," + 
					 "'')"; 

		Statement cmd = conexao.createStatement();
		cmd.execute(sql);
	}
}
