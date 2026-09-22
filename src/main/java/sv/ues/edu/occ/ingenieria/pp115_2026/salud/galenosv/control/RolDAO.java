package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * DAO de Rol. La lógica de crear/buscar/actualizar/eliminar ya está en
 * DefaultDAO; acá solo se conecta la unidad de persistencia.
 *
 * @author oscar
 */
@Stateless
@LocalBean
public class RolDAO extends DefaultDAO<Rol> implements Serializable {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}