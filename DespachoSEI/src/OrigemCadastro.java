import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.templates.CadastroTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Origem;

@SuppressWarnings("serial")
public class OrigemCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtOrigemId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblOrigemId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(2, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public OrigemCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		pnlCamposEditaveis.add(lblOrigemId);
		pnlCamposEditaveis.add(txtOrigemId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		
		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtOrigemId.setText("");
		txtDescricao.setText("");
	}

	public void salvarRegistro() throws Exception {
		Origem entidade = MyUtils.entidade(despachoServico.obterOrigem(txtOrigemId.getTextAsInteger(-1), null));
		if (entidade == null) {
			entidade = new Origem();
		}
		entidade.setDescricao(txtDescricao.getText());
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from origem where origemid = " + id;
		JPAUtils.executeUpdate(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtOrigemId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterOrigem(null, null), "origemId", "descricao"));
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
