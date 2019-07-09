import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class JPAUtils {

	public static <T> void persistir(EntityManager em, T entidade) {
		try {
			em.getTransaction().begin();
			em.merge(entidade);
			em.getTransaction().commit();;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
	}

	public static <T> void excluir(EntityManager em, T entidade) {
		try {
			em.getTransaction().begin();
			em.remove(entidade);
			em.getTransaction().commit();;
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> executeQuery(EntityManager em, String sql, Map<String, Object> parametros) {
		em.clear();
		Query qry = em.createQuery(sql);
		for (String parametro : parametros.keySet()) {
			qry.setParameter(parametro, parametros.get(parametro));
		}
		return qry.getResultList();
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> executeNativeQuery(EntityManager em, String sql) {
		em.clear();
		Query qry = em.createNativeQuery(sql);
		return qry.getResultList();
	}

	public static void executeUpdate(EntityManager em, String sql) {
		em.getTransaction().begin();
		Query qry = em.createNativeQuery(sql);
		qry.executeUpdate();
		em.getTransaction().commit();
	}
}
