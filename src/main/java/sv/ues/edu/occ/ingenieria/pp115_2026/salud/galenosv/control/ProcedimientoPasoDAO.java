package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ProcedimientoPasoDAO extends DefaultDAO<ProcedimientoPaso> {

    // Inyectamos el EntityManager, que es la conexión con la base de datos.
    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    // Entrega la conexión a DefaultDAO para que realice las operaciones.
    @Override
    public EntityManager getEntityManager() {

        return em;
    }

    // Devuelve solo los pasos que pertenecen a un procedimiento específico.
    // Se usa para la pestaña "Pasos" dentro de la pantalla de Procedimiento,
    // en vez de traer siempre los 100 pasos de todos los procedimientos.
    public List<ProcedimientoPaso> findByProcedimiento(UUID idProcedimiento) {
        return buscarPorPadre(
                "SELECT pp FROM ProcedimientoPaso pp WHERE pp.idProcedimiento.idProcedimiento = :id",
                "id", idProcedimiento);
    }
}
