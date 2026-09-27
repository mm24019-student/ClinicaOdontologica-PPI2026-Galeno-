package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoDAO extends DefaultDAO<ConsultaProcedimientoPaso>{
    
    
     @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;
 
    @Override
    public EntityManager getEntityManager() {
        return em;
    }
    
    // Devuelve solo los exámenes del paso indicado (para la pestaña
    // "Exámenes" dentro de un paso, en vez de traer todos los de todos los pasos).
    public List<ConsultaProcedimientoPaso> findByConsultaProcedimiento(UUID idConsultaProcedimiento) {
        return buscarPorPadre(
                "SELECT e FROM ConsultaProcedimientoPaso e WHERE e.idConsultaProcedimiento.idConsultaProcedimiento = :id",
                "id", idConsultaProcedimiento);
    }
 
    // Un paso no se puede borrar físicamente si ya generó una orden de examen
    // (orden_examen tiene FK hacia este paso). En vez de dejar que la base de
    // datos reviente con una violación de llave foránea al final de la
    // transacción (lo que en pantalla se ve como un RollbackException opaco),
    // lo validamos antes y devolvemos un mensaje de negocio claro.
    private boolean tieneOrdenesDeExamen(UUID idPaso) {
        Long total = getEntityManager()
                .createQuery(
                    "SELECT COUNT(o) FROM OrdenExamen o "
                    + "WHERE o.idConsultaProcedimientoPaso.idConsultaProcedimientoPaso = :id",
                    Long.class)
                .setParameter("id", idPaso)
                .getSingleResult();
        return total != null && total > 0;
    }
 
    @Override
    public void eliminar(UUID id) throws IllegalArgumentException, IllegalStateException {
        if (id != null && tieneOrdenesDeExamen(id)) {
            throw new IllegalStateException(
                "No se puede eliminar este paso porque ya tiene una o más órdenes de examen "
                + "asociadas. Elimine primero esas órdenes de examen, o cambie el estado del "
                + "paso a CANCELADO en lugar de borrarlo.");
        }
        super.eliminar(id);
    }
}
