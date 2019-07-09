import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class MunicipioCorretoCadastro extends CadastroTemplate {

	private ImoveisServico imoveisServico;
	private JTextField txtMunicipioCorrecaoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipioCorrecaoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNomeIncorreto = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNomeIncorreto = new MyLabel("Nome Incorreto") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNomeCorreto = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNomeCorreto = new MyLabel("Nome Correto") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;

	public MunicipioCorretoCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		imoveisServico = new ImoveisServico(conexao);

		pnlCamposEditaveis.add(lblMunicipioCorrecaoId);
		pnlCamposEditaveis.add(txtMunicipioCorrecaoId);
		pnlCamposEditaveis.add(lblNomeIncorreto);
		pnlCamposEditaveis.add(txtNomeIncorreto);
		pnlCamposEditaveis.add(lblNomeCorreto);
		pnlCamposEditaveis.add(txtNomeCorreto);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtMunicipioCorrecaoId.setText("");
		txtNomeIncorreto.setText("");
		txtNomeCorreto.setText("");
	}

	public void salvarRegistro() throws Exception {
		MunicipioCorrecao entidade = new MunicipioCorrecao();
		if (txtMunicipioCorrecaoId.getText() != null && !txtMunicipioCorrecaoId.getText().trim().equals("")) {
			entidade = imoveisServico.obterMunicipioCorrecao(Integer.parseInt(txtMunicipioCorrecaoId.getText()), null, null).iterator().next();
		}
		entidade.setNomeIncorreto(txtNomeIncorreto.getText().trim());
		if (txtNomeCorreto.getText().trim().equals("")) entidade.setNomeCorreto(null);
		else entidade.setNomeCorreto(txtNomeCorreto.getText().trim());
		imoveisServico.persistir(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		MunicipioCorrecao entidade = imoveisServico.obterMunicipioCorrecao(id, null, null).iterator().next();
		imoveisServico.excluir(entidade);
	}

	public void prepararParaEdicao() {
		Integer municipioId = Integer.parseInt(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		MunicipioCorrecao entidade = null;
		try {
			entidade = imoveisServico.obterMunicipioCorrecao(municipioId, null, null).iterator().next();
		} catch (Exception e) {
			e.printStackTrace();
		}
		txtMunicipioCorrecaoId.setText(municipioId.toString());
		txtNomeIncorreto.setText(entidade.getNomeIncorreto());
		if (entidade.getNomeCorreto() != null) txtNomeCorreto.setText(entidade.getNomeCorreto());
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(imoveisServico.obterMunicipioCorrecao(null, null, null), "getMunicipioCorrecaoId", "getNomeIncorreto", "getNomeCorreto"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Município Incorreto", 400));
			colunas.add(new MyTableColumn("Município Correto", 400));
		}
		return this.colunas;
	}
}
