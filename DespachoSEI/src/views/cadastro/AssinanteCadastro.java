package views.cadastro;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.components.MyButton;
import framework.components.MyCheckBox;
import framework.components.MyLabel;
import framework.components.MyTableColumn;
import framework.components.MyTableModel;
import framework.components.MyTextField;
import framework.templates.CadastroTemplate;
import framework.utils.JPAUtils;
import framework.utils.MyUtils;
import model.Assinante;
import services.DespachoServico;

@SuppressWarnings("serial")
public class AssinanteCadastro extends CadastroTemplate {

	private EntityManager conexao;
	private MyTextField txtAssinanteId = new MyTextField() {{ setEnabled(false); }};
	private MyLabel lblAssinanteId = new MyLabel("Id") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtNome = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNome = new MyLabel("Nome") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtCargo = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblCargo = new MyLabel("Cargo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtSetor = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblSetor = new MyLabel("Setor") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkSuperior = new MyCheckBox("Superior") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyCheckBox chkAtivo = new MyCheckBox("Ativo") {{ setEnabled(false); setInclusao(true); setEdicao(true); setSelected(true); }};
	private MyTextField txtNumeroProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblNumeroProcesso = new MyLabel("Nº Processo") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtBlocoAssinatura = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblBlocoAssinatura = new MyLabel("Bloco de Assinatura") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyTextField txtPastaArquivoProcesso = new MyTextField() {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private MyLabel lblPastaArquivoProcesso = new MyLabel("Pasta de arquivos para processos individuais") {{ setEnabled(false); setInclusao(true); setEdicao(true); }};
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(8, 2));
	private MyButton btnTornarPadrao = new MyButton("Tornar este assinante padrão") {{ setEnabled(false); setInclusao(false); setEdicao(true); }};
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public AssinanteCadastro(String tituloJanela, EntityManager conexao) {
		super(tituloJanela);
		this.conexao = conexao;

		despachoServico = new DespachoServico(conexao);
		
		pnlCamposEditaveis.add(lblAssinanteId);
		pnlCamposEditaveis.add(txtAssinanteId);
		pnlCamposEditaveis.add(lblNome);
		pnlCamposEditaveis.add(txtNome);
		pnlCamposEditaveis.add(lblCargo);
		pnlCamposEditaveis.add(txtCargo);
		pnlCamposEditaveis.add(lblSetor);
		pnlCamposEditaveis.add(txtSetor);
		pnlCamposEditaveis.add(chkSuperior);
		pnlCamposEditaveis.add(chkAtivo);
		pnlCamposEditaveis.add(lblNumeroProcesso);
		pnlCamposEditaveis.add(txtNumeroProcesso);
		pnlCamposEditaveis.add(lblBlocoAssinatura);
		pnlCamposEditaveis.add(txtBlocoAssinatura);
		pnlCamposEditaveis.add(lblPastaArquivoProcesso);
		pnlCamposEditaveis.add(txtPastaArquivoProcesso);
		
		btnTornarPadrao.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				despachoServico.salvarAssinantePadrao(Integer.parseInt(txtAssinanteId.getText()));
			}
		});

		this.setBtnBotoesAbaixoPosteriores(btnTornarPadrao);

		this.setPnlCamposEditaveis(pnlCamposEditaveis);
		this.inicializar();
	}
	
	public void limparCamposEditaveis() {
		txtAssinanteId.setText("");
		txtNome.setText("");
		txtCargo.setText("");
		txtSetor.setText("");
		chkSuperior.setSelected(false);
		chkAtivo.setSelected(true);
		txtNumeroProcesso.setText("");
		txtBlocoAssinatura.setText("");
		txtPastaArquivoProcesso.setText("");
	}

	public void salvarRegistro() throws Exception {
		Assinante entidade = MyUtils.entidade(despachoServico.obterAssinante(txtAssinanteId.getTextAsInteger(-1), null, null, null));
		if (entidade == null) {
			entidade = new Assinante();
		}
		entidade.setNome(txtNome.getText());
		entidade.setCargo(txtCargo.getText());
		entidade.setSetor(txtSetor.getText());
		entidade.setSuperior(chkSuperior.isSelected());
		entidade.setAtivo(chkAtivo.isSelected());
		entidade.setNumeroProcessoSEI(txtNumeroProcesso.getText());
		entidade.setBlocoAssinatura(txtBlocoAssinatura.getText());
		entidade.setPastaArquivoProcesso(txtPastaArquivoProcesso.getText());

		JPAUtils.persistir(conexao, entidade);
	}

	public void excluirRegistro(Integer id) throws Exception {
		JPAUtils.executeUpdate(conexao, "delete from assinante where assinanteid = " + id);
	}

	public void prepararParaEdicao() {
		txtAssinanteId.setText(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		
		try {
			Assinante entidade = despachoServico.obterAssinante(Integer.parseInt(txtAssinanteId.getText()), null, null, null).iterator().next();

			txtNome.setText(entidade.getNome());
			txtCargo.setText(entidade.getCargo());
			txtSetor.setText(entidade.getSetor());
			chkSuperior.setSelected(entidade.getSuperior());
			chkAtivo.setSelected(entidade.getAtivo());
			txtNumeroProcesso.setText(entidade.getNumeroProcessoSEI());
			txtBlocoAssinatura.setText(entidade.getBlocoAssinatura());
			txtPastaArquivoProcesso.setText(entidade.getPastaArquivoProcesso());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter informações do Assinante para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(despachoServico.obterAssinante(null, null, null, null), "assinanteId", "nome", "cargo", "setor", "superiorAsString", "ativoAsString", "numeroProcessoSEI", "blocoAssinatura", "pastaArquivoProcesso"));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 30, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Nome", 300, true));
			colunas.add(new MyTableColumn("Cargo", 200, true));
			colunas.add(new MyTableColumn("Setor", 200, true));
			colunas.add(new MyTableColumn("Superior?", 80, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Ativo?", 80, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo", 150, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Bloco Assinatura", 100, true, JLabel.CENTER));
			colunas.add(new MyTableColumn("Pasta arq. proc. indiv.", 200, true));
		}
		return this.colunas;
	}
}
