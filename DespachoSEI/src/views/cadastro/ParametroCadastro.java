package views.cadastro;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.components.MyCheckBox;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.templates.CadastroTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Parametro;
import services.DespachoServico;

@SuppressWarnings("serial")
public class ParametroCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtParametroId = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(false); }};
	private MyLabel lblParametroId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtConteudo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblConteudo = new MyLabel("Conteúdo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkAtivo = new MyCheckBox("Ativo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public ParametroCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

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
		Parametro entidade = MyUtils.entidade(despachoServico.obterParametro(txtParametroId.getTextAsInteger(), null));
		if (entidade == null) {
			entidade = new Parametro();
			entidade.setParametroId(txtParametroId.getTextAsInteger());
		}
		entidade.setDescricao(txtDescricao.getText());
		entidade.setConteudo(txtConteudo.getText());
		entidade.setAtivo(chkAtivo.isSelected());
		
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from Parametro where parametroId = " + id;
		JPAUtils.executeUpdate(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtParametroId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
		txtConteudo.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 3).toString());
		chkAtivo.setSelected(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 4).toString().contentEquals("Sim") ? true : false);
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterParametro(null, null), "parametroId", "descricao", "conteudo", "ativoAsString"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 250, true));
			colunas.add(new MyTableColumn("Conteúdo", 500, true));
			colunas.add(new MyTableColumn("Ativo?", 100, true, JLabel.CENTER));
		}
		return this.colunas;
	}
}
