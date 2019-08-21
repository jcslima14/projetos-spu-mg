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

	public List<Solicitacao> obterSolicitacao(Integer solicitacaoId, Origem origem, String numeroProcesso) throws Exception {
		List<Solicitacao> retorno = new ArrayList<Solicitacao>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from solicitacao where 1 = 1");
		if (solicitacaoId != null) {
			sql.append(" and solicitacaoid = " + solicitacaoId);
		} else {
			if (origem != null) {
				sql.append(" and origemid = " + origem.getOrigemId());
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
					obterMunicipio(false, rs.getInt("municipioid"), null).iterator().next(),
					obterDestino(rs.getInt("destinoid"), null, null, null, null).iterator().next(),
					rs.getString("cartorio"),
					obterTipoImovel(rs.getInt("tipoimovelid"), null).iterator().next(),
					rs.getString("endereco"),
					rs.getString("cooredenada"),
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
				sql.append(" and sr.datahoramovimentacao = '" + dataHoraMovimentacao + "'");
			}
			if (arquivosProcessados != null) {
				sql.append(" and sr.arquivosprocessados = " + (arquivosProcessados ? "true" : "false"));
			}
			if (somenteMunicipiosPreenchidos) {
				sql.append(" and s.municipioid is not null");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new SolicitacaoEnvio(rs.getInt("solicitacaoenvioid"), 
					obterSolicitacao(rs.getInt("solicitacaoid"), null, null).iterator().next(),
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
					obterSolicitacao(rs.getInt("solicitacaoid"), null, null).iterator().next(),
					obterTipoResposta(rs.getInt("tipoprocessoid"), null).iterator().next(),
					rs.getString("observacao"), 
					obterAssinante(rs.getInt("assinanteid"), null, null, null).iterator().next(),
					obterAssinante(rs.getInt("assinanteidsuperior"), null, null, null).iterator().next(),
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

	public List<TipoDespacho> obterTipoDespacho(Integer tipoDespachoId, String descricao) throws Exception {
		List<TipoDespacho> retorno = new ArrayList<TipoDespacho>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from tipodespacho where 1= 1");
		if (tipoDespachoId != null) {
			sql.append(" and tipodespachoid = " + tipoDespachoId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new TipoDespacho(rs.getInt("tipodespachoid"), rs.getString("descricao"), rs.getString("numerodocumentosei"), rs.getBoolean("gerarprocessoindividual"), rs.getString("unidadeaberturaprocesso"), rs.getString("tipoprocesso"), rs.getBoolean("imprimirresposta")));
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
					rs.getString("numerodocumentosei"), 
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
			List<Municipio> comarcas = new ArrayList<Municipio>();
			if (obterComarca) {
				comarcas = obterMunicipio(false, rs.getInt("municipioidcomarca"), null);
			}

			retorno.add(new Municipio(rs.getInt("municipioid"),
									  rs.getString("nome"),
									  (comarcas.size() > 0 ? comarcas.iterator().next() : null),
									  obterDestino(rs.getInt("destinoid"), null, null, null, null).iterator().next(),
									  obterTipoResposta(rs.getInt("tiporespostaid"), null).iterator().next()
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
			retorno.add(new Assinante(rs.getInt("assinanteid"), rs.getString("nome"), rs.getBoolean("ativo"), rs.getString("cargo"), rs.getString("setor"), rs.getBoolean("superior"), rs.getString("numeroprocesso"), rs.getString("blocoassinatura")));
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
		}
	}

	public void preencherOpcoesAssinante(MyComboBox cbbAssinante, List<Assinante> opcoesIniciais, Boolean superior, Boolean ativo) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Assinante>();
			opcoesIniciais.addAll(obterAssinante(null, null, superior, ativo));
			MyUtils.insereOpcoesComboBox(cbbAssinante, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Assinante: \n\n" + e.getMessage());
		}
	}

	public void preencherOpcoesMunicipio(MyComboBox cbbMunicipio, List<Municipio> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Municipio>();
			opcoesIniciais.addAll(obterMunicipio(false, null, null));
			MyUtils.insereOpcoesComboBox(cbbMunicipio, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Município: \n\n" + e.getMessage());
		}
	}

	public void preencherOpcoesDestino(MyComboBox cbbDestino, List<Destino> opcoesIniciais) {
		try {
			if (opcoesIniciais == null) opcoesIniciais = new ArrayList<Destino>();
			opcoesIniciais.addAll(obterDestino(null, null, null, null, "abreviacao"));
			MyUtils.insereOpcoesComboBox(cbbDestino, opcoesIniciais);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Erro ao carregar as opções de Destino: \n\n" + e.getMessage());
		}
	}
}
