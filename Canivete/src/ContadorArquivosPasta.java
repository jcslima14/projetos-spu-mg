import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import framework.utils.MyUtils;
import framework.utils.SpringUtilities;

@SuppressWarnings("serial")
public class ContadorArquivosPasta extends JInternalFrame {

	private JFileChooser filSelecionarDiretorio = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoDiretorio = new JButton("Selecionar diretório");
	private JLabel lblDiretorioInicial = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarDiretorio = new JLabel("Diretório:", JLabel.TRAILING) {{ setLabelFor(filSelecionarDiretorio); }};
	private JLabel lblDiretorioSendoProcessado = new JLabel();
	private JTable tabela = new JTable();

	public ContadorArquivosPasta(String tituloJanela) {
		super(tituloJanela);
		setSize(1000, 500);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		JPanel pnlPrincipal = new JPanel();
		pnlPrincipal.setLayout(new BoxLayout(pnlPrincipal, BoxLayout.Y_AXIS));
		
		JPanel painelArquivo = new JPanel() {{ add(lblSelecionarDiretorio); add(btnAbrirJanelaSelecaoDiretorio); }};

		JPanel painelDados = new JPanel();
		painelDados.setLayout(new SpringLayout());
		JButton botaoProcessar = new JButton("Processar");
		JButton botaoGerarCSV = new JButton("Gerar CSV");

		painelDados.add(painelArquivo);
		painelDados.add(lblDiretorioInicial);
		painelDados.add(botaoProcessar);
		painelDados.add(botaoGerarCSV);

		SpringUtilities.makeGrid(painelDados,
                2, 2, //rows, cols
                6, 6, //initX, initY
                6, 6); //xPad, yPad

		JPanel pnlSecundario = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pnlSecundario.add(painelDados);

		pnlPrincipal.add(pnlSecundario, BorderLayout.NORTH);
		pnlPrincipal.add(lblDiretorioSendoProcessado, BorderLayout.SOUTH);
		
		add(pnlPrincipal, BorderLayout.NORTH);
		JScrollPane areaDeRolagem = new JScrollPane(tabela);
		add(areaDeRolagem, BorderLayout.CENTER);

		botaoGerarCSV.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							gerarArquivoCSV(tabela);
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Erro ao gerar o arquivo CSV: \n \n" + e.getMessage() + "\n" + stackTraceToString(e));
						}
					}

					private String stackTraceToString(Exception e) {
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						return sw.toString();
					}
				}).start();
			} 
		}); 

		botaoProcessar.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						try {
							contarArquivosPastas(lblDiretorioInicial.getText());
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Erro ao executar o processamento: \n \n" + e.getMessage());
						}
					}
				}).start();
			} 
		}); 

		btnAbrirJanelaSelecaoDiretorio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filSelecionarDiretorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				filSelecionarDiretorio.setAcceptAllFileFilterUsed(false);
				int retorno = filSelecionarDiretorio.showOpenDialog(ContadorArquivosPasta.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filSelecionarDiretorio.getSelectedFile().exists()) {
						lblDiretorioInicial.setText(filSelecionarDiretorio.getSelectedFile().getAbsolutePath());
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

	private void incluirPastasNoMapa(Map<String, Integer> mapaArquivos, String caminhoInicial) {
		File pastaInicial = new File(caminhoInicial);
		lblDiretorioSendoProcessado.setText("Lendo a pasta: " + caminhoInicial);
		if (pastaInicial != null && pastaInicial.listFiles() != null) {
			int contador = 0;
			for (File arquivo : pastaInicial.listFiles()) {
				if (arquivo.isHidden()) continue;
				if (arquivo.isDirectory()) {
					incluirPastasNoMapa(mapaArquivos, arquivo.getAbsolutePath());
				} else {
					contador ++;
				}
			}
			mapaArquivos.put(caminhoInicial, contador);
		}
	}

	private void atualizarTabelaArquivos(Map<String, Integer> mapaArquivos, JTable tabela) {
		DefaultTableModel modelo = new DefaultTableModel();
		modelo.addColumn("Pasta");
		modelo.addColumn("Quantidade");

		for (Entry<String, Integer> entrada : mapaArquivos.entrySet()) {
			modelo.addRow(new Object[] { entrada.getKey(), entrada.getValue() });
		}

		tabela.setModel(modelo);
		tabela.getColumnModel().getColumn(0).setPreferredWidth(600);
		tabela.getColumnModel().getColumn(0).setMaxWidth(600);
		tabela.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.LEFT); }});
		tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
		tabela.getColumnModel().getColumn(1).setMaxWidth(150);
		tabela.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.RIGHT); }});
	}

	private void contarArquivosPastas(String caminhoInicial) throws Exception {
		Map<String, Integer> mapaArquivos = new TreeMap<String, Integer>();
		incluirPastasNoMapa(mapaArquivos, caminhoInicial);
		lblDiretorioSendoProcessado.setText("Fim do processamento");
		atualizarTabelaArquivos(mapaArquivos, tabela);
	}

	private void gerarArquivoCSV(JTable tabela) throws Exception {
		String nomeArquivo = "Arquivos_" + MyUtils.formatarData(new Date(), "yyyyMMdd_HHmmss") + ".csv";
		FileWriter csv = new FileWriter(System.getProperty("user.home") + "/Downloads/" + nomeArquivo);
		TableModel model = tabela.getModel();

        for (int i = 0; i < model.getColumnCount(); i++) {
            csv.write(model.getColumnName(i) + "\t");
        }

        csv.write("\n");

        for (int i = 0; i < model.getRowCount(); i++) {
            for (int j = 0; j < model.getColumnCount(); j++) {
                csv.write(model.getValueAt(i, j).toString() + "\t");
            }
            csv.write("\n");
        }

        csv.close();
        lblDiretorioSendoProcessado.setText("Arquivo '" + nomeArquivo + "' gerado com sucesso...");
    }
}
