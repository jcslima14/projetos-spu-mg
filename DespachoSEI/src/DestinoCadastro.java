import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyCheckBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class DestinoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtDestinoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblDestinoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAbreviacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAbreviacao = new MyLabel("Abreviação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtArtigo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblArtigo = new MyLabel("Artigo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkUsarComarca = new MyCheckBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblUsarComarca = new MyLabel("Usar Comarca") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtCaminhoDespachos = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblCaminhoDespachos = new MyLabel("Caminho Despachos") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(6, 2));
	private List<MyTableColumn> colunas;

	public DestinoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		pnlCamposEditaveis.add(lblDestinoId);
		pnlCamposEditaveis.add(txtDestinoId);
		pnlCamposEditaveis.add(lblAbreviacao);
		pnlCamposEditaveis.add(txtAbreviacao);
		pnlCamposEditaveis.add(lblArtigo);
		pnlCamposEditaveis.add(txtArtigo);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(lblUsarComarca);
		pnlCamposEditaveis.add(chkUsarComarca);
		pnlCamposEditaveis.add(lblCaminhoDespachos);
		pnlCamposEditaveis.add(txtCaminhoDespachos);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtDestinoId.setText("");
		txtAbreviacao.setText("");
		txtArtigo.setText("");
		txtDescricao.setText("");
		chkUsarComarca.setSelected(false);
		txtCaminhoDespachos.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtDestinoId.getText() != null && !txtDestinoId.getText().trim().equals("")) {
			sql += "update destino "
				+  "   set abreviacao = '" + txtAbreviacao.getText() + "' "
				+  "     , artigo = '" + txtArtigo.getText() + "' "
				+  "	 , descricao = '" + txtDescricao.getText().trim() + "' "
				+  "	 , usarcomarca = " + (chkUsarComarca.isSelected() ? "true" : "false") 
				+  "	 , caminhodespachos = '" + txtCaminhoDespachos.getText().trim() + "' "
				+  " where destinoid = " + txtDestinoId.getText();
		} else {
			sql += "insert into destino (abreviacao, artigo, descricao, usarcomarca, caminhodespachos) values ('" + txtAbreviacao.getText().trim() + "', '" + txtArtigo.getText().trim() + "', '" + txtDescricao.getText().trim() + "', " + (chkUsarComarca.isSelected() ? "true" : "false") + ", '" + txtCaminhoDespachos.getText().trim() + "')";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from destino where destinoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtDestinoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtAbreviacao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
		txtArtigo.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 3).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 4).toString());
		chkUsarComarca.setSelected(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 5).toString().equals("Sim") ? true : false);
		txtCaminhoDespachos.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 6).toString());
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select destinoid, abreviacao, artigo, descricao, case when usarcomarca then 'Sim' else 'Não' end as usarcomarca, caminhodespachos from destino");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Abreviacao", 150, true));
			colunas.add(new MyTableColumn("Artigo", 30, true));
			colunas.add(new MyTableColumn("Descrição", 300, true));
			colunas.add(new MyTableColumn("Usar Comarca?", 100, true) {{ setAlignment(JLabel.CENTER); }});
			colunas.add(new MyTableColumn("Caminho Despachos", 300, JLabel.LEFT));
		}
		return this.colunas;
	}
}
