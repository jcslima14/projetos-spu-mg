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
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class MunicipioCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtMunicipioId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNome = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNome = new MyLabel("Nome") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipioComarca = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipioComarca = new MyLabel("Comarca") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbDestino = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDestino = new MyLabel("Destino") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(5, 2));
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta Padrão") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public MunicipioCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Nenhuma)")); }});
		despachoServico.preencherOpcoesMunicipio(cbbMunicipioComarca, null);
		despachoServico.preencherOpcoesDestino(cbbDestino, null);
		
		pnlCamposEditaveis.add(lblMunicipioId);
		pnlCamposEditaveis.add(txtMunicipioId);
		pnlCamposEditaveis.add(lblNome);
		pnlCamposEditaveis.add(txtNome);
		pnlCamposEditaveis.add(lblMunicipioComarca);
		pnlCamposEditaveis.add(cbbMunicipioComarca);
		pnlCamposEditaveis.add(lblDestino);
		pnlCamposEditaveis.add(cbbDestino);
		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtMunicipioId.setText("");
		txtNome.setText("");
		cbbMunicipioComarca.setSelectedIndex(0);
		cbbDestino.setSelectedIndex(0);
		cbbTipoResposta.setSelectedIndex(0);
	}

	public void salvarRegistro() throws Exception {
		Municipio municipio = new Municipio(txtMunicipioId.getText().equals("") ? null : Integer.parseInt(txtMunicipioId.getText()), txtNome.getText(), 
				new Municipio(MyUtils.idItemSelecionado(cbbMunicipioComarca)), new Destino(MyUtils.idItemSelecionado(cbbDestino)), new TipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta)));

		String sql = "";
		if (municipio.getMunicipioId() != null) {
			sql += "update municipio "
				+  "   set nome = '" + municipio.getNome().replace("'", "''") + "' " 
				+  "     , municipioidcomarca = " + municipio.getMunicipioComarca().getMunicipioId() 
				+  "     , destinoid = " + municipio.getDestino().getDestinoId() 
				+  "     , tiporespostaid = " + (municipio.getTipoResposta().getTipoRespostaId().equals(0) ? "null" : municipio.getTipoResposta().getTipoRespostaId())
				+  " where municipioid = " + municipio.getMunicipioId();
		} else {
			sql += "insert into municipio (nome, municipioidcomarca, destinoid, tiporespostaid) values ("
				+  "'" + municipio.getNome() + "', " 
				+  municipio.getMunicipioComarca().getMunicipioId() + ", " 
				+  municipio.getDestino().getDestinoId() + ", "
				+  (municipio.getTipoResposta().getTipoRespostaId().equals(0) ? "null" : municipio.getTipoResposta().getTipoRespostaId()) + ") "; 
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from municipio where municipioid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		txtMunicipioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			Municipio entidade = despachoServico.obterMunicipio(true, Integer.parseInt(txtMunicipioId.getText()), null).iterator().next();

			txtNome.setText(entidade.getNome());
			cbbMunicipioComarca.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbMunicipioComarca, entidade.getMunicipioComarca() == null ? 0 : entidade.getMunicipioComarca().getMunicipioId(), null));
			cbbDestino.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbDestino, entidade.getDestino() == null ? 0 : entidade.getDestino().getDestinoId(), null));
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta() == null ? 0 : entidade.getTipoResposta().getTipoRespostaId(), null));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter as informações do Município para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
										"select m.municipioid "
									  + "	  , m.nome "
									  + "	  , mc.nome as comarca "
									  + "	  , d.abreviacao as destino "
									  + "	  , tr.descricao as tiporesposta "
									  + "  from municipio m "
									  + "  left join municipio mc on mc.municipioid = m.municipioidcomarca "
									  + "  left join destino d using (destinoid) "
									  + "  left join tiporesposta tr using (tiporespostaid) ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Nome", 300));
			colunas.add(new MyTableColumn("Comarca", 300));
			colunas.add(new MyTableColumn("Destino", 150));
			colunas.add(new MyTableColumn("Tipo de Resposta Padrão", 200));
		}
		return this.colunas;
	}
}
