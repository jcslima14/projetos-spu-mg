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

import framework.CadastroController;
import framework.MyCheckBox;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class SolicitacaoRespostaCadastro extends CadastroController {

	private JTextField txtSolicitacaoRespostaId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoRespostaId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtObservacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblObservacao = new MyLabel("Observação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbAssinante = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAssinante = new MyLabel("Assinante") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAssinanteSuperior = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblAssinanteSuperior = new MyLabel("Assinante Superior") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroDocumentoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroDocumentoSEI = new MyLabel("Nº Documento SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDataHoraResposta = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblDataHoraResposta = new MyLabel("Data/Hora Resposta") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcessoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcessoSEI = new MyLabel("Nº Documento SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkRespostaImpressa = new MyCheckBox("Resposta Impressa") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDataHoraImpressao = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblDataHoraImpressao = new MyLabel("Data/Hora Impressão") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkRespostaNoBlocoAssinatura = new MyCheckBox("Resposta no Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtBlocoAssinatura = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblBlocoAssinatura = new MyLabel("Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(7, 5));
	private List<MyTableColumn> colunas;

	private Connection conexao;
	private DespachoServico despachoServico;
	private Solicitacao solicitacao;

	public SolicitacaoRespostaCadastro(Connection conexao, DespachoServico despachoServico, Solicitacao solicitacao) {
		this.despachoServico = despachoServico;
		this.solicitacao = solicitacao;
		this.conexao = conexao;

		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Selecione o tipo de resposta)")); }});
		despachoServico.preencherOpcoesAssinante(cbbAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Selecione o assinante)", null, null, null, null, null, null)); }}, false, true); 

		pnlCamposEditaveis.add(lblSolicitacaoRespostaId);
		pnlCamposEditaveis.add(txtSolicitacaoRespostaId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		//
		pnlCamposEditaveis.add(lblTipoResposta);
		pnlCamposEditaveis.add(cbbTipoResposta);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblObservacao);
		pnlCamposEditaveis.add(txtObservacao);
		//
		pnlCamposEditaveis.add(lblAssinante);
		pnlCamposEditaveis.add(cbbAssinante);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblAssinanteSuperior);
		pnlCamposEditaveis.add(txtAssinanteSuperior);
		//
		pnlCamposEditaveis.add(lblNumeroProcessoSEI);
		pnlCamposEditaveis.add(txtNumeroProcessoSEI);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblNumeroDocumentoSEI);
		pnlCamposEditaveis.add(txtNumeroDocumentoSEI);
		//
		pnlCamposEditaveis.add(lblDataHoraResposta);
		pnlCamposEditaveis.add(txtDataHoraResposta);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		//
		pnlCamposEditaveis.add(chkRespostaImpressa);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblDataHoraImpressao);
		pnlCamposEditaveis.add(txtDataHoraImpressao);
		//
		pnlCamposEditaveis.add(chkRespostaNoBlocoAssinatura);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblBlocoAssinatura);
		pnlCamposEditaveis.add(txtBlocoAssinatura);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtSolicitacaoRespostaId.setText("");
		cbbTipoResposta.setSelectedIndex(0);
		txtObservacao.setText("");
		cbbAssinante.setSelectedIndex(0);
		txtAssinanteSuperior.setText("");
		txtNumeroDocumentoSEI.setText("");
		txtDataHoraResposta.setText("");
		txtNumeroProcessoSEI.setText("");
		chkRespostaImpressa.setSelected(false);
		txtDataHoraImpressao.setText("");
		txtBlocoAssinatura.setText("");
		chkRespostaNoBlocoAssinatura.setSelected(false);
	}

	public void salvarRegistro() throws Exception {
		TipoResposta tipoResposta = MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null));
		Assinante assinante = MyUtils.entidade(despachoServico.obterAssinante(MyUtils.idItemSelecionado(cbbAssinante), null, null, null));
		SolicitacaoResposta entidade;
		
		if (txtSolicitacaoRespostaId.getText().equals("")) {
			entidade = new SolicitacaoResposta(null, solicitacao, tipoResposta, txtObservacao.getText(), assinante, null, txtNumeroDocumentoSEI.getText(), null, txtNumeroProcessoSEI.getText(), chkRespostaImpressa.isSelected(), null, txtBlocoAssinatura.getText(), chkRespostaNoBlocoAssinatura.isSelected());
		} else {
			entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText()), null, null, null, false, false, null, null, null, null));
			entidade.setTipoResposta(tipoResposta);
			entidade.setObservacao(txtObservacao.getText());
			entidade.setAssinante(assinante);
			entidade.setNumeroDocumentoSEI(txtNumeroDocumentoSEI.getText());
			entidade.setNumeroProcessoSEI(txtNumeroProcessoSEI.getText());
			entidade.setRespostaImpressa(chkRespostaImpressa.isSelected());
			entidade.setBlocoAssinatura(txtBlocoAssinatura.getText());
			entidade.setRespostaNoBlocoAssinatura(chkRespostaNoBlocoAssinatura.isSelected());
		}

		despachoServico.salvarSolicitacaoResposta(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		MyUtils.execute(conexao, "delete from solicitacaoresposta where solicitacaorespostaid = " + id);
	}

	public void prepararParaEdicao() {
		txtSolicitacaoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

		try {
			SolicitacaoResposta entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText()), null, null, null, false, false, null, null, null, null));
			cbbTipoResposta.setSelectedIndex(MyUtils.itemSelecionado(cbbTipoResposta, entidade.getTipoResposta() == null ? 0 : entidade.getTipoResposta().getTipoRespostaId(), null));
			txtObservacao.setText(entidade.getObservacao());
			cbbAssinante.setSelectedIndex(MyUtils.itemSelecionado(cbbAssinante, entidade.getAssinante() == null ? 0 : entidade.getAssinante().getAssinanteId(), null));
			txtAssinanteSuperior.setText(entidade.getAssinanteSuperior() == null ? "" : entidade.getAssinanteSuperior().getNome());
			txtNumeroDocumentoSEI.setText(entidade.getNumeroDocumentoSEI());
			txtDataHoraResposta.setText(entidade.getDataHoraResposta());
			txtNumeroProcessoSEI.setText(entidade.getNumeroProcessoSEI());
			chkRespostaImpressa.setSelected(entidade.getRespostaImpressa());
			txtDataHoraImpressao.setText(entidade.getDataHoraImpressao());
			txtBlocoAssinatura.setText(entidade.getBlocoAssinatura());
			chkRespostaNoBlocoAssinatura.setSelected(entidade.getRespostaNoBlocoAssinatura());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter os dados do resposta da solicitação de análise: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
				"select sr.solicitacaorespostaid "
			  + "	  , tr.descricao as tiporesposta "
			  + "	  , sr.observacao "
			  + "     , a.nome as assinante "
			  + "	  , asp.nome as assinantesuperior "
			  + "	  , sr.numeroprocessosei "
			  + "	  , sr.numerodocumentosei "
			  + "     , sr.datahoraresposta "
			  + "	  , case when sr.respostaimpressa then 'Sim' else 'Não' end as respostaimpressa "
			  + "	  , sr.datahoraimpressao "
			  + "	  , case when sr.respostanoblocoassinatura then 'Sim' else 'Não' end as respostanoblocoassinatura "
			  + "	  , sr.blocoassinatura "
			  + "  from solicitacaoresposta sr "
			  + "  left join tiporesposta tr using (tiporespostaid) "
			  + "  left join assinante a using (assinanteid) "
			  + "  left join assinante asp on sr.assinanteidsuperior = asp.assinanteid "
			  + " where sr.solicitacaoid = " + solicitacao.getSolicitacaoId()
			  + " order by coalesce(sr.datahoraresposta, '9999-12-31 23:59:59') desc ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Tipo Resposta", 200));
			colunas.add(new MyTableColumn("Observação", 100));
			colunas.add(new MyTableColumn("Assinante", 200));
			colunas.add(new MyTableColumn("Assinante Superior", 150));
			colunas.add(new MyTableColumn("Nº Processo SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Documento SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Data/Hora Resposta", 400, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resp. Impr.?", 80, JLabel.CENTER));
			colunas.add(new MyTableColumn("Data/Hora Impressão", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resp. Bloco?", 80, JLabel.CENTER));
			colunas.add(new MyTableColumn("Bloco Assinatura", 100, JLabel.CENTER));
		}

		return this.colunas;
	}
}
