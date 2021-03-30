package views.cadastro;
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
import model.TipoProcesso;
import services.DespachoServico;

@SuppressWarnings("serial")
public class TipoProcessoCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtTipoProcessoId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoProcessoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(2, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public TipoProcessoCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

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
		TipoProcesso entidade = new TipoProcesso();
		entidade.setTipoProcessoId(txtTipoProcessoId.getTextAsInteger());
		entidade.setDescricao(txtDescricao.getText());
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from tipoprocesso where tipoprocessoid = " + id);
	}

	public void prepararParaEdicao() {
		txtTipoProcessoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterTipoProcesso(null, null), "tipoProcessoId", "descricao"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 200, true));
		}
		return this.colunas;
	}
}
