package views.processo;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.components.MyCheckBox;
import framework.components.MyComboBox;
import framework.components.MyComboBoxModel;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.models.ComboBoxItem;
import framework.templates.CadastroTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import model.Municipio;
import model.Origem;
import model.Solicitacao;
import model.SolicitacaoResposta;
import model.TipoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class DespachoCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private JTextField txtSolicitacaoRespostaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoRespostaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtOrigem = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtTipoProcesso = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Nº Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAutor = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblAutor = new MyLabel("Autor") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtMunicipio = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtTipoImovel = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblTipoImovel = new MyLabel("Tipo Imóvel") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtEndereco = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblEndereco = new MyLabel("Endereço") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbAssinante = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAssinante = new MyLabel("Assinado por") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtObservacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblObservacao = new MyLabel("Observação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcessoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcessoSEI = new MyLabel("Nº Processo Individual no SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroDocumentoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroDocumentoSEI = new MyLabel("Nº Documento SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkRespostaImpressa = new MyCheckBox("Resposta impressa") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkRespostaNoBlocoAssinatura = new MyCheckBox("Resposta no bloco de assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(7, 5));

	private MyComboBox cbbFiltroOrigem = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroOrigem = new MyLabel("Origem") {{ setExclusao(true); }};
	private MyTextField txtFiltroNumeroProcesso = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroProcesso = new MyLabel("Número do Processo") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroMunicipio = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroMunicipio = new MyLabel("Município") {{ setExclusao(true); }};
	private MyTextField txtFiltroAutor = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroAutor = new MyLabel("Autor") {{ setExclusao(true); }};
	private MyTextField txtFiltroNumeroProcessoSEI = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroProcessoSEI = new MyLabel("Nº Processo Individual SEI") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroTipoResposta = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroTipoResposta = new MyLabel("Tipo de Resposta") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroAssinante = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroAssinante = new MyLabel("Assinante") {{ setExclusao(true); }};
	private MyTextField txtFiltroNumeroDocumentoSEI = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroDocumentoSEI = new MyLabel("Nº Documento Gerado no SEI") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroSituacaoResposta = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroSituacaoResposta = new MyLabel("Situação das Respostas") {{ setExclusao(true); }};
	private MyComboBox cbbOrdenacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblOrdenacao = new MyLabel("Ordenar por") {{ setExclusao(true); }};
	private JPanel pnlFiltros = new JPanel(new GridLayout(5, 5));

	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public DespachoCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.setExibirBotaoIncluir(false);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesOrigem(cbbFiltroOrigem, new ArrayList<Origem>() {{ add(new Origem(0, "(Todas)")); }});
		despachoServico.preencherOpcoesMunicipio(cbbFiltroMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Todos)", null, null, null)); }});
		despachoServico.preencherOpcoesTipoResposta(cbbFiltroTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Todos)")); }}, null);
		despachoServico.preencherOpcoesAssinante(cbbFiltroAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Todos)")); }}, false, null);

		opcoesOrdenacao();
		opcoesSituacaoResposta();
		
		pnlFiltros.add(lblFiltroOrigem);
		pnlFiltros.add(cbbFiltroOrigem);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroNumeroProcesso);
		pnlFiltros.add(txtFiltroNumeroProcesso);

		pnlFiltros.add(lblFiltroMunicipio);
		pnlFiltros.add(cbbFiltroMunicipio);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroAutor);
		pnlFiltros.add(txtFiltroAutor);

		pnlFiltros.add(lblFiltroTipoResposta);
		pnlFiltros.add(cbbFiltroTipoResposta);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroAssinante);
		pnlFiltros.add(cbbFiltroAssinante);

		pnlFiltros.add(lblFiltroNumeroDocumentoSEI);
		pnlFiltros.add(txtFiltroNumeroDocumentoSEI);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroSituacaoResposta);
		pnlFiltros.add(cbbFiltroSituacaoResposta);

		pnlFiltros.add(lblFiltroNumeroProcessoSEI);
		pnlFiltros.add(txtFiltroNumeroProcessoSEI);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblOrdenacao);
		pnlFiltros.add(cbbOrdenacao);

		this.setPnlFiltros(pnlFiltros);

		despachoServico.preencherOpcoesAssinante(cbbAssinante, null, false, true);

		pnlCamposEditaveis.add(lblSolicitacaoRespostaId);
		pnlCamposEditaveis.add(txtSolicitacaoRespostaId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(txtOrigem);

		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(txtTipoProcesso);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);

		pnlCamposEditaveis.add(lblAutor);
		pnlCamposEditaveis.add(txtAutor);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(txtMunicipio);
		
		pnlCamposEditaveis.add(lblTipoImovel);
		pnlCamposEditaveis.add(txtTipoImovel);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblEndereco);
		pnlCamposEditaveis.add(txtEndereco);

		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblAssinante);
		pnlCamposEditaveis.add(cbbAssinante);

		pnlCamposEditaveis.add(lblObservacao);
		pnlCamposEditaveis.add(txtObservacao);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(chkRespostaImpressa);
		pnlCamposEditaveis.add(chkRespostaNoBlocoAssinatura);

		pnlCamposEditaveis.add(lblNumeroProcessoSEI);
		pnlCamposEditaveis.add(txtNumeroProcessoSEI);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblNumeroDocumentoSEI);
		pnlCamposEditaveis.add(txtNumeroDocumentoSEI);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void opcoesOrdenacao() {
		cbbOrdenacao.setModel(new MyComboBoxModel());
		cbbOrdenacao.addItem(new ComboBoxItem(null, "coalesce(datahoraresposta, '9999-12-31') desc", "Data Resposta (mais recente)"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "numeroprocesso", "Número do Processo"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "autor collate nocase", "Autor"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "municipio collate nocase ", "Município"));
	}

	private void opcoesSituacaoResposta() {
		cbbFiltroSituacaoResposta.setModel(new MyComboBoxModel());
		cbbFiltroSituacaoResposta.addItem(new ComboBoxItem(0, null, "(Todos)"));
		cbbFiltroSituacaoResposta.addItem(new ComboBoxItem(1, null, "Somente os que já foram gerados"));
		cbbFiltroSituacaoResposta.addItem(new ComboBoxItem(2, null, "Somente os que ainda não foram gerados"));
	}

	public void limparCamposEditaveis() {
		txtSolicitacaoRespostaId.setText("");
		txtOrigem.setText("");
		txtTipoProcesso.setText("");
		txtNumeroProcesso.setText("");
		txtAutor.setText("");
		txtMunicipio.setText("");
		txtTipoImovel.setText("");
		txtEndereco.setText("");
		cbbTipoResposta.setSelectedIndex(0);
		cbbAssinante.setSelectedIndex(0);
		txtObservacao.setText("");
		chkRespostaImpressa.setSelected(false);
		chkRespostaNoBlocoAssinatura.setSelected(false);
		txtNumeroProcessoSEI.setText("");
		txtNumeroDocumentoSEI.setText("");
	}

	public void salvarRegistro() throws Exception {
		SolicitacaoResposta entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText())));
		Solicitacao solicitacao = entidade.getSolicitacao();
		
		// se o usuário estiver zerando o número de processo, seta o controle de arquivos anexados para false
		if (!MyUtils.emptyStringIfNull(solicitacao.getNumeroProcessoSEI()).trim().equals("") && txtNumeroProcessoSEI.getText().trim().equals("")) solicitacao.setArquivosAnexados(false);
		solicitacao.setNumeroProcessoSEI(txtNumeroProcessoSEI.getText());

		solicitacao = despachoServico.salvarSolicitacao(solicitacao);

		entidade.setSolicitacao(solicitacao);
		entidade.setTipoResposta(MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null, null)));
		entidade.setAssinante(MyUtils.entidade(despachoServico.obterAssinante(MyUtils.idItemSelecionado(cbbAssinante), null, null, null)));
		entidade.setRespostaImpressa(chkRespostaImpressa.isSelected());
		entidade.setRespostaNoBlocoAssinatura(chkRespostaNoBlocoAssinatura.isSelected());
		entidade.setObservacao(txtObservacao.getText());
		entidade.setNumeroDocumentoSEI(txtNumeroDocumentoSEI.getText());

		despachoServico.salvarSolicitacaoResposta(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from solicitacaoresposta where solicitacaorespostaid = " + id);
	}

	public void prepararParaEdicao() {
		try {
			txtSolicitacaoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			SolicitacaoResposta entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText())));

			despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Selecione o tipo de resposta)")); }}, entidade.getSolicitacao().getOrigem());

			txtOrigem.setText(entidade.getSolicitacao().getOrigem().getDescricao());
			txtTipoProcesso.setText(entidade.getSolicitacao().getTipoProcesso().getDescricao());
			txtNumeroProcesso.setText(entidade.getSolicitacao().getNumeroProcesso());
			txtAutor.setText(MyUtils.emptyStringIfNull(entidade.getSolicitacao().getAutor()));
			txtMunicipio.setText(entidade.getSolicitacao().getMunicipio() == null ? "" : entidade.getSolicitacao().getMunicipio().getNome());
			txtTipoImovel.setText(entidade.getSolicitacao().getTipoImovel() == null ? "" : entidade.getSolicitacao().getTipoImovel().getDescricao());
			txtEndereco.setText(MyUtils.emptyStringIfNull(entidade.getSolicitacao().getEndereco()));
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta() == null ? 0 : entidade.getTipoResposta().getTipoRespostaId(), null));
			cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, entidade.getAssinante().getAssinanteId(), null));
			txtObservacao.setText(MyUtils.emptyStringIfNull(entidade.getObservacao()));
			txtNumeroProcessoSEI.setText(MyUtils.emptyStringIfNull(entidade.getSolicitacao().getNumeroProcessoSEI()));
			txtNumeroDocumentoSEI.setText(MyUtils.emptyStringIfNull(entidade.getNumeroDocumentoSEI()));
			chkRespostaImpressa.setSelected(entidade.getRespostaImpressa());
			chkRespostaNoBlocoAssinatura.setSelected(entidade.getRespostaNoBlocoAssinatura());

			if (cbbTipoResposta.getSelectedIndex() == 0) {
				despachoServico.selecionarRespostaPadraoPorMunicipio(cbbTipoResposta, entidade.getSolicitacao().getMunicipio(), entidade.getSolicitacao().getOrigem());
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter informações da resposta à solicitação de análise: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		StringBuilder sql = new StringBuilder("");
		
		sql.append("select sr.solicitacaorespostaid ");
		sql.append("	  , o.descricao as origem ");
		sql.append("	  , tp.descricao as tipoprocesso ");
		sql.append("	  , s.numeroprocesso ");
		sql.append("	  , s.autor ");
		sql.append("	  , m.nome as municipio ");
		sql.append("	  , ti.descricao as tipoimovel ");
		sql.append("	  , s.endereco ");
		sql.append("	  , tr.descricao as tiporesposta ");
		sql.append("	  , a.nome as assinante ");
		sql.append("	  , sr.observacao ");
		sql.append("	  , s.numeroprocessosei as numeroprocessoseiindividual");
		sql.append("	  , sr.numerodocumentosei ");
		sql.append("	  , coalesce(sr.datahoraresposta, '') as datahoraresposta ");
		sql.append("	  , sr.numeroprocessosei as numeroprocessoseiresposta ");
		sql.append("      , case when respostaimpressa then 'Sim' else 'Não' end as respostaimpressa ");
		sql.append("      , coalesce(sr.datahoraimpressao, '') as datahoraimpressao ");
		sql.append("      , case when sr.respostanoblocoassinatura then 'Sim' else 'Não' end as respostanoblocoassinatura ");
		sql.append("  from solicitacaoresposta sr ");
		sql.append(" inner join solicitacao s using (solicitacaoid) ");
		sql.append(" inner join origem o using (origemid) ");
		sql.append(" inner join tipoprocesso tp using (tipoprocessoid) ");
		sql.append("  left join municipio m using (municipioid) ");
		sql.append("  left join tipoimovel ti using (tipoimovelid) ");
		sql.append("  left join tiporesposta tr on sr.tiporespostaid = tr.tiporespostaid ");
		sql.append(" inner join assinante a using (assinanteid) ");
		sql.append(" where 1 = 1");
		
		if (!MyUtils.idItemSelecionado(cbbFiltroOrigem).equals(0)) {
			sql.append(" and s.origemid = " + MyUtils.idItemSelecionado(cbbFiltroOrigem));
		}
		
		if (!txtFiltroNumeroProcesso.getText().trim().equals("")) {
			sql.append(" and s.numeroprocesso like '%" + txtFiltroNumeroProcesso.getText().replaceAll("\\D+", "") + "%'");
		}
		
		if (!MyUtils.idItemSelecionado(cbbFiltroMunicipio).equals(0)) {
			sql.append(" and s.municipioid = " + MyUtils.idItemSelecionado(cbbFiltroMunicipio));
		}
		
		if (!txtFiltroAutor.getText().trim().equals("")) {
			sql.append(" and s.autor like '%" + txtFiltroAutor.getText() + "%'");
		}
		
		if (!txtFiltroNumeroProcessoSEI.getText().trim().equals("")) {
			sql.append(" and replace(replace(replace(s.numeroprocessosei, '.', ''), '-', ''), '/', '') like '%" + txtFiltroNumeroProcessoSEI.getText().replaceAll("\\D+", "") + "%'");
		}
		
		if (!MyUtils.idItemSelecionado(cbbFiltroTipoResposta).equals(0)) {
			sql.append(" and sr.tiporespostaid = " + MyUtils.idItemSelecionado(cbbFiltroTipoResposta));
		}
		
		if (!MyUtils.idItemSelecionado(cbbFiltroAssinante).equals(0)) {
			sql.append(" and sr.assinanteid = " + MyUtils.idItemSelecionado(cbbFiltroAssinante));
		}

		if (!txtFiltroNumeroDocumentoSEI.getText().trim().equals("")) {
			sql.append(" and sr.numerodocumentosei like '%" + txtFiltroNumeroDocumentoSEI.getText().replaceAll("\\D+", "") + "%'");
		}
		
		if (MyUtils.idItemSelecionado(cbbFiltroSituacaoResposta).equals(1)) {
			sql.append(" and coalesce(sr.numerodocumentosei, '') <> '' ");
		}
		
		if (MyUtils.idItemSelecionado(cbbFiltroSituacaoResposta).equals(2)) {
			sql.append(" and coalesce(sr.numerodocumentosei, '') = '' ");
		}

		sql.append(" order by ").append(MyUtils.idStringItemSelecionado(cbbOrdenacao));
		
		List<Object[]> rs = JPAUtils.executeNativeQuery(conexao, sql.toString());

		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 40, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Origem", 80, JLabel.CENTER));
			colunas.add(new MyTableColumn("Tipo Processo", 75, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo", 160, JLabel.CENTER));
			colunas.add(new MyTableColumn("Autor", 200));
			colunas.add(new MyTableColumn("Município", 200));
			colunas.add(new MyTableColumn("Tipo Imóvel", 60, JLabel.CENTER));
			colunas.add(new MyTableColumn("Endereço", 200));
			colunas.add(new MyTableColumn("Tipo Resposta", 150));
			colunas.add(new MyTableColumn("Assinado por", 150));
			colunas.add(new MyTableColumn("Observação", 50));
			colunas.add(new MyTableColumn("Nº Processo Indiv. SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Documento SEI", 70, JLabel.CENTER));
			colunas.add(new MyTableColumn("Data Documento SEI", 120, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo Resp. SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resp. Impr.?", 80, JLabel.CENTER));
			colunas.add(new MyTableColumn("Data/Hora Impressão", 130, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resp. bloco?", 80, JLabel.CENTER));
		}
		return this.colunas;
	}
}
