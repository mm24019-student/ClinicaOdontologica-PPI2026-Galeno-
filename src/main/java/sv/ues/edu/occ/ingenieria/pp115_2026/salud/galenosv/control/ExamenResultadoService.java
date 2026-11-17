package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenResultado;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Stateless
@LocalBean
public class ExamenResultadoService extends DefaultService<ExamenResultado> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<ExamenResultado> findRangeConRelaciones(int first, int max) {
        TypedQuery<ExamenResultado> q = em.createQuery(
                "SELECT e FROM ExamenResultado e JOIN FETCH e.idOrdenExamen",
                ExamenResultado.class);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }
    
    public List<ExamenResultado> findByOrdenExamen(UUID idOrdenExamen) {
    TypedQuery<ExamenResultado> q = em.createQuery(
            "SELECT DISTINCT r FROM ExamenResultado r "
            + "JOIN FETCH r.idOrdenExamen o "
            + "JOIN FETCH o.idConsultaProcedimientoPaso p "
            + "JOIN FETCH p.idConsultaProcedimiento cp "
            + "JOIN FETCH p.idProcedimientoPaso pp "
            + "LEFT JOIN FETCH pp.idProcedimiento "
            + "LEFT JOIN FETCH pp.idRol "
            + "LEFT JOIN FETCH p.idPersonaRol pr "
            + "LEFT JOIN FETCH pr.idPersona "
            + "LEFT JOIN FETCH pr.idRol "
            + "LEFT JOIN FETCH pr.idClinica "
            + "WHERE o.idOrdenExamen = :id "
            + "ORDER BY r.fechaCreacion DESC",
            ExamenResultado.class);
    q.setParameter("id", idOrdenExamen);
    return q.getResultList();
}

    public ExamenResultado buscarConRelaciones(UUID id) {
        TypedQuery<ExamenResultado> q = em.createQuery(
                "SELECT e FROM ExamenResultado e JOIN FETCH e.idOrdenExamen WHERE e.idExamenResultado = :id",
                ExamenResultado.class);
        q.setParameter("id", id);
        List<ExamenResultado> resultado = q.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public OrdenExamen referenciaOrdenExamen(UUID id) {
        return em.getReference(OrdenExamen.class, id);
    }
}