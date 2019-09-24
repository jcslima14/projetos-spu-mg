import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.CadastroTemplate;
import framework.DialogTemplate;
import framework.MyButton;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class MunicipioCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyButton btnImportarPlanilha = new MyButton("Importar Planilha");

	private JFileChooser filArquivo = new JFileChooser();
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JPanel pnlArquivo = new JPanel();
	private JPanel pnlDialogo = new JPanel();
	private JTextArea txtTexto = new JTextArea(30, 100);
	private JScrollPane scpAreaRolavel = new JScrollPane(txtTexto);
	private JButton btnIniciarImportacaoPlanillha = new JButton("Iniciar Importação da Planilha");
	
	private JTextField txtMunicipioId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNome = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNome = new MyLabel("Nome") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(2, 2));

	private List<MyTableColumn> colunas;
	private SPUNetServico cadastroServico;

	public MunicipioCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		cadastroServico = new SPUNetServico(conexao);
		
		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));
		pnlArquivo.setLayout(new GridLayout(1, 2));
		pnlArquivo.add(new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }});
		pnlArquivo.add(lblNomeArquivo);

		pnlDialogo.setLayout(new BorderLayout());
		pnlDialogo.add(pnlArquivo, BorderLayout.NORTH);
		pnlDialogo.add(scpAreaRolavel, BorderLayout.CENTER);
		
		pnlCamposEditaveis.add(lblMunicipioId);
		pnlCamposEditaveis.add(txtMunicipioId);
		pnlCamposEditaveis.add(lblNome);
		pnlCamposEditaveis.add(txtNome);

		btnImportarPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mostrarTelaImportacaoPlanilha();
			}
		});

		btnIniciarImportacaoPlanillha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							importarPlanilha();
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Erro ao processar os arquivos: \n" + e.getMessage());
						}
					}
				}).start();
			}
		});

		btnAbrirArquivo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Action detalhes = filArquivo.getActionMap().get("viewTypeDetails");
				detalhes.actionPerformed(null);
				int retorno = filArquivo.showOpenDialog(MunicipioCadastro.this);
				if (retorno == JFileChooser.APPROVE_OPTION) {
					if (filArquivo.getSelectedFile().exists()) {
						lblNomeArquivo.setText(filArquivo.getSelectedFile().getAbsolutePath());
					}
				}
			}
		});

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.setBtnBotoesAcimaPosteriores(new MyButton[] { btnImportarPlanilha });
		this.inicializar();
	}

	private void importarPlanilha() throws Exception {
		if (lblNomeArquivo.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(null, "É necessário escolher um arquivo para ser processado.");
			return;
		}

		File fileInput = new File(lblNomeArquivo.getText());
		Workbook wb = WorkbookFactory.create(fileInput);
		Sheet planilha = wb.getSheetAt(0);
		
		List<String> municipiosLidos = new ArrayList<String>();

		MyUtils.appendLogArea(txtTexto, "Lendo as informações da planilha...");

		for (int l = 1; l <= planilha.getLastRowNum(); l++) {
			Row linha = planilha.getRow(l);
			String nome = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 0)).trim();

			if (!nome.equals("")) municipiosLidos.add(nome);
		}

		wb.close();
		
		MyUtils.appendLogArea(txtTexto, "Incluindo os municípios novos...");
		
		// percorre a lista de municípios lidos, inserindo os que não existirem
		for (String nome : municipiosLidos) {
			Municipio municipio = MyUtils.entidade(cadastroServico.obterMunicipio(null, nome));
			if (municipio == null) {
				municipio = new Municipio();
				municipio.setNome(nome);
				cadastroServico.gravarEntidade(municipio);
				MyUtils.appendLogArea(txtTexto, "Município de " + nome + " incluído com sucesso");
			} else {
				MyUtils.appendLogArea(txtTexto, "Município de " + nome + " já está cadastrado na base de dados");
			}
		}

		MyUtils.appendLogArea(txtTexto, "Fim do Processamento");
		executarAtualizar();
	}

	private void mostrarTelaImportacaoPlanilha() {
		DialogTemplate janelaDialogo = new DialogTemplate("Importação de Planilha");
		janelaDialogo.setPnlAreaCentral(new JPanel() {{ add(pnlDialogo); }});
		janelaDialogo.setPnlBotoes(new JPanel() {{ add(btnIniciarImportacaoPlanillha); }});
		janelaDialogo.inicializar();
		janelaDialogo.abrirJanela();
	}

	public void limparCamposEditaveis() {
		txtMunicipioId.setText("");
		txtNome.setText("");
	}

	public void salvarRegistro() throws Exception {
		Municipio entidade = new Municipio();
		if (!txtMunicipioId.getText().equals("")) {
			entidade.setMunicipioId(Integer.parseInt(txtMunicipioId.getText()));
		}

		entidade.setNome(txtNome.getText());

		cadastroServico.gravarEntidade(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from municipio where municipioid = " + id;
		JPAUtils.executeUpdate(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtMunicipioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			Municipio entidade = MyUtils.entidade(cadastroServico.obterMunicipio(Integer.parseInt(txtMunicipioId.getText()), null));

			txtNome.setText(entidade.getNome());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter as informações do Município para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(cadastroServico.obterMunicipio(null, null), "getMunicipioId", "getNome"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Nome", 300));
		}
		return this.colunas;
	}
}
