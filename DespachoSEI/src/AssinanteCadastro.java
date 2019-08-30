import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyButton;
import framework.MyCheckBox;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class AssinanteCadastro extends CadastroTemplate {

	private Connection conexao;
	private JTextField txtAssinanteId = new JTextField() {{ setEnabled(false); }};
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
	private JPanel pnlCamposEditaveis = new JPanel(new GridLayout(7, 2));
	private MyButton btnTornarPadrao = new MyButton("Tornar este assinante padrão") {{ setEnabled(false); setInclusao(false); setEdicao(true); }};
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;

	public AssinanteCadastro(String tituloJanela, Connection conexao) {
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
	}

	public void salvarRegistro() throws Exception {
		String sql = "";
		if (txtAssinanteId.getText() != null && !txtAssinanteId.getText().trim().equals("")) {
			sql += "update assinante "
				+ "	   set nome = '" + txtNome.getText().trim() + "' "
				+  "     , cargo = '" + txtCargo.getText() + "' "
				+  "     , setor = '" + txtSetor.getText() + "' "
				+  "	 , superior = " + (chkSuperior.isSelected() ? "true" : "false") 
				+  "	 , ativo = " + (chkAtivo.isSelected() ? "true" : "false") 
				+  "     , numeroprocessosei = '" + txtNumeroProcesso.getText() + "' "
				+  "     , blocoassinatura = '" + txtBlocoAssinatura.getText() + "' "
				+  " where assinanteid = " + txtAssinanteId.getText();
		} else {
			sql += "insert into assinante (nome, cargo, setor, superior, ativo, numeroprocessosei, blocoassinatura) values ("
				+  "'" + txtNome.getText().trim() + "', "
				+  "'" + txtCargo.getText().trim() + "', "
				+  "'" + txtSetor.getText().trim() + "', "
				+  (chkSuperior.isSelected() ? "true" : "false") + ", "
				+  (chkAtivo.isSelected() ? "true" : "false") + ", "
				+  "'" + txtNumeroProcesso.getText() + "', "
				+  "'" + txtBlocoAssinatura.getText() + "') ";
		}
		MyUtils.execute(conexao, sql);
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "";
		sql += "delete from assinante where assinanteid = " + id;
		MyUtils.execute(conexao, sql);
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
			txtNumeroProcesso.setText(entidade.getNumeroProcesso());
			txtBlocoAssinatura.setText(entidade.getBlocoAssinatura());
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter informações do Assinante para edição: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, "select assinanteid, nome, cargo, setor, case when superior then 'Sim' else 'Não' end as superior, case when ativo then 'Sim' else 'Não' end as ativo, numeroprocessosei, blocoassinatura from assinante");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
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
		}
		return this.colunas;
	}
}
