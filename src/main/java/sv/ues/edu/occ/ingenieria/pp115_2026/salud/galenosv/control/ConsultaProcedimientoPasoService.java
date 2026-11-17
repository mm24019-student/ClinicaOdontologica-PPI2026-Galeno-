package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoService extends DefaultService<ConsultaProcedimientoPaso> {

    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // Devuelve solo los exámenes del paso indicado (para la pestaña
    // "Exámenes" dentro de un paso, en vez de traer todos los de todos los pasos).
        public List<ConsultaProcedimientoPaso> findByConsultaProcedimiento(UUID idConsultaProcedimiento) {
        return getEntityManager().createQuery(
                "SELECT DISTINCT e FROM ConsultaProcedimientoPaso e "
                + "JOIN FETCH e.idConsultaProcedimiento cp "
                + "JOIN FETCH e.idProcedimientoPaso pp "
                + "LEFT JOIN FETCH pp.idProcedimiento "
                + "LEFT JOIN FETCH pp.idRol "
                + "LEFT JOIN FETCH e.idPersonaRol pr "
                + "LEFT JOIN FETCH pr.idPersona "
                + "LEFT JOIN FETCH pr.idRol "
                + "LEFT JOIN FETCH pr.idClinica "
                + "WHERE cp.idConsultaProcedimiento = :id "
                + "ORDER BY e.fechaInicio",
                ConsultaProcedimientoPaso.class)
                .setParameter("id", idConsultaProcedimiento)
                .getResultList();
    }

    // Si el cliente no indicó responsable, se asigna la persona que tiene el
    // rol del paso en la clínica de la consulta. Así el siguiente paso queda
    // a cargo de quien corresponde aunque el empleado actual tenga otro rol.
    @Override
    public void crear(ConsultaProcedimientoPaso registro) throws IllegalArgumentException, IllegalStateException {
        if (registro != null && registro.getIdPersonaRol() == null) {
            registro.setIdPersonaRol(responsableDe(registro));
        }
        super.crear(registro);
    }

    private PersonaRol responsableDe(ConsultaProcedimientoPaso paso) {
        if (paso.getIdProcedimientoPaso() == null
                || paso.getIdProcedimientoPaso().getIdRol() == null
                || paso.getIdConsultaProcedimiento() == null) {
            return null;
        }
        UUID idClinica = getEntityManager().createQuery(
                "SELECT pr.idClinica.idClinica FROM ConsultaProcedimiento cp "
                + "JOIN cp.idConsulta c JOIN c.idPersonaRol pr "
                + "WHERE cp.idConsultaProcedimiento = :id", UUID.class)
                .setParameter("id", paso.getIdConsultaProcedimiento().getIdConsultaProcedimiento())
                .getResultList().stream().findFirst().orElse(null);
        if (idClinica == null) {
            return null;
        }
        UUID idRol = paso.getIdProcedimientoPaso().getIdRol().getIdRol();
        List<PersonaRol> responsables = getEntityManager().createQuery(
                "SELECT pr FROM PersonaRol pr "
                + "WHERE pr.idRol.idRol = :idRol "
                + "AND pr.idClinica.idClinica = :idClinica "
                + "AND pr.idPersona IS NOT NULL "
                + "ORDER BY pr.fechaCreacion", PersonaRol.class)
                .setParameter("idRol", idRol)
                .setParameter("idClinica", idClinica)
                .setMaxResults(1)
                .getResultList();
        if (responsables.isEmpty()) {
            throw new IllegalStateException(
                    "No hay una persona asignada al rol del siguiente paso en esta clínica.");
        }
        return responsables.get(0);
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

    // Sobrescribe eliminar() de DefaultService: primero valida que el paso no tenga
    // órdenes de examen y, si todo está bien, delega en super.eliminar(id).
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
