import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.JPAUtils;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class TipoImovelCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtTipoImovelId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoImovelId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(2, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public TipoImovelCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		pnlCamposEditaveis.add(lblTipoImovelId);
		pnlCamposEditaveis.add(txtTipoImovelId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtTipoImovelId.setText("");
		txtDescricao.setText("");
	}

	public void salvarRegistro() throws Exception {
		TipoImovel entidade = MyUtils.entidade(despachoServico.obterTipoImovel(txtTipoImovelId.getTextAsInteger(-1), null));
		if (entidade == null) {
			entidade = new TipoImovel();
		}
		entidade.setDescricao(txtDescricao.getText());
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from tipoimovel where tipoimovelid = " + id);
	}

	public void prepararParaEdicao() {
		txtTipoImovelId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterTipoImovel(null, null), "tipoImovelId", "descricao"));
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
