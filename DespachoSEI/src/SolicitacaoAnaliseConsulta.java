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
	private MyComboBox cbbFiltroAssinante = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroAssinante = new MyLabel("Assinante") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroSituacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroSituacao = new MyLabel("Situação") {{ setExclusao(true); }};
	private MyComboBox cbbFiltroPendencias = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblFiltroPendencias = new MyLabel("Pendências") {{ setExclusao(true); }};
	private MyComboBox cbbOrdenacao = new MyComboBox() {{ setExclusao(true); }};
	private MyLabel lblOrdenacao = new MyLabel("Ordenar por") {{ setExclusao(true); }};
	private JPanel pnlFiltros = new JPanel(new GridLayout(5, 5));

	public SolicitacaoAnaliseConsulta(String tituloJanela, Connection conexao, JDesktopPane desktop) {
		super(tituloJanela);
		this.conexao = conexao;
		this.desktop = desktop;
		
		this.desktop.add(this);

		despachoServico = new DespachoServico(conexao);

		despachoServico.preencherOpcoesOrigem(cbbFiltroOrigem, new ArrayList<Origem>() {{ add(new Origem(0, "(Todas)", null)); }});
		despachoServico.preencherOpcoesMunicipio(cbbFiltroMunicipio, new ArrayList<Municipio>() {{ add(new Municipio(0, "(Todos)", null, null, null)); }});
		despachoServico.preencherOpcoesAssinante(cbbFiltroAssinante, new ArrayList<Assinante>() {{ add(new Assinante(0, "(Todos)")); }}, false, null);

		opcoesOrdenacao();
		opcoesFiltroSituacao();
		opcoesFiltroPendencias();
		
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

		pnlFiltros.add(lblFiltroSituacao);
		pnlFiltros.add(cbbFiltroSituacao);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroPendencias);
		pnlFiltros.add(cbbFiltroPendencias);

		pnlFiltros.add(lblFiltroNumeroProcessoSEI);
		pnlFiltros.add(txtFiltroNumeroProcessoSEI);
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblFiltroAssinante);
		pnlFiltros.add(cbbFiltroAssinante);

		pnlFiltros.add(new JPanel());
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(new JPanel());
		pnlFiltros.add(lblOrdenacao);
		pnlFiltros.add(cbbOrdenacao);

		this.setPnlFiltros(pnlFiltros);

		this.inicializar(false);
	}

	@Override
	public void incluirRegistro() {
		Solicitacao entidade = new Solicitacao();
		SolicitacaoAnaliseCadastro janela = new SolicitacaoAnaliseCadastro(conexao, despachoServico, entidade, this);
		this.desktop.add(janela);
		janela.abrirJanela();
	}
	
	private void opcoesOrdenacao() {
		cbbOrdenacao.setModel(new MyComboBoxModel());
		cbbOrdenacao.addItem(new ComboBoxItem(null, "numeroprocesso", "Número do Processo"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "autor collate nocase", "Autor"));
		cbbOrdenacao.addItem(new ComboBoxItem(null, "municipio collate nocase ", "Município"));
	}
	
	private void opcoesFiltroSituacao() {
		cbbFiltroSituacao.setModel(new MyComboBoxModel());
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "(Todas)", "(Todas)"));
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "Indeterminado", "Indeterminado"));
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "Pendente de Análise", "Pendente de Análise"));
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "Em Análise", "Em Análise"));
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "Aguardando Geração da Resposta", "Aguardando Geração da Resposta"));
		cbbFiltroSituacao.addItem(new ComboBoxItem(null, "Respondido", "Respondido"));
	}
	
	private void opcoesFiltroPendencias() {
		cbbFiltroPendencias.setModel(new MyComboBoxModel());
		cbbFiltroPendencias.addItem(new ComboBoxItem(0, null, "(Todos)"));
		cbbFiltroPendencias.addItem(new ComboBoxItem(1, null, "Com pendências"));
		cbbFiltroPendencias.addItem(new ComboBoxItem(2, null, "Sem pendências"));
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
			Solicitacao entidade = MyUtils.entidade(despachoServico.obterSolicitacao(solicitacaoId, null, null, null, null));

			SolicitacaoAnaliseCadastro janela = new SolicitacaoAnaliseCadastro(conexao, despachoServico, entidade, this);
			this.desktop.add(janela);
			janela.abrirJanela();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao obter os dados da solicitação: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public TableModel obterDados() throws Exception {
		StringBuilder sql = new StringBuilder("");
		sql.append("select * from (");
		sql.append("select s.solicitacaoid ");
		sql.append("	  , o.descricao as origem ");
		sql.append("	  , tp.descricao as tipoprocesso ");
		sql.append("	  , s.numeroprocesso ");
		sql.append("	  , s.chavebusca ");
		sql.append("	  , s.autor ");
		sql.append("	  , case when datahoramovimentacao is null then 'Indeterminado' ");
		sql.append("         	 when datahoramovimentacao is not null and (datahoraresposta is null or datahoraresposta < datahoramovimentacao) then 'Pendente de Análise' ");
		sql.append("         	 when datahoramovimentacao is not null and datahoraresposta = '9999-12-31' and (select tiporespostaid from solicitacaoresposta sr2 where sr2.solicitacaoid = s.solicitacaoid and coalesce(sr2.datahoraresposta, '9999-12-31') = ur.datahoraresposta) is null then 'Em Análise' ");
		sql.append("         	 when datahoramovimentacao is not null and datahoraresposta = '9999-12-31' and (select tiporespostaid from solicitacaoresposta sr2 where sr2.solicitacaoid = s.solicitacaoid and coalesce(sr2.datahoraresposta, '9999-12-31') = ur.datahoraresposta) is not null then 'Aguardando Geração da Resposta' ");
		sql.append("         	 when datahoramovimentacao is not null and datahoraresposta <> '9999-12-31' and datahoraresposta > datahoramovimentacao then 'Respondido' ");
		sql.append("    	end as situacao ");
		sql.append("	  , (select a.nome from solicitacaoresposta sr2 inner join assinante a using (assinanteid) where sr2.solicitacaoid = s.solicitacaoid and coalesce(sr2.datahoraresposta, '9999-12-31') = ur.datahoraresposta) as assinante ");
		sql.append("	  , m.nome as municipio ");
		sql.append("	  , coalesce((select tr.descricao from municipiotiporesposta mtr inner join tiporesposta tr using (tiporespostaid) where mtr.municipioid = s.municipioid and mtr.origemid = s.origemid limit 1), tr.descricao) as tiporespostapadrao ");
		sql.append("	  , d.descricao as destino ");
		sql.append("	  , s.cartorio ");
		sql.append("	  , ti.descricao as tipoimovel ");
		sql.append("	  , s.endereco ");
		sql.append("	  , s.coordenada ");
		sql.append("	  , s.area ");
		sql.append("	  , s.numeroprocessosei ");
		sql.append("	  , (select group_concat(pendencia, ', ') from ");
		sql.append("	    	       (select 'Autor não informado' as pendencia ");
		sql.append("	    	          from solicitacao s2 ");
		sql.append("	    	         where s2.solicitacaoid = s.solicitacaoid ");
		sql.append("	    	           and coalesce(autor, '') = '' ");
		sql.append("	    	         union ");
		sql.append("	    	        select 'Município não informado' as pendencia ");
		sql.append("	    	          from solicitacao s2 ");
		sql.append("	    	         where s2.solicitacaoid = s.solicitacaoid ");
		sql.append("	    	           and coalesce(municipioid, 0) = 0 ");
		sql.append("	    	         union ");
		sql.append("	    	        select 'Destino não informado' as pendencia ");
		sql.append("	    	          from solicitacao s2 ");
		sql.append("	    	         where s2.solicitacaoid = s.solicitacaoid ");
		sql.append("	    	           and coalesce(destinoid, 0) = 0 ");
		sql.append("	    	         union ");
		sql.append("	    	        select 'Cartório não informado' as pendencia ");
		sql.append("	    	          from solicitacao s2 ");
		sql.append("	    	         inner join destino d2 using (destinoid) ");
		sql.append("	    	         where s2.solicitacaoid = s.solicitacaoid ");
		sql.append("	    	           and usarcartorio ");
		sql.append("	    	           and coalesce(cartorio, '') = '' ");
		sql.append("	    	         union ");
		sql.append("	    	        select 'Tipo de Imóvel não informado' as pendencia ");
		sql.append("	    	          from solicitacao s2 ");
		sql.append("	    	         where s2.solicitacaoid = s.solicitacaoid ");
		sql.append("	    	           and coalesce(tipoimovelid, 0) = 0) as pendencias) as pendencias ");
		sql.append("	  , case when s.arquivosanexados then 'Sim' else 'Não' end as arquivosanexados ");
		sql.append("  from solicitacao s ");
		sql.append(" inner join origem o using (origemid) ");
		sql.append(" inner join tipoprocesso tp using (tipoprocessoid) ");
		sql.append("  left join municipio m using (municipioid) ");
		sql.append("  left join tiporesposta tr using (tiporespostaid) ");
		sql.append("  left join destino d using (destinoid) ");
		sql.append("  left join tipoimovel ti using (tipoimovelid) ");
		sql.append("  left join (select solicitacaoid, max(datahoramovimentacao) as datahoramovimentacao from solicitacaoenvio se group by 1) as ue using (solicitacaoid) ");
		sql.append("  left join (select solicitacaoid, max(coalesce(datahoraresposta, '9999-12-31')) as datahoraresposta from solicitacaoresposta sr group by 1) as ur using (solicitacaoid) ");
		sql.append(" where 1 = 1");

		if (!MyUtils.idItemSelecionado(cbbFiltroOrigem).equals(0)) {
			sql.append(" and s.origemid = " + MyUtils.idItemSelecionado(cbbFiltroOrigem));
		}

		if (!txtFiltroNumeroProcesso.getText().trim().equals("")) {
			sql.append(" and s.numeroprocesso like '%" + txtFiltroNumeroProcesso.getText().replaceAll("\\D+", "") + "%'");
		}

		if (!MyUtils.idItemSelecionado(cbbFiltroMunicipio).equals(0)) {
			sql.append(" and s.municipioid = " + MyUtils.idItemSelecionado(cbbFiltroMunicipio));
		}

		if (!txtFiltroAutor.getText().trim().equals("")) {
			sql.append(" and s.autor like '%" + txtFiltroAutor.getText() + "%'");
		}

		if (!txtFiltroNumeroProcessoSEI.getText().trim().equals("")) {
			sql.append(" and replace(replace(replace(s.numeroprocessosei, '.', ''), '-', ''), '/', '') like '%" + txtFiltroNumeroProcessoSEI.getText().replaceAll("\\D+", "") + "%'");
		}

		sql.append(") as t where 1 = 1 "); 
		
		if (cbbFiltroSituacao.getSelectedIndex() != 0) {
			sql.append(" and situacao = '" + MyUtils.idStringItemSelecionado(cbbFiltroSituacao) + "' ");
		}
		
		if (cbbFiltroPendencias.getSelectedIndex() > 0) {
			sql.append(" and pendencias is " + (MyUtils.idItemSelecionado(cbbFiltroPendencias).equals(1) ? "not" : "") + " null ");
		}

		if (cbbFiltroAssinante.getSelectedIndex() > 0) {
			sql.append(" and assinante = '" + cbbFiltroAssinante.getSelectedItem().toString() + "'");
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
			colunas.add(new MyTableColumn("Chave de Busca", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Autor", 250));
			colunas.add(new MyTableColumn("Situação", 150));
			colunas.add(new MyTableColumn("Assinante", 200));
			colunas.add(new MyTableColumn("Município", 200));
			colunas.add(new MyTableColumn("Tipo de Resposta Padrão", 150));
			colunas.add(new MyTableColumn("Destino", 200));
			colunas.add(new MyTableColumn("Cartório", 150));
			colunas.add(new MyTableColumn("Tipo Imóvel", 60, JLabel.CENTER));
			colunas.add(new MyTableColumn("Endereço", 200));
			colunas.add(new MyTableColumn("Coordenada", 80));
			colunas.add(new MyTableColumn("Área", 80));
			colunas.add(new MyTableColumn("Nº Processo SEI", 150, JLabel.CENTER));
			colunas.add(new MyTableColumn("Pendências", 200));
			colunas.add(new MyTableColumn("Arq. Anex.?", 60, JLabel.CENTER));
		}

		return this.colunas;
	}
}
