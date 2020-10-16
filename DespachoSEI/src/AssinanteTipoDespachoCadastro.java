import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.templates.CadastroTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.AssinanteTipoResposta;

@SuppressWarnings("serial")
public class AssinanteTipoDespachoCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtAssinanteTipoRespostaId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblAssinanteTipoRespostaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbAssinante = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAssinante = new MyLabel("Assinado por") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtBlocoAssinatura = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblBlocoAssinatura = new MyLabel("Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public AssinanteTipoDespachoCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, null, null);
		despachoServico.preencherOpcoesAssinante(cbbAssinante, null, false, null);

		pnlCamposEditaveis.add(lblAssinanteTipoRespostaId);
		pnlCamposEditaveis.add(txtAssinanteTipoRespostaId);
		pnlCamposEditaveis.add(lblAssinante);
		pnlCamposEditaveis.add(cbbAssinante);
		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);
		pnlCamposEditaveis.add(lblBlocoAssinatura);
		pnlCamposEditaveis.add(txtBlocoAssinatura);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtAssinanteTipoRespostaId.setText("");
		cbbAssinante.setSelectedIndex(0);
		cbbTipoResposta.setSelectedIndex(0);
		txtBlocoAssinatura.setText("");
	}

	public void salvarRegistro() throws Exception {
		AssinanteTipoResposta entidade = new AssinanteTipoResposta();
		entidade.setAssinanteTipoRespostaId(txtAssinanteTipoRespostaId.getTextAsInteger());
		entidade.setAssinante(MyUtils.entidade(despachoServico.obterAssinante(MyUtils.idItemSelecionado(cbbAssinante), null, null, null)));
		entidade.setTipoResposta(MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null, null)));
		entidade.setBlocoAssinatura(txtBlocoAssinatura.getText());
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from assinantetiporesposta where assinantetiporespostaid = " + id);
	}

	public void prepararParaEdicao() {
		txtAssinanteTipoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		AssinanteTipoResposta entidade = null;
		try {
			entidade = despachoServico.obterAssinanteTipoResposta(Integer.parseInt(txtAssinanteTipoRespostaId.getText()), null, null).iterator().next();
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta().getTipoRespostaId(), null));
			cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, entidade.getAssinante().getAssinanteId(), null));
			txtBlocoAssinatura.setText(entidade.getBlocoAssinatura());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterAssinanteTipoResposta(null, null, null), "assinanteTipoRespostaId", "assinante.nome", "tipoResposta.descricao", "blocoAssinatura"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Assinante", 300, true));
			colunas.add(new MyTableColumn("Tipo de Despacho", 300, true));
			colunas.add(new MyTableColumn("Bloco Assinatura", 100, true, JLabel.CENTER));
		}
		return this.colunas;
	}
}
