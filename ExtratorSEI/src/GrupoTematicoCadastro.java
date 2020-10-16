import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.components.MyComboBox;
import framework.components.MyComboBoxModel;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.templates.CadastroTemplate;
import framework.utils.MyUtils;

@SuppressWarnings("serial")
public class GrupoTematicoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtGrupoTematicoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblGrupoTematicoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbUnidade = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblUnidade = new MyLabel("Unidade") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(3, 2));
	private List<MyTableColumn> colunas;

	public GrupoTematicoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		opcoesUnidade();

		pnlCamposEditaveis.add(lblGrupoTematicoId);
		pnlCamposEditaveis.add(txtGrupoTematicoId);
		pnlCamposEditaveis.add(lblUnidade);
		pnlCamposEditaveis.add(cbbUnidade);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void opcoesUnidade() {
		cbbUnidade.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbUnidade, "select unidadeid, nome from unidade order by nome");
	}

	public void limparCamposEditaveis() {
		txtGrupoTematicoId.setText("");
		cbbUnidade.setSelectedIndex(0);
		txtDescricao.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtGrupoTematicoId.getText() != null && !txtGrupoTematicoId.getText().trim().equals("")) {
			sql += "update grupotematico "
				+  "   set unidadeid = " + MyUtils.idItemSelecionado(cbbUnidade)
				+  "     , descricao = '" + txtDescricao.getText().trim() + "' "
				+  " where grupotematicoid = " + txtGrupoTematicoId.getText();
		} else {
			sql += "insert into grupotematico (unidadeid, descricao) values (" + MyUtils.idItemSelecionado(cbbUnidade) + ", '" + txtDescricao.getText().trim() + "')";
		}

		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from grupotematico where grupotematicoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		ResultSet rs;
		try {
			txtGrupoTematicoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			rs = MyUtils.executeQuery(conexao, "select * from grupotematico where grupotematicoid = " + txtGrupoTematicoId.getText());
			rs.next();

			cbbUnidade.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbUnidade, rs.getInt("unidadeid"), null));
			txtDescricao.setText(rs.getString("descricao"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select gt.grupotematicoid, u.nome as unidade, gt.descricao from grupotematico gt inner join unidade u using (unidadeid) order by u.nome, gt.descricao");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Unidade", 200, true));
			colunas.add(new MyTableColumn("Descrição", 200, true));
		}
		return this.colunas;
	}
}
