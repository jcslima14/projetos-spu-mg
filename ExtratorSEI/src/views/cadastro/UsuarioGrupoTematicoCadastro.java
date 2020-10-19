package views.cadastro;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.components.MyCheckBox;
import framework.components.MyComboBox;
import framework.components.MyComboBoxModel;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.templates.CadastroTemplate;
import framework.utils.MyUtils;

@SuppressWarnings("serial")
public class UsuarioGrupoTematicoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtUsuarioGrupoTematicoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblUsuarioGrupoTematicoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbGrupoTematico = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblGrupoTematico = new MyLabel("Grupo Temático") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbUsuario = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblUsuario = new MyLabel("Usuário") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkAtivo = new MyCheckBox("Ativo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;

	public UsuarioGrupoTematicoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		opcoesGrupoTematico();
		opcoesUsuario();

		pnlCamposEditaveis.add(lblUsuarioGrupoTematicoId);
		pnlCamposEditaveis.add(txtUsuarioGrupoTematicoId);
		pnlCamposEditaveis.add(lblGrupoTematico);
		pnlCamposEditaveis.add(cbbGrupoTematico);
		pnlCamposEditaveis.add(lblUsuario);
		pnlCamposEditaveis.add(cbbUsuario);
		pnlCamposEditaveis.add(chkAtivo);
		pnlCamposEditaveis.add(new JPanel());

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void opcoesGrupoTematico() {
		cbbGrupoTematico.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbGrupoTematico, "select grupotematicoid, descricao from grupotematico order by descricao");
	}

	private void opcoesUsuario() {
		cbbUsuario.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbUsuario, "select usuarioid, coalesce(primeironome, nome) as nome from usuario order by nome");
	}

	public void limparCamposEditaveis() {
		txtUsuarioGrupoTematicoId.setText("");
		cbbGrupoTematico.setSelectedIndex(0);
		cbbUsuario.setSelectedIndex(0);
		chkAtivo.setSelected(true);
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtUsuarioGrupoTematicoId.getText() != null && !txtUsuarioGrupoTematicoId.getText().trim().equals("")) {
			sql += "update usuariogrupotematico "
				+  "   set grupotematicoid = " + MyUtils.idItemSelecionado(cbbGrupoTematico)
				+  "     , usuarioid = " + MyUtils.idItemSelecionado(cbbUsuario)
				+  "     , ativo = " + (chkAtivo.isSelected() ? "true" : "false")
				+  " where usuariogrupotematicoid = " + txtUsuarioGrupoTematicoId.getText();
		} else {
			sql += "insert into usuariogrupotematico (grupotematicoid, usuarioid, ativo) values (" + MyUtils.idItemSelecionado(cbbGrupoTematico) + ", " + MyUtils.idItemSelecionado(cbbUsuario) + ", " + (chkAtivo.isSelected() ? "true" : "false") + ")";
		}

		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from usuariogrupotematico where usuariogrupotematicoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		ResultSet rs;
		try {
			txtUsuarioGrupoTematicoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			rs = MyUtils.executeQuery(conexao, "select * from usuariogrupotematico where usuariogrupotematicoid = " + txtUsuarioGrupoTematicoId.getText());
			rs.next();

			cbbGrupoTematico.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbGrupoTematico, rs.getInt("grupotematicoid"), null));
			cbbUsuario.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbUsuario, rs.getInt("usuarioid"), null));
			chkAtivo.setSelected(rs.getBoolean("ativo"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
				"select ugt.usuariogrupotematicoid, un.nome as unidade, gt.descricao as grupotematico, coalesce(us.primeironome, us.nome) as usuario, case when ugt.ativo then 'Sim' else 'Não' end as ativo "
			  + "  from usuariogrupotematico ugt "
			  + " inner join grupotematico gt using (grupotematicoid) "
			  + " inner join usuario us using (usuarioid) "
			  + " inner join unidade un on gt.unidadeid = un.unidadeid "
			  + " order by unidade, grupotematico, usuario");
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
			colunas.add(new MyTableColumn("Grupo Temático", 200, true));
			colunas.add(new MyTableColumn("Usuário", 200, true));
			colunas.add(new MyTableColumn("Ativo", 100, true));
		}
		return this.colunas;
	}
}
