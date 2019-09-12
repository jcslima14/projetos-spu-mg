import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

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
public class CadastroSPUNet extends JFrame {

	public CadastroSPUNet() {
		super("Respostas do SEI");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);

		EntityManager conexao = obterConexaoEM();

		JMenuItem sbmImportacaoPlanilha = new JMenuItem("Importação de Planiliha");
		JMenuItem sbmCatalogacaoSPUNet = new JMenuItem("Catalogação no SPUNet");
		JMenu mnuProcessamento = new JMenu("Processamento") {{ add(sbmImportacaoPlanilha); 
															   add(sbmCatalogacaoSPUNet); 
															}};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuProcessamento); }};

		sbmCatalogacaoSPUNet.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CatalogacaoSPUNet janela = new CatalogacaoSPUNet("Resposta a Processos do SPUNet", conexao);
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
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("CadastroSPUNet");
			conexao = emf.createEntityManager();
			criarTabelas(conexao);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados. \n \n" + e1.getMessage());
			e1.printStackTrace();
		}
		
		return conexao;
	}

	public static boolean tabelaExiste(EntityManager conexao, String nomeTabela) {
		boolean retorno = false;
		String sql = "select * from sqlite_master sm where tbl_name = '" + nomeTabela + "'";

		try {
			List<Object[]> rs = JPAUtils.executeNativeQuery(conexao, sql);
			retorno = !rs.isEmpty();
		} catch (Exception e) {
			System.out.println("Erro: " + e.getMessage());
			e.printStackTrace();
		}
		
		return retorno;
	}

	public static void main(String args[]) {
		CadastroSPUNet app = new CadastroSPUNet();
		app.setSize(1000, 500);
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// app.pack(); 
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 

	private void criarTabelas(EntityManager conexao) throws Exception {
		criarTabelaGeoinformacao(conexao);
	}

	private void criarTabelaGeoinformacao(EntityManager conexao) throws Exception {
		if (!tabelaExiste(conexao, "geoinformacao")) {
			String sql = "CREATE TABLE geoinformacao " + 
						 "(" + 
						 "  geoinformacaoid integer primary key autoincrement not null," + 
						 "  cadastrado boolean NOT NULL," + 
						 "  identformatoprodutocdg varchar NOT NULL," + 
						 "  identprodutocdg varchar NOT NULL," + 
						 "  identtituloproduto varchar NOT NULL," + 
						 "  identdatacriacao varchar NOT NULL," + 
						 "  identdatadigitalizacao varchar NOT NULL," + 
						 "  identresumo varchar NOT NULL," + 
						 "  identstatus varchar NOT NULL," + 
						 "  identinstituicao varchar NOT NULL," + 
						 "  identfuncao varchar NOT NULL," + 
						 "  sisrefdatum varchar NOT NULL," + 
						 "  sisrefprojecao varchar NOT NULL," + 
						 "  sisrefobservacao varchar NOT NULL," + 
						 "  identcdgtiporeprespacial varchar NOT NULL," + 
						 "  identcdgescala varchar NOT NULL," + 
						 "  identcdgidioma varchar NOT NULL," + 
						 "  identcdgcategoria varchar NOT NULL," + 
						 "  identcdguf varchar NOT NULL," + 
						 "  identcdgmunicipio varchar NOT NULL," + 
						 "  identcdgdatum varchar NOT NULL," + 
						 "  qualidadenivel varchar NOT NULL," + 
						 "  qualidadelinhagem varchar NOT NULL," + 
						 "  distribuicaoformato varchar NOT NULL," + 
						 "  distribuicaoinstituicao varchar NOT NULL," + 
						 "  distribuicaofuncao varchar NOT NULL," + 
						 "  metadadoidioma varchar NOT NULL," + 
						 "  metadadoinstituicao varchar NOT NULL," + 
						 "  metadadofuncao varchar NOT NULL," + 
						 "  infadictipoarticulacao varchar NOT NULL," + 
						 "  infadiccamadainf varchar NOT NULL" + 
						 ")";
	
			JPAUtils.executeUpdate(conexao, sql);
		}
	}
}
