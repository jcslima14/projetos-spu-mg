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
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class AssinanteTipoDespachoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtAssinanteTipoRespostaId = new JTextField() {{ setEnabled(false); }};
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

	public AssinanteTipoDespachoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, null);
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
		String sql = "";
		if (txtAssinanteTipoRespostaId.getText() != null && !txtAssinanteTipoRespostaId.getText().trim().equals("")) {
			sql += "update assinantetiporesposta "
				+  "   set assinanteid = " + MyUtils.idItemSelecionado(cbbAssinante)
				+  "     , tiporespostaid = " + MyUtils.idItemSelecionado(cbbTipoResposta)
				+  "     , blocoassinatura = '" + txtBlocoAssinatura.getText() + "' "
				+  " where assinantetiporespostaid = " + txtAssinanteTipoRespostaId.getText();
		} else {
			sql += "insert into assinantetiporesposta (assinanteid, tiporespostaid, blocoassinatura) values ("
				+  MyUtils.idItemSelecionado(cbbAssinante) + ", "
				+  MyUtils.idItemSelecionado(cbbTipoResposta) + ", "
				+  "'" + txtBlocoAssinatura.getText() + "') ";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from assinantetiporesposta where assinantetiporespostaid = " + id;
		MyUtils.execute(conexao, sql);
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
		String sql = "";
		sql += "select atr.assinantetiporespostaid ";
		sql += "	 , a.nome as assinante ";
		sql += "	 , tr.descricao as tiporesposta ";
		sql += "	 , atr.blocoassinatura ";
		sql += "  from assinantetiporesposta atr ";
		sql += " inner join assinante a using (assinanteid) ";
		sql += " inner join tiporesposta tr using (tiporespostaid) ";
		sql += " order by a.nome, tr.descricao ";

		ResultSet rs = MyUtils.executeQuery(conexao, sql);
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
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
