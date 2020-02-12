import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

@SuppressWarnings("serial")
public class Canivete extends JFrame {

	public Canivete() {
		super("Canivete Suíço");

        this.setLayout(new BorderLayout());
		JDesktopPane desktop = new JDesktopPane();
		setContentPane(desktop);
		
		JMenuItem sbmCopiarNomeArquivo = new JMenuItem("Copiar nomes de arquivos");
		JMenuItem sbmLocalizarArquivosTamanho = new JMenuItem("Localizar arquivos por tamanho");
		JMenuItem sbmLocalizarPastasDuplicadas = new JMenuItem("Localizar pastas duplicadas");
		JMenuItem sbmContadorArquivosPasta = new JMenuItem("Contar arquivos contidos em pastas");
		JMenu mnuFerramentas = new JMenu("Ferramentas") {{ add(sbmCopiarNomeArquivo); add(sbmLocalizarArquivosTamanho); add(sbmLocalizarPastasDuplicadas); add(sbmContadorArquivosPasta); }};
		JMenuBar barraMenu = new JMenuBar() {{ add(mnuFerramentas); }};

		sbmCopiarNomeArquivo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CopiarNomeArquivo janela = new CopiarNomeArquivo("Copiar nomes de arquivos");
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmLocalizarArquivosTamanho.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalizadorArquivosPorTamanho janela = new LocalizadorArquivosPorTamanho("Localizar arquivos por tamanho");
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmLocalizarPastasDuplicadas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LocalizadorPastasDuplicadas janela = new LocalizadorPastasDuplicadas("Localizar pastas duplicadas");
				desktop.add(janela);
				janela.abrirJanela();
			}
		});

		sbmContadorArquivosPasta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ContadorArquivosPasta janela = new ContadorArquivosPasta("Contar arquivos em pastas");
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
		Canivete app = new Canivete();
		app.setSize(1000, 500);
		app.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		app.setVisible(true);
		app.setExtendedState(app.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	} 
}
