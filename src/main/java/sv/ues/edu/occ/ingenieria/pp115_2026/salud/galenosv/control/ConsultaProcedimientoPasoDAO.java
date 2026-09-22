package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso> implements Serializable{
    
     @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
}
