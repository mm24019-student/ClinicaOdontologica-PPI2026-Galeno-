package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;

@Stateless
@LocalBean
public class ExamenResultadoDAO extends DefaultDAO<ExamenResultado> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenResultado> findByOrdenExamen(UUID idOrdenExamen) {
        TypedQuery<ExamenResultado> q = em.createQuery(
                "SELECT r FROM ExamenResultado r WHERE r.idOrdenExamen.idOrdenExamen = :id "
                + "ORDER BY r.fechaCreacion DESC",
                ExamenResultado.class);
        q.setParameter("id", idOrdenExamen);
        return q.getResultList();
    }
}