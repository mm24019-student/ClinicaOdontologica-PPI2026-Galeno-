package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

@Stateless
@LocalBean
public class ExamenTipoExamenService extends DefaultService<ExamenTipoExamen> {

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

    // Trae la lista con Examen y TipoExamen ya cargados, para que el REST
    // pueda leerlos sin error de relacion perezosa.
    public List<ExamenTipoExamen> findRangeConRelaciones(int first, int max) {
        TypedQuery<ExamenTipoExamen> q = em.createQuery(
                "SELECT e FROM ExamenTipoExamen e JOIN FETCH e.idExamen JOIN FETCH e.idTipoExamen",
                ExamenTipoExamen.class);
        q.setFirstResult(first);
        q.setMaxResults(max);
        return q.getResultList();
    }

    public ExamenTipoExamen buscarConRelaciones(UUID id) {
        TypedQuery<ExamenTipoExamen> q = em.createQuery(
                "SELECT e FROM ExamenTipoExamen e JOIN FETCH e.idExamen JOIN FETCH e.idTipoExamen WHERE e.idExamenTipoExamen = :id",
                ExamenTipoExamen.class);
        q.setParameter("id", id);
        List<ExamenTipoExamen> resultado = q.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    // Referencias livianas: para fijar una relacion (FK) sin cargar el objeto completo.
    public Examen referenciaExamen(UUID id) {
        return em.getReference(Examen.class, id);
    }

    public TipoExamen referenciaTipoExamen(UUID id) {
        return em.getReference(TipoExamen.class, id);
    }
}
