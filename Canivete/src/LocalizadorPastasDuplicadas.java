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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

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

import framework.MyUtils;
import framework.SpringUtilities;

@SuppressWarnings("serial")
public class LocalizadorPastasDuplicadas extends JInternalFrame {

	private JFileChooser filSelecionarDiretorio = new JFileChooser();
	private JButton btnAbrirJanelaSelecaoDiretorio = new JButton("Selecionar diret�rio");
	private JLabel lblDiretorioInicial = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblSelecionarDiretorio = new JLabel("Diret�rio:", JLabel.TRAILING) {{ setLabelFor(filSelecionarDiretorio); }};
	private JTextField txtTamanhoMinimo = new JTextField(10);
	private JLabel lblTamanhoMinimo = new JLabel("Tamanho M�nimo:");
	private JComboBox<String> cbbUnidadeMinimo = new JComboBox<String>(new String[] { "Megabytes", "Gigabytes", "Terabytes" });
	private JLabel lblDiretorioSendoProcessado = new JLabel();
	private JTable tabela = new JTable();

	public LocalizadorPastasDuplicadas(String tituloJanela) {
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
		painelDados.add(lblTamanhoMinimo);
		painelDados.add(new JPanel() {{ setLayout(new FlowLayout()); add(txtTamanhoMinimo); add(cbbUnidadeMinimo); }});
		painelDados.add(botaoProcessar);
		painelDados.add(botaoGerarCSV);

		SpringUtilities.makeGrid(painelDados,
                3, 2, //rows, cols
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
							localizarArquivos(lblDiretorioInicial.getText(), obterTamanho(txtTamanhoMinimo.getText(), cbbUnidadeMinimo.getSelectedItem().toString(), " do tamanho m�nimo."));
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
				int retorno = filSelecionarDiretorio.showOpenDialog(LocalizadorPastasDuplicadas.this);
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

	private Object[] incluirArquivosNoMapa(Map<String, List<String>> mapaArquivos, String caminhoInicial, long tamanhoMinimo) {
		Object[] retorno = new Object[] { new Long(0), new String() };
		File pastaInicial = new File(caminhoInicial);
		lblDiretorioSendoProcessado.setText("Lendo a pasta: " + caminhoInicial);
		if (pastaInicial != null && pastaInicial.listFiles() != null) {
			File[] arquivos = pastaInicial.listFiles();
			Arrays.sort(arquivos);
			String chave = "";
			long tamanho = 1;
			for (File arquivo : arquivos) {
				if (arquivo.isDirectory()) {
					Object[] retornado = incluirArquivosNoMapa(mapaArquivos, arquivo.getAbsolutePath(), tamanhoMinimo);
					chave += retornado[1].toString();
					tamanho += (Long) retornado[0];
				} else {
					if (arquivo.getName().equalsIgnoreCase("thumbs.db")) continue;
					chave += arquivo.getName() + ";" + arquivo.length() + ";" + arquivo.lastModified() + "|";
					tamanho += arquivo.length();
				}
			}
			retorno = new Object[] { new Long(tamanho), new String(chave) }; 
			if (!chave.equalsIgnoreCase("") && tamanho >= tamanhoMinimo) {
				chave = tamanho + ":" + chave;
				if (mapaArquivos.get(chave) == null) mapaArquivos.put(chave, new ArrayList<String>());
				mapaArquivos.get(chave).add(caminhoInicial);
			}
		}
		return retorno;
	}

	private void atualizarTabelaArquivos(Map<String, List<String>> mapaArquivos, JTable tabela) {
		DefaultTableModel modelo = new DefaultTableModel();
		modelo.addColumn("Sequencial");
		modelo.addColumn("Pasta");

		eliminarPastasInferiores(mapaArquivos);
		
		int sequencial = 0;
		
		for (String chave : mapaArquivos.keySet()) {
			List<String> listaPastas = mapaArquivos.get(chave);
			if (listaPastas.size() > 1) {
				sequencial ++;
				Collections.sort(listaPastas, new PastaComparator());
				for (String pasta : listaPastas) {
					modelo.addRow(new Object[] { sequencial, pasta });
				}
			}
		}

		tabela.setModel(modelo);
		tabela.getColumnModel().getColumn(0).setPreferredWidth(150);
		tabela.getColumnModel().getColumn(0).setMaxWidth(150);
		tabela.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {{ setHorizontalAlignment(JLabel.RIGHT); }});
	}

	private class PastaComparator implements Comparator<String> {
		@Override
		public int compare(String o1, String o2) {
			int r = 0;

			String[] oa1 = o1.split(Pattern.quote(File.separator));
			String[] oa2 = o2.split(Pattern.quote(File.separator));

			int t = (oa1.length > oa2.length ? oa1.length : oa2.length);

			for (int i = 0; i < t; i++) {
				String s1 = "";
				String s2 = "";
				
				if (i < oa1.length) {
					s1 = oa1[i];
				}
				
				if (i < oa2.length) {
					s2 = oa2[i];
				}
				
				r = s1.compareTo(s2);
				if (r != 0) {
					break;
				}
			}
			
			return r;
		}
		
	}
	
	private void eliminarPastasInferiores(Map<String, List<String>> mapaArquivos) {
		List<String> chavesAExcluir = new ArrayList<String>();

		for (String chave : mapaArquivos.keySet()) {
			List<String> listaSubpastas = mapaArquivos.get(chave);
			String caminhoSuperiores = "";
			Collections.sort(listaSubpastas, new PastaComparator());
			// monta a string com todos os superiores concatenados para verificar se existe em outra lista
			for (String pasta : listaSubpastas) {
				caminhoSuperiores += ("|" + (new File(pasta)).getParent());
			}
			
			for (String outraChave : mapaArquivos.keySet()) {
				if (chave.equals(outraChave) || mapaArquivos.get(outraChave).size() != listaSubpastas.size()) continue;

				String caminhoPastas = "";
				List<String> listaPastaSup = mapaArquivos.get(outraChave);
				Collections.sort(listaPastaSup, new PastaComparator());
				for (String pastaSup : listaPastaSup) {
					caminhoPastas += ("|" + pastaSup);
				}

				if (caminhoSuperiores.equalsIgnoreCase(caminhoPastas)) {
					chavesAExcluir.add(chave);
					break;
				}
			}
		}

		for (String chaveAExcluir : chavesAExcluir) {
			mapaArquivos.remove(chaveAExcluir);
		}
	}
	
	private void localizarArquivos(String caminhoInicial, long tamanhoMinimo) throws Exception {
		Map<String, List<String>> mapaArquivos = new TreeMap<String, List<String>>();
		incluirArquivosNoMapa(mapaArquivos, caminhoInicial, tamanhoMinimo);
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
}
