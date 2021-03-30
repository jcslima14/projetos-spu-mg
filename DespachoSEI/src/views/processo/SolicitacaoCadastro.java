package views.processo;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.components.MyButton;
import framework.components.MyCheckBox;
import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.controllers.CadastroController;
import framework.templates.DialogTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import model.Destino;
import model.Municipio;
import model.Origem;
import model.Solicitacao;
import model.SolicitacaoEnvio;
import model.SolicitacaoResposta;
import model.TipoImovel;
import model.TipoProcesso;
import model.TipoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class SolicitacaoCadastro extends CadastroController {

	private SolicitacaoAnaliseConsulta solicitacaoAnaliseConsulta;

	private MyTextField txtSolicitacaoId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbOrigem = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoProcesso = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Nº do Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtChaveBusca = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblChaveBusca = new MyLabel("Sapiens: NUP / SPUNet: Nº Atendimento") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAutor = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblAutor = new MyLabel("Autor") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipio = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbDestino = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDestino = new MyLabel("Destino") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtCartorio = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblCartorio = new MyLabel("Cartório") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoImovel = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoImovel = new MyLabel("Tipo do Imóvel") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtEndereco = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblEndereco = new MyLabel("Endereço") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtCoordenada = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblCoordenada = new MyLabel("Coordenada") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtArea = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblArea = new MyLabel("Área") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcessoSEI = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcessoSEI = new MyLabel("Nº de Processo do SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkArquivosAnexados = new MyCheckBox("Arquivos Anexados") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(8, 5));

	private DespachoServico despachoServico;
	private Solicitacao entidade;
	private SolicitacaoRespostaCadastro solicitacaoRespostaCadastro;
	private SolicitacaoEnvioCadastro solicitacaoEnvioCadastro;
	private JInternalFrame janela;

	public SolicitacaoRespostaCadastro getSolicitacaoRespostaCadastro() {
		return solicitacaoRespostaCadastro;
	}

	public SolicitacaoEnvioCadastro getSolicitacaoEnvioCadastro() {
		return solicitacaoEnvioCadastro;
	}

	public SolicitacaoCadastro(EntityManager conexao, DespachoServico despachoServico, Solicitacao entidade, SolicitacaoAnaliseConsulta solicitacaoAnaliseConsulta, JInternalFrame janela) {
		this.setExibirBotoesCadastro(false);
		this.setExibirTabelaDados(false);
		this.setExibirBotaoCancelarEdicao(false);
		this.solicitacaoAnaliseConsulta = solicitacaoAnaliseConsulta;
		this.janela = janela;
		
		this.despachoServico = despachoServico;
		this.entidade = entidade;

		this.solicitacaoRespostaCadastro = new SolicitacaoRespostaCadastro(conexao, despachoServico);
		this.solicitacaoEnvioCadastro = new SolicitacaoEnvioCadastro(conexao, despachoServico);

		this.despachoServico.preencherOpcoesOrigem(cbbOrigem, null);
		this.despachoServico.preencherOpcoesTipoImovel(cbbTipoImovel, new ArrayList<TipoImovel>() {{ add(new TipoImovel(0, "(Selecione o tipo do imóvel)")); }});
		this.despachoServico.preencherOpcoesTipoProcesso(cbbTipoProcesso, null);
		this.despachoServico.preencherOpcoesMunicipio(cbbMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Selecione o município)", null, null, null)); }});
		// this.despachoServico.preencherOpcoesDestino(cbbDestino, new ArrayList<Destino>() {{ add(new Destino(0, null, "(Selecione o destino)", null, null)); }});
		
		pnlCamposEditaveis.add(lblSolicitacaoId);
		pnlCamposEditaveis.add(txtSolicitacaoId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		//
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(cbbOrigem);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(cbbTipoProcesso);
		//
		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblChaveBusca);
		pnlCamposEditaveis.add(txtChaveBusca);
		//
		pnlCamposEditaveis.add(lblAutor);
		pnlCamposEditaveis.add(txtAutor);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblTipoImovel);
		pnlCamposEditaveis.add(cbbTipoImovel);
		//
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(cbbMunicipio);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblDestino);
		pnlCamposEditaveis.add(cbbDestino);
		//
		pnlCamposEditaveis.add(lblCartorio);
		pnlCamposEditaveis.add(txtCartorio);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblEndereco);
		pnlCamposEditaveis.add(txtEndereco);
		//
		pnlCamposEditaveis.add(lblCoordenada);
		pnlCamposEditaveis.add(txtCoordenada);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblArea);
		pnlCamposEditaveis.add(txtArea);
		//
		pnlCamposEditaveis.add(lblNumeroProcessoSEI);
		pnlCamposEditaveis.add(txtNumeroProcessoSEI);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(chkArquivosAnexados);
		pnlCamposEditaveis.add(new JPanel());

		cbbOrigem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					alterarOpcoesDestino();
					alterarSelecaoDestino();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Erro ao verificar se o destino deve ser selecionado automaticamente: \n\n" + e.getMessage());
					e.printStackTrace();
				}
			}
		});

		cbbMunicipio.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					alterarOpcoesDestino();
					alterarSelecaoDestino();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Erro ao verificar se o destino deve ser selecionado automaticamente: \n\n" + e.getMessage());
					e.printStackTrace();
				}
			}
		});
		
		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	private void alterarOpcoesDestino() {
		despachoServico.preencherOpcoesDestino(cbbDestino, new ArrayList<Destino>() {{ add(new Destino(0, null, "(Selecione o destino)", null, null)); }}, new Origem(MyUtils.idItemSelecionado(cbbOrigem)));
	}
	
	private void alterarSelecaoDestino() throws Exception {
		boolean habilitado = true;
		if (MyUtils.idItemSelecionado(cbbOrigem).equals(Origem.SAPIENS_ID)) {
			Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(MyUtils.idItemSelecionado(cbbMunicipio), null));
			if (municipio != null && municipio.getDestino() != null) {
				cbbDestino.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbDestino, municipio.getDestino().getDestinoId(), null));
				habilitado = false;
			}
		}

		cbbDestino.setEnabled(habilitado);
		cbbDestino.setInclusao(habilitado);
		cbbDestino.setEdicao(habilitado);
		txtCartorio.setEnabled(habilitado);
		txtCartorio.setInclusao(habilitado);
		txtCartorio.setEdicao(habilitado);
	}

	public void limparCamposEditaveis() {
		txtSolicitacaoId.setText("");
		cbbOrigem.setSelectedIndex(0);
		cbbTipoProcesso.setSelectedIndex(0);
		txtNumeroProcesso.setText("");
		txtChaveBusca.setText("");
		txtAutor.setText("");
		cbbMunicipio.setSelectedIndex(0);
		cbbDestino.setSelectedIndex(0);
		txtCartorio.setText("");
		cbbTipoImovel.setSelectedIndex(0);
		txtEndereco.setText("");
		txtCoordenada.setText("");
		txtArea.setText("");
		txtNumeroProcessoSEI.setText("");
		chkArquivosAnexados.setSelected(false);
	}

	public void salvarRegistro() throws Exception {
		Origem origem = MyUtils.entidade(despachoServico.obterOrigem(MyUtils.idItemSelecionado(cbbOrigem), null));
		TipoProcesso tipoProcesso = MyUtils.entidade(despachoServico.obterTipoProcesso(MyUtils.idItemSelecionado(cbbTipoProcesso), null));
		TipoImovel tipoImovel = MyUtils.entidade(despachoServico.obterTipoImovel(MyUtils.idItemSelecionado(cbbTipoImovel), null));
		Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(MyUtils.idItemSelecionado(cbbMunicipio), null));
		Destino destino = null;

		if (origem.getOrigemId().equals(Origem.SAPIENS_ID) && municipio != null && municipio.getDestino() != null) {
			destino = municipio.getDestino();
		} else {
			destino = MyUtils.entidade(despachoServico.obterDestino(MyUtils.idItemSelecionado(cbbDestino), null, null, null, null, null));
		}

		entidade.setOrigem(origem);
		entidade.setTipoProcesso(tipoProcesso);
		entidade.setNumeroProcesso(txtNumeroProcesso.getText());
		entidade.setChaveBusca(txtChaveBusca.getText());
		entidade.setAutor(txtAutor.getText());
		entidade.setMunicipio(municipio);
		entidade.setDestino(destino);
		entidade.setCartorio(destino == null ? null : (destino.getUsarCartorio() ? txtCartorio.getText() : null));
		entidade.setTipoImovel(tipoImovel);
		entidade.setEndereco(txtEndereco.getText());
		entidade.setCoordenada(txtCoordenada.getText());
		entidade.setArea(txtArea.getText());
		entidade.setNumeroProcessoSEI(txtNumeroProcessoSEI.getText());
		entidade.setArquivosAnexados(chkArquivosAnexados.isSelected());

		String msgVld = validarSolicitacao(entidade);
		if (msgVld != null) {
			JOptionPane.showMessageDialog(null, msgVld);
			return;
		}
		
		entidade = despachoServico.salvarSolicitacao(entidade);

		if (txtSolicitacaoId.getText().equals("")) {
			SolicitacaoEnvio envio = new SolicitacaoEnvio(null, entidade, MyUtils.formatarData(new Date(), "yyyy-MM-dd HH:mm:ss"), null, true, null);
			despachoServico.salvarSolicitacaoEnvio(envio);
			solicitacaoEnvioCadastro.executarAtualizar();
		}
		
		prepararParaEdicao();

		this.solicitacaoAnaliseConsulta.executarAtualizar();
	}

	private String validarSolicitacao(Solicitacao entidade) throws Exception {
		if (entidade.getOrigem() == null) return "A Origem da Solicitação deve ser informada.";
		if (entidade.getTipoProcesso() == null) return "O Tipo de Processo deve ser informado.";
		if (entidade.getTipoProcesso().getTipoProcessoId().equals(1)) {
			if (entidade.getNumeroProcesso().trim().equals("")) {
				return "O Número do Processo deve ser informado.";
			} else {
				Solicitacao s = MyUtils.entidade(despachoServico.obterSolicitacao(null, entidade.getOrigem(), entidade.getTipoProcesso(), entidade.getNumeroProcesso(), null));
				if (s != null && !Objects.equals(s.getSolicitacaoId(), entidade.getSolicitacaoId())) {
					return "Já existe uma outra solicitação cadastrada com esta origem, tipo e número de processo.";
				}
			}
		}

		return null;
	}

	public void excluirRegistro(Integer id) throws Exception {
	}

	public void prepararParaEdicao() {
		txtSolicitacaoId.setText(MyUtils.emptyStringIfNull(entidade.getSolicitacaoId()));
		cbbOrigem.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbOrigem, entidade.getOrigem() == null ? 0 : entidade.getOrigem().getOrigemId(), null));
		cbbTipoProcesso.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoProcesso, entidade.getTipoProcesso() == null ? 0 : entidade.getTipoProcesso().getTipoProcessoId(), null));
		txtNumeroProcesso.setText(entidade.getNumeroProcesso());
		txtChaveBusca.setText(entidade.getChaveBusca());
		txtAutor.setText(MyUtils.emptyStringIfNull(entidade.getAutor()));
		cbbMunicipio.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbMunicipio, (entidade.getMunicipio() == null ? 0 : entidade.getMunicipio().getMunicipioId()), null));
		cbbDestino.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbDestino, (entidade.getDestino() == null ? 0 : entidade.getDestino().getDestinoId()), null));
		txtCartorio.setText(MyUtils.emptyStringIfNull(entidade.getCartorio()));
		cbbTipoImovel.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoImovel, (entidade.getTipoImovel() == null ? 0 : entidade.getTipoImovel().getTipoImovelId()), null));
		txtEndereco.setText(MyUtils.emptyStringIfNull(entidade.getEndereco()));
		txtCoordenada.setText(MyUtils.emptyStringIfNull(entidade.getCoordenada()));
		txtArea.setText(MyUtils.emptyStringIfNull(entidade.getArea()));
		txtNumeroProcessoSEI.setText(MyUtils.emptyStringIfNull(entidade.getNumeroProcessoSEI()));
		chkArquivosAnexados.setSelected(entidade.getArquivosAnexados() == null ? true : entidade.getArquivosAnexados());

		janela.setTitle(obterTituloJanela());
	}

	public TableModel obterDados() throws Exception {
		return null;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		return null;
	}

	@Override
	public void incluirRegistro() throws Exception {
	}

	private String obterTituloJanela() {
		if (entidade.getSolicitacaoId() == null) {
			return "Nova Solicitação de Análise";
		} else {
			return "Solicitação de Análise - Nº Processo: ".concat(entidade.getNumeroProcesso()).concat(" - Autor: ").concat(MyUtils.emptyStringIfNull(entidade.getAutor())).concat(" - Município: ").concat(entidade.getMunicipio() == null ? "(Não identificado ainda)" : entidade.getMunicipio().getNome());
		}
		
	}
	
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
		private MyLabel lblNumeroProcessoSEI = new MyLabel("Nº Processo SEI") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyCheckBox chkRespostaImpressa = new MyCheckBox("Resposta Impressa") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyTextField txtDataHoraImpressao = new MyTextField() {{ setEnabled(false); }};
		private MyLabel lblDataHoraImpressao = new MyLabel("Data/Hora Impressão") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyCheckBox chkRespostaNoBlocoAssinatura = new MyCheckBox("Resposta no Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyTextField txtBlocoAssinatura = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyLabel lblBlocoAssinatura = new MyLabel("Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(7, 5));
		private List<MyTableColumn> colunas;

		private EntityManager conexao;
		private DespachoServico despachoServico;

		public SolicitacaoRespostaCadastro(EntityManager conexao, DespachoServico despachoServico) {
			this.despachoServico = despachoServico;
			this.conexao = conexao;

			despachoServico.preencherOpcoesAssinante(cbbAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Selecione o assinante)")); }}, false, true); 

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
			TipoResposta tipoResposta = MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null, null));
			Assinante assinante = MyUtils.entidade(despachoServico.obterAssinante(MyUtils.idItemSelecionado(cbbAssinante), null, null, null));
			SolicitacaoResposta entidade;
			
			if (txtSolicitacaoRespostaId.getText().equals("")) {
				entidade = new SolicitacaoResposta(null, SolicitacaoCadastro.this.entidade, tipoResposta, txtObservacao.getText(), assinante, null, txtNumeroDocumentoSEI.getText(), null, txtNumeroProcessoSEI.getText(), chkRespostaImpressa.isSelected(), null, txtBlocoAssinatura.getText(), chkRespostaNoBlocoAssinatura.isSelected());
			} else {
				entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText())));
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

			SolicitacaoCadastro.this.solicitacaoAnaliseConsulta.executarAtualizar();
		}

		public void excluirRegistro(Integer id) throws Exception {
			JPAUtils.executeUpdate(conexao, "delete from solicitacaoresposta where solicitacaorespostaid = " + id);
		}

		public void prepararParaEdicao() {
			txtSolicitacaoRespostaId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			try {
				SolicitacaoResposta entidade = MyUtils.entidade(despachoServico.obterSolicitacaoResposta(Integer.parseInt(txtSolicitacaoRespostaId.getText())));

				despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Selecione o tipo de resposta)")); }}, entidade.getSolicitacao().getOrigem());

				cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta() == null ? 0 : entidade.getTipoResposta().getTipoRespostaId(), null));
				txtObservacao.setText(entidade.getObservacao());
				cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, entidade.getAssinante() == null ? 0 : entidade.getAssinante().getAssinanteId(), null));
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
			List<SolicitacaoResposta> solicitacoes = despachoServico.obterSolicitacaoResposta(SolicitacaoCadastro.this.entidade);
			Collections.sort(solicitacoes, new Comparator<SolicitacaoResposta>() {
				@Override
				public int compare(SolicitacaoResposta o1, SolicitacaoResposta o2) {
					String d1 = MyUtils.emptyStringIfNull(o1.getDataHoraResposta()).equals("") ? "9999-12-31 23:59:59" : o1.getDataHoraResposta();
					String d2 = MyUtils.emptyStringIfNull(o2.getDataHoraResposta()).equals("") ? "9999-12-31 23:59:59" : o2.getDataHoraResposta();
					return d2.compareTo(d1);
				}
			});

			TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(solicitacoes, "solicitacaoRespostaId", "tipoResposta.descricao", 
					"observacao", "assinante.nome", "assinanteSuperior.nome", "numeroProcessoSEI", "numeroDocumentoSEI", "dataHoraResposta", "respostaImpressaAsString", "dataHoraImpressao", "respostaNoBlocoAssinaturaAsString", "blocoAssinatura"));

			return tm;
		}

		@Override
		public List<MyTableColumn> getColunas() {
			if (this.colunas == null) {
				colunas = new ArrayList<MyTableColumn>();
				colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
				colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
				colunas.add(new MyTableColumn("Tipo Resposta", 300));
				colunas.add(new MyTableColumn("Observação", 150));
				colunas.add(new MyTableColumn("Assinante", 300));
				colunas.add(new MyTableColumn("Assinante Superior", 150));
				colunas.add(new MyTableColumn("Nº Processo SEI", 150, JLabel.CENTER));
				colunas.add(new MyTableColumn("Nº Documento SEI", 150, JLabel.CENTER));
				colunas.add(new MyTableColumn("Data/Hora Resposta", 150, JLabel.CENTER));
				colunas.add(new MyTableColumn("Resp. Impr.?", 80, JLabel.CENTER));
				colunas.add(new MyTableColumn("Data/Hora Impressão", 150, JLabel.CENTER));
				colunas.add(new MyTableColumn("Resp. Bloco?", 80, JLabel.CENTER));
				colunas.add(new MyTableColumn("Bloco Assinatura", 100, JLabel.CENTER));
			}

			return this.colunas;
		}

		@Override
		public void incluirRegistro() throws Exception {
			Solicitacao s = SolicitacaoCadastro.this.entidade;
			SolicitacaoResposta resposta = MyUtils.entidade(despachoServico.obterSolicitacaoRespostaPendente(s));
			if (resposta != null) {
				throw new Exception("Esta solicitação de análise de usucapião já possui uma resposta pendente de finalização. Altere a resposta pendente ao invés de incluir uma nova.");
			}
			despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Selecione o tipo de resposta)")); }}, s.getOrigem());
			despachoServico.selecionarRespostaPadraoPorMunicipio(cbbTipoResposta, s.getMunicipio(), s.getOrigem());
			despachoServico.selecionarAssinantePadrao(cbbAssinante);
		}
	}

	public class SolicitacaoEnvioCadastro extends CadastroController {

		private JTextField txtSolicitacaoEnvioId = new JTextField() {{ setEnabled(false); }};
		private MyLabel lblSolicitacaoEnvioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyTextField txtDataHoraMovimentacao = new MyTextField() {{ setEnabled(false); }};
		private MyLabel lblDataHoraMovimentacao = new MyLabel("Data/Hora de Movimentação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyCheckBox chkArquivosProcessados = new MyCheckBox("Arquivos Processados") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
		private MyButton btnMostrarResultadoDownload = new MyButton("Resultado do download") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
		private MyButton btnMostrarResultadoProcessamento = new MyButton("Resultado do Processamento") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
		private JTextArea txtTexto = new JTextArea(30, 100);
		private JScrollPane scpAreaRolavel = new JScrollPane(txtTexto) {{ getViewport().setPreferredSize(new Dimension(800, 400)); }};
		private JButton btnCopiarAreaTransferencia = new JButton("Copiar");
		private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(3, 2));
		private List<MyTableColumn> colunas;

		private EntityManager conexao;
		private DespachoServico despachoServico;
		private String resultadoDownload;
		private String resultadoProcessamento;

		public SolicitacaoEnvioCadastro(EntityManager conexao, DespachoServico despachoServico) {
			this.setExibirBotaoIncluir(false);
			this.despachoServico = despachoServico;
			this.conexao = conexao;

			pnlCamposEditaveis.add(lblSolicitacaoEnvioId);
			pnlCamposEditaveis.add(txtSolicitacaoEnvioId);
			pnlCamposEditaveis.add(lblDataHoraMovimentacao);
			pnlCamposEditaveis.add(txtDataHoraMovimentacao);
			pnlCamposEditaveis.add(chkArquivosProcessados);
			pnlCamposEditaveis.add(new JPanel() {{ add(btnMostrarResultadoDownload); add(btnMostrarResultadoProcessamento); }});

			btnMostrarResultadoDownload.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mostrarMensagemDialogo(resultadoDownload, "Resultado do Download");
				}
			});

			btnMostrarResultadoProcessamento.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					mostrarMensagemDialogo(resultadoProcessamento, "Resultado do Processamento dos Arquivos");
				}
			});

			this.setPnlCamposEditaveis(pnlCamposEditaveis);
			this.inicializar();
		}

		private void mostrarMensagemDialogo(String mensagem, String tituloJanela) {
			btnCopiarAreaTransferencia.removeAll();
			btnCopiarAreaTransferencia.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					StringSelection stringSelection = new StringSelection(txtTexto.getText());
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, null);
				}
			});
			DialogTemplate janelaDialogo = new DialogTemplate(tituloJanela);
			txtTexto.setText(mensagem);
			janelaDialogo.setPnlAreaCentral(new JPanel() {{ add(scpAreaRolavel); }});
			janelaDialogo.setPnlBotoes(new JPanel() {{ add(btnCopiarAreaTransferencia); }});
			janelaDialogo.inicializar();
			janelaDialogo.abrirJanela();
		}

		public void limparCamposEditaveis() {
			txtSolicitacaoEnvioId.setText("");
			txtDataHoraMovimentacao.setText("");
			chkArquivosProcessados.setSelected(false);
			resultadoDownload = "";
			resultadoProcessamento = "";
		}

		public void salvarRegistro() throws Exception {
			SolicitacaoEnvio entidade = MyUtils.entidade(despachoServico.obterSolicitacaoEnvio(Integer.parseInt(txtSolicitacaoEnvioId.getText()), null, null, null, null, null, false));
			entidade.setArquivosProcessados(chkArquivosProcessados.isSelected());

			despachoServico.salvarSolicitacaoEnvio(entidade);
		}

		public void excluirRegistro(Integer id) throws Exception {
			JPAUtils.executeUpdate(conexao, "delete from solicitacaoenvio where solicitacaoenvioid = " + id);
		}

		public void prepararParaEdicao() {
			txtSolicitacaoEnvioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			try {
				SolicitacaoEnvio entidade = MyUtils.entidade(despachoServico.obterSolicitacaoEnvio(Integer.parseInt(txtSolicitacaoEnvioId.getText()), null, null, null, null, null, false));
				txtDataHoraMovimentacao.setText(entidade.getDataHoraMovimentacao());
				resultadoProcessamento = entidade.getResultadoProcessamento();
				resultadoDownload = entidade.getResultadoDownload();
				chkArquivosProcessados.setSelected(entidade.getArquivosProcessados());
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Erro ao obter os dados do envio da solicitação de análise: \n\n" + e.getMessage());
				e.printStackTrace();
			}
		}

		public TableModel obterDados() throws Exception {
			List<SolicitacaoEnvio> solicitacoes = despachoServico.obterSolicitacaoEnvio(SolicitacaoCadastro.this.entidade);
			Collections.sort(solicitacoes, new Comparator<SolicitacaoEnvio>() {
				@Override
				public int compare(SolicitacaoEnvio o1, SolicitacaoEnvio o2) {
					String d1 = MyUtils.emptyStringIfNull(o1.getDataHoraMovimentacao()).equals("") ? "9999-12-31 23:59:59" : o1.getDataHoraMovimentacao();
					String d2 = MyUtils.emptyStringIfNull(o2.getDataHoraMovimentacao()).equals("") ? "9999-12-31 23:59:59" : o2.getDataHoraMovimentacao();
					return d2.compareTo(d1);
				}
			});

			TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(solicitacoes, "solicitacaoEnvioId", "dataHoraMovimentacao", "resultadoDownload", "arquivosProcessadosAsString", "resultadoProcessamento"));
			return tm;
		}

		@Override
		public List<MyTableColumn> getColunas() {
			if (this.colunas == null) {
				colunas = new ArrayList<MyTableColumn>();
				colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
				colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
				colunas.add(new MyTableColumn("Data/Hora Movimentação", 150, JLabel.CENTER));
				colunas.add(new MyTableColumn("Resultado do Download", 400));
				colunas.add(new MyTableColumn("Arq. Proc.?", 80, JLabel.CENTER));
				colunas.add(new MyTableColumn("Resultado do Processamento dos Arquivos", 400));
			}

			return this.colunas;
		}

		@Override
		public void incluirRegistro() throws Exception {
		}
	}
}
