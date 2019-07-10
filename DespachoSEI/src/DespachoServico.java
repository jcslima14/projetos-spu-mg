import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DespachoServico {
	private Connection conexao;
	
	public DespachoServico(Connection conexao) {
		this.conexao = conexao;
	}

	public List<Parametro> obterParametro(Integer parametroId, String descricao) throws Exception {
		List<Parametro> retorno = new ArrayList<Parametro>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from parametro where 1= 1");
		if (parametroId != null) {
			sql.append(" and parametroid = " + parametroId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like '" + descricao + "'");
			}
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Parametro(rs.getInt("parametroid"), rs.getString("descricao"), rs.getString("conteudo")));
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

	public List<AssinanteTipoDespacho> obterAssinanteTipoDespacho(Integer assinanteTipoDespachoId, Integer assinanteId, Integer tipoDespachoId) throws Exception {
		List<AssinanteTipoDespacho> retorno = new ArrayList<AssinanteTipoDespacho>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from assinantetipodespacho where 1 = 1");
		if (assinanteTipoDespachoId != null) {
			sql.append(" and assinantetipodespachoid = " + assinanteTipoDespachoId);
		}
		if (assinanteId != null) {
			sql.append(" and assinanteid = " + assinanteId);
		}
		if (tipoDespachoId != null) {
			sql.append(" and tipodespachoid = " + tipoDespachoId);
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			Assinante assinante = obterAssinante(rs.getInt("assinanteid"), null, null).iterator().next();
			TipoDespacho tipoDespacho = obterTipoDespacho(rs.getInt("tipodespachoid"), null).iterator().next();
			retorno.add(new AssinanteTipoDespacho(rs.getInt("assinantetipodespachoid"), assinante, tipoDespacho, rs.getString("blocoassinatura")));
		}

		return retorno;
	}

	public List<ProcessoRecebido> obterProcessoRecebido(String numeroUnico, Boolean arquivosProcessados, String dataHoraMovimentacao, boolean somenteMunicipiosPreenchidos) throws Exception {
		List<ProcessoRecebido> retorno = new ArrayList<ProcessoRecebido>();

		StringBuilder sql = new StringBuilder("");
		sql.append("select * from processorecebido where 1 = 1");
		if (numeroUnico != null) {
			sql.append(" and numerounico = '" + numeroUnico + "'");
		}
		if (arquivosProcessados != null) {
			sql.append(" and arquivosprocessados = " + (arquivosProcessados ? "true" : "false"));
		}
		if (dataHoraMovimentacao != null) {
			sql.append(" and datahoramovimentacao = '" + dataHoraMovimentacao + "' ");
		}
		if (somenteMunicipiosPreenchidos) {
			sql.append(" and municipioid is not null");
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			Municipio municipio = null;
			List<Municipio> municipios = obterMunicipio(false, rs.getInt("municipioid"), null);
			if (municipios != null && !municipios.isEmpty()) {
				municipio = municipios.iterator().next();
			}
			retorno.add(new ProcessoRecebido(rs.getInt("processorecebidoid"), rs.getString("numerounico"), rs.getString("datahoramovimentacao"), municipio, rs.getString("resultadodownload"), rs.getBoolean("arquivosprocessados"), rs.getString("resultadoprocessamento")));
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
			retorno.add(new TipoDespacho(rs.getInt("tipodespachoid"), rs.getString("descricao"), rs.getString("numerodocumentosei"), rs.getBoolean("gerarprocessoindividual"), rs.getString("unidadeaberturaprocesso"), rs.getString("tipoprocesso")));
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

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			List<Municipio> comarcas = new ArrayList<Municipio>();
			if (obterComarca) {
				comarcas = obterMunicipio(false, rs.getInt("municipioidcomarca"), null);
			}

			retorno.add(new Municipio(rs.getInt("municipioid"),
									  rs.getString("nome"),
									  (comarcas.size() > 0 ? comarcas.iterator().next() : null),
									  obterDestino(rs.getInt("destinoid"), null, null, null).iterator().next()));
		}
		
		return retorno;
	}

	public List<Assinante> obterAssinante(Integer assinanteId, String nome, Boolean superior) throws Exception {
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
		}

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Assinante(rs.getInt("assinanteid"), rs.getString("nome"), rs.getString("cargo"), rs.getString("setor"), rs.getBoolean("superior"), rs.getString("numeroprocesso"), rs.getString("blocoassinatura")));
		}
		
		return retorno;
	}

	public List<Destino> obterDestino(Integer destinoId, String descricao, String abreviacao, String municipio) throws Exception {
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

		ResultSet rs = MyUtils.executeQuery(conexao, sql.toString());

		while (rs.next()) {
			retorno.add(new Destino(rs.getInt("destinoid"), rs.getString("artigo"), rs.getString("descricao"), rs.getBoolean("usarcomarca"), rs.getString("caminhodespachos")));
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
}
