package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ProcedimientoPasoSecuenciaService extends DefaultService<ProcedimientoPasoSecuencia>{

    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
      // Devuelve solo las secuencias del paso indicado (para la pestaña
    // "Secuencias" dentro de un paso, en vez de traer todas las de todos los pasos).
        public List<ProcedimientoPasoSecuencia> findByProcedimientoPaso(UUID idProcedimientoPaso) {
        return getEntityManager().createQuery(
                "SELECT s FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id",
                ProcedimientoPasoSecuencia.class)
                .setParameter("id", idProcedimientoPaso)
                .getResultList();
    }

    public List<ProcedimientoPasoSecuencia> findByProcedimiento(UUID idProcedimiento) {
        return getEntityManager().createQuery(
                "SELECT DISTINCT s FROM ProcedimientoPasoSecuencia s "
                + "JOIN FETCH s.idProcedimientoPaso pp "
                + "LEFT JOIN FETCH pp.idProcedimiento "
                + "LEFT JOIN FETCH pp.idRol "
                + "WHERE pp.idProcedimiento.idProcedimiento = :id",
                ProcedimientoPasoSecuencia.class)
                .setParameter("id", idProcedimiento)
                .getResultList();
    }
}
