package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

@Stateless
@LocalBean
public class ProcedimientoPasoExamenService extends DefaultService<ProcedimientoPasoExamen>{

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    
    // Devuelve solo los exámenes del paso indicado (para la pestaña
    // "Exámenes" dentro de un paso, en vez de traer todos los de todos los pasos).
        public List<ProcedimientoPasoExamen> findByProcedimientoPaso(UUID idProcedimientoPaso) {
        return getEntityManager().createQuery(
                "SELECT DISTINCT e FROM ProcedimientoPasoExamen e "
                + "JOIN FETCH e.idExamen x "
                + "JOIN FETCH e.idProcedimientoPaso pp "
                + "LEFT JOIN FETCH pp.idProcedimiento "
                + "LEFT JOIN FETCH pp.idRol "
                + "WHERE pp.idProcedimientoPaso = :id "
                + "ORDER BY x.nombre",
                ProcedimientoPasoExamen.class)
                .setParameter("id", idProcedimientoPaso)
                .getResultList();
    }
}