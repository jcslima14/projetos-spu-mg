import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class TipoPlanilhaCadastro extends CadastroTemplate {

	private ImoveisServico imoveisServico;
	private JTextField txtTipoPlanilhaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoPlanilhaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtLinhaCabecalho = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblLinhaCabecalho = new MyLabel("Linha Cabeçalho") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;

	public TipoPlanilhaCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		imoveisServico = new ImoveisServico(conexao);

		pnlCamposEditaveis.add(lblTipoPlanilhaId);
		pnlCamposEditaveis.add(txtTipoPlanilhaId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(lblLinhaCabecalho);
		pnlCamposEditaveis.add(txtLinhaCabecalho);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtTipoPlanilhaId.setText("");
		txtDescricao.setText("");
		txtLinhaCabecalho.setText("");
	}

	public void salvarRegistro() throws Exception {
		TipoPlanilha entidade = new TipoPlanilha();
		if (txtTipoPlanilhaId.getText() != null && !txtTipoPlanilhaId.getText().trim().equals("")) {
			entidade = imoveisServico.obterTipoPlanilha(Integer.parseInt(txtTipoPlanilhaId.getText()), null).iterator().next();
		}
		entidade.setDescricao(txtDescricao.getText().trim());
		entidade.setLinhaCabecalho(Integer.parseInt(txtLinhaCabecalho.getText().trim()));
		imoveisServico.persistir(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		TipoPlanilha entidade = imoveisServico.obterTipoPlanilha(id, null).iterator().next();
		imoveisServico.excluir(entidade);
	}

	public void prepararParaEdicao() {
		Integer tipoPlanilhaId = Integer.parseInt(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		TipoPlanilha entidade = null;
		try {
			entidade = imoveisServico.obterTipoPlanilha(tipoPlanilhaId, null).iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		txtTipoPlanilhaId.setText(tipoPlanilhaId.toString());
		txtDescricao.setText(entidade.getDescricao());
		txtLinhaCabecalho.setText(entidade.getLinhaCabecalho().toString());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(imoveisServico.obterTipoPlanilha(null, null), "getTipoPlanilhaId", "getDescricao", "getLinhaCabecalho"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 200));
			colunas.add(new MyTableColumn("Linha Cabeçalho", 120));
		}
		return this.colunas;
	}
}
