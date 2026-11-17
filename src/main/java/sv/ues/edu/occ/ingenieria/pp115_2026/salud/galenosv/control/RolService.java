package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Service de Rol. La lógica de crear/buscar/actualizar/eliminar ya está en
 * DefaultService; acá solo se conecta la unidad de persistencia.
 *
 * @author oscar
 */
@Stateless
@LocalBean
public class RolService extends DefaultService<Rol>{

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}