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
public class TipoRespostaCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtTipoRespostaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoRespostaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDescricao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDescricao = new MyLabel("Descrição") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtTipoDocumento = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoDocumento = new MyLabel("Tipo de Documento") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroDocumentoModelo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroDocumentoModelo = new MyLabel("Nº Documento SEI para Modelo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkGerarProcessoIndividual = new MyCheckBox("Gerar processo individual") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkImprimirResposta = new MyCheckBox("Gerar resposta em PDF") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtQuantidadeAssinaturas = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblQuantidadeAssinaturas = new MyLabel("Quantidade de Assinaturas no Documento") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtUnidadeAberturaProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblUnidadeAberturaProcesso = new MyLabel("Unidade para abertura de processo individual") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtTipoProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo de Processo no SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(8, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public TipoRespostaCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		pnlCamposEditaveis.add(lblTipoRespostaId);
		pnlCamposEditaveis.add(txtTipoRespostaId);
		pnlCamposEditaveis.add(lblDescricao);
		pnlCamposEditaveis.add(txtDescricao);
		pnlCamposEditaveis.add(lblTipoDocumento);
		pnlCamposEditaveis.add(txtTipoDocumento);
		pnlCamposEditaveis.add(lblNumeroDocumentoModelo);
		pnlCamposEditaveis.add(txtNumeroDocumentoModelo);
		pnlCamposEditaveis.add(chkGerarProcessoIndividual);
		pnlCamposEditaveis.add(chkImprimirResposta);
		pnlCamposEditaveis.add(lblQuantidadeAssinaturas);
		pnlCamposEditaveis.add(txtQuantidadeAssinaturas);
		pnlCamposEditaveis.add(lblUnidadeAberturaProcesso);
		pnlCamposEditaveis.add(txtUnidadeAberturaProcesso);
		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(txtTipoProcesso);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtTipoRespostaId.setText("");
		txtDescricao.setText("");
		txtTipoDocumento.setText("");
		txtNumeroDocumentoModelo.setText("");
		chkGerarProcessoIndividual.setSelected(false);
		chkImprimirResposta.setSelected(false);
		txtQuantidadeAssinaturas.setText("");
		txtUnidadeAberturaProcesso.setText("");
		txtTipoProcesso.setText("");
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtTipoRespostaId.getText() != null && !txtTipoRespostaId.getText().trim().equals("")) {
			sql += "update tiporesposta "
				+  "   set descricao = '" + txtDescricao.getText().trim() + "' "
				+  "     , tipodocumento = '" + txtTipoDocumento.getText() + "' "
				+  "     , numerodocumentomodelo = '" + txtNumeroDocumentoModelo.getText() + "' "
				+  "     , gerarprocessoindividual = " + (chkGerarProcessoIndividual.isSelected() ? "true" : "false")
				+  "     , unidadeaberturaprocesso = '" + txtUnidadeAberturaProcesso.getText() + "' "
				+  "     , tipoprocesso = '" + txtTipoProcesso.getText() + "' "
				+  "     , quantidadeassinaturas = '" + txtQuantidadeAssinaturas.getText() + "' "
				+  "     , imprimirresposta = " + (chkImprimirResposta.isSelected() ? "true" : "false")
				+  " where tiporespostaid = " + txtTipoRespostaId.getText();
		} else {
			sql += "insert into tiporesposta (descricao, tipodocumento, numerodocumentosei, gerarprocessoindividual, unidadeaberturaprocesso, tipoprocesso, imprimirresposta, quantidadeassinaturas) values (";
			sql += "'" + txtDescricao.getText().trim() + "', ";
			sql += "'" + txtTipoDocumento.getText() + "', ";
			sql += "'" + txtNumeroDocumentoModelo.getText() + "', ";
			sql += (chkGerarProcessoIndividual.isSelected() ? "true" : "false") + ", ";
			sql += "'" + txtUnidadeAberturaProcesso.getText() + "', ";
			sql += "'" + txtTipoProcesso.getText() + "', ";
			sql += (chkGerarProcessoIndividual.isSelected() ? "true" : "false") + ", ";
			sql += txtQuantidadeAssinaturas.getText() + ") ";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from tiporesposta where tiporespostaid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtTipoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			TipoResposta entidade = despachoServico.obterTipoResposta(Integer.parseInt(txtTipoRespostaId.getText()), null).iterator().next();

			txtDescricao.setText(entidade.getDescricao());
			txtTipoDocumento.setText(entidade.getTipoDocumento());
			txtNumeroDocumentoModelo.setText(entidade.getNumeroDocumentoModelo());
			chkGerarProcessoIndividual.setSelected(entidade.getGerarProcessoIndividual());
			txtUnidadeAberturaProcesso.setText(entidade.getUnidadeAberturaProcesso());
			txtTipoProcesso.setText(entidade.getTipoProcesso());
			chkImprimirResposta.setSelected(entidade.getImprimirResposta());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select tiporespostaid, descricao, tipodocumento, numerodocumentosei, case when gerarprocessoindividual then 'Sim' else 'Não' end as gerarprocessoindividual, coalesce(unidadeaberturaprocesso, '') as unidadeaberturaprocesso, tipoprocesso, case when imprimirresposta then 'Sim' else 'Não' end as imprimirresposta, quantidadeassinaturas from tiporesposta");
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
			colunas.add(new MyTableColumn("Tipo de Documento", 100, true));
			colunas.add(new MyTableColumn("Nº Documento SEI", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Gerar processo?", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Unidade Abertura Processo", 150, true));
			colunas.add(new MyTableColumn("Tipo de Processo no SEI", 250, true));
			colunas.add(new MyTableColumn("Imprimir resposta?", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Quantidade Assinaturas", 100, true, JLabel.RIGHT));
		}
		return this.colunas;
	}
}
