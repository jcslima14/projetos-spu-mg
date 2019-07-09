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
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 2));
	private List<MyTableColumn> colunas;

	public MunicipioCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		opcoesComarca();
		opcoesDestino();

		pnlCamposEditaveis.add(lblMunicipioId);
		pnlCamposEditaveis.add(txtMunicipioId);
		pnlCamposEditaveis.add(lblNome);
		pnlCamposEditaveis.add(txtNome);
		pnlCamposEditaveis.add(lblMunicipioComarca);
		pnlCamposEditaveis.add(cbbMunicipioComarca);
		pnlCamposEditaveis.add(lblDestino);
		pnlCamposEditaveis.add(cbbDestino);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtMunicipioId.setText("");
		txtNome.setText("");
		cbbMunicipioComarca.setSelectedIndex(0);
		cbbDestino.setSelectedIndex(0);
	}

	private void opcoesComarca() {
		cbbMunicipioComarca.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbMunicipioComarca, "select municipioid, nome from municipio order by nome");
	}

	private void opcoesDestino() {
		cbbDestino.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbDestino, "select destinoid, abreviacao from destino order by abreviacao");
	}

	public void salvarRegistro() throws Exception {
		Municipio municipio = new Municipio(txtMunicipioId.getText().equals("") ? null : Integer.parseInt(txtMunicipioId.getText()), txtNome.getText(), 
				new Municipio(MyUtils.idItemSelecionado(cbbMunicipioComarca)), new Destino(MyUtils.idItemSelecionado(cbbDestino)));

		String sql = "";
		if (municipio.getMunicipioId() != null) {
			sql += "update municipio "
				+  "   set nome = '" + municipio.getNome().replace("'", "''") + "' " 
				+  "     , municipioidcomarca = " + municipio.getMunicipioComarca().getMunicipioId() 
				+  "     , destinoid = " + municipio.getDestino().getDestinoId() 
				+  " where municipioid = " + municipio.getMunicipioId();
		} else {
			sql += "insert into municipio (nome, municipioidcomarca, destinoid) values ("
				+  "'" + municipio.getNome() + "', " 
				+  municipio.getMunicipioComarca().getMunicipioId() + ", " 
				+  municipio.getDestino().getDestinoId() + ") "; 
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from municipio where municipioid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		ResultSet rs;
		try {
			txtMunicipioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			rs = MyUtils.executeQuery(conexao, "select * from municipio where municipioid = " + txtMunicipioId.getText());
			rs.next();

			txtNome.setText(rs.getString("nome"));
			cbbMunicipioComarca.setSelectedIndex(MyUtils.itemSelecionado(cbbMunicipioComarca, rs.getInt("municipioidcomarca"), null));
			cbbDestino.setSelectedIndex(MyUtils.itemSelecionado(cbbDestino, rs.getInt("destinoid"), null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
										"select m.municipioid "
									  + "	  , m.nome "
									  + "	  , mc.nome as comarca "
									  + "	  , d.abreviacao as destino "
									  + "  from municipio m "
									  + "  left join municipio mc on mc.municipioid = m.municipioidcomarca "
									  + "  left join destino d using (destinoid) ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Nome", 400));
			colunas.add(new MyTableColumn("Comarca", 400));
			colunas.add(new MyTableColumn("Destino", 150));
		}
		return this.colunas;
	}
}
