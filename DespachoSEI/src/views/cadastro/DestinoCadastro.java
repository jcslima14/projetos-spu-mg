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
import model.Destino;
import services.DespachoServico;

@SuppressWarnings("serial")
public class DestinoCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtDestinoId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblDestinoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAbreviacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAbreviacao = new MyLabel("Abreviação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtArtigo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblArtigo = new MyLabel("Artigo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkUsarCartorio = new MyCheckBox("Usar cartório como nome do destinatário") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(5, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public DestinoCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;
		despachoServico = new DespachoServico(conexao);

		pnlCamposEditaveis.add(lblDestinoId);
		pnlCamposEditaveis.add(txtDestinoId);
		pnlCamposEditaveis.add(lblAbreviacao);
		pnlCamposEditaveis.add(txtAbreviacao);
		pnlCamposEditaveis.add(lblArtigo);
		pnlCamposEditaveis.add(txtArtigo);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(chkUsarCartorio);
		pnlCamposEditaveis.add(new JPanel());

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtDestinoId.setText("");
		txtAbreviacao.setText("");
		txtArtigo.setText("");
		txtDescricao.setText("");
		chkUsarCartorio.setSelected(false);
	}

	public void salvarRegistro() throws Exception {
		Destino entidade = MyUtils.entidade(despachoServico.obterDestino(txtDestinoId.getTextAsInteger(-1), null, null, null, null, null));
		if (entidade == null) {
			entidade = new Destino();
		}
		entidade.setAbreviacao(txtAbreviacao.getText());
		entidade.setArtigo(txtArtigo.getText());
		entidade.setDescricao(txtDescricao.getText());
		entidade.setUsarCartorio(chkUsarCartorio.isSelected());
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from destino where destinoid = " + id);
	}

	public void prepararParaEdicao() {
		txtDestinoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtAbreviacao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
		txtArtigo.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 3).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 4).toString());
		chkUsarCartorio.setSelected(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 5).toString().equals("Sim") ? true : false);
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterDestino(null, null, null, null, null, null), "destinoId", "abreviacao", "artigo", "descricao", "usarCartorioAsString"));
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
			colunas.add(new MyTableColumn("Descrição", 400, true));
			colunas.add(new MyTableColumn("Usar cartório?", 100, true, JLabel.CENTER));
		}
		return this.colunas;
	}
}
