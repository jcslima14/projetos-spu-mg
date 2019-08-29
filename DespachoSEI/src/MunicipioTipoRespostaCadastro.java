import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyUtils;

@SuppressWarnings("serial")
public class MunicipioTipoRespostaCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtMunicipioTipoRespostaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipioTipoRespostaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipio = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbOrigem = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta Padrão") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public MunicipioTipoRespostaCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		despachoServico.preencherOpcoesMunicipio(cbbMunicipio, null);
		despachoServico.preencherOpcoesOrigem(cbbOrigem, null);
		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, null);

		pnlCamposEditaveis.add(lblMunicipioTipoRespostaId);
		pnlCamposEditaveis.add(txtMunicipioTipoRespostaId);
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(cbbMunicipio);
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(cbbOrigem);
		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtMunicipioTipoRespostaId.setText("");
		cbbMunicipio.setSelectedIndex(0);
		cbbOrigem.setSelectedIndex(0);
		cbbTipoResposta.setSelectedIndex(0);
	}

	public void salvarRegistro() throws Exception {
		MunicipioTipoResposta municipioTipoResposta = new MunicipioTipoResposta(txtMunicipioTipoRespostaId.getText().equals("") ? null : Integer.parseInt(txtMunicipioTipoRespostaId.getText()),  
				new Municipio(MyUtils.idItemSelecionado(cbbMunicipio)), new Origem(MyUtils.idItemSelecionado(cbbOrigem)), new TipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta)));

		String sql = "";
		if (municipioTipoResposta.getMunicipioTipoRespostaId() != null) {
			sql += "update municipiotiporesposta "
				+  "   set municipioid = " + municipioTipoResposta.getMunicipio().getMunicipioId() 
				+  "     , origemid = " + municipioTipoResposta.getOrigem().getOrigemId() 
				+  "     , tiporespostaid = " + municipioTipoResposta.getTipoResposta().getTipoRespostaId()
				+  " where municipiotiporespostaid = " + municipioTipoResposta.getMunicipioTipoRespostaId();
		} else {
			sql += "insert into municipiotiporesposta (municipioid, origemid, tiporespostaid) values ("
				+  municipioTipoResposta.getMunicipio().getMunicipioId() + ", " 
				+  municipioTipoResposta.getOrigem().getOrigemId() + ", "
				+  municipioTipoResposta.getTipoResposta().getTipoRespostaId() + ") "; 
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from municipiotiporesposta where municipiotiporespostaid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtMunicipioTipoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			MunicipioTipoResposta entidade = MyUtils.entidade(despachoServico.obterMunicipioTipoResposta(Integer.parseInt(txtMunicipioTipoRespostaId.getText()), null, null, null));

			cbbMunicipio.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbMunicipio, entidade.getMunicipio().getMunicipioId(), null));
			cbbOrigem.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbOrigem, entidade.getOrigem().getOrigemId(), null));
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta().getTipoRespostaId(), null));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter as informações do Município x Tipo de Resposta para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
										"select mtr.municipiotiporespostaid "
									  + "	  , m.nome as municipio "
									  + "	  , o.descricao as origem "
									  + "	  , tr.descricao as tiporesposta "
									  + "  from municipiotiporesposta mtr "
									  + "  left join municipio m using (municipioid) "
									  + "  left join origem o using (origemid) "
									  + "  left join tiporesposta tr using (tiporespostaid) "
									  + " order by municipio collate nocase, origem collate nocase ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Município", 400));
			colunas.add(new MyTableColumn("Origem", 100, JLabel.CENTER));
			colunas.add(new MyTableColumn("Tipo de Resposta Padrão", 200));
		}
		return this.colunas;
	}
}
