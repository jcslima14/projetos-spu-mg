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
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class TipoProcessoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtTipoProcessoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoProcessoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(2, 2));
	private List<MyTableColumn> colunas;

	public TipoProcessoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		pnlCamposEditaveis.add(lblTipoProcessoId);
		pnlCamposEditaveis.add(txtTipoProcessoId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		
		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtTipoProcessoId.setText("");
		txtDescricao.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtTipoProcessoId.getText() != null && !txtTipoProcessoId.getText().trim().equals("")) {
			sql += "update tipoprocesso set descricao = '" + txtDescricao.getText().trim() + "' where tipoprocessoid = " + txtTipoProcessoId.getText();
		} else {
			sql += "insert into tipoprocesso (descricao) values ('" + txtDescricao.getText().trim() + "')";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from tipoprocesso where tipoprocessoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtTipoProcessoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select * from tipoprocesso");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 200, true));
		}
		return this.colunas;
	}
}
