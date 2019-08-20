import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyCheckBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class ParametroCadastro extends CadastroTemplate {

	private Connection conexao;
	private MyTextField txtParametroId = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(false); }};
	private MyLabel lblParametroId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtConteudo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblConteudo = new MyLabel("Conteúdo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkAtivo = new MyCheckBox("Ativo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;

	public ParametroCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		pnlCamposEditaveis.add(lblParametroId);
		pnlCamposEditaveis.add(txtParametroId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(lblConteudo);
		pnlCamposEditaveis.add(txtConteudo);
		pnlCamposEditaveis.add(chkAtivo);
		pnlCamposEditaveis.add(new JPanel());
		
		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtParametroId.setText("");
		txtDescricao.setText("");
		txtConteudo.setText("");
		chkAtivo.setSelected(true);
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		sql += "insert into parametro (parametroid, descricao, conteudo, ativo) ";
		sql += "select " + txtParametroId.getText();
		sql += "	 , '" + txtDescricao.getText().trim() + "'";
		sql += "	 , '" + txtConteudo.getText() + "'";
		sql += "	 , " + (chkAtivo.isSelected() ? "true" : "false");
		sql += " where not exists (select 1 from parametro where parametroid = " + txtParametroId.getText() + ")";
		MyUtils.execute(conexao, sql);

		sql = "";
		sql += "update parametro "
			+  "   set descricao = '" + txtDescricao.getText().trim() + "' "
			+  "     , conteudo = '" + txtConteudo.getText() + "' "
			+  "     , ativo = " + (chkAtivo.isSelected() ? "true" : "false")
			+  " where parametroid = " + txtParametroId.getText();
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from paraemtro where parametroid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtParametroId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
		txtConteudo.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 3).toString());
		chkAtivo.setSelected(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 4).toString().contentEquals("Sim") ? true : false);
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select parametroid, descricao, conteudo, case when ativo then 'Sim' else 'Não' end as ativo from parametro");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 250, true));
			colunas.add(new MyTableColumn("Conteúdo", 500, true));
			colunas.add(new MyTableColumn("Ativo?", 100, true, JLabel.CENTER));
		}
		return this.colunas;
	}
}
