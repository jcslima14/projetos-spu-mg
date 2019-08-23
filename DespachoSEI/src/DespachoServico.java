import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
					obterOrigem(rs.getInt("origemid"), null).iterator().next(),
					obterTipoProcesso(rs.getInt("tipoprocessoid"), null).iterator().next(),
					rs.getString("numeroprocesso"), 
					rs.getString("autor"),
					MyUtils.entidade(obterMunicipio(false, rs.getInt("municipioid"), null)),
					MyUtils.entidade(obterDestino(rs.getInt("destinoid"), null, null, null, null)),
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
					obterSolicitacao(rs.getInt("solicitacaoid"), null, null, null).iterator().next(),
					rs.getString("datahoramovimentacao"), 
					rs.getString("resultadodownload"),
					rs.getBoolean("arquivosprocessados"),
					rs.getString("resultadoprocessamento")
					));
		}

		return retorno;
	}

	public List<SolicitacaoResposta> obterSolicitacaoResposta(Integer solicitacaoRespostaId, Solicitacao solicitacao, Origem origem, String numeroProcesso, boolean somenteDocumentosNaoGerados, boolean somenteDocumentosGerados,
			Boolean respostaImpressa, Boolean respostaNoBlocoAssinatura, Assinante assinante, Boolean tipoRespostaImprimirResposta) throws Exception {
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
			if (somenteDocumentosNaoGerados) {
				sql.append(" and coalesce(sr.numerodocumentosei, '' ) = '' ");
			}
			if (somenteDocumentosGerados) {
				sql.append(" and coalesce(sr.numerodocumentosei, '' ) <> '' ");
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
					obterSolicitacao(rs.getInt("solicitacaoid"), null, null, null).iterator().next(),
					obterTipoResposta(rs.getInt("tipoprocessoid"), null).iterator().next(),
					rs.getString("observacao"), 
					obterAssinante(rs.getInt("assinanteid"), null, null, null).iterator().next(),
					MyUtils.entidade(obterAssinante(rs.getInt("assinanteidsuperior"), null, null, null)),
					rs.getString("numerodocumentosei"),
					rs.getString("datahoraresposta"),
					rs.getString("numeroprocessosei"),
					rs.getBoolean("repostaimpressa"),
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
			return parametros.iterator().next().getConteudo();
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
			Assinante assinante = obterAssinante(rs.getInt("assinanteid"), null, null, null).iterator().next();
			TipoResposta tipoResposta = obterTipoResposta(rs.getInt("tiporespostaid"), null).iterator().next();
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

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new TipoImovel(rs.getInt("tipoimovelid"), rs.getString("descricao")));
		}
		
		return retorno;
	}

	public List<TipoResposta> obterTipoResposta(Integer tipoRespostaId, String descricao) throws Exception {
		List<TipoResposta> retorno = new ArrayList<TipoResposta>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from tiporesposta where 1 = 1");
		if (tipoRespostaId != null) {
			sql.append(" and tiporespostaid = " + tipoRespostaId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		sql.append(" order by descricao ");
		
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
					rs.getInt("quantidadeassinaturas")));
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

		sql.append(" order by nome ");
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			Municipio comarca = null;
			if (obterComarca) {
				comarca = MyUtils.entidade(obterMunicipio(false, rs.getInt("municipioidcomarca"), null));
			}

			retorno.add(new Municipio(rs.getInt("municipioid"),
									  rs.getString("nome"),
									  comarca,
									  MyUtils.entidade(obterDestino(rs.getInt("destinoid"), null, null, null, null)),
									  MyUtils.entidade(obterTipoResposta(rs.getInt("tiporespostaid"), null))
					));
		}
		
		return retorno;
	}

	public List<Assinante> obterAssinante(Integer assinanteId, String nome, Boolean superior, Boolean ativo) throws Exception {
		List<Assinante> retorno = new ArrayList<Assinante>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from assinante where 1= 1");
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

	public List<Destino> obterDestino(Integer destinoId, String descricao, String abreviacao, String municipio, String orderBy) throws Exception {
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

			if (municipio != null) {
				sql.append(" and destinoid = (select destinoid from municipio where nome = '" + municipio.replace("'", "''") + "')");
			}
		}

		if (orderBy == null) orderBy = "descricao";
		sql.append(" order by " + orderBy);
		
		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Destino(rs.getInt("destinoid"), rs.getString("artigo"), rs.getString("descricao"), rs.getString("abreviacao"), rs.getBoolean("usarcartorio")));
		}
		
		return retorno;
	}

	public void salvarDespacho(Despacho despacho) throws Exception {
		String sql = "";
		if (despacho.getDespachoId() != null) {
			sql += "update despacho "
				+  "   set tipoprocessoid = " + despacho.getTipoProcesso().getTipoProcessoId() 
				+  "     , numeroprocesso = '" + despacho.getNumeroProcesso() + "' " 
				+  "     , autor = '" + despacho.getAutor().replaceAll("'", "''") + "' " 
				+  "     , comarca = '" + despacho.getComarca().replaceAll("'", "''") + "' " 
				+  "     , tipoimovelid = " + despacho.getTipoImovel().getTipoImovelId() 
				+  "     , endereco = '" + despacho.getEndereco().replaceAll("'", "''") + "' " 
				+  "     , municipio = '" + despacho.getMunicipio().replaceAll("'", "''") + "' " 
				+  "     , coordenada = '" + despacho.getCoordenada().replaceAll("'", "''") + "' " 
				+  "     , area = '" + despacho.getArea().replaceAll("'", "''") + "' " 
				+  "     , tipodespachoid = " + despacho.getTipoDespacho().getTipoDespachoId() 
				+  "     , assinanteid = " + despacho.getAssinante().getAssinanteId() 
				+  "     , destinoid = " + despacho.getDestino().getDestinoId() 
				+  "     , observacao = '" + despacho.getObservacao().replaceAll("'", "''") + "' " 
				+  "     , numerodocumentosei = '" + despacho.getNumeroDocumentoSEI() + "' "
				+  "     , despachoimpresso = " + (despacho.getDespachoImpresso() ? "true" : "false")
				+  "     , despachonoblocoassinatura = " + (despacho.getDespachoNoBlocoAssinatura() ? "true" : "false")
				+  " where despachoid = " + despacho.getDespachoId();
		} else {
			sql += "insert into despacho (datadespacho, tipoprocessoid, numeroprocesso, autor, comarca, tipoimovelid, endereco, municipio, coordenada, area, tipodespachoid, assinanteid, destinoid, "
				+  "observacao, numerodocumentosei, arquivosanexados, despachoimpresso, despachonoblocoassinatura) values ("
				+  (despacho.getDataDespacho() == null ? "date('now','localtime')" : "'" + despacho.getDataDespacho() + "'") + ", " 
				+  despacho.getTipoProcesso().getTipoProcessoId() + ", " 
				+  "'" + despacho.getNumeroProcesso() + "', " 
				+  "'" + despacho.getAutor().replaceAll("'", "''") + "', " 
				+  "'" + despacho.getComarca().replaceAll("'", "''") + "', " 
				+  despacho.getTipoImovel().getTipoImovelId() + ", " 
				+  "'" + despacho.getEndereco().replaceAll("'", "''") + "', " 
				+  "'" + despacho.getMunicipio().replaceAll("'", "''") + "', " 
				+  "'" + despacho.getCoordenada().replaceAll("'", "''") + "', " 
				+  "'" + despacho.getArea().replaceAll("'", "''") + "', " 
				+  despacho.getTipoDespacho().getTipoDespachoId() + ", " 
				+  despacho.getAssinante().getAssinanteId() + ", "
				+  despacho.getDestino().getDestinoId() + ", " 
				+  "'" + despacho.getObservacao().replaceAll("'", "''") + "', " 
				+  "'" + despacho.getNumeroDocumentoSEI() + "', " 
				+  "false, " 
				+  "false, " 
				+  "false) "; 
		}
		MyUtils.execute(conexao, sql);
	}

	public void preencherOpcoesTipoResposta(MyComboBox cbbTipoResposta, List<TipoResposta> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<TipoResposta>();
			opcoesIniciais.addAll(obterTipoResposta(null, null));
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

	public void preencherOpcoesDestino(MyComboBox cbbDestino, List<Destino> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Destino>();
			opcoesIniciais.addAll(obterDestino(null, null, null, null, "abreviacao"));
			MyUtils.insereOpcoesComboBox(cbbDestino, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Destino: \n\n" + e.getMessage());
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
		
		return obterSolicitacao(null, solicitacao.getOrigem(), solicitacao.getTipoProcesso(), solicitacao.getNumeroProcesso()).iterator().next();
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
		
		return obterSolicitacaoEnvio(null, solicitacaoEnvio.getSolicitacao(), null, null, solicitacaoEnvio.getDataHoraMovimentacao(), null, false).iterator().next();
	}

	public void salvarSolicitacaoResposta(SolicitacaoResposta resposta) throws Exception {
		StringBuilder sql = new StringBuilder("");

		if (resposta.getSolicitacaoRespostaId() == null || resposta.getSolicitacaoRespostaId().equals(0)) {
			sql.append("insert into solicitacaoresposta (solicitacaoid, tiporespostaid, observacao, assinanteid, assinanteidsuperior, numerodocumentosei, datahoraresposta, numeroprocessosei, respostaimpressa, datahoraimpressao, blocoassinatura, respostanoblocoassinatura) ");
			sql.append("select " + resposta.getSolicitacao().getSolicitacaoId());
			sql.append("     , " + resposta.getTipoResposta().getTipoRespostaId());
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
			sql.append("     , tiporespostaid = " + resposta.getTipoResposta().getTipoRespostaId());
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
}
