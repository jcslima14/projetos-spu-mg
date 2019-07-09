import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class LocalizadorArquivosPorTamanho extends JInternalFrame {

	private JFileChooser filSelecionarDiretorio = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoDiretorio = new JButton("Selecionar diretório");
	private JLabel lblDiretorioInicial = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarDiretorio = new JLabel("Diretório:", JLabel.TRAILING) {{ setLabelFor(filSelecionarDiretorio); }};
	private JTextField txtTamanhoMinimo = new JTextField(10);
	private JLabel lblTamanhoMinimo = new JLabel("Tamanho Mínimo:");
	private JComboBox<String> cbbUnidadeMinimo = new JComboBox<String>(new String[] { "Megabytes", "Gigabytes", "Terabytes" });
	private JTextField txtTamanhoMaximo = new JTextField(10);
	private JLabel lblTamanhoMaximo = new JLabel("Tamanho Máximo:");
	private JComboBox<String> cbbUnidadeMaximo = new JComboBox<String>(new String[] { "Megabytes", "Gigabytes", "Terabytes" });
	private JLabel lblDiretorioSendoProcessado = new JLabel();
	private JTable tabela = new JTable();

	public LocalizadorArquivosPorTamanho(String tituloJanela) {
		super(tituloJanela);
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
		painelDados.add(lblTamanhoMinimo);
		painelDados.add(new JPanel() {{ setLayout(new FlowLayout()); add(txtTamanhoMinimo); add(cbbUnidadeMinimo); }});
		painelDados.add(lblTamanhoMaximo);
		painelDados.add(new JPanel() {{ setLayout(new FlowLayout(FlowLayout.LEFT)); add(txtTamanhoMaximo); add(cbbUnidadeMaximo); }});
		painelDados.add(botaoProcessar);
		painelDados.add(botaoGerarCSV);

		SpringUtilities.makeGrid(painelDados,
                4, 2, //rows, cols
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
							localizarArquivos(lblDiretorioInicial.getText(), obterTamanho(txtTamanhoMinimo.getText(), cbbUnidadeMinimo.getSelectedItem().toString(), " do tamanho mínimo."), obterTamanho(txtTamanhoMaximo.getText(), cbbUnidadeMaximo.getSelectedItem().toString(), " do tamanho máximo."));
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
				int retorno = filSelecionarDiretorio.showOpenDialog(LocalizadorArquivosPorTamanho.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filSelecionarDiretorio.getSelectedFile().exists()) {
						lblDiretorioInicial.setText(filSelecionarDiretorio.getSelectedFile().getAbsolutePath());
					}
				}
			}
		});
    }

	private long obterTamanho(String strTamanho, String unidade, String mensagemAdicional) {
		long tamanho;

		try {
			tamanho = Long.parseLong(strTamanho);
			tamanho *= 1024;
			if (unidade.equalsIgnoreCase("megabytes")) {
				tamanho *= 1024;
			} else if (unidade.equalsIgnoreCase("gigabytes")) {
				tamanho *= 1024 * 1024;
			} else if (unidade.equalsIgnoreCase("terabytes")) {
				tamanho *= 1024 * 1024 * 1024;
			}
		} catch (Exception e) {
			throw new RuntimeException("Erro ao obter o valor" + mensagemAdicional);
		}

		return tamanho;
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

	private void incluirArquivosNoMapa(Map<Long, List<String[]>> mapaArquivos, String caminhoInicial, long minimo, long maximo) {
		File pastaInicial = new File(caminhoInicial);
		lblDiretorioSendoProcessado.setText("Lendo a pasta: " + caminhoInicial);
		if (pastaInicial != null && pastaInicial.listFiles() != null) {
			for (File arquivo : pastaInicial.listFiles()) {
				if (arquivo.isDirectory()) {
					incluirArquivosNoMapa(mapaArquivos, arquivo.getAbsolutePath(), minimo, maximo);
				} else {
					long tamanho = arquivo.length();
					if (tamanho >= minimo && tamanho <= maximo) {
						List<String[]> listaAtual = mapaArquivos.get(tamanho * -1);
						if (listaAtual == null) listaAtual = new ArrayList<String[]>() {{ add(new String[] { arquivo.getName(), arquivo.getParent() }); }};
						else listaAtual.add(new String[] { arquivo.getName(), arquivo.getParent() });
						mapaArquivos.put(tamanho * -1, listaAtual);
					}
				}
			}
		}
	}

	private void atualizarTabelaArquivos(Map<Long, List<String[]>> mapaArquivos, JTable tabela) {
		DefaultTableModel modelo = new DefaultTableModel();
		modelo.addColumn("Bytes");
		modelo.addColumn("Situação");
		modelo.addColumn("Nome do Arquivo");
		modelo.addColumn("Pasta");

		for (Long tamanho : mapaArquivos.keySet()) {
			List<String[]> listaArquivos = mapaArquivos.get(tamanho);
			for (String[] dadosArquivo : listaArquivos) {
				modelo.addRow(new Object[] { tamanho * -1, listaArquivos.size() == 1 ? "" : "Mesmo tamanho", dadosArquivo[0], dadosArquivo[1] });
			}
		}

		tabela.setModel(modelo);
		tabela.getColumnModel().getColumn(0).setPreferredWidth(150);
		tabela.getColumnModel().getColumn(0).setMaxWidth(150);
		tabela.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.RIGHT); }});
		tabela.getColumnModel().getColumn(1).setPreferredWidth(150);
		tabela.getColumnModel().getColumn(1).setMaxWidth(150);
		tabela.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.CENTER); }});
	}

	private void localizarArquivos(String caminhoInicial, long tamanhoMinimo, long tamanhoMaximo) throws Exception {
		if (tamanhoMinimo > tamanhoMaximo) {
			throw new RuntimeException("O tamanho máximo deve ser maior que o tamanho mínimo (1 gigabyte = 1024 MEGAbytes; 1 terabyte = 1024 GIGAbytes). \n Verifique os valores e as unidades informadas e faça as correções necessárias para executar o processamento.");
		}
		Map<Long, List<String[]>> mapaArquivos = new TreeMap<Long, List<String[]>>();
		incluirArquivosNoMapa(mapaArquivos, caminhoInicial, tamanhoMinimo, tamanhoMaximo);
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
