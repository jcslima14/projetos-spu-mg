import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JOptionPane;

import framework.MyComboBox;
import framework.MyUtils;

public class DespachoServico {
	private Connection conexao;
	
	public DespachoServico(Connection conexao) {
		this.conexao = conexao;
	}

	public List<Solicitacao> obterSolicitacao(Integer solicitacaoId, Origem origem, TipoProcesso tipoProcesso, String numeroProcesso) throws Exception {
		List<Solicitacao> retorno = new ArrayList<Solicitacao>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from solicitacao where 1 = 1");
		if (solicitacaoId != null) {
			sql.append(" and solicitacaoid = " + solicitacaoId);
		} else {
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and origemid = " + origem.getOrigemId());
			}
			if (tipoProcesso != null && tipoProcesso.getTipoProcessoId() != null) {
				sql.append(" and tipoprocessoid = " + tipoProcesso.getTipoProcessoId());
			}
			if (numeroProcesso != null) {
				sql.append(" and numeroprocesso = '" + numeroProcesso + "'");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Solicitacao(rs.getInt("solicitacaoid"), 
					MyUtils.entidade(obterOrigem(rs.getInt("origemid"), null)),
					MyUtils.entidade(obterTipoProcesso(rs.getInt("tipoprocessoid"), null)),
					rs.getString("numeroprocesso"), 
					rs.getString("autor"),
					MyUtils.entidade(obterMunicipio(true, rs.getInt("municipioid"), null)),
					MyUtils.entidade(obterDestino(rs.getInt("destinoid"), null, null, null, null, null)),
					rs.getString("cartorio"),
					MyUtils.entidade(obterTipoImovel(rs.getInt("tipoimovelid"), null)),
					rs.getString("endereco"),
					rs.getString("coordenada"),
					rs.getString("area"),
					rs.getString("numeroprocessosei"),
					rs.getBoolean("arquivosanexados")
					));
		}

