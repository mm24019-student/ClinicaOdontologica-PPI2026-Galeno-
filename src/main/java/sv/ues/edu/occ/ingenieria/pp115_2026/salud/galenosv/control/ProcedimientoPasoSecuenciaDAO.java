package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
}
