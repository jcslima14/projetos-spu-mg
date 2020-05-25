import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.JPAUtils;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;
import model.MunicipioTipoResposta;
import model.Origem;

@SuppressWarnings("serial")
public class MunicipioTipoRespostaCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtMunicipioTipoRespostaId = new MyTextField() {{ setEnabled(false); }};
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

	public MunicipioTipoRespostaCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		despachoServico.preencherOpcoesMunicipio(cbbMunicipio, null);
		despachoServico.preencherOpcoesOrigem(cbbOrigem, null);
		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, null, null);

		pnlCamposEditaveis.add(lblMunicipioTipoRespostaId);
		pnlCamposEditaveis.add(txtMunicipioTipoRespostaId);
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(cbbMunicipio);
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(cbbOrigem);
		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);

		cbbOrigem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					alterarOpcoesTipoResposta();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Erro ao atualizar os tipos de resposta de acordo com a origem selecionada: \n\n" + e.getMessage());
					e.printStackTrace();
				}
			}
		});

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void alterarOpcoesTipoResposta() throws Exception {
		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, null, new Origem(MyUtils.idItemSelecionado(cbbOrigem)));
	}

	public void limparCamposEditaveis() {
		txtMunicipioTipoRespostaId.setText("");
		cbbMunicipio.setSelectedIndex(0);
		cbbOrigem.setSelectedIndex(0);
		cbbTipoResposta.setSelectedIndex(0);
	}

	public void salvarRegistro() throws Exception {
		MunicipioTipoResposta entidade = new MunicipioTipoResposta();
		entidade.setMunicipioTipoRespostaId(txtMunicipioTipoRespostaId.getTextAsInteger());
		entidade.setMunicipio(MyUtils.entidade(despachoServico.obterMunicipio(MyUtils.idItemSelecionado(cbbMunicipio), null)));
		entidade.setOrigem(MyUtils.entidade(despachoServico.obterOrigem(MyUtils.idItemSelecionado(cbbOrigem), null)));
		entidade.setTipoResposta(MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null, null)));
		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from municipiotiporesposta where municipiotiporespostaid = " + id);
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
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterMunicipioTipoResposta(null, null, null, null), "municipioTipoRespostaId", "municipio.nome", "origem.descricao", "tipoResposta.descricao"));
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