		return retorno;
	}

	public List<SolicitacaoEnvio> obterSolicitacaoEnvio(Integer solicitacaoEnvioId, Solicitacao solicitacao, Origem origem, String numeroProcesso, String dataHoraMovimentacao, Boolean arquivosProcessados, boolean somenteMunicipiosPreenchidos) throws Exception {
		List<SolicitacaoEnvio> retorno = new ArrayList<SolicitacaoEnvio>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select se.* from solicitacaoenvio se inner join solicitacao s using (solicitacaoid) where 1 = 1");
		if (solicitacaoEnvioId != null) {
			sql.append(" and se.solicitacaoenvioid = " + solicitacaoEnvioId);
		} else {
			if (solicitacao != null && solicitacao.getSolicitacaoId() != null) {
				sql.append(" and s.solicitacaoid = " + solicitacao.getSolicitacaoId());
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and s.origemid = " + origem.getOrigemId());
			}
			if (numeroProcesso != null) {
				sql.append(" and s.numeroprocesso = '" + numeroProcesso + "'");
			}
			if (dataHoraMovimentacao != null) {
				sql.append(" and se.datahoramovimentacao = '" + dataHoraMovimentacao + "'");
			}
			if (arquivosProcessados != null) {
				sql.append(" and se.arquivosprocessados = " + (arquivosProcessados ? "true" : "false"));
			}
			if (somenteMunicipiosPreenchidos) {
				sql.append(" and s.municipioid is not null");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new SolicitacaoEnvio(rs.getInt("solicitacaoenvioid"), 
					MyUtils.entidade(obterSolicitacao(rs.getInt("solicitacaoid"), null, null, null)),
					rs.getString("datahoramovimentacao"), 
					rs.getString("resultadodownload"),
					rs.getBoolean("arquivosprocessados"),
					rs.getString("resultadoprocessamento")
					));
		}

		return retorno;
	}

	public List<SolicitacaoResposta> obterRespostasAGerar() throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, null, null, null, null, null, null, null);
		Iterator<SolicitacaoResposta> i = respostas.iterator();
		while (i.hasNext()) {
			// se o nº de documento do SEI estiver preenchido, não retorna a resposta
			SolicitacaoResposta resposta = i.next();
			if (!MyUtils.emptyStringIfNull(resposta.getNumeroDocumentoSEI()).equals("")) {
				i.remove();
			}
		}
		return  respostas;
	}

	public List<SolicitacaoResposta> obterRespostasAImprimir(Boolean respostaImpressa, Boolean respostaNoBlocoAssinatura, Assinante assinante, Boolean tipoRespostaImprimirResposta) throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, null, null, null, respostaImpressa, respostaNoBlocoAssinatura, assinante, tipoRespostaImprimirResposta);
		Iterator<SolicitacaoResposta> i = respostas.iterator();
		while (i.hasNext()) {
			// se o nº de documento do SEI estiver vazio, não retorna a resposta
			SolicitacaoResposta resposta = i.next();
			if (MyUtils.emptyStringIfNull(resposta.getNumeroDocumentoSEI()).equals("")) {
				i.remove();
			}
		}
		return  respostas;
	}

	public List<SolicitacaoResposta> obterSolicitacaoResposta(Integer solicitacaoRespostaId) throws Exception {
		return obterSolicitacaoResposta(solicitacaoRespostaId, null, null, null, null, null, null, null);
	}
	
	public List<SolicitacaoResposta> obterSolicitacaoResposta(Integer solicitacaoRespostaId, Solicitacao solicitacao, Origem origem, String numeroProcesso, Boolean respostaImpressa, Boolean respostaNoBlocoAssinatura, Assinante assinante, 
			Boolean tipoRespostaImprimirResposta) throws Exception {
		List<SolicitacaoResposta> retorno = new ArrayList<SolicitacaoResposta>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select sr.* from solicitacaoresposta sr ");
		sql.append(" inner join solicitacao s using (solicitacaoid) ");
		sql.append("  left join tiporesposta tr using (tiporespostaid) ");
		sql.append(" where 1 = 1");
		if (solicitacaoRespostaId != null) {
			sql.append(" and sr.solicitacaorespostaid = " + solicitacaoRespostaId);
		} else {
			if (solicitacao != null && solicitacao.getSolicitacaoId() != null) {
				sql.append(" and s.solicitacaoid = " + solicitacao.getSolicitacaoId());
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and s.origemid = " + origem.getOrigemId());
			}
			if (numeroProcesso != null) {
				sql.append(" and s.numeroprocesso = '" + numeroProcesso + "' ");
			}
			if (respostaImpressa != null) {
				sql.append(" and sr.respostaimpressa = " + (respostaImpressa ? "true" : "false"));
			}
			if (respostaNoBlocoAssinatura != null) {
				sql.append(" and sr.respostanoblocoassinatura = " + (respostaNoBlocoAssinatura ? "true" : "false"));
			}
			if (assinante != null && assinante.getAssinanteId() != null) {
				sql.append(" and sr.assinanteid = " + assinante.getAssinanteId());
			}
			if (tipoRespostaImprimirResposta != null) {
				sql.append(" and tr.imprimirresposta = " + (tipoRespostaImprimirResposta ? "true" : "false"));
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new SolicitacaoResposta(rs.getInt("solicitacaorespostaid"), 
					MyUtils.entidade(obterSolicitacao(rs.getInt("solicitacaoid"), null, null, null)),
					MyUtils.entidade(obterTipoResposta(rs.getInt("tiporespostaid"), null)),
					rs.getString("observacao"), 
					MyUtils.entidade(obterAssinante(rs.getInt("assinanteid"), null, null, null)),
					MyUtils.entidade(obterAssinante(rs.getInt("assinanteidsuperior"), null, null, null)),
					rs.getString("numerodocumentosei"),
					rs.getString("datahoraresposta"),
					rs.getString("numeroprocessosei"),
					rs.getBoolean("respostaimpressa"),
					rs.getString("datahoraimpressao"),
					rs.getString("blocoassinatura"),
					rs.getBoolean("respostanoblocoassinatura")
					));
		}

		return retorno;
	}

	public List<Origem> obterOrigem(Integer origemId, String descricao) throws Exception {
		List<Origem> retorno = new ArrayList<Origem>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from origem where 1 = 1");
		if (origemId != null) {
			sql.append(" and origemid = " + origemId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Origem(rs.getInt("origemid"), rs.getString("descricao")));
		}
		
		return retorno;
	}

	public List<Parametro> obterParametro(Integer parametroId, String descricao) throws Exception {
		List<Parametro> retorno = new ArrayList<Parametro>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from parametro where 1 = 1");
		if (parametroId != null) {
			sql.append(" and parametroid = " + parametroId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Parametro(rs.getInt("parametroid"), rs.getString("descricao"), rs.getString("conteudo"), rs.getBoolean("ativo")));
		}
		
		return retorno;
	}

	public String obterConteudoParametro(int parametroId) {
		return obterConteudoParametro(parametroId, null);
	}

	public String obterConteudoParametro(int parametroId, String valorDefault) {
		List<Parametro> parametros = null;

		try {
			parametros = obterParametro(parametroId, null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		if (parametros == null || parametros.isEmpty()) {
			return valorDefault;
		} else {
			return MyUtils.entidade(parametros).getConteudo();
		}
	}

	public void salvarConteudoParametro(int parametroId, String conteudo) {
		StringBuilder sql = new StringBuilder("");
		sql.append("insert into parametro (parametroid, descricao, conteudo) ");
		sql.append("select " + parametroId);
		sql.append("	 , '" + Parametro.obterDescricao(parametroId) + "'");
		sql.append("	 , '" + conteudo.replace("'", "''") + "'");
		sql.append(" where not exists (select 1 from parametro where parametroid = " + parametroId + ")");

		try {
			MyUtils.execute(conexao, sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		sql = new StringBuilder("");
		sql.append("update parametro ");
		sql.append("   set descricao = '" + Parametro.obterDescricao(parametroId) + "'");
		sql.append("	 , conteudo = '" + conteudo.replace("'", "''") + "'");
		sql.append(" where parametroid = " + parametroId);

		try {
			MyUtils.execute(conexao, sql.toString());
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public List<AssinanteTipoResposta> obterAssinanteTipoResposta(Integer assinanteTipoRespostaId, Integer assinanteId, Integer tipoRespostaId) throws Exception {
		List<AssinanteTipoResposta> retorno = new ArrayList<AssinanteTipoResposta>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from assinantetiporesposta where 1 = 1");
		if (assinanteTipoRespostaId != null) {
			sql.append(" and assinantetiporespostaid = " + assinanteTipoRespostaId);
		}
		if (assinanteId != null) {
			sql.append(" and assinanteid = " + assinanteId);
		}
		if (tipoRespostaId != null) {
			sql.append(" and tiporespostaid = " + tipoRespostaId);
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			Assinante assinante = MyUtils.entidade(obterAssinante(rs.getInt("assinanteid"), null, null, null));
			TipoResposta tipoResposta = MyUtils.entidade(obterTipoResposta(rs.getInt("tiporespostaid"), null));
			retorno.add(new AssinanteTipoResposta(rs.getInt("assinantetiporespostaid"), assinante, tipoResposta, rs.getString("blocoassinatura")));
		}

		return retorno;
	}

	public List<TipoProcesso> obterTipoProcesso(Integer tipoProcessoId, String descricao) throws Exception {
		List<TipoProcesso> retorno = new ArrayList<TipoProcesso>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from tipoprocesso where 1 = 1");
		if (tipoProcessoId != null) {
			sql.append(" and tipoprocessoid = " + tipoProcessoId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		sql.append(" order by descricao collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new TipoProcesso(rs.getInt("tipoprocessoid"), rs.getString("descricao")));
		}
		
		return retorno;
	}

	public List<TipoImovel> obterTipoImovel(Integer tipoImovelId, String descricao) throws Exception {
		List<TipoImovel> retorno = new ArrayList<TipoImovel>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from tipoimovel where 1= 1");
		if (tipoImovelId != null) {
			sql.append(" and tipoimovelid = " + tipoImovelId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		sql.append(" order by descricao collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new TipoImovel(rs.getInt("tipoimovelid"), rs.getString("descricao")));
		}
		
		return retorno;
	}

	public List<TipoResposta> obterTipoResposta(Integer tipoRespostaId, String descricao) throws Exception {
		return obterTipoResposta(tipoRespostaId, descricao, null, false);
	}

	public List<TipoResposta> obterTipoResposta(Integer tipoRespostaId, String descricao, Origem origem, boolean incluirSemOrigem) throws Exception {
		List<TipoResposta> retorno = new ArrayList<TipoResposta>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from tiporesposta where 1 = 1");
		if (tipoRespostaId != null) {
			sql.append(" and tiporespostaid = " + tipoRespostaId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and (origemid = " + origem.getOrigemId());
				if (incluirSemOrigem) sql.append(" or origemid is null");
				sql.append(")");
			}
		}

		sql.append(" order by descricao collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new TipoResposta(rs.getInt("tiporespostaid"),
					rs.getString("descricao"), 
					rs.getString("tipodocumento"), 
					rs.getString("numerodocumentomodelo"), 
					rs.getBoolean("gerarprocessoindividual"), 
					rs.getString("unidadeaberturaprocesso"), 
					rs.getString("tipoprocesso"), 
					rs.getBoolean("imprimirresposta"), 
					rs.getInt("quantidadeassinaturas"),
					MyUtils.entidade(obterOrigem(rs.getInt("origemid"), null))));
		}
		
		return retorno;
	}

	public List<Municipio> obterMunicipio(boolean obterComarca, Integer municipioId, String nome) throws Exception {
		List<Municipio> retorno = new ArrayList<Municipio>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from municipio where 1 = 1");
		if (municipioId != null) {
			sql.append(" and municipioid = " + municipioId);
		} else {
			if (nome != null) {
				sql.append(" and nome like '" + nome.replace("'", "''") + "'");
			}
		}

		sql.append(" order by nome collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			Municipio comarca = null;
			if (obterComarca) {
				comarca = MyUtils.entidade(obterMunicipio(false, rs.getInt("municipioidcomarca"), null));
			}

			retorno.add(new Municipio(rs.getInt("municipioid"),
									  rs.getString("nome"),
									  comarca,
									  MyUtils.entidade(obterDestino(rs.getInt("destinoid"), null, null, null, null, null)),
									  MyUtils.entidade(obterTipoResposta(rs.getInt("tiporespostaid"), null))
					));
		}
		
		return retorno;
	}

	public List<MunicipioTipoResposta> obterMunicipioTipoResposta(Integer municipioTipoRespostaId, Municipio municipio, Origem origem, TipoResposta tipoResposta) throws Exception {
		List<MunicipioTipoResposta> retorno = new ArrayList<MunicipioTipoResposta>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select mtr.* from municipiotiporesposta mtr ");
		sql.append(" inner join municipio m using (municipioid) ");
		sql.append(" inner join origem o using (origemid) ");
		sql.append(" inner join tiporesposta tr using (tiporespostaid) ");
		sql.append(" where 1 = 1 ");
		if (municipioTipoRespostaId != null) {
			sql.append(" and municipiotiporespostaid = " + municipioTipoRespostaId);
		} else {
			if (municipio != null && municipio.getMunicipioId() != null) {
				sql.append(" and mtr.municipioid = " + municipio.getMunicipioId());
			}
			if (origem != null && origem.getOrigemId() != null) {
				sql.append(" and mtr.origemid = " + origem.getOrigemId());
			}
			if (tipoResposta != null && tipoResposta.getTipoRespostaId() != null) {
				sql.append(" and mtr.tiporespostaid = " + tipoResposta.getTipoRespostaId());
			}
		}

		sql.append(" order by m.nome collate nocase, o.descricao collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new MunicipioTipoResposta(rs.getInt("municipiotiporespostaid"),
										MyUtils.entidade(obterMunicipio(false, rs.getInt("municipioid"), null)),
										MyUtils.entidade(obterOrigem(rs.getInt("origemid"), null)),
										MyUtils.entidade(obterTipoResposta(rs.getInt("tiporespostaid"), null))
					));
		}
		
		return retorno;
	}

	public List<Assinante> obterAssinante(Integer assinanteId, String nome, Boolean superior, Boolean ativo) throws Exception {
		List<Assinante> retorno = new ArrayList<Assinante>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from assinante where 1 = 1");
		if (assinanteId != null) {
			sql.append(" and assinanteid = " + assinanteId);
		} else {
			if (nome != null) {
				sql.append(" and nome like '" + nome + "'");
			}
			if (superior != null) {
				sql.append(" and superior = " + (superior ? "true" : "false"));
			}
			if (ativo != null) {
				sql.append(" and ativo = " + (ativo ? "true" : "false"));
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Assinante(rs.getInt("assinanteid"), rs.getString("nome"), rs.getBoolean("ativo"), rs.getString("cargo"), rs.getString("setor"), rs.getBoolean("superior"), rs.getString("numeroprocessosei"), rs.getString("blocoassinatura")));
		}
		
		return retorno;
	}

	public List<Destino> obterDestino(Integer destinoId, String descricao, String abreviacao, Boolean usarCartorio, String municipio, String orderBy) throws Exception {
		List<Destino> retorno = new ArrayList<Destino>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from destino where 1 = 1");
		if (destinoId != null) {
			sql.append(" and destinoid = " + destinoId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}

			if (abreviacao != null) {
				sql.append(" and abreviacao like '" + abreviacao + "'");
			}

			if (usarCartorio != null) {
				sql.append(" and usarcartorio = " + usarCartorio);
			}

			if (municipio != null) {
				sql.append(" and destinoid = (select destinoid from municipio where nome = '" + municipio.replace("'", "''") + "')");
			}
		}

		if (orderBy == null) orderBy = "descricao";
		sql.append(" order by " + orderBy + " collate nocase ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Destino(rs.getInt("destinoid"), rs.getString("artigo"), rs.getString("descricao"), rs.getString("abreviacao"), rs.getBoolean("usarcartorio")));
		}
		
		return retorno;
	}

	public void preencherOpcoesTipoResposta(MyComboBox cbbTipoResposta, List<TipoResposta> opcoesIniciais, Origem origem) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoResposta>();
			opcoesIniciais.addAll(obterTipoResposta(null, null, origem, true));
			MyUtils.insereOpcoesComboBox(cbbTipoResposta, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Resposta: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesAssinante(MyComboBox cbbAssinante, List<Assinante> opcoesIniciais, Boolean superior, Boolean ativo) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Assinante>();
			opcoesIniciais.addAll(obterAssinante(null, null, superior, ativo));
			MyUtils.insereOpcoesComboBox(cbbAssinante, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Assinante: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesMunicipio(MyComboBox cbbMunicipio, List<Municipio> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Municipio>();
			opcoesIniciais.addAll(obterMunicipio(false, null, null));
			MyUtils.insereOpcoesComboBox(cbbMunicipio, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Município: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesDestino(MyComboBox cbbDestino, List<Destino> opcoesIniciais, Origem origem) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Destino>();
			opcoesIniciais.addAll(obterDestino(null, null, null, origem == null ? null : (origem.getOrigemId().equals(Origem.SAPIENS_ID) ? false : true), null, "abreviacao"));
			MyUtils.insereOpcoesComboBox(cbbDestino, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Destino: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesTipoImovel(MyComboBox cbbTipoImovel, List<TipoImovel> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoImovel>();
			opcoesIniciais.addAll(obterTipoImovel(null, null));
			MyUtils.insereOpcoesComboBox(cbbTipoImovel, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Imóvel: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesTipoProcesso(MyComboBox cbbTipoProcesso, List<TipoProcesso> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoProcesso>();
			opcoesIniciais.addAll(obterTipoProcesso(null, null));
			MyUtils.insereOpcoesComboBox(cbbTipoProcesso, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Tipo de Processo: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public void preencherOpcoesOrigem(MyComboBox cbbOrigem, List<Origem> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Origem>();
			opcoesIniciais.addAll(obterOrigem(null, null));
			MyUtils.insereOpcoesComboBox(cbbOrigem, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Origem: \n\n" + e.getMessage());
			e.printStackTrace();
		}
	}

	public Solicitacao salvarSolicitacao(Solicitacao solicitacao) throws Exception {
		StringBuilder sql = new StringBuilder("");

		if (solicitacao.getSolicitacaoId() == null || solicitacao.getSolicitacaoId().equals(0)) {
			sql.append("insert into solicitacao (origemid, tipoprocessoid, numeroprocesso, autor, municipioid, destinoid, cartorio, tipoimovelid, endereco, coordenada, area, numeroprocessosei, arquivosanexados) ");
			sql.append("select " + solicitacao.getOrigem().getOrigemId());
			sql.append("	 , " + solicitacao.getTipoProcesso().getTipoProcessoId());
			sql.append("	 , '" + solicitacao.getNumeroProcesso() + "'");
			sql.append("	 , '" + solicitacao.getAutor() + "'");
			sql.append("	 , " + (solicitacao.getMunicipio() == null ? "null" : solicitacao.getMunicipio().getMunicipioId()));
			sql.append("	 , " + (solicitacao.getDestino() == null ? "null" : solicitacao.getDestino().getDestinoId()));
			sql.append("	 , " + (solicitacao.getCartorio() == null ? "null" : "'" + solicitacao.getCartorio() + "'"));
			sql.append("	 , " + (solicitacao.getTipoImovel() == null ? "null" : solicitacao.getTipoImovel().getTipoImovelId()));
			sql.append("	 , " + (solicitacao.getEndereco() == null ? "null" : "'" + solicitacao.getEndereco() + "'"));
			sql.append("	 , " + (solicitacao.getCoordenada() == null ? "null" : "'" + solicitacao.getCoordenada() + "'"));
			sql.append("	 , " + (solicitacao.getArea() == null ? "null" : "'" + solicitacao.getArea() + "'"));
			sql.append("	 , " + (solicitacao.getNumeroProcessoSEI() == null ? "null" : "'" + solicitacao.getNumeroProcessoSEI() + "'"));
			sql.append("	 , " + (solicitacao.getArquivosAnexados() == null ? "null" : (solicitacao.getArquivosAnexados() ? "true" : "false")));
			sql.append(" where not exists (select 1 from solicitacao where origemid = " + solicitacao.getOrigem().getOrigemId() + " and numeroprocesso = '" + solicitacao.getNumeroProcesso() + "')");
		} else {
			sql.append("update solicitacao ");
			sql.append("   set origemid = " + solicitacao.getOrigem().getOrigemId());
			sql.append("	 , tipoprocessoid = " + solicitacao.getTipoProcesso().getTipoProcessoId());
			sql.append("	 , numeroprocesso = '" + solicitacao.getNumeroProcesso() + "'");
			sql.append("	 , autor = '" + solicitacao.getAutor() + "'");
			sql.append("	 , municipioid = " + (solicitacao.getMunicipio() == null ? "null" : solicitacao.getMunicipio().getMunicipioId()));
			sql.append("	 , destinoid = " + (solicitacao.getDestino() == null ? "null" : solicitacao.getDestino().getDestinoId()));
			sql.append("	 , cartorio = " + (solicitacao.getCartorio() == null ? "null" : "'" + solicitacao.getCartorio() + "'"));
			sql.append("	 , tipoimovelid = " + (solicitacao.getTipoImovel() == null ? "null" : solicitacao.getTipoImovel().getTipoImovelId()));
			sql.append("	 , endereco = " + (solicitacao.getEndereco() == null ? "null" : "'" + solicitacao.getEndereco() + "'"));
			sql.append("	 , coordenada = " + (solicitacao.getCoordenada() == null ? "null" : "'" + solicitacao.getCoordenada() + "'"));
			sql.append("	 , area = " + (solicitacao.getArea() == null ? "null" : "'" + solicitacao.getArea() + "'"));
			sql.append("	 , numeroprocessosei = " + (solicitacao.getNumeroProcessoSEI() == null ? "null" : "'" + solicitacao.getNumeroProcessoSEI() + "'"));
			sql.append("	 , arquivosanexados = " + (solicitacao.getArquivosAnexados() == null ? "null" : (solicitacao.getArquivosAnexados() ? "true" : "false")));
			sql.append(" where solicitacaoid = " + solicitacao.getSolicitacaoId());
		}

		MyUtils.execute(conexao, sql.toString());
		
		return MyUtils.entidade(obterSolicitacao(null, solicitacao.getOrigem(), solicitacao.getTipoProcesso(), solicitacao.getNumeroProcesso()));
	}

	public SolicitacaoEnvio salvarSolicitacaoEnvio(SolicitacaoEnvio solicitacaoEnvio) throws Exception {
		StringBuilder sql = new StringBuilder("");

		if (solicitacaoEnvio.getSolictacaoEnvioId() == null || solicitacaoEnvio.getSolictacaoEnvioId().equals(0)) {
			sql.append("insert into solicitacaoenvio (solicitacaoid, datahoramovimentacao, resultadodownload, arquivosprocessados, resultadoprocessamento) ");
			sql.append("select " + solicitacaoEnvio.getSolicitacao().getSolicitacaoId());
			sql.append("     , '" + solicitacaoEnvio.getDataHoraMovimentacao() + "'");
			sql.append("	 , " + (solicitacaoEnvio.getResultadoDownload() == null ? "null" : "'" + solicitacaoEnvio.getResultadoDownload().replace("'", "") + "'"));
			sql.append("	 , " + (solicitacaoEnvio.getArquivosProcessados() == null ? "null" : (solicitacaoEnvio.getArquivosProcessados() ? "true" : "false")));
			sql.append("	 , " + (solicitacaoEnvio.getResultadoProcessamento() == null ? "null" : "'" + solicitacaoEnvio.getResultadoProcessamento().replace("'", "") + "'"));
			sql.append(" where not exists (select 1 from solicitacaoenvio where solicitacaoid = " + solicitacaoEnvio.getSolicitacao().getSolicitacaoId() + " and datahoramovimentacao = '" + solicitacaoEnvio.getDataHoraMovimentacao() + "')");
		} else {
			sql.append("update solicitacaoenvio ");
			sql.append("   set solicitacaoid = " + solicitacaoEnvio.getSolicitacao().getSolicitacaoId());
			sql.append("     , datahoramovimentacao = '" + solicitacaoEnvio.getDataHoraMovimentacao() + "'");
			sql.append("	 , resultadodownload = " + (solicitacaoEnvio.getResultadoDownload() == null ? "null" : "'" + solicitacaoEnvio.getResultadoDownload().replace("'", "") + "'"));
			sql.append("	 , arquivosprocessados = " + (solicitacaoEnvio.getArquivosProcessados() == null ? "null" : (solicitacaoEnvio.getArquivosProcessados() ? "true" : "false")));
			sql.append("	 , resultadoprocessamento = " + (solicitacaoEnvio.getResultadoProcessamento() == null ? "null" : "'" + solicitacaoEnvio.getResultadoProcessamento().replace("'", "") + "'"));
			sql.append(" where not exists (select 1 from solicitacaoenvio where solicitacaoid = " + solicitacaoEnvio.getSolicitacao().getSolicitacaoId() + " and datahoramovimentacao = '" + solicitacaoEnvio.getDataHoraMovimentacao() + "')");
		}

		MyUtils.execute(conexao, sql.toString());
		
		return MyUtils.entidade(obterSolicitacaoEnvio(null, solicitacaoEnvio.getSolicitacao(), null, null, solicitacaoEnvio.getDataHoraMovimentacao(), null, false));
	}

	public void salvarSolicitacaoResposta(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");

		if (resposta.getSolicitacaoRespostaId() == null || resposta.getSolicitacaoRespostaId().equals(0)) {
			sql.append("insert into solicitacaoresposta (solicitacaoid, tiporespostaid, observacao, assinanteid, assinanteidsuperior, numerodocumentosei, datahoraresposta, numeroprocessosei, respostaimpressa, datahoraimpressao, blocoassinatura, respostanoblocoassinatura) ");
			sql.append("select " + resposta.getSolicitacao().getSolicitacaoId());
			sql.append("     , " + (resposta.getTipoResposta() == null ? "null" : resposta.getTipoResposta().getTipoRespostaId()));
			sql.append("	 , " + (resposta.getObservacao() == null ? "null" : "'" + resposta.getObservacao().replace("'", "") + "'"));
			sql.append("	 , " + (resposta.getAssinante() == null ? "null" : resposta.getAssinante().getAssinanteId()));
			sql.append("	 , " + (resposta.getAssinanteSuperior() == null ? "null" : resposta.getAssinanteSuperior().getAssinanteId()));
			sql.append("	 , " + (resposta.getNumeroDocumentoSEI() == null ? "null" : "'" + resposta.getNumeroDocumentoSEI() + "'"));
			sql.append("	 , " + (resposta.getDataHoraResposta() == null ? "null" : "'" + resposta.getDataHoraResposta() + "'"));
			sql.append("	 , " + (resposta.getNumeroProcessoSEI() == null ? "null" : "'" + resposta.getNumeroProcessoSEI() + "'"));
			sql.append("	 , " + (resposta.getRespostaImpressa() == null ? "null" : (resposta.getRespostaImpressa() ? "true" : "false")));
			sql.append("	 , " + (resposta.getDataHoraImpressao() == null ? "null" : "'" + resposta.getDataHoraImpressao() + "'"));
			sql.append("	 , " + (resposta.getBlocoAssinatura() == null ? "null" : "'" + resposta.getBlocoAssinatura() + "'"));
			sql.append("	 , " + (resposta.getRespostaNoBlocoAssinatura() == null ? "null" : (resposta.getRespostaNoBlocoAssinatura() ? "true" : "false")));
		} else {
			sql.append("update solicitacaoresposta ");
			sql.append("   set solicitacaoid = " + resposta.getSolicitacao().getSolicitacaoId());
			sql.append("     , tiporespostaid = " + (resposta.getTipoResposta() == null ? "null" : resposta.getTipoResposta().getTipoRespostaId()));
			sql.append("	 , observacao = " + (resposta.getObservacao() == null ? "null" : "'" + resposta.getObservacao().replace("'", "") + "'"));
			sql.append("	 , assinanteid = " + (resposta.getAssinante() == null ? "null" : resposta.getAssinante().getAssinanteId()));
			sql.append("	 , assinanteidsuperior = " + (resposta.getAssinanteSuperior() == null ? "null" : resposta.getAssinanteSuperior().getAssinanteId()));
			sql.append("	 , numerodocumentosei = " + (resposta.getNumeroDocumentoSEI() == null ? "null" : "'" + resposta.getNumeroDocumentoSEI() + "'"));
			sql.append("	 , datahoraresposta = " + (resposta.getDataHoraResposta() == null ? "null" : "'" + resposta.getDataHoraResposta() + "'"));
			sql.append("	 , numeroprocessosei = " + (resposta.getNumeroProcessoSEI() == null ? "null" : "'" + resposta.getNumeroProcessoSEI() + "'"));
			sql.append("	 , respostaimpressa = " + (resposta.getRespostaImpressa() == null ? "null" : (resposta.getRespostaImpressa() ? "true" : "false")));
			sql.append("	 , datahoraimpressao = " + (resposta.getDataHoraImpressao() == null ? "null" : "'" + resposta.getDataHoraImpressao() + "'"));
			sql.append("	 , blocoassinatura = " + (resposta.getBlocoAssinatura() == null ? "null" : "'" + resposta.getBlocoAssinatura() + "'"));
			sql.append("	 , respostanoblocoassinatura = " + (resposta.getRespostaNoBlocoAssinatura() == null ? "null" : (resposta.getRespostaNoBlocoAssinatura() ? "true" : "false")));
			sql.append(" where solicitacaorespostaid = " + resposta.getSolicitacaoRespostaId());
		}

		MyUtils.execute(conexao, sql.toString());
	}

	public void selecionarRespostaPadraoPorMunicipio(MyComboBox cbbTipoResposta, Municipio municipio, Origem origem) throws Exception {
		MunicipioTipoResposta municipioTipoResposta = MyUtils.entidade(obterMunicipioTipoResposta(null, municipio == null ? new Municipio(0) : municipio, origem, null));
		TipoResposta tipoRespostaPadrao = null;

		if (municipioTipoResposta != null) {
			tipoRespostaPadrao = municipioTipoResposta.getTipoResposta();
		} else {
			if (municipio != null && municipio.getTipoResposta() != null) {
				tipoRespostaPadrao = municipio.getTipoResposta();
			}
		}
		if (tipoRespostaPadrao != null) {
			cbbTipoResposta.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbTipoResposta, tipoRespostaPadrao.getTipoRespostaId(), null));
		}
	}

	public List<SolicitacaoResposta> obterSolicitacaoRespostaPendente(Solicitacao solicitacao) throws Exception {
		List<SolicitacaoResposta> respostas = obterSolicitacaoResposta(null, solicitacao, null, null, null, null, null, null);
		Iterator<SolicitacaoResposta> i = respostas.iterator();

		while (i.hasNext()) {
			if (!MyUtils.emptyStringIfNull(i.next().getDataHoraResposta()).equals("")) {
				i.remove();
			}
		}

		return respostas;
	}

	public void selecionarAssinantePadrao(MyComboBox cbbAssinante) {
		String arquivoPropriedades = System.getProperty("user.home");
		// se retornou o nome do diretório, continua
		if (!MyUtils.emptyStringIfNull(arquivoPropriedades).trim().equals("")) {
			// continua se o diretório existir
			if (MyUtils.arquivoExiste(arquivoPropriedades)) {
				// adiciona o nome da pasta escondida de ferramentas SPU
				arquivoPropriedades += "\\.ferramentasspu";
				// continua se a pasta existe
				if (MyUtils.arquivoExiste(arquivoPropriedades)) {
					// verifica se o arquivo de propriedades existe
					arquivoPropriedades += "\\ferramentasspu.properties";
					if (MyUtils.arquivoExiste(arquivoPropriedades)) {
						Properties props = MyUtils.obterPropriedades(arquivoPropriedades);
						cbbAssinante.setSelectedIndex(MyUtils.comboBoxItemIndex(cbbAssinante, Integer.parseInt(props.getProperty("assinantepadrao", "0")),  null));
					}
				}
			}
		}
	}

	public void salvarAssinantePadrao(Integer assinanteId) {
		String arquivoPropriedades = System.getProperty("user.home");
		// se retornou o nome do diretório, continua
		if (!MyUtils.emptyStringIfNull(arquivoPropriedades).trim().equals("")) {
			// continua se o diretório existir
			if (MyUtils.arquivoExiste(arquivoPropriedades)) {
				// adiciona o nome da pasta escondida de ferramentas SPU
				arquivoPropriedades += "\\.ferramentasspu";
				// se a pasta não existe, cria antes de continuar
				if (!MyUtils.arquivoExiste(arquivoPropriedades)) {
					(new File(arquivoPropriedades)).mkdir();
				}

				// verifica se o arquivo de propriedades existe
				arquivoPropriedades += "\\ferramentasspu.properties";
				Properties props = new Properties();
				if (MyUtils.arquivoExiste(arquivoPropriedades)) {
					props = MyUtils.obterPropriedades(arquivoPropriedades);
				}
				props.setProperty("assinantepadrao", assinanteId.toString());
				MyUtils.salvarPropriedades(props, arquivoPropriedades);
				JOptionPane.showMessageDialog(null, "Assinante padrão salvo com sucesso!");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Não foi possível obter o nome da pasta do usuário desta estação de trabalho");
		}
	}
}
