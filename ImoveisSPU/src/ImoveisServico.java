import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class ImoveisServico {
	private EntityManager em;

	public ImoveisServico(EntityManager em) {
		this.em = em;
	}

	public List<MunicipioCorrecao> obterMunicipioCorrecao(Integer municipioCorrecaoId, String nomeIncorreto, String nomeCorreto) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select mc from MunicipioCorrecao mc ");
		sql.append(" where 1 = 1 ");
		if (municipioCorrecaoId != null) {
			sql.append(" and municipioCorrecaoId = :municipioCorrecaoId");
			parametros.put("municipioCorrecaoId", municipioCorrecaoId);
		} else {
			if (nomeIncorreto != null) {
				sql.append(" and nomeIncorreto like :nomeIncorreto ");
				parametros.put("nomeIncorreto", nomeIncorreto);
			}

			if (nomeCorreto != null) {
				sql.append(" and nomeCorreto like :nomeCorreto ");
				parametros.put("nomeCorreto", nomeCorreto);
			}
		}

		sql.append(" order by mc.nomeIncorreto ");

		return JPAUtils.executeQuery(em, sql.toString(), parametros);
	}

	public List<TipoPlanilha> obterTipoPlanilha(Integer tipoPlanilhaId, String descricao) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select t from TipoPlanilha t ");
		sql.append(" where 1 = 1 ");
		if (tipoPlanilhaId != null) {
			sql.append(" and tipoPlanilhaId = :tipoPlanilhaId");
			parametros.put("tipoPlanilhaId", tipoPlanilhaId);
		} else {
			if (descricao != null) {
				sql.append(" and descricao like :descricao ");
				parametros.put("descricao", descricao);
			}
		}

		sql.append(" order by descricao ");

		return JPAUtils.executeQuery(em, sql.toString(), parametros);
	}

	public List<EstruturaPlanilha> obterEstruturaPlanilha(Integer estruturaPlanilhaId, String descricaoTipoPlanilha, String nomeCampo) throws Exception {
		Map<String, Object> parametros = new LinkedHashMap<String, Object>();
		StringBuilder sql = new StringBuilder("");
		sql.append("select t from EstruturaPlanilha t ");
		sql.append(" join fetch t.tipoPlanilha tp ");
		sql.append(" where 1 = 1 ");
		if (estruturaPlanilhaId != null) {
			sql.append(" and estruturaPlanilhaId = :estruturaPlanilhaId");
			parametros.put("estruturaPlanilhaId", estruturaPlanilhaId);
		} else {
			if (descricaoTipoPlanilha != null) {
				sql.append(" and tp.descricao like :descricaoTipoPlanilha ");
				parametros.put("descricaoTipoPlanilha", descricaoTipoPlanilha);
			}

			if (nomeCampo != null) {
				sql.append(" and t.nomeCampo like :nomeCampo ");
				parametros.put("nomeCampo", nomeCampo);
			}
}

		sql.append(" order by tp.descricao, t.nomeCampo ");

		return JPAUtils.executeQuery(em, sql.toString(), parametros);
	}
	
	public <T> T persistir(T entidade) {
		entidade = JPAUtils.persistir(em, entidade);
		return entidade;
	}
	
	public <T> void excluir(T entidade) {
		JPAUtils.excluir(em, entidade);
	}
}
