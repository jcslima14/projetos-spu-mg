import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroController;
import framework.MyCheckBox;
import framework.MyComboBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class SolicitacaoEntidadeCadastro extends CadastroController {

	private JTextField txtSolicitacaoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbOrigem = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbTipoProcesso = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoProcesso = new MyLabel("Tipo Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Nº do Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
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
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(7, 5));

	private DespachoServico despachoServico;
	private Solicitacao entidade;

	public SolicitacaoEntidadeCadastro(DespachoServico despachoServico, Solicitacao entidade) {
		this.setExibirBotoesCadastro(false);
		this.setExibirTabelaDados(false);
		
		this.despachoServico = despachoServico;
		this.entidade = entidade;

		this.despachoServico.preencherOpcoesOrigem(cbbOrigem, null);
		this.despachoServico.preencherOpcoesTipoImovel(cbbTipoImovel, new ArrayList<TipoImovel>() {{ add(new TipoImovel(0, "(Selecione o tipo do imóvel)")); }});
		this.despachoServico.preencherOpcoesTipoProcesso(cbbTipoProcesso, null);
		this.despachoServico.preencherOpcoesMunicipio(cbbMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Selecione o município)", null, null, null)); }});
		this.despachoServico.preencherOpcoesDestino(cbbDestino, new ArrayList<Destino>() {{ add(new Destino(0, "(Selecione o destino)", null, null, null)); }});

		pnlCamposEditaveis.add(lblSolicitacaoId);
		pnlCamposEditaveis.add(txtSolicitacaoId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(cbbOrigem);
		//
		pnlCamposEditaveis.add(lblTipoProcesso);
		pnlCamposEditaveis.add(cbbTipoProcesso);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);
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

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}

	public void limparCamposEditaveis() {
		txtSolicitacaoId.setText("");
		cbbOrigem.setSelectedIndex(0);
		cbbTipoProcesso.setSelectedIndex(0);
		txtNumeroProcesso.setText("");
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
		Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(false, MyUtils.idItemSelecionado(cbbMunicipio), null));
		Destino destino = null;

		if (origem.getOrigemId().equals(Origem.SAPIENS_ID) && (municipio == null || municipio.getDestino() == null)) {
			destino = null;
		} else {
			destino = MyUtils.entidade(despachoServico.obterDestino(MyUtils.idItemSelecionado(cbbDestino), null, null, null, null));
		}
		
		entidade.setOrigem(origem);
		entidade.setTipoProcesso(tipoProcesso);
		entidade.setNumeroProcesso(txtNumeroProcesso.getText());
		entidade.setAutor(txtAutor.getText());
		entidade.setMunicipio(municipio);
		entidade.setDestino(destino);
		entidade.setCartorio(txtCartorio.getText());
		entidade.setTipoImovel(tipoImovel);
		entidade.setEndereco(txtEndereco.getText());
		entidade.setCoordenada(txtCoordenada.getText());
		entidade.setArea(txtArea.getText());
		entidade.setNumeroProcessoSEI(txtNumeroProcessoSEI.getText());
		entidade.setArquivosAnexados(chkArquivosAnexados.isSelected());

		entidade = despachoServico.salvarSolicitacao(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
	}

	public void prepararParaEdicao() {
		txtSolicitacaoId.setText(MyUtils.emptyStringIfNull(entidade.getSolicitacaoId()));
		cbbOrigem.setSelectedIndex(MyUtils.itemSelecionado(cbbOrigem, entidade.getOrigem().getOrigemId(), null));
		cbbTipoProcesso.setSelectedIndex(MyUtils.itemSelecionado(cbbTipoProcesso, entidade.getTipoProcesso().getTipoProcessoId(), null));
		txtNumeroProcesso.setText(entidade.getNumeroProcesso());
		txtAutor.setText(MyUtils.emptyStringIfNull(entidade.getAutor()));
		cbbMunicipio.setSelectedIndex(MyUtils.itemSelecionado(cbbMunicipio, (entidade.getMunicipio() == null ? 0 : entidade.getMunicipio().getMunicipioId()), null));
		cbbDestino.setSelectedIndex(MyUtils.itemSelecionado(cbbDestino, (entidade.getDestino() == null ? 0 : entidade.getDestino().getDestinoId()), null));
		txtCartorio.setText(MyUtils.emptyStringIfNull(entidade.getCartorio()));
		cbbTipoImovel.setSelectedIndex(MyUtils.itemSelecionado(cbbTipoImovel, (entidade.getTipoImovel() == null ? 0 : entidade.getTipoImovel().getTipoImovelId()), null));
		txtEndereco.setText(MyUtils.emptyStringIfNull(entidade.getEndereco()));
		txtCoordenada.setText(MyUtils.emptyStringIfNull(entidade.getCoordenada()));
		txtArea.setText(MyUtils.emptyStringIfNull(entidade.getArea()));
		txtNumeroProcessoSEI.setText(MyUtils.emptyStringIfNull(entidade.getNumeroProcessoSEI()));
		chkArquivosAnexados.setSelected(entidade.getArquivosAnexados());
	}

	public TableModel obterDados() throws Exception {
		return null;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		return null;
	}
}
