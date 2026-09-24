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
public class ProcedimientoPasoExamenDAO extends DefaultDAO<ProcedimientoPasoExamen>{

    @PersistenceContext(unitName = "Galeno-PU")
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    
    // Devuelve solo los exámenes del paso indicado (para la pestaña
    // "Exámenes" dentro de un paso, en vez de traer todos los de todos los pasos).
    public List<ProcedimientoPasoExamen> findByProcedimientoPaso(UUID idProcedimientoPaso) {
        return buscarPorPadre(
                "SELECT e FROM ProcedimientoPasoExamen e WHERE e.idProcedimientoPaso.idProcedimientoPaso = :id",
                "id", idProcedimientoPaso);
    }
}