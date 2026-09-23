package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

/**
 * DAO de Persona. Toda la lógica de crear/buscar/actualizar/eliminar
 * ya está en DefaultDAO; acá solo se conecta la unidad de persistencia.
 *
 * @author oscar
 */
@Stateless
@LocalBean
public class PersonaDAO extends DefaultDAO<Persona> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

}