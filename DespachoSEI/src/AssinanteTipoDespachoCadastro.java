import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class AssinanteTipoDespachoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtAssinanteTipoDespachoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblAssinanteTipoDespachoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbAssinante = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAssinante = new MyLabel("Assinado por") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoDespacho = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoDespacho = new MyLabel("Tipo de Despacho") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtBlocoAssinatura = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblBlocoAssinatura = new MyLabel("Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public AssinanteTipoDespachoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		opcoesAssinante();
		opcoesTipoDespacho();

		pnlCamposEditaveis.add(lblAssinanteTipoDespachoId);
		pnlCamposEditaveis.add(txtAssinanteTipoDespachoId);
		pnlCamposEditaveis.add(lblAssinante);
		pnlCamposEditaveis.add(cbbAssinante);
		pnlCamposEditaveis.add(lblTipoDespacho);
		pnlCamposEditaveis.add(cbbTipoDespacho);
		pnlCamposEditaveis.add(lblBlocoAssinatura);
		pnlCamposEditaveis.add(txtBlocoAssinatura);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void opcoesAssinante() {
		cbbAssinante.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbAssinante, "select assinanteid, nome from assinante");
	}

	private void opcoesTipoDespacho() {
		cbbTipoDespacho.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbTipoDespacho, "select tipodespachoid, descricao from tipodespacho", new ArrayList<ComboBoxItem>());
	}

	public void limparCamposEditaveis() {
		txtAssinanteTipoDespachoId.setText("");
		cbbAssinante.setSelectedIndex(0);
		cbbTipoDespacho.setSelectedIndex(0);
		txtBlocoAssinatura.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtAssinanteTipoDespachoId.getText() != null && !txtAssinanteTipoDespachoId.getText().trim().equals("")) {
			sql += "update assinantetipodespacho "
				+  "   set assinanteid = " + MyUtils.idItemSelecionado(cbbAssinante)
				+  "     , tipodespachoid = " + MyUtils.idItemSelecionado(cbbTipoDespacho)
				+  "     , blocoassinatura = '" + txtBlocoAssinatura.getText() + "' "
				+  " where assinantetipodespachoid = " + txtAssinanteTipoDespachoId.getText();
		} else {
			sql += "insert into assinantetipodespacho (assinanteid, tipodespachoid, blocoassinatura) values ("
				+  MyUtils.idItemSelecionado(cbbAssinante) + ", "
				+  MyUtils.idItemSelecionado(cbbTipoDespacho) + ", "
				+  "'" + txtBlocoAssinatura.getText() + "') ";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from assinantetipodespacho where assinantetipodespachoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtAssinanteTipoDespachoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		AssinanteTipoDespacho entidade = null;
		try {
			entidade = despachoServico.obterAssinanteTipoDespacho(Integer.parseInt(txtAssinanteTipoDespachoId.getText()), null, null).iterator().next();
			cbbTipoDespacho.setSelectedIndex(MyUtils.itemSelecionado(cbbTipoDespacho, entidade.getTipoDespacho().getTipoDespachoId(), null));
			cbbAssinante.setSelectedIndex(MyUtils.itemSelecionado(cbbAssinante, entidade.getAssinante().getAssinanteId(), null));
			txtBlocoAssinatura.setText(entidade.getBlocoAssinatura());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		String sql = "";
		sql += "select atd.assinantetipodespachoid ";
		sql += "	 , a.nome as assinante ";
		sql += "	 , td.descricao as tipodespacho ";
		sql += "	 , atd.blocoassinatura ";
		sql += "  from assinantetipodespacho atd ";
		sql += " inner join assinante a using (assinanteid) ";
		sql += " inner join tipodespacho td using (tipodespachoid) ";
		sql += " order by a.nome, td.descricao ";

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
