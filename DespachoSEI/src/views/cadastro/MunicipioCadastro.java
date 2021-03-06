package views.cadastro;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import framework.components.MyButton;
import framework.components.MyComboBox;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.enums.NivelMensagem;
import framework.templates.CadastroTemplate;
import framework.templates.DialogTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Destino;
import model.Municipio;
import model.Origem;
import model.TipoResposta;
import services.DespachoServico;

@SuppressWarnings("serial")
public class MunicipioCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyButton btnImportarPlanilha = new MyButton("Importar Planilha", MyUtils.obterIcone("/icons/029-list.png"));

	private JFileChooser filArquivo = MyUtils.obterJFileChooser("Planilhas Excel", "xls", "xlsx");
	private JButton btnAbrirArquivo = new JButton("Selecionar arquivo");
	private JLabel lblNomeArquivo = new JLabel("") {{ setVerticalTextPosition(SwingConstants.TOP); setSize(600, 20); }};
	private JLabel lblArquivo = new JLabel("Arquivo:", JLabel.TRAILING) {{ setLabelFor(filArquivo); }};
	private JPanel pnlArquivo = new JPanel();
	private JPanel pnlDialogo = new JPanel();
	private JTextPane txtTexto = MyUtils.obterPainelNotificacoes();
	private JScrollPane scpAreaRolavel = new JScrollPane(txtTexto) {{ getViewport().setPreferredSize(new Dimension(800, 400)); }};
	private JButton btnIniciarImportacaoPlanillha = new JButton("Iniciar Importa��o da Planilha");
	
	private MyTextField txtMunicipioId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblMunicipioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNome = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNome = new MyLabel("Nome") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipioComarca = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipioComarca = new MyLabel("Comarca") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbDestino = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDestino = new MyLabel("Destino") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(5, 2));
	private MyComboBox cbbTipoResposta = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblTipoResposta = new MyLabel("Tipo de Resposta Padr�o") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};

	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public MunicipioCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		despachoServico.preencherOpcoesTipoResposta(cbbTipoResposta, new ArrayList<TipoResposta>() {{ add(new TipoResposta(0, "(Nenhuma)")); }}, null);
		despachoServico.preencherOpcoesMunicipio(cbbMunicipioComarca, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Selecione a comarca)", null, null, null)); }});
		despachoServico.preencherOpcoesDestino(cbbDestino, new ArrayList<Destino>() {{ add(new Destino(0, null, "(Selecione o destino das respostas judiciais)", null, null)); }}, Origem.SAPIENS);

		filArquivo.setFileFilter(new FileNameExtensionFilter("Arquivos Excel (xlsx, xls)", "xls", "xlsx"));
		pnlArquivo.setLayout(new GridLayout(1, 2));
		pnlArquivo.add(new JPanel() {{ add(lblArquivo); add(btnAbrirArquivo); }});
		pnlArquivo.add(lblNomeArquivo);

		pnlDialogo.setLayout(new BorderLayout());
		pnlDialogo.add(pnlArquivo, BorderLayout.NORTH);
		pnlDialogo.add(scpAreaRolavel, BorderLayout.CENTER);
		
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

		btnImportarPlanilha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				txtTexto.setText("");
				mostrarTelaImportacaoPlanilha();
			}
		});

		btnIniciarImportacaoPlanillha.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							importarPlanilha();
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "Erro ao processar os arquivos: \n" + e.getMessage());
						}
					}
				}).start();
			}
		});

		btnAbrirArquivo.addActionListener(MyUtils.openFileDialogWindow(null, filArquivo, lblNomeArquivo, MunicipioCadastro.this, null));

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.setBtnBotoesAcimaPosteriores(new MyButton[] { btnImportarPlanilha });
		this.inicializar();
	}

	private void importarPlanilha() throws Exception {
		if (lblNomeArquivo.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(null, "� necess�rio escolher um arquivo para ser processado.");
			return;
		}

		File fileInput = new File(lblNomeArquivo.getText());
		Workbook wb = WorkbookFactory.create(fileInput);
		Sheet planilha = wb.getSheetAt(0);
		
		Map<String, String[]> municipiosLidos = new LinkedHashMap<String, String[]>();

		MyUtils.appendLogArea(txtTexto, "Lendo as informa��es da planilha...", NivelMensagem.DESTAQUE_NEGRITO);

		for (int l = 1; l <= planilha.getLastRowNum(); l++) {
			Row linha = planilha.getRow(l);
			String nome = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 0)).trim();
			String comarca = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 1)).trim();
			String destino = MyUtils.emptyStringIfNull(MyUtils.obterValorCelula(linha, 2)).trim();

			if (!nome.equals("")) municipiosLidos.put(nome, new String[] { comarca, destino });
		}

		wb.close();
		
		MyUtils.appendLogArea(txtTexto, "Incluindo os munic�pios novos...", NivelMensagem.DESTAQUE_ITALICO);
		
		// percorre a lista de munic�pios lidos, inserindo os que n�o existirem
		for (String nome : municipiosLidos.keySet()) {
			Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(null, nome));
			if (municipio == null) {
				municipio = new Municipio(null, nome, null, null, null);
				salvarMunicipio(municipio);
				MyUtils.appendLogArea(txtTexto, "Munic�pio de " + nome + " inclu�do com sucesso");
			} else {
				MyUtils.appendLogArea(txtTexto, "Munic�pio de " + nome + " j� estava na base de dados", NivelMensagem.ALERTA);
			}
		}

		MyUtils.appendLogArea(txtTexto, "Atualizando as informa��es de comarca e destino das respostas judiciais...", NivelMensagem.DESTAQUE_ITALICO);

		// percorre novamente a lista, atualizando comarca e destino, se tiverem sido informado
		for (String nome : municipiosLidos.keySet()) {
			Municipio municipio = MyUtils.entidade(despachoServico.obterMunicipio(null, nome));
			Municipio oComarca = null;
			Destino oDestino = null;
			String comarca = municipiosLidos.get(nome)[0];
			String destino = municipiosLidos.get(nome)[1];
			String msgErro = "";

			if (!comarca.equals("")) {
				oComarca = MyUtils.entidade(despachoServico.obterMunicipio(null, comarca));
				if (oComarca != null) municipio.setMunicipioComarca(oComarca);
				else msgErro += "A comarca informada (" + comarca + ") n�o foi encontrada";
			}

			if (!destino.equals("")) {
				oDestino = MyUtils.entidade(despachoServico.obterDestino(null, destino, null, false, null, null));
				if (oDestino == null) {
					oDestino = MyUtils.entidade(despachoServico.obterDestino(null, null, destino, false, null, null));
				}
				if (oDestino != null) municipio.setDestino(oDestino);
				else msgErro += (msgErro.equals("") ? "" : " - ") + "O destino informado (" + destino + ") n�o foi encontrado";
			}

			if ((!comarca.equals("") && oComarca != null) || (!destino.equals("") && oDestino != null)) {
				salvarMunicipio(municipio);
				MyUtils.appendLogArea(txtTexto, "O munic�pio de " + nome + " foi atualizado na base de dados: " + (msgErro.equals("") ? "Todas as informa��es adicionais foram atualizadas" : msgErro));
			} else {
				if (!msgErro.equals("")) {
					MyUtils.appendLogArea(txtTexto, "O munic�pio de " + nome + " n�o teve nenhuma informa��o atualizada: " + msgErro, NivelMensagem.ERRO);
				}
			}
		}

		MyUtils.appendLogArea(txtTexto, "Fim do Processamento", NivelMensagem.OK);
		executarAtualizar();
	}

	private void mostrarTelaImportacaoPlanilha() {
		DialogTemplate janelaDialogo = new DialogTemplate("Importa��o de Planilha");
		janelaDialogo.setPnlAreaCentral(new JPanel() {{ add(pnlDialogo); }});
		janelaDialogo.setPnlBotoes(new JPanel() {{ add(btnIniciarImportacaoPlanillha); }});
		janelaDialogo.inicializar();
		janelaDialogo.abrirJanela();
	}

	public void limparCamposEditaveis() {
		txtMunicipioId.setText("");
		txtNome.setText("");
		cbbMunicipioComarca.setSelectedIndex(0);
		cbbDestino.setSelectedIndex(0);
		cbbTipoResposta.setSelectedIndex(0);
	}

	public void salvarRegistro() throws Exception {
		Municipio municipio = new Municipio(txtMunicipioId.getTextAsInteger(), txtNome.getText(), 
				MyUtils.entidade(despachoServico.obterMunicipio(MyUtils.idItemSelecionado(cbbMunicipioComarca), null)), 
				MyUtils.entidade(despachoServico.obterDestino(MyUtils.idItemSelecionado(cbbDestino), null, null, null, null, null)),
				MyUtils.entidade(despachoServico.obterTipoResposta(MyUtils.idItemSelecionado(cbbTipoResposta), null, null)));

		salvarMunicipio(municipio);
	}

	private void salvarMunicipio(Municipio entidade) throws Exception {
		JPAUtils.persistir(conexao, entidade);
	}
	
	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from municipio where municipioid = " + id);
	}

	public void prepararParaEdicao() {
		txtMunicipioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			Municipio entidade = despachoServico.obterMunicipio(Integer.parseInt(txtMunicipioId.getText()), null).iterator().next();

			txtNome.setText(entidade.getNome());
			cbbMunicipioComarca.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbMunicipioComarca, entidade.getMunicipioComarca() == null ? 0 : entidade.getMunicipioComarca().getMunicipioId(), null));
			cbbDestino.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbDestino, entidade.getDestino() == null ? 0 : entidade.getDestino().getDestinoId(), null));
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, entidade.getTipoResposta() == null ? 0 : entidade.getTipoResposta().getTipoRespostaId(), null));
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter as informa��es do Munic�pio para edi��o: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterMunicipio(null, null), "municipioId", "nome", "municipioComarca.nome", "destino.abreviacao", "tipoResposta.descricao"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 20, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Nome", 300));
			colunas.add(new MyTableColumn("Comarca", 300));
			colunas.add(new MyTableColumn("Destino", 150));
			colunas.add(new MyTableColumn("Tipo de Resposta Padr�o", 200));
		}
		return this.colunas;
	}
}
