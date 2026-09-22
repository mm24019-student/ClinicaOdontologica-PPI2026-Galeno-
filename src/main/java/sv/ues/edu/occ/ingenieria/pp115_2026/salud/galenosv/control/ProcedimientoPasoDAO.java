package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO extends DefaultDAO<ProcedimientoPaso>{

    // Inyectamos el EntityManager, que es la conexión con la base de datos.
    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    // Entrega la conexión a DefaultDAO para que realice las operaciones.
    @Override
    public EntityManager getEntityManager() {

        return em;
    }
}
