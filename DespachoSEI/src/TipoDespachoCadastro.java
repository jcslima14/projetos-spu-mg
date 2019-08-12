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
import framework.MyCheckBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class TipoDespachoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtTipoDespachoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoDespachoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroDocumentoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroDocumentoSEI = new MyLabel("Nº Documento SEI para Modelo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkGerarProcessoIndividual = new MyCheckBox("Gerar processo individual") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtUnidadeAberturaProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblUnidadeAberturaProcesso = new MyLabel("Unidade para abertura de processo individual") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtTipoProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo de Processo no SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(6, 2));
	private List<MyTableColumn> colunas;

	public TipoDespachoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		pnlCamposEditaveis.add(lblTipoDespachoId);
		pnlCamposEditaveis.add(txtTipoDespachoId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(lblNumeroDocumentoSEI);
		pnlCamposEditaveis.add(txtNumeroDocumentoSEI);
		pnlCamposEditaveis.add(chkGerarProcessoIndividual);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblUnidadeAberturaProcesso);
		pnlCamposEditaveis.add(txtUnidadeAberturaProcesso);
		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(txtTipoProcesso);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtTipoDespachoId.setText("");
		txtDescricao.setText("");
		txtNumeroDocumentoSEI.setText("");
		chkGerarProcessoIndividual.setSelected(false);
		txtUnidadeAberturaProcesso.setText("");
		txtTipoProcesso.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtTipoDespachoId.getText() != null && !txtTipoDespachoId.getText().trim().equals("")) {
			sql += "update tipodespacho "
				+  "set descricao = '" + txtDescricao.getText().trim() + "' "
				+  "     , numerodocumentosei = '" + txtNumeroDocumentoSEI.getText() + "' "
				+  "     , gerarprocessoindividual = " + (chkGerarProcessoIndividual.isSelected() ? "true" : "false")
				+  "     , unidadeaberturaprocesso = '" + txtUnidadeAberturaProcesso.getText() + "' "
				+  "     , tipoprocesso = '" + txtTipoProcesso.getText() + "' "
				+  " where tipodespachoid = " + txtTipoDespachoId.getText();
		} else {
			sql += "insert into tipodespacho (descricao, numerodocumentosei, gerarprocessoindividual, unidadeaberturaprocesso, tipoprocesso) values (";
			sql += "'" + txtDescricao.getText().trim() + "', ";
			sql += "'" + txtNumeroDocumentoSEI.getText() + "', ";
			sql += (chkGerarProcessoIndividual.isSelected() ? "true" : "false") + ", ";
			sql += "'" + txtUnidadeAberturaProcesso.getText() + "', ";
			sql += "'" + txtTipoProcesso.getText() + "') ";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from tipodespacho where tipodespachoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtTipoDespachoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		txtDescricao.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 2).toString());
		txtNumeroDocumentoSEI.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 3).toString());
		chkGerarProcessoIndividual.setSelected(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 4).toString().equalsIgnoreCase("Sim") ? true : false);
		txtUnidadeAberturaProcesso.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 5).toString());
		Object tipoProcesso = this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 6);
		txtTipoProcesso.setText(tipoProcesso == null ? "" : tipoProcesso.toString());
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select tipodespachoid, descricao, numerodocumentosei, case when gerarprocessoindividual then 'Sim' else 'Não' end as gerarprocessoindividual, coalesce(unidadeaberturaprocesso, '') as unidadeaberturaprocesso, tipoprocesso from tipodespacho");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Descrição", 250, true));
			colunas.add(new MyTableColumn("Nº Documento SEI", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Gerar processo?", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Unidade Abertura Processo", 150, true));
			colunas.add(new MyTableColumn("Tipo de Processo no SEI", 250, true));
		}
		return this.colunas;
	}
}
