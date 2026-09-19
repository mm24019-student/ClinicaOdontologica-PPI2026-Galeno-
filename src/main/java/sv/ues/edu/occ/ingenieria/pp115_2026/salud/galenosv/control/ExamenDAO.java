package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

@Stateless
@LocalBean
public class ExamenDAO extends DefaultDAO<Examen> implements Serializable {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {

        return em;
    }

}
