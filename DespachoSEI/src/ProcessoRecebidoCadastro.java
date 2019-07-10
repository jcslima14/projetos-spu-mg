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
import java.util.Arrays;
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

	private JTextField txtProcessoRecebidoId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblProcessoRecebidoId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNumeroUnico = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroUnico = new MyLabel("Número Único") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDataMovimentacao = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblDataMovimentacao = new MyLabel("Data/Hora Movimentação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyComboBox cbbMunicipio = new MyComboBox() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblMunicipio = new MyLabel("Município") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkArquivosProcessados = new MyCheckBox("Arquivos processados") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(3, 5));

	private MyTextField txtFiltroNumeroUnico = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroUnico = new MyLabel("Número Único") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroMunicipio = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroMunicipio = new MyLabel("Município") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroArquivosProcessados = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroArquivosProcessados = new MyLabel("Arquivos Processados") {{ setExclusao(true); }};
	private MyComboBox cbbOrdenacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblOrdenacao = new MyLabel("Ordenar por") {{ setExclusao(true); }};
	private JPanel pnlFiltros = new JPanel(new GridLayout(2, 5));

	private List<MyTableColumn> colunas;

	private DespachoServico despachoServico;

	private String resultadoDownload;
	private String resultadoProcessamento;

	public ProcessoRecebidoCadastro(String tituloJanela, Connection conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);

		opcoesMunicipio(cbbMunicipio, new ComboBoxItem(0, null, "(Selecione o município)"));
		opcoesMunicipio(cbbFiltroMunicipio, new ComboBoxItem(0, null, "(Todos)"), new ComboBoxItem(-1, null, "(Somente os vazios)"), new ComboBoxItem(-2, null, "(Todos os preenchidos)"));
		opcoesArquivosProcessados();
		opcoesOrdenacao();

		pnlFiltros.add(lblFiltroNumeroUnico);
		pnlFiltros.add(txtFiltroNumeroUnico);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroMunicipio);
		pnlFiltros.add(cbbFiltroMunicipio);
		pnlFiltros.add(lblFiltroArquivosProcessados);
		pnlFiltros.add(cbbFiltroArquivosProcessados);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblOrdenacao);
		pnlFiltros.add(cbbOrdenacao);
		
		pnlCamposEditaveis.add(lblProcessoRecebidoId);
		pnlCamposEditaveis.add(txtProcessoRecebidoId);
		pnlCamposEditaveis.add(new JPanel());
		pnlCamposEditaveis.add(lblNumeroUnico);
		pnlCamposEditaveis.add(txtNumeroUnico);
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
		txtProcessoRecebidoId.setText("");
		txtNumeroUnico.setText("");
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
		cbbOrdenacao.addItem(new ComboBoxItem(null, "numerounico", "Número Único"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "datahoramovimentacao desc", "Data Movimentação mais recente"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "m.nome", "Município"));
	}

	private void opcoesMunicipio(MyComboBox comboMunicipio, ComboBoxItem... itensAdicionais) {
		comboMunicipio.setModel(new MyComboBoxModel());
		MyUtils.insereOpcoesComboBox(conexao, comboMunicipio, "select municipioid, nome from municipio order by nome", Arrays.asList(itensAdicionais));
	}

	public void salvarRegistro() throws Exception {
		List<Municipio> municipios = despachoServico.obterMunicipio(false, MyUtils.idItemSelecionado(cbbMunicipio), null);
		Municipio municipio = (municipios != null && !municipios.isEmpty() ? municipios.iterator().next() : new Municipio(0));

		ProcessoRecebido entidade = new ProcessoRecebido(txtProcessoRecebidoId.getText().equals("") ? null : Integer.parseInt(txtProcessoRecebidoId.getText()), txtNumeroUnico.getText(), 
				txtDataMovimentacao.getText(), municipio, resultadoDownload, chkArquivosProcessados.isSelected(), resultadoProcessamento);

		salvarRegistro(entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from processorecebido where processorecebidoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		ResultSet rs;
		try {
			txtProcessoRecebidoId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());

			rs = MyUtils.executeQuery(conexao, "select * from processorecebido where processorecebidoid = " + txtProcessoRecebidoId.getText());
			rs.next();

			txtNumeroUnico.setText(rs.getString("numerounico"));
			txtDataMovimentacao.setText(rs.getString("datahoramovimentacao"));
			resultadoDownload = rs.getString("resultadodownload");
			resultadoProcessamento = rs.getString("resultadoprocessamento");
			cbbMunicipio.setSelectedIndex(MyUtils.itemSelecionado(cbbMunicipio, rs.getInt("municipioid"), null));
			chkArquivosProcessados.setSelected(rs.getBoolean("arquivosprocessados"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select t.processorecebidoid ");
		sql.append("	  , t.numerounico ");
		sql.append("	  , t.datahoramovimentacao ");
		sql.append("	  , m.nome as municipio ");
		sql.append("	  , t.resultadodownload ");
		sql.append("	  , t.resultadoprocessamento ");
		sql.append("	  , case when t.arquivosprocessados then 'Sim' else 'Não' end as arquivosprocessados ");
		sql.append("  from processorecebido t ");
		sql.append("  left join municipio m using (municipioid) ");
		sql.append(" where 1 = 1 ");

		if (!txtFiltroNumeroUnico.getText().trim().equals("")) {
			sql.append(" and numerounico like '%" + txtFiltroNumeroUnico.getText().trim().replace("'", "") + "%' ");
		}

		Integer filtroMunicipio = MyUtils.idItemSelecionado(cbbFiltroMunicipio);
		if (filtroMunicipio.equals(-2)) {
			sql.append(" and municipioid is not null ");
		} else if (filtroMunicipio.equals(-1)) {
			sql.append(" and municipioid is null ");
		} else if (filtroMunicipio.intValue() > 0) {
			sql.append(" and municipioid = " + filtroMunicipio);
		}

		Integer filtroArquivosProcessados = MyUtils.idItemSelecionado(cbbFiltroArquivosProcessados);
		if (filtroArquivosProcessados.equals(0)) {
			sql.append(" and not arquivosprocessados ");
		} else if (filtroArquivosProcessados.equals(1)) {
			sql.append(" and arquivosprocessados ");
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
			colunas.add(new MyTableColumn("Número Único", 200, JLabel.CENTER));
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

		List<ProcessoRecebido> processos = despachoServico.obterProcessoRecebido(null, false, null, true);
		
		for (ProcessoRecebido processo : processos) {
			processamentoArquivosOk(processo, pastaDownload, pastaDestino);
		}
		
		JOptionPane.showMessageDialog(null, "Processamento finalizado. Verifique o resultado filtrando os registros processados.");
		this.executarAtualizar();
	}

	private void processamentoArquivosOk(ProcessoRecebido processo, File pastaDownload, File pastaDestino) throws Exception {
		String processoFormatado = processo.getNumeroUnico().replace("/", "").replace(".", "").replace("-", "");
		String pastaProcesso = processoFormatado + " (" + MyUtils.formatarData(MyUtils.obterData(processo.getDataHoraMovimentacao(), "yyyy-MM-dd HH:mm:ss"), "yyyyMMdd_HHmm") + ")";
		File pastaOrigem = new File(pastaDownload, pastaProcesso);
		String msgRetorno = "";
		if (!pastaOrigem.exists() || !pastaOrigem.isDirectory()) {
			msgRetorno = "A pasta com os arquivos do processo (" + pastaProcesso + ") não foi encontrada ou não é uma pasta válida.";
		} else {
			int arquivosCopiados = 0;
			int seqCopia = 0;
			
			for (File arquivoOrigem : pastaOrigem.listFiles()) {
				if (!arquivoOrigem.isDirectory()) {
					String nomeArquivo = arquivoOrigem.getName().substring(0, arquivoOrigem.getName().lastIndexOf(".")) + " " + processo.getMunicipio().getNome();
					String extensaoArquivo = arquivoOrigem.getName().substring(arquivoOrigem.getName().lastIndexOf(".") + 1);
					MyUtils.appendLogArea(txtTexto, "Copiando o arquivo '" + arquivoOrigem.getName() + "'");
					String novoNome = nomeArquivo + "." + extensaoArquivo;

					// troca o nome do arquivo, caso já exista no destino
					File arquivoDestino = null;
					do {
						arquivoDestino = new File(pastaDestino, novoNome);

						if (arquivoDestino.exists()) {
							novoNome = processoFormatado + " (" + (++seqCopia) + ") " + processo.getMunicipio().getNome() + "." + extensaoArquivo;
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
			processo.setArquivosProcessados(true);
		}

		processo.setResultadoProcessamento(msgRetorno);
		salvarRegistro(processo);
	}

	private void salvarRegistro(ProcessoRecebido entidade) throws Exception {
		String sql = "";
		if (entidade.getProcessoRecebidoId() != null) {
			sql += "update processorecebido "
				+  "   set numerounico = '" + entidade.getNumeroUnico() + "' " 
				+  "     , datahoramovimentacao = '" + entidade.getDataHoraMovimentacao() + "' " 
				+  "     , municipioid = " + (entidade.getMunicipio().getMunicipioId().equals(0) ? "null" : entidade.getMunicipio().getMunicipioId())
				+  "     , resultadodownload = " + (entidade.getResultadoDownload() == null || entidade.getResultadoDownload().trim().equals("") ? "null" : "'"  + entidade.getResultadoDownload() + "'")
				+  "     , arquivosprocessados = " + (entidade.getArquivosProcessados() ? "true" : "false") 
				+  "     , resultadoprocessamento = " + (entidade.getResultadoProcessamento() == null || entidade.getResultadoProcessamento().trim().equals("") ? "null" : "'"  + entidade.getResultadoProcessamento() + "'")
				+  " where processorecebidoid = " + entidade.getProcessoRecebidoId();
		} else {
			sql += "insert into processorecebido (numerounico, datahoramovimentacao, municipioid, resultadodownload, resultadoprocessamento, arquivosprocessados) values ("
				+  "'" + entidade.getNumeroUnico() + "', " 
				+  "'" + entidade.getDataHoraMovimentacao() + "', " 
				+  (entidade.getMunicipio().getMunicipioId().equals(0) ? "null" : entidade.getMunicipio().getMunicipioId()) + ", " 
				+  (entidade.getResultadoDownload().trim().equals("") ? "null" : "'"  + entidade.getResultadoDownload() + "'") + ","
				+  (entidade.getResultadoProcessamento().trim().equals("") ? "null" : "'"  + entidade.getResultadoProcessamento() + "'") + ","
				+  (entidade.getArquivosProcessados() ? "true" : "false") + ") "; 
		}

		MyUtils.execute(conexao, sql);
	}
}
