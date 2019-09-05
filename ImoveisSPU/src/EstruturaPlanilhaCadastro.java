import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyCheckBox;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class EstruturaPlanilhaCadastro extends CadastroTemplate {

	private ImoveisServico imoveisServico;
	private JTextField txtEstruturaPlanilhaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblEstruturaPlanilhaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoPlanilha = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoPlanilha = new MyLabel("Tipo de Planilha") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNomeCampo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNomeCampo = new MyLabel("Nome do Campo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNomeColuna = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNomeColuna = new MyLabel("Nome da Coluna") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkObrigatorio = new MyCheckBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblObrigatorio = new MyLabel("Obrigatório") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(6, 2));
	private List<MyTableColumn> colunas;

	public EstruturaPlanilhaCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		imoveisServico = new ImoveisServico(conexao);

		try {
			opcoesTipoPlanilha();
		} catch (Exception e) {
			e.printStackTrace();
		}

		pnlCamposEditaveis.add(lblEstruturaPlanilhaId);
		pnlCamposEditaveis.add(txtEstruturaPlanilhaId);
		pnlCamposEditaveis.add(lblTipoPlanilha);
		pnlCamposEditaveis.add(cbbTipoPlanilha);
		pnlCamposEditaveis.add(lblNomeCampo);
		pnlCamposEditaveis.add(txtNomeCampo);
		pnlCamposEditaveis.add(lblNomeColuna);
		pnlCamposEditaveis.add(txtNomeColuna);
		pnlCamposEditaveis.add(lblObrigatorio);
		pnlCamposEditaveis.add(chkObrigatorio);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void opcoesTipoPlanilha() throws Exception {
		cbbTipoPlanilha.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(cbbTipoPlanilha, imoveisServico.obterTipoPlanilha(null, null));
	}

	public void limparCamposEditaveis() {
		txtEstruturaPlanilhaId.setText("");
		cbbTipoPlanilha.setSelectedIndex(0);
		txtNomeCampo.setText("");
		txtNomeColuna.setText("");
		chkObrigatorio.setSelected(false);
	}

	public void salvarRegistro() throws Exception {
		EstruturaPlanilha entidade = new EstruturaPlanilha();
		if (txtEstruturaPlanilhaId.getText() != null && !txtEstruturaPlanilhaId.getText().trim().equals("")) {
			entidade = imoveisServico.obterEstruturaPlanilha(Integer.parseInt(txtEstruturaPlanilhaId.getText()), null, null).iterator().next();
		}
		entidade.setTipoPlanilha(imoveisServico.obterTipoPlanilha(MyUtils.idItemSelecionado(cbbTipoPlanilha), null).iterator().next());
		entidade.setNomeCampo(txtNomeCampo.getText().trim());
		entidade.setNomeColuna(txtNomeColuna.getText().trim());
		entidade.setObrigatorio(chkObrigatorio.isSelected()); 
		imoveisServico.persistir(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		EstruturaPlanilha entidade = imoveisServico.obterEstruturaPlanilha(id, null, null).iterator().next();
		imoveisServico.excluir(entidade);
	}

	public void prepararParaEdicao() {
		Integer estruturaPlanilhaId = Integer.parseInt(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		EstruturaPlanilha entidade = null;
		try {
			entidade = imoveisServico.obterEstruturaPlanilha(estruturaPlanilhaId, null, null).iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		cbbTipoPlanilha.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoPlanilha, entidade.getTipoPlanilha().getTipoPlanilhaId(), null));
		txtEstruturaPlanilhaId.setText(estruturaPlanilhaId.toString());
		txtNomeCampo.setText(entidade.getNomeCampo());
		txtNomeColuna.setText(entidade.getNomeColuna());
		chkObrigatorio.setSelected(entidade.getObrigatorio());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(imoveisServico.obterEstruturaPlanilha(null, null, null), "getEstruturaPlanilhaId", "getDescricaoTipoPlanilha", "getNomeCampo", "getNomeColuna", "getObrigatorioSimNao"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Tipo de Planilha", 200));
			colunas.add(new MyTableColumn("Nome do Campo", 200));
			colunas.add(new MyTableColumn("Nome da Coluna", 200));
			colunas.add(new MyTableColumn("Obrigatório?", 100));
		}
		return this.colunas;
	}
}
