import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class SPUNetServico {
	private EntityManager em;

	public SPUNetServico(EntityManager em) {
		this.em = em;
	}

	public List<Geoinformacao> obterGeoinformacao(Integer geoinformacaoId, Boolean cadastrado, String identTituloProduto) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select g from Geoinformacao g ");
		sql.append(" where 1 = 1 ");
		if (geoinformacaoId != null) {
			sql.append(" and geoinformacaoId = :geoinformacaoId");
			parametros.put("geoinformacaoId", geoinformacaoId);
		} else {
			if (cadastrado != null) {
				sql.append(" and cadastrado = :cadastrado ");
				parametros.put("cadastrado", cadastrado);
			}
			if (identTituloProduto != null) {
				sql.append(" and identTituloProduto = :identTituloProduto ");
				parametros.put("identTituloProduto", identTituloProduto);
			}
		}

		return JPAUtils.executeQuery(em, sql.toString(), parametros);
	}

	public List<Municipio> obterMunicipio(Integer municipioId, String nome) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select m from Municipio m ");
		sql.append(" where 1 = 1 ");
		if (municipioId != null) {
			sql.append(" and municipioId = :municipioId");
			parametros.put("municipioId", municipioId);
		} else {
			if (nome != null) {
				sql.append(" and nome = :nome ");
				parametros.put("nome", nome);
			}
		}

		return JPAUtils.executeQuery(em, sql.toString(), parametros);
	}

	public <T> T gravarEntidade(T entidade) {
		return JPAUtils.persistir(em, entidade);
	}
}
