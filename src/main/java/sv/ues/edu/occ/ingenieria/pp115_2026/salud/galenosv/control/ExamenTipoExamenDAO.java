package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;

@Stateless
@LocalBean
public class ExamenTipoExamenDAO extends DefaultDAO<ExamenTipoExamen> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenTipoExamen> findByExamen(UUID idExamen) {
        return em.createQuery(
                "SELECT e FROM ExamenTipoExamen e "
                + "WHERE e.idExamen.idExamen = :idExamen "
                + "ORDER BY e.fechaCreacion DESC",
                ExamenTipoExamen.class)
                .setParameter("idExamen", idExamen)
                .getResultList();
    }
}
