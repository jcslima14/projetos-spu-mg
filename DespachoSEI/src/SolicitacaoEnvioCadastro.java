import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroController;
import framework.DialogTemplate;
import framework.MyButton;
import framework.MyCheckBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class SolicitacaoEnvioCadastro extends CadastroController {

	private JTextField txtSolicitacaoEnvioId = new JTextField() {{ setEnabled(false); }};
	private MyLabel lblSolicitacaoEnvioId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtDataHoraMovimentacao = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblDataHoraMovimentacao = new MyLabel("Data/Hora de Movimentação") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkArquivosProcessados = new MyCheckBox("Arquivos Processados") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyButton btnMostrarResultadoDownload = new MyButton("Resultado do download") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
	private MyButton btnMostrarResultadoProcessamento = new MyButton("Resultado do Processamento") {{ setEnabled(false); setEdicao(true); setInclusao(false); setExclusao(false); }};
	private JTextArea txtTexto = new JTextArea(30, 100);
	private JScrollPane scpAreaRolavel = new JScrollPane(txtTexto);
	private JButton btnCopiarAreaTransferencia = new JButton("Copiar");
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(3, 2));
	private List<MyTableColumn> colunas;

	private Connection conexao;
	private DespachoServico despachoServico;
	private Solicitacao solicitacao;
	private String resultadoDownload;
	private String resultadoProcessamento;

	public SolicitacaoEnvioCadastro(Connection conexao, DespachoServico despachoServico, Solicitacao solicitacao) {
		this.setExibirBotaoIncluir(false);
		this.despachoServico = despachoServico;
		this.solicitacao = solicitacao;
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
		MyUtils.execute(conexao, "delete from solicitacaoenvio where solicitacaoenvioid = " + id);
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
		ResultSet rs = MyUtils.executeQuery(conexao, 
				"select se.solicitacaoenvioid "
			  + "	  , se.datahoramovimentacao "
			  + "	  , se.resultadodownload "
			  + "	  , case when se.arquivosprocessados then 'Sim' else 'Não' end as arquivosprocessados "
			  + "	  , se.resultadoprocessamento "
			  + "  from solicitacaoenvio se "
			  + " where solicitacaoid = " + solicitacao.getSolicitacaoId()
			  + " order by datahoramovimentacao desc ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 60, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Data/Hora Movimentação", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resultado do Download", 400));
			colunas.add(new MyTableColumn("Arq. Proc.?", 80, JLabel.CENTER));
			colunas.add(new MyTableColumn("Resultado do Processamento dos Arquivos", 400));
		}

		return this.colunas;
	}
}
