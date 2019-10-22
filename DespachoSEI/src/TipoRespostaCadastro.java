import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.JPAUtils;
import framework.MyCheckBox;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class TipoRespostaCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtTipoRespostaId = new MyTextField() {{ setEnabled(false); }};
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
	private MyComboBox cbbOrigem = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtRespostaSPUNet = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblRespostaSPUNet = new MyLabel("Resposta no SPUNet") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtComplementoSPUNet = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblComplementoSPUNet = new MyLabel("Complemento à resposta no SPUNet") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(11, 2));
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public TipoRespostaCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		despachoServico.preencherOpcoesOrigem(cbbOrigem, new ArrayList<Origem>() {{ add(new Origem(0, "(Qualquer origem)")); }});
		
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
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(cbbOrigem);
		pnlCamposEditaveis.add(lblRespostaSPUNet);
		pnlCamposEditaveis.add(txtRespostaSPUNet);
		pnlCamposEditaveis.add(lblComplementoSPUNet);
		pnlCamposEditaveis.add(txtComplementoSPUNet);

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
		cbbOrigem.setSelectedIndex(0);
		txtRespostaSPUNet.setText("");
		txtComplementoSPUNet.setText("");
	}

	public void salvarRegistro() throws Exception {
		TipoResposta entidade = new TipoResposta();
		entidade.setTipoRespostaId(txtTipoRespostaId.getTextAsInteger());
		entidade.setDescricao(txtDescricao.getText());
		entidade.setTipoDocumento(txtTipoDocumento.getText());
		entidade.setNumeroDocumentoModelo(txtNumeroDocumentoModelo.getText());
		entidade.setGerarProcessoIndividual(chkGerarProcessoIndividual.isSelected());
		entidade.setUnidadeAberturaProcesso(txtUnidadeAberturaProcesso.getText());
		entidade.setTipoProcesso(txtTipoProcesso.getText());
		entidade.setImprimirResposta(chkImprimirResposta.isSelected());
		entidade.setQuantidadeAssinaturas(txtQuantidadeAssinaturas.getTextAsInteger());
		entidade.setOrigem(MyUtils.entidade(despachoServico.obterOrigem(MyUtils.idItemSelecionado(cbbOrigem), null)));
		entidade.setRespostaSPUNet(txtRespostaSPUNet.getText());
		entidade.setComplementoSPUNet(txtComplementoSPUNet.getText());

		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from tiporesposta where tiporespostaid = " + id);
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
			txtQuantidadeAssinaturas.setText(entidade.getQuantidadeAssinaturas() == null ? "" : entidade.getQuantidadeAssinaturas().toString());
			cbbOrigem.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbOrigem, (entidade.getOrigem() == null ? 0 : entidade.getOrigem().getOrigemId()), null));
			txtRespostaSPUNet.setText(MyUtils.emptyStringIfNull(entidade.getRespostaSPUNet()));
			txtComplementoSPUNet.setText(MyUtils.emptyStringIfNull(entidade.getComplementoSPUNet()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterTipoResposta(null, null), "tipoRespostaId", "descricao", "tipoDocumento", "numeroDocumentoModelo", 
				"gerarProcessoIndividualAsString", "unidadeAberturaProcesso", "tipoProcesso", "imprimirRespostaAsString", "quantidadeAssinaturas", "origem.descricao", "respostaSPUNet", "complementoSPUNet"));
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
			colunas.add(new MyTableColumn("Quantidade Assinaturas", 80, true, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Origem", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resposta no SPUNet", 150, true));
			colunas.add(new MyTableColumn("Complemento à Resposta no SPUNet", 150, true));
		}
		return this.colunas;
	}
}
