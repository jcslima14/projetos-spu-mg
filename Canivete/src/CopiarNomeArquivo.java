import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;

import framework.utils.SpringUtilities;

@SuppressWarnings("serial")
public class CopiarNomeArquivo extends JInternalFrame {

	private JFileChooser filSelecionarDiretorio = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoDiretorio = new JButton("Selecionar diretório");
	private JLabel lblDiretorioArquivos = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarDiretorio = new JLabel("Diretório:", JLabel.TRAILING) {{ setLabelFor(filSelecionarDiretorio); }};
	private JList<String> lstArquivos = new JList<String>();

	public CopiarNomeArquivo(String tituloJanela) {
		super(tituloJanela);
		setSize(1000, 500);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		JPanel painelArquivo = new JPanel() {{ add(lblSelecionarDiretorio); add(btnAbrirJanelaSelecaoDiretorio); }};

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Copiar"); 
		JCheckBox chkRetirarExtensao = new JCheckBox("Retirar extensão", true);

		painelDados.add(painelArquivo);
		painelDados.add(lblDiretorioArquivos);
		painelDados.add(chkRetirarExtensao);
		painelDados.add(new JPanel());
		painelDados.add(botaoProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
                3, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad

		add(painelDados, BorderLayout.NORTH);
		JScrollPane areaDeRolagem = new JScrollPane(lstArquivos);
		add(areaDeRolagem, BorderLayout.CENTER);

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				String listaNomes = "";
				for (String item : lstArquivos.getSelectedValuesList()) {
					listaNomes = listaNomes.concat(item).concat("\n");
				}
				StringSelection stringSelection = new StringSelection(listaNomes);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, null);
			} 
		}); 

		btnAbrirJanelaSelecaoDiretorio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filSelecionarDiretorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filSelecionarDiretorio.setAcceptAllFileFilterUsed(false);
				int retorno = filSelecionarDiretorio.showOpenDialog(CopiarNomeArquivo.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filSelecionarDiretorio.getSelectedFile().exists()) {
						lblDiretorioArquivos.setText(filSelecionarDiretorio.getSelectedFile().getAbsolutePath());
						try {
							preencherListaNomeArquivos(filSelecionarDiretorio.getSelectedFile().getAbsolutePath(), chkRetirarExtensao.isSelected());
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
    }

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		try {
			this.setMaximum(true);
		} catch (PropertyVetoException e) {
			e.printStackTrace();
		}
		this.setVisible(true);
		this.show();
	}

	private void preencherListaNomeArquivos(String diretorio, boolean retirarExtensao) throws Exception {
        // inicia o loop para leitura dos arquivos do diretório
		DefaultListModel<String> modelo = new DefaultListModel<String>();
		lstArquivos.setModel(modelo);
        for (File arquivo : obterArquivos(diretorio)) {
        	System.out.println(arquivo);
        	if (retirarExtensao && arquivo.getName().lastIndexOf(".") != -1) modelo.addElement(arquivo.getName().substring(0, arquivo.getName().lastIndexOf(".")));
        	else modelo.addElement(arquivo.getName());
        }
	}

	private ArrayList<File> obterArquivos(String nomeDiretorio) {
		ArrayList<File> retorno = new ArrayList<File>();
		File diretorio = new File(nomeDiretorio);
		for (File arquivo : diretorio.listFiles()) {
			if (!arquivo.isDirectory()) {
				retorno.add(arquivo);
			}
		}
		return retorno;
	}
}
