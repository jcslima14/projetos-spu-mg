import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
public class ImoveisCIDI extends JFrame {

	public ImoveisCIDI() {
		super("Cadastro SPUNet");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);

		EntityManager conexao = obterConexaoEM();

		JMenuItem sbmExtracaoDadosImoveis = new JMenuItem("Extrair Dados de Imóveis CIDI");
		JMenuItem sbmMunicipio = new JMenuItem("Cadastro de Município");
		JMenu mnuCadastro = new JMenu("Cadastro") {{ add(sbmMunicipio); 
												  }};
		JMenu mnuProcessamento = new JMenu("Processamento") {{ add(sbmExtracaoDadosImoveis); 
															}};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuCadastro); add(mnuProcessamento); }};

		sbmExtracaoDadosImoveis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ExtracaoDadosCIDI janela = new ExtracaoDadosCIDI("Extração de Dados dos Imóveis", conexao);
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

	private EntityManager obterConexaoEM() {
		EntityManager conexao = null;
		try {
			EntityManagerFactory emf = Persistence.createEntityManagerFactory("ImoveisCIDI");
			conexao = emf.createEntityManager();
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(null, "Erro ao conectar com o banco de dados. \n \n" + e1.getMessage());
			e1.printStackTrace();
		}
		
		return conexao;
	}

	public static void main(String args[]) {
		ImoveisCIDI app = new ImoveisCIDI();
		app.setSize(1000, 500);
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		// app.pack(); 
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 
}
