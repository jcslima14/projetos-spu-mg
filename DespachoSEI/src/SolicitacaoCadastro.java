import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyUtils;

@SuppressWarnings("serial")
public class SolicitacaoCadastro extends CadastroTemplate {

	private Connection conexao;
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;
	private JDesktopPane desktop;

	public SolicitacaoCadastro(String tituloJanela, Connection conexao, JDesktopPane desktop) {
		super(tituloJanela);
		this.conexao = conexao;
		this.desktop = desktop;
		
		this.desktop.add(this);

		despachoServico = new DespachoServico(conexao);
		this.inicializar(false);
	}

	public void limparCamposEditaveis() {
	}

	public void salvarRegistro() throws Exception {
	}

	public void excluirRegistro(Integer id) throws Exception {
		String sql = "delete from solicitacaoresposta where solicitacaoid = " + id;
		MyUtils.execute(conexao, sql);

		sql = "delete from solicitacaoenvio where solicitacaoid = " + id;
		MyUtils.execute(conexao, sql);

		sql = "delete from solicitacao where solicitacaoid = " + id;
		MyUtils.execute(conexao, sql);
	}

	public void prepararParaEdicao() {
		Integer solicitacaoId = Integer.parseInt(this.getTabela().getValueAt(this.getTabela().getSelectedRow(), 1).toString());
		try {
			Solicitacao entidade = MyUtils.entidade(despachoServico.obterSolicitacao(solicitacaoId, null, null, null));
			SolicitacaoCompletaCadastro janela = new SolicitacaoCompletaCadastro("Dados da Solicitação", despachoServico, entidade);
			this.desktop.add(janela);
			janela.abrirJanela();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter os dados da solicitação: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		ResultSet rs = MyUtils.executeQuery(conexao, 
										"select s.solicitacaoid "
									  + "	  , o.descricao as origem "
									  + "	  , tp.descricao as tipoprocesso "
									  + "	  , s.numeroprocesso "
									  + "	  , s.autor "
									  + "	  , m.nome as municipio "
									  + "	  , d.descricao as destino "
									  + "	  , s.cartorio "
									  + "	  , ti.descricao as tipoimovel "
									  + "	  , s.endereco "
									  + "	  , s.coordenada "
									  + "	  , s.area "
									  + "	  , s.numeroprocessosei "
									  + "	  , case when s.arquivosanexados then 'Sim' else 'Não' end as arquivosanexados "
									  + "  from solicitacao s "
									  + " inner join origem o using (origemid) "
									  + " inner join tipoprocesso tp using (tipoprocessoid) "
									  + "  left join municipio m using (municipioid) "
									  + "  left join destino d using (destinoid) "
									  + "  left join tipoimovel ti using (tipoimovelid) ");
		TableModel tm = new MyTableModel(MyUtils.obterTitulosColunas(getColunas()), MyUtils.obterDados(rs));
		return tm;
	}

	@Override
	public List<MyTableColumn> getColunas() {
		if (this.colunas == null) {
			colunas = new ArrayList<MyTableColumn>();
			colunas.add(new MyTableColumn("", 16, false) {{ setRenderCheckbox(true); }});
			colunas.add(new MyTableColumn("Id", 40, JLabel.RIGHT));
			colunas.add(new MyTableColumn("Origem", 75, JLabel.CENTER));
			colunas.add(new MyTableColumn("Tipo Processo", 75, JLabel.CENTER));
			colunas.add(new MyTableColumn("Nº Processo", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Autor", 250));
			colunas.add(new MyTableColumn("Município", 200));
			colunas.add(new MyTableColumn("Destino", 200));
			colunas.add(new MyTableColumn("Cartório", 150));
			colunas.add(new MyTableColumn("Tipo Imóvel", 60, JLabel.CENTER));
			colunas.add(new MyTableColumn("Endereço", 200));
			colunas.add(new MyTableColumn("Coordenada", 80));
			colunas.add(new MyTableColumn("Área", 80));
			colunas.add(new MyTableColumn("Nº Processo SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Arq. Anex.?", 60, JLabel.CENTER));
		}
		return this.colunas;
	}
}
