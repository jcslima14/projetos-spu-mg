import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import com.google.common.io.Files;

import framework.CadastroTemplate;
import framework.ComboBoxItem;
import framework.DialogTemplate;
import framework.MyButton;
import framework.MyCheckBox;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class ProcessoRecebidoCadastro extends CadastroTemplate {

	private Connection conexao;

	private MyButton btnProcessarArquivos = new MyButton("Processar Arquivos");
	private MyButton btnMostrarResultadoDownload = new MyButton("Resultado do download") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
	private MyButton btnMostrarResultadoProcessamento = new MyButton("Resultado do Processamento") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
	private JTextArea txtTexto = new JTextArea(30, 100);
	private JScrollPane scpAreaRolavel = new JScrollPane(txtTexto);
	private JButton btnCopiarAreaTransferencia = new JButton("Copiar");
	private JButton btnIniciarProcessamentoArquivos = new JButton("Iniciar Processamento");

	private JTextField txtSolicitacaoEnvioId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoEnvioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtOrigem = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblOrigem = new MyLabel("Origem") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Número Único") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtAutor = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblAutor = new MyLabel("Autor") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDataMovimentacao = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblDataMovimentacao = new MyLabel("Data/Hora Movimentação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipio = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkArquivosProcessados = new MyCheckBox("Arquivos processados") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(4, 5));

	private MyTextField txtFiltroNumeroProcesso = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroProcesso = new MyLabel("Número do Processo") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroMunicipio = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroMunicipio = new MyLabel("Município") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroArquivosProcessados = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroArquivosProcessados = new MyLabel("Arquivos Processados") {{ setExclusao(true); }};
	private MyComboBox cbbOrdenacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblOrdenacao = new MyLabel("Ordenar por") {{ setExclusao(true); }};
	private JPanel pnlFiltros = new JPanel(new GridLayout(2, 5));

	private SolicitacaoEnvio entidadeEditada;
	
	private List<MyTableColumn> colunas;

	private DespachoServico despachoServico;

	private String resultadoDownload;
	private String resultadoProcessamento;

	public ProcessoRecebidoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesMunicipio(cbbMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Selecione o município)", null, null, null)); }});
		despachoServico.preencherOpcoesMunicipio(cbbFiltroMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Todos)", null, null, null)); add(new Municipio(-1, "(Somente os vazios)", null, null, null)); add(new Municipio(-2, "(Todos os preenchidos)", null, null, null)); }});
		opcoesArquivosProcessados();
		opcoesOrdenacao();

		pnlFiltros.add(lblFiltroNumeroProcesso);
		pnlFiltros.add(txtFiltroNumeroProcesso);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroMunicipio);
		pnlFiltros.add(cbbFiltroMunicipio);
		pnlFiltros.add(lblFiltroArquivosProcessados);
		pnlFiltros.add(cbbFiltroArquivosProcessados);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblOrdenacao);
		pnlFiltros.add(cbbOrdenacao);
		
		pnlCamposEditaveis.add(lblSolicitacaoEnvioId);
		pnlCamposEditaveis.add(txtSolicitacaoEnvioId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblOrigem);
		pnlCamposEditaveis.add(txtOrigem);
		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblAutor);
		pnlCamposEditaveis.add(txtAutor);
		pnlCamposEditaveis.add(lblDataMovimentacao);
		pnlCamposEditaveis.add(txtDataMovimentacao);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblMunicipio);
		pnlCamposEditaveis.add(cbbMunicipio);
		pnlCamposEditaveis.add(chkArquivosProcessados);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(btnMostrarResultadoDownload);
		pnlCamposEditaveis.add(btnMostrarResultadoProcessamento);

		btnProcessarArquivos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mostrarTelaProcessamentoArquivos();
			}
		});

		btnIniciarProcessamentoArquivos.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								processarArquivos();
							} catch (Exception e) {
								e.printStackTrace();
								throw new RuntimeException(e);
							}
						}
					}).start();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null, "Erro ao processar os arquivos: \n" + e1.getMessage());
				}
			}
		});

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
		this.setPnlFiltros(pnlFiltros);
		this.setBtnBotoesPosteriores(new MyButton[] { btnProcessarArquivos });
		this.inicializar();
	}

	private void mostrarMensagemDialogo(String mensagem, String tituloJanela) {
		btnCopiarAreaTransferencia.removeAll();
		btnCopiarAreaTransferencia.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				StringSelection stringSelection = new StringSelection(mensagem);
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

	private void mostrarTelaProcessamentoArquivos() {
		DialogTemplate janelaDialogo = new DialogTemplate("Processamento dos Arquivos");
		txtTexto.setText("");
		janelaDialogo.setPnlAreaCentral(new JPanel() {{ add(scpAreaRolavel); }});
		janelaDialogo.setPnlBotoes(new JPanel() {{ add(btnIniciarProcessamentoArquivos); }});
		janelaDialogo.inicializar();
		janelaDialogo.abrirJanela();
	}

	public void limparCamposEditaveis() {
		txtSolicitacaoEnvioId.setText("");
		txtNumeroProcesso.setText("");
		txtDataMovimentacao.setText("");
		cbbMunicipio.setSelectedIndex(0);
		resultadoDownload = "";
		resultadoProcessamento = "";
		chkArquivosProcessados.setSelected(false);
	}

	private void opcoesArquivosProcessados() {
		cbbFiltroArquivosProcessados.setModel(new MyComboBoxModel());
		cbbFiltroArquivosProcessados.addItem(new ComboBoxItem(-1, null, "(Todos)"));
		cbbFiltroArquivosProcessados.addItem(new ComboBoxItem(0, null, "Não"));
		cbbFiltroArquivosProcessados.addItem(new ComboBoxItem(1, null, "Sim"));
	}

	private void opcoesOrdenacao() {
		cbbOrdenacao.setModel(new MyComboBoxModel());
		cbbOrdenacao.addItem(new ComboBoxItem(null, "numeroprocesso collate nocase", "Número do Processo"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "datahoramovimentacao desc", "Data Movimentação mais recente"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "municipio collate nocase", "Município"));
	}

	public void salvarRegistro() throws Exception {
		List<Municipio> municipios = despachoServico.obterMunicipio(false, MyUtils.idItemSelecionado(cbbMunicipio), null);
		Municipio municipio = (municipios != null && !municipios.isEmpty() ? municipios.iterator().next() : null);

		entidadeEditada.getSolicitacao().setMunicipio(municipio);
		entidadeEditada.setArquivosProcessados(chkArquivosProcessados.isSelected());

		salvarRegistro(entidadeEditada, true);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from solicitacaoenvio where solicitacaoenvioid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		try {
			txtSolicitacaoEnvioId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			entidadeEditada = despachoServico.obterSolicitacaoEnvio(Integer.parseInt(txtSolicitacaoEnvioId.getText()), null, null, null, null, null, false).iterator().next();

			txtOrigem.setText(entidadeEditada.getSolicitacao().getOrigem().getDescricao());
			txtNumeroProcesso.setText(entidadeEditada.getSolicitacao().getNumeroProcesso());
			txtAutor.setText(entidadeEditada.getSolicitacao().getAutor());
			txtDataMovimentacao.setText(entidadeEditada.getDataHoraMovimentacao());
			resultadoDownload = entidadeEditada.getResultadoDownload();
			resultadoProcessamento = entidadeEditada.getResultadoProcessamento();
			cbbMunicipio.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbMunicipio, entidadeEditada.getSolicitacao().getMunicipio() == null ? 0 : entidadeEditada.getSolicitacao().getMunicipio().getMunicipioId(), null));
			chkArquivosProcessados.setSelected(entidadeEditada.getArquivosProcessados());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter informações do Envio de Solicitação para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select se.solicitacaoenvioid ");
		sql.append("	 , o.descricao origem ");
		sql.append("	 , s.numeroprocesso numeroprocesso ");
		sql.append("	 , s.autor autor ");
		sql.append("	 , se.datahoramovimentacao ");
		sql.append("	 , m.nome as municipio ");
		sql.append("	 , se.resultadodownload ");
		sql.append("	 , se.resultadoprocessamento ");
		sql.append("	 , case when se.arquivosprocessados then 'Sim' else 'Não' end as arquivosprocessados ");
		sql.append("  from solicitacaoenvio se ");
		sql.append(" inner join solicitacao s using (solicitacaoid) ");
		sql.append(" inner join origem o using (origemid) ");
		sql.append("  left join municipio m using (municipioid) ");
		sql.append(" where 1 = 1 ");

		if (!txtFiltroNumeroProcesso.getText().trim().equals("")) {
			sql.append(" and s.numeroprocesso like '%" + txtFiltroNumeroProcesso.getText().trim() + "%' ");
		}

		Integer filtroMunicipio = MyUtils.idItemSelecionado(cbbFiltroMunicipio);
		if (filtroMunicipio.equals(-2)) {
			sql.append(" and s.municipioid is not null ");
		} else if (filtroMunicipio.equals(-1)) {
			sql.append(" and s.municipioid is null ");
		} else if (filtroMunicipio.intValue() > 0) {
			sql.append(" and s.municipioid = " + filtroMunicipio);
		}

		Integer filtroArquivosProcessados = MyUtils.idItemSelecionado(cbbFiltroArquivosProcessados);
		if (filtroArquivosProcessados.equals(0)) {
			sql.append(" and not s.arquivosprocessados ");
		} else if (filtroArquivosProcessados.equals(1)) {
			sql.append(" and s.arquivosprocessados ");
		}

		sql.append(" order by ").append(MyUtils.idStringItemSelecionado(cbbOrdenacao));

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Origem", 60, JLabel.CENTER));
			colunas.add(new MyTableColumn("Número Processo", 200, JLabel.CENTER));
			colunas.add(new MyTableColumn("Autor", 300));
			colunas.add(new MyTableColumn("Data Movimentação", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Município", 400));
			colunas.add(new MyTableColumn("Resultado do Download", 400));
			colunas.add(new MyTableColumn("Resultado do Processamento", 400));
			colunas.add(new MyTableColumn("Arq. Proc.?", 100, JLabel.CENTER));
		}
		return this.colunas;
	}

	private void processarArquivos() throws Exception {
		if (JOptionPane.showConfirmDialog(null, "Confirma o processamento dos arquivos?", "Processamento dos arquivos", JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION) {
			return;
		}

		txtTexto.setText("");
		String pastaDownloadSapiens = null;
		String pastaDestinoFinalSapiens = null;

		try {
			pastaDownloadSapiens = despachoServico.obterParametro(Parametro.PASTA_DOWNLOAD_SAPIENS, null).iterator().next().getConteudo();
			pastaDestinoFinalSapiens = despachoServico.obterParametro(Parametro.PASTA_DESTINO_PROCESSOS_SAPIENS, null).iterator().next().getConteudo();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao tentar obter o nome das pastas referentes aos arquivos de processos do Sapiens. Verifique se estão cadastrados os parâmetros 1 (pasta de downloads dos arquivos do Sapiens) e 2 (pasta de destino final dos processos recebidos do Sapiens).");
			return;
		}

		File pastaDownload = new File(pastaDownloadSapiens);
		if (!pastaDownload.exists() || !pastaDownload.isDirectory()) {
			JOptionPane.showMessageDialog(null, "A pasta de download de arquivos do Sapiens não foi encontrada ou não é uma pasta válida.");
			return;
		}

		File pastaDestino = new File(pastaDestinoFinalSapiens);
		if (!pastaDestino.exists() || !pastaDestino.isDirectory()) {
			JOptionPane.showMessageDialog(null, "A pasta de destino dos processos do Sapiens não foi encontrada ou não é uma pasta válida.");
			return;
		}

		List<SolicitacaoEnvio> envios = despachoServico.obterSolicitacaoEnvio(null, null, null, null, null, false, true);
		
		for (SolicitacaoEnvio envio : envios) {
			processamentoArquivosOk(envio, pastaDownload, pastaDestino);
		}
		
		JOptionPane.showMessageDialog(null, "Processamento finalizado. Verifique o resultado filtrando os registros processados.");
		this.executarAtualizar();
	}

	private void processamentoArquivosOk(SolicitacaoEnvio envio, File pastaDownload, File pastaDestino) throws Exception {
		String processoFormatado = envio.getSolicitacao().getNumeroProcesso();
		String pastaProcesso = processoFormatado + " (" + MyUtils.formatarData(MyUtils.obterData(envio.getDataHoraMovimentacao(), "yyyy-MM-dd HH:mm:ss"), "yyyyMMdd_HHmm") + ")";
		File pastaOrigem = new File(pastaDownload, pastaProcesso);
		String msgRetorno = "";
		if (!pastaOrigem.exists() || !pastaOrigem.isDirectory()) {
			msgRetorno = "A pasta com os arquivos do processo (" + pastaProcesso + ") não foi encontrada ou não é uma pasta válida.";
		} else {
			int arquivosCopiados = 0;
			int seqCopia = 0;
			
			for (File arquivoOrigem : pastaOrigem.listFiles()) {
				if (!arquivoOrigem.isDirectory()) {
					String nomeArquivo = arquivoOrigem.getName().substring(0, arquivoOrigem.getName().lastIndexOf(".")) + " " + envio.getSolicitacao().getMunicipio().getNome();
					String extensaoArquivo = arquivoOrigem.getName().substring(arquivoOrigem.getName().lastIndexOf(".") + 1);
					MyUtils.appendLogArea(txtTexto, "Copiando o arquivo '" + arquivoOrigem.getName() + "'");
					String novoNome = nomeArquivo + "." + extensaoArquivo;

					// troca o nome do arquivo, caso já exista no destino
					File arquivoDestino = null;
					do {
						arquivoDestino = new File(pastaDestino, novoNome);

						if (arquivoDestino.exists()) {
							novoNome = processoFormatado + " (" + (++seqCopia) + ") " + envio.getSolicitacao().getMunicipio().getNome() + "." + extensaoArquivo;
						}
					} while (arquivoDestino.exists());
					Files.copy(arquivoOrigem, arquivoDestino);
					do {
						TimeUnit.MILLISECONDS.sleep(200);
					} while (!arquivoDestino.exists() || arquivoOrigem.length() != arquivoDestino.length());
	
					arquivosCopiados ++;
				}
			}
	
			// apaga os arquivos da pasta de origem e a própria pasta de origem
			for (File arquivoOrigem : pastaOrigem.listFiles()) {
				arquivoOrigem.delete();
				MyUtils.appendLogArea(txtTexto, "Excluindo o arquivo '" + arquivoOrigem.getName() + "'");
			}
			pastaOrigem.delete();
			MyUtils.appendLogArea(txtTexto, "Excluindo a pasta '" + pastaOrigem.getName() + "'");
			msgRetorno = "Foram copiados " + arquivosCopiados + " arquivos para a pasta de destino final dos processos do Sapiens.";
			envio.setArquivosProcessados(true);
		}

		envio.setResultadoProcessamento(msgRetorno);
		salvarRegistro(envio, false);
	}

	private void salvarRegistro(SolicitacaoEnvio entidade, boolean atualizarSolicitacao) throws Exception {
		String sql = "";

		if (atualizarSolicitacao) {
			sql += "update solicitacao "
				+  "   set municipioid = " + (entidade.getSolicitacao().getMunicipio() == null ? "null" : entidade.getSolicitacao().getMunicipio().getMunicipioId());
	
			// se a origem for Sapiens, define o destinatário de acordo com a tabela de municípios
			if (entidade.getSolicitacao().getOrigem().getOrigemId().equals(Origem.SAPIENS_ID)) {
				sql += " , destinoid = " + (entidade.getSolicitacao().getMunicipio() == null ? "null" : entidade.getSolicitacao().getMunicipio().getDestino().getDestinoId());
			}
	
			sql += " where solicitacaoid = " + entidade.getSolicitacao().getSolicitacaoId();
	
			MyUtils.execute(conexao, sql);
		}

		sql = "";
		sql += "update solicitacaoenvio ";
		sql += "   set arquivosprocessados = " + (entidade.getArquivosProcessados() ? "true" : "false");
		sql += "     , resultadoprocessamento = " + (entidade.getResultadoProcessamento() == null || entidade.getResultadoProcessamento().trim().equals("") ? "null" : "'" + entidade.getResultadoProcessamento() + "'");
		sql += " where solicitacaoenvioid = " + entidade.getSolictacaoEnvioId();

		MyUtils.execute(conexao, sql);
	}
}
