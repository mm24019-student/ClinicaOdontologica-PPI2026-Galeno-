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
public class ProcedimientoPasoSecuenciaDAO extends DefaultDAO<ProcedimientoPasoSecuencia>{

    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
      // Devuelve solo las secuencias del paso indicado (para la pestaña
    // "Secuencias" dentro de un paso, en vez de traer todas las de todos los pasos).
    public List<ProcedimientoPasoSecuencia> findByProcedimientoPaso(UUID idProcedimientoPaso) {
        return buscarPorPadre(
                "SELECT s FROM ProcedimientoPasoSecuencia s WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id",
                "id", idProcedimientoPaso);
    }
}
