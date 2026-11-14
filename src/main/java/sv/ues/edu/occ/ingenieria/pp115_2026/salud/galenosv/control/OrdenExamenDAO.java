package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Stateless
@LocalBean
public class OrdenExamenDAO extends DefaultDAO<OrdenExamen> {

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    public List<OrdenExamen> findRangeConRelaciones(int first, int max) {
        TypedQuery<OrdenExamen> q = em.createQuery(
                "SELECT o FROM OrdenExamen o JOIN FETCH o.idConsultaProcedimientoPaso",
                OrdenExamen.class);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    public List<OrdenExamen> findByConsultaProcedimientoPaso(UUID idPaso) {
        TypedQuery<OrdenExamen> q = em.createQuery(
                "SELECT DISTINCT o FROM OrdenExamen o "
                + "JOIN FETCH o.idConsultaProcedimientoPaso p "
                + "JOIN FETCH p.idConsultaProcedimiento cp "
                + "JOIN FETCH p.idProcedimientoPaso pp "
                + "LEFT JOIN FETCH pp.idProcedimiento "
                + "LEFT JOIN FETCH pp.idRol "
                + "LEFT JOIN FETCH p.idPersonaRol pr "
                + "LEFT JOIN FETCH pr.idPersona "
                + "LEFT JOIN FETCH pr.idRol "
                + "LEFT JOIN FETCH pr.idClinica "
                + "WHERE p.idConsultaProcedimientoPaso = :id "
                + "ORDER BY o.fechaCreacion DESC", OrdenExamen.class);
        q.setParameter("id", idPaso);
        return q.getResultList();
    }

    public OrdenExamen buscarConRelaciones(UUID id) {
        TypedQuery<OrdenExamen> q = em.createQuery(
                "SELECT o FROM OrdenExamen o JOIN FETCH o.idConsultaProcedimientoPaso WHERE o.idOrdenExamen = :id",
                OrdenExamen.class);
        q.setParameter("id", id);
        List<OrdenExamen> resultado = q.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public ConsultaProcedimientoPaso referenciaConsultaProcedimientoPaso(UUID id) {
        return em.getReference(ConsultaProcedimientoPaso.class, id);
    }
}