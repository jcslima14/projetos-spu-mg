package views.processamento;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import framework.components.MyComboBoxModel;
import framework.models.ComboBoxItem;
import framework.utils.MyUtils;
import framework.utils.SpringUtilities;

@SuppressWarnings("serial")
public class ExportacaoPlanilha extends JInternalFrame {
						
	private Connection conexao;
	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(300, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:") {{ setLabelFor(filArquivo); }};
	private JPanel painelDados = new JPanel() {{ setLayout(new SpringLayout()); }};
	private JComboBox<ComboBoxItem> cbbUnidade = new JComboBox<ComboBoxItem>();
	private JLabel lblUnidade = new JLabel("Unidade:") {{ setLabelFor(cbbUnidade); }};
	private JCheckBox chkApenasAbertos = new JCheckBox() {{ setSelected(true); }};
	private JLabel lblApenasAbertos = new JLabel("Apenas com trâmite em aberto na unidade");
	private JButton btnProcessar = new JButton("Processar");
	private Properties propriedades = MyUtils.obterPropriedades("extratorsei.properties");

	public ExportacaoPlanilha(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		setResizable(true);
		setMaximizable(true);
		setIconifiable(true);
		setClosable(true);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));
		
		this.conexao = conexao;

		opcoesUnidade();

		JPanel painelArquivo = new JPanel();
		painelArquivo.add(lblArquivo);
		painelArquivo.add(btnAbrirArquivo);

		painelDados.add(painelArquivo);
		painelDados.add(lblNomeArquivo);
		painelDados.add(new JPanel() {{ add(chkApenasAbertos); add(lblApenasAbertos); }});
		painelDados.add(new JPanel());
		painelDados.add(lblUnidade);
		painelDados.add(cbbUnidade);
		painelDados.add(btnProcessar); 
		painelDados.add(new JPanel()); 

		SpringUtilities.makeGrid(painelDados,
	            4, 2, //rows, cols
	            6, 6, //initX, initY
	            6, 6); //xPad, yPad

		add(painelDados, BorderLayout.WEST);

		btnProcessar.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String mensagemErro = null;
				if (lblNomeArquivo.getText().equals("")) {
					mensagemErro = "Para iniciar o processamento é necessário informar o arquivo a ser gerado.";
				}

				if (mensagemErro != null) {
					JOptionPane.showMessageDialog(null, mensagemErro);
					return;
				}

				try {
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							try {
								ComboBoxItem unidadeSelecionada = (ComboBoxItem) cbbUnidade.getSelectedItem();
								exportarArquivo(lblNomeArquivo.getText(), unidadeSelecionada.getIntId(), chkApenasAbertos.isSelected());
								JOptionPane.showMessageDialog(null, "Arquivo gerado com sucesso!");
							} catch (Exception e) {
								JOptionPane.showMessageDialog(null, "Erro ao importar a planilha de dados: \n \n" + e.getMessage() + "\n" + MyUtils.stackTraceToString(e));
								e.printStackTrace();
							}
						}
					}).start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		btnAbrirArquivo.addActionListener(MyUtils.openFileDialogWindow(propriedades.getProperty("exportacao_planilha_default_path"), filArquivo, lblNomeArquivo, ExportacaoPlanilha.this, new Runnable() {
			@Override
			public void run() {
				if (!propriedades.getProperty("exportacao_planilha_default_path").equals(filArquivo.getSelectedFile().getParent())) {
					propriedades.put("exportacao_planilha_default_path", filArquivo.getSelectedFile().getParent());
					MyUtils.salvarPropriedades(propriedades, "extratorsei.properties");
				}
			}
		}));
	}

	public void abrirJanela() {
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack(); 
		this.setVisible(true);
		this.show();
	}

	private void exportarArquivo(String arquivo, Integer unidadeId, boolean apenasAbertos) throws Exception {
		Workbook wb = new XSSFWorkbook();
		wb.createSheet("Planilha1");
		Sheet planilha = wb.getSheetAt(0);
		// seleciona os dados a serem exportados
		String sql = "";
		sql += "select p.numeroprocesso, pm.gravidade, pm.urgencia, pm.tendencia, pm.gravidade * pm.urgencia * pm.tendencia as gut, u.cpf, u.nome, tm.descricao, strftime('%d/%m/%Y %H:%M', pm.datahora) as datahora, pm.detalhe, pm.municipio ";
		sql += "  from processo p ";
		sql += " inner join processomarcador pm using (processoid) ";
		sql += " inner join tipomarcador tm using (tipomarcadorid) ";
		sql += " inner join processotramite pt using (processoid) ";
		sql += "  left join usuario u on pt.usuarioidatribuido = u.usuarioid ";
		sql += " where pm.sequencial = (select max(sequencial) from processomarcador pm where pm.processoid = p.processoid and pm.unidadeid = " + unidadeId + ") ";
		sql += "   and pt.datainicio = (select max(datainicio) from processotramite pt where pt.processoid = p.processoid and pt.unidadeid = " + unidadeId + ") ";
		sql += "   and pt.unidadeid = " + unidadeId;
		sql += "   and pm.unidadeid = " + unidadeId;
		if (apenasAbertos) {
			sql += " and pt.datafim is null ";
		}

		Statement cmd = conexao.createStatement();
		ResultSet dados = cmd.executeQuery(sql);
		
		int l = 0;
		int c = 0;
		
		String[] cabecalho = new String[] { "Processo", "Gravidade", "Urgência", "Tendência", "GxUxT", "CPF Servidor", "Nome Servidor", "Status", "Data/Hora", "Detalhe da Situação", "Município" };
		List<Integer> colunasNumericas = new ArrayList<Integer>() {{ add(2); add(3); add(4); add(5); }};
		
		planilha.createRow(l);
		for (String cab : cabecalho) {
			planilha.getRow(l).createCell(c++).setCellValue(cab);
		}

		while (dados.next()) {
			planilha.createRow(++l);
			for (c = 0; c < cabecalho.length; c++) {
//				String teste = dados.getString(c+1);
//				if (teste == null) System.out.println("Valor da coluna " + (c+1) + " é nulo.");
				if (colunasNumericas.contains(c+1)) {
					if (dados.getString(c+1) != null) {
						planilha.getRow(l).createCell(c).setCellValue(dados.getInt(c+1));
					}
				} else {
					planilha.getRow(l).createCell(c).setCellValue(dados.getString(c+1));
				}
			}
		}

		cmd.close();
		File fileOutput = new File(filArquivo.getSelectedFile().getAbsolutePath() + (filArquivo.getSelectedFile().getAbsolutePath().trim().toLowerCase().endsWith("xlsx") ? "" : ".xlsx"));
		FileOutputStream fos = new FileOutputStream(fileOutput);
		wb.write(fos);
		fos.flush();
		fos.close();
	}

	private void opcoesUnidade() {
		cbbUnidade.setModel(new MyComboBoxModel());
		insereOpcoesComboBox(cbbUnidade, "select unidadeid, nome from unidade order by nome");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void insereOpcoesComboBox(JComboBox comboBox, String sql) {
		try {
			Statement cmd = conexao.createStatement();
			ResultSet rs = cmd.executeQuery(sql);
			while (rs.next()) {
				comboBox.addItem(new ComboBoxItem(rs.getInt(1), null, rs.getString(2)));
			}
			cmd.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
