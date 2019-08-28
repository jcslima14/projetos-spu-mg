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
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class DespachoCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtDespachoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblDespachoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoProcesso = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Nº Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAutor = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAutor = new MyLabel("Autor") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtComarca = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblComarca = new MyLabel("Comarca") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoImovel = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoImovel = new MyLabel("Tipo Imóvel") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtEndereco = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblEndereco = new MyLabel("Endereço") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtMunicipio = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtCoordenada = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblCoordenada = new MyLabel("Coordenada") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtArea = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblArea = new MyLabel("Área") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoDespacho = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoDespacho = new MyLabel("Tipo de Despacho") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbDestino = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDestino = new MyLabel("Destino") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbAssinante = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAssinante = new MyLabel("Assinado por") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtObservacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblObservacao = new MyLabel("Observação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroDocumentoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroDocumentoSEI = new MyLabel("Nº Documento SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkDespachoImpresso = new MyCheckBox("Despacho impresso") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkDespachoNoBlocoAssinatura = new MyCheckBox("Despacho no bloco de assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(8, 5));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public DespachoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		opcoesTipoProcesso();
		opcoesTipoImovel();
		opcoesTipoDespacho();
		opcoesAssinante();
		opcoesDestino();

		pnlCamposEditaveis.add(lblDespachoId);
		pnlCamposEditaveis.add(txtDespachoId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(cbbTipoProcesso);

		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblAutor);
		pnlCamposEditaveis.add(txtAutor);
		
		pnlCamposEditaveis.add(lblComarca);
		pnlCamposEditaveis.add(txtComarca);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblTipoImovel);
		pnlCamposEditaveis.add(cbbTipoImovel);

		pnlCamposEditaveis.add(lblEndereco);
		pnlCamposEditaveis.add(txtEndereco);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(txtMunicipio);

		pnlCamposEditaveis.add(lblCoordenada);
		pnlCamposEditaveis.add(txtCoordenada);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblArea);
		pnlCamposEditaveis.add(txtArea);

		pnlCamposEditaveis.add(lblTipoDespacho);
		pnlCamposEditaveis.add(cbbTipoDespacho);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblDestino);
		pnlCamposEditaveis.add(cbbDestino);

		pnlCamposEditaveis.add(lblAssinante);
		pnlCamposEditaveis.add(cbbAssinante);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblObservacao);
		pnlCamposEditaveis.add(txtObservacao);

		pnlCamposEditaveis.add(lblNumeroDocumentoSEI);
		pnlCamposEditaveis.add(txtNumeroDocumentoSEI);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(chkDespachoImpresso);
		pnlCamposEditaveis.add(chkDespachoNoBlocoAssinatura);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtDespachoId.setText("");
		cbbTipoProcesso.setSelectedIndex(0);
		txtNumeroProcesso.setText("");
		txtAutor.setText("");
		txtComarca.setText("");
		cbbTipoImovel.setSelectedIndex(0);
		txtEndereco.setText("");
		txtMunicipio.setText("");
		txtCoordenada.setText("");
		txtArea.setText("");
		cbbTipoDespacho.setSelectedIndex(0);
		cbbAssinante.setSelectedIndex(0);
		cbbDestino.setSelectedIndex(0);
		txtObservacao.setText("");
		txtNumeroDocumentoSEI.setText("");
		chkDespachoImpresso.setSelected(false);
		chkDespachoNoBlocoAssinatura.setSelected(false);
	}

	private void opcoesTipoProcesso() {
		cbbTipoProcesso.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbTipoProcesso, "select tipoprocessoid, descricao from tipoprocesso");
	}

	private void opcoesTipoImovel() {
		cbbTipoImovel.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbTipoImovel, "select tipoimovelid, descricao from tipoimovel");
	}

	private void opcoesTipoDespacho() {
		cbbTipoDespacho.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbTipoDespacho, "select tipodespachoid, descricao from tipodespacho");
	}

	private void opcoesAssinante() {
		cbbAssinante.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbAssinante, "select assinanteid, nome from assinante");
	}

	private void opcoesDestino() {
		cbbDestino.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, cbbDestino, "select destinoid, abreviacao from destino");
	}

	public void salvarRegistro() throws Exception {
		Despacho despacho = new Despacho(txtDespachoId.getText().equals("") ? null : Integer.parseInt(txtDespachoId.getText()), null, new TipoProcesso(MyUtils.idItemSelecionado(cbbTipoProcesso)),
				txtNumeroProcesso.getText(), txtAutor.getText(), txtComarca.getText(), new TipoImovel(MyUtils.idItemSelecionado(cbbTipoImovel)), txtEndereco.getText(), txtMunicipio.getText(), 
				txtCoordenada.getText(), txtArea.getText(), new TipoDespacho(MyUtils.idItemSelecionado(cbbTipoDespacho)), new Assinante(MyUtils.idItemSelecionado(cbbAssinante)), 
				new Destino(MyUtils.idItemSelecionado(cbbDestino)), txtObservacao.getText(), txtNumeroDocumentoSEI.getText(), null, null, null, (chkDespachoImpresso.isSelected()), null, null,
				(chkDespachoNoBlocoAssinatura.isSelected()));

		despachoServico.salvarDespacho(despacho);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from despacho where despachoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		ResultSet rs;
		try {
			txtDespachoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			rs = MyUtils.executeQuery(conexao, "select * from despacho where despachoid = " + txtDespachoId.getText());
			rs.next();

			cbbTipoProcesso.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoProcesso, rs.getInt("tipoprocessoid"), null));
			txtNumeroProcesso.setText(rs.getString("numeroprocesso"));
			txtAutor.setText(rs.getString("autor"));
			txtComarca.setText(rs.getString("comarca"));
			cbbTipoImovel.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoImovel, rs.getInt("tipoimovelid"), null));
			txtEndereco.setText(rs.getString("endereco"));
			txtMunicipio.setText(rs.getString("municipio"));
			txtCoordenada.setText(rs.getString("coordenada"));
			txtArea.setText(rs.getString("area"));
			cbbTipoDespacho.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoDespacho, rs.getInt("tipodespachoid"), null));
			cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, rs.getInt("assinanteid"), null));
			cbbDestino.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbDestino, rs.getInt("destinoid"), null));
			txtObservacao.setText(rs.getString("observacao"));
			txtNumeroDocumentoSEI.setText(rs.getString("numerodocumentosei"));
			chkDespachoImpresso.setSelected(rs.getBoolean("despachoimpresso"));
			chkDespachoNoBlocoAssinatura.setSelected(rs.getBoolean("despachonoblocoassinatura"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
										"select despachoid "
									  + "	  , datadespacho "
									  + "	  , tp.descricao as tp_descricao "
									  + "	  , despacho.numeroprocesso "
									  + "	  , autor "
									  + "	  , comarca "
									  + "	  , ti.descricao as ti_descricao "
									  + "	  , endereco "
									  + "	  , municipio "
									  + "	  , coordenada "
									  + "	  , area "
									  + "	  , td.descricao as td_descricao "
									  + "	  , a.nome "
									  + "	  , d.abreviacao as destino "
									  + "	  , observacao "
									  + "	  , despacho.numerodocumentosei "
									  + "	  , datahoradespacho "
									  + "     , numeroprocessosei "
									  + "     , case when despachoimpresso then 'Sim' else 'Não' end as despachoimpresso "
									  + "     , datahoraimpressao "
									  + "     , case when despachonoblocoassinatura then 'Sim' else 'Não' end as despachonoblocoassinatura "
									  + "  from despacho "
									  + " inner join tipoprocesso tp using (tipoprocessoid) "
									  + " inner join tipoimovel ti using (tipoimovelid) "
									  + " inner join tipodespacho td using (tipodespachoid) "
									  + " inner join assinante a using (assinanteid) "
									  + " inner join destino d using (destinoid) ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 40, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Data Despacho", 75, JLabel.CENTER));
			colunas.add(new MyTableColumn("Tipo Processo", 75, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo", 130));
			colunas.add(new MyTableColumn("Autor", 200));
			colunas.add(new MyTableColumn("Comarca", 150));
			colunas.add(new MyTableColumn("Tipo Imóvel", 60, JLabel.CENTER));
			colunas.add(new MyTableColumn("Endereço", 200));
			colunas.add(new MyTableColumn("Município", 120));
			colunas.add(new MyTableColumn("Coordenada", 80));
			colunas.add(new MyTableColumn("Área", 80));
			colunas.add(new MyTableColumn("Tipo Despacho", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Assinado por", 150));
			colunas.add(new MyTableColumn("Destino", 150));
			colunas.add(new MyTableColumn("Observação", 50));
			colunas.add(new MyTableColumn("Nº Documento SEI", 60));
			colunas.add(new MyTableColumn("Data Documento SEI", 120, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Despacho Impresso?", 100, JLabel.CENTER));
			colunas.add(new MyTableColumn("Data/Hora Impressão", 120, JLabel.CENTER));
			colunas.add(new MyTableColumn("Despacho no bloco?", 100, JLabel.CENTER));
		}
		return this.colunas;
	}
}
