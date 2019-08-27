import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import framework.CadastroTemplate;
import framework.ComboBoxItem;
import framework.MyComboBox;
import framework.MyComboBoxModel;
import framework.MyLabel;
import framework.MyTableColumn;
import framework.MyTableModel;
import framework.MyTextField;
import framework.MyUtils;

@SuppressWarnings("serial")
public class SolicitacaoAnaliseConsulta extends CadastroTemplate {

	private Connection conexao;
	private List<MyTableColumn> colunas;
	private DespachoServico despachoServico;
	private JDesktopPane desktop;

	private MyComboBox cbbFiltroOrigem = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroOrigem = new MyLabel("Origem") {{ setExclusao(true); }};
	private MyTextField txtFiltroNumeroProcesso = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroProcesso = new MyLabel("Número do Processo") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroMunicipio = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroMunicipio = new MyLabel("Município") {{ setExclusao(true); }};
	private MyTextField txtFiltroAutor = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroAutor = new MyLabel("Autor") {{ setExclusao(true); }};
	private MyTextField txtFiltroNumeroProcessoSEI = new MyTextField() {{ setExclusao(true); }};
	private MyLabel lblFiltroNumeroProcessoSEI = new MyLabel("Nº Processo SEI") {{ setExclusao(true); }};
	private MyComboBox cbbOrdenacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblOrdenacao = new MyLabel("Ordenar por") {{ setExclusao(true); }};
	private JPanel pnlFiltros = new JPanel(new GridLayout(3, 5));

	public SolicitacaoAnaliseConsulta(String tituloJanela, Connection conexao, JDesktopPane desktop) {
		super(tituloJanela);
		this.conexao = conexao;
		this.desktop = desktop;
		
		this.desktop.add(this);

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesOrigem(cbbFiltroOrigem, new ArrayList<Origem>() {{ add(new Origem(0, "(Todas)")); }});
		despachoServico.preencherOpcoesMunicipio(cbbFiltroMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Todos)", null, null, null)); }});

		opcoesOrdenacao();
		
		pnlFiltros.add(lblFiltroOrigem);
		pnlFiltros.add(cbbFiltroOrigem);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroNumeroProcesso);
		pnlFiltros.add(txtFiltroNumeroProcesso);

		pnlFiltros.add(lblFiltroMunicipio);
		pnlFiltros.add(cbbFiltroMunicipio);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroAutor);
		pnlFiltros.add(txtFiltroAutor);

		pnlFiltros.add(lblFiltroNumeroProcessoSEI);
		pnlFiltros.add(txtFiltroNumeroProcessoSEI);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblOrdenacao);
		pnlFiltros.add(cbbOrdenacao);

		this.setPnlFiltros(pnlFiltros);

		this.inicializar(false);
	}

	private void opcoesOrdenacao() {
		cbbOrdenacao.setModel(new MyComboBoxModel());
		cbbOrdenacao.addItem(new ComboBoxItem(null, "numeroprocesso", "Número do Processo"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "autor", "Autor"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "municipio", "Município"));
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
			StringBuilder titulo = new StringBuilder("Solicitação de Análise - Nº Processo: ").append(entidade.getNumeroProcesso()).append(" - Autor: ").append(entidade.getAutor()).append(" - Município: ").append(entidade.getMunicipio() == null ? "(Não identificado ainda)" : entidade.getMunicipio().getNome());

			SolicitacaoAnaliseCadastro janela = new SolicitacaoAnaliseCadastro(titulo.toString(), conexao, despachoServico, entidade, this);
			this.desktop.add(janela);
			janela.abrirJanela();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter os dados da solicitação: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select s.solicitacaoid ");
		sql.append("	  , o.descricao as origem ");
		sql.append("	  , tp.descricao as tipoprocesso ");
		sql.append("	  , s.numeroprocesso ");
		sql.append("	  , s.autor ");
		sql.append("	  , m.nome as municipio ");
		sql.append("	  , d.descricao as destino ");
		sql.append("	  , s.cartorio ");
		sql.append("	  , ti.descricao as tipoimovel ");
		sql.append("	  , s.endereco ");
		sql.append("	  , s.coordenada ");
		sql.append("	  , s.area ");
		sql.append("	  , s.numeroprocessosei ");
		sql.append("	  , case when s.arquivosanexados then 'Sim' else 'Não' end as arquivosanexados ");
		sql.append("  from solicitacao s ");
		sql.append(" inner join origem o using (origemid) ");
		sql.append(" inner join tipoprocesso tp using (tipoprocessoid) ");
		sql.append("  left join municipio m using (municipioid) ");
		sql.append("  left join destino d using (destinoid) ");
		sql.append("  left join tipoimovel ti using (tipoimovelid) ");
		sql.append(" where 1 = 1");

		if (!MyUtils.idItemSelecionado(cbbFiltroOrigem).equals(0)) {
			sql.append(" and origemid = " + MyUtils.idItemSelecionado(cbbFiltroOrigem));
		}

		if (!txtFiltroNumeroProcesso.getText().trim().equals("")) {
			sql.append(" and numeroprocesso like '%" + txtFiltroNumeroProcesso.getText() + "%'");
		}

		if (!MyUtils.idItemSelecionado(cbbFiltroMunicipio).equals(0)) {
			sql.append(" and municipioid = " + MyUtils.idItemSelecionado(cbbFiltroMunicipio));
		}

		if (!txtFiltroAutor.getText().trim().equals("")) {
			sql.append(" and autor like '%" + txtFiltroAutor.getText() + "%'");
		}

		if (!txtFiltroNumeroProcessoSEI.getText().trim().equals("")) {
			sql.append(" and numeroprocessosei like '%" + txtFiltroNumeroProcessoSEI.getText() + "%'");
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
