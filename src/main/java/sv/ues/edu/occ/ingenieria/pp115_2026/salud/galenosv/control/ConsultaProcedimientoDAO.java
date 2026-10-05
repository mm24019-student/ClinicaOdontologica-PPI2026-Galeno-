package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Consulta;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoDAO extends DefaultDAO<ConsultaProcedimiento> {

    @PersistenceContext(unitName = "Galeno-PU")
    private EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    // Devuelve solo los exámenes del paso indicado (para la pestaña
    // "Exámenes" dentro de un paso, en vez de traer todos los de todos los pasos).
    public List<ConsultaProcedimiento> findByConsulta(UUID idConsulta) {
        return buscarPorPadre(
                "SELECT e FROM ConsultaProcedimiento e WHERE e.idConsulta.idConsulta = :id",
                "id", idConsulta);
    }

    // Al crear un procedimiento de la consulta se generan automáticamente sus
    // pasos, tomados de los pasos definidos para el tipo de procedimiento
    // elegido. Todo ocurre en la misma transacción: si algo falla, no queda ni
    // el procedimiento ni pasos sueltos.
    @Override
    public void crear(ConsultaProcedimiento registro) throws IllegalArgumentException, IllegalStateException {
        super.crear(registro);
        generarPasos(registro);
    }

    private void generarPasos(ConsultaProcedimiento cp) {
        if (cp.getIdProcedimiento() == null) {
            return;
        }
        UUID idClinica = clinicaDe(cp);
        List<ProcedimientoPaso> iniciales = findIniciales(cp.getIdProcedimiento());
        List<ProcedimientoPaso> todos = findTodos(cp.getIdProcedimiento());

        // 1) Se valida el responsable de TODOS los pasos (iniciales y dependientes),
        // pero solo se preparan para crear los iniciales. No se persiste nada aun.
        Map<ProcedimientoPaso, PersonaRol> responsables = new LinkedHashMap<>();
        List<String> sinResponsable = new ArrayList<>();
        for (ProcedimientoPaso definido : todos) {
            PersonaRol responsable = responsableDe(definido, idClinica);
            if (responsable == null) {
                sinResponsable.add("rol \"" + nombreRol(definido) + "\" del paso \""
                        + definido.getNombre() + "\"");
            } else if (iniciales.contains(definido)) {
                responsables.put(definido, responsable);
            }
        }
        if (!sinResponsable.isEmpty()) {
            throw new IllegalStateException(
                    "No se puede registrar el procedimiento: en la clinica de la consulta no hay ninguna "
                    + "persona asignada para " + String.join("; ", sinResponsable));
        }

        // 2) Todos tienen responsable: se persisten solo los pasos iniciales.
        for (Map.Entry<ProcedimientoPaso, PersonaRol> e : responsables.entrySet()) {
            ProcedimientoPaso definido = e.getKey();
            ConsultaProcedimientoPaso paso = new ConsultaProcedimientoPaso(UUID.randomUUID());
            paso.setIdConsultaProcedimiento(cp);
            paso.setIdProcedimientoPaso(definido);
            paso.setEstado("PENDIENTE");
            paso.setFechaInicio(cp.getFechaInicio());
            paso.setFechaFin(cp.getFechaFin());
            paso.setIdPersonaRol(e.getValue());
            em.persist(paso);
        }
    }

    private String nombreRol(ProcedimientoPaso definido) {
        return definido.getIdRol() == null ? "" : definido.getIdRol().getNombre();
    }

// Pasos INICIALES de un procedimiento: los que no dependen de otro paso. Es la
    // MISMA lista que generarPasos() usa para crear, así que lo que la pantalla
    // valide antes de guardar y lo que se crea nunca se contradicen.
    public List<ProcedimientoPaso> findIniciales(UUID idProcedimiento) {
        List<ProcedimientoPaso> todos = em.createQuery(
                "SELECT pp FROM ProcedimientoPaso pp "
                + "WHERE pp.idProcedimiento.idProcedimiento = :id ORDER BY pp.nombre",
                ProcedimientoPaso.class)
                .setParameter("id", idProcedimiento)
                .getResultList();
        List<UUID> dependientes = em.createQuery(
                "SELECT s.idProcedimientoPaso.idProcedimientoPaso FROM ProcedimientoPasoSecuencia s "
                + "WHERE s.idProcedimientoPaso.idProcedimiento.idProcedimiento = :id "
                + "AND s.idProcedimientoPasoReferencia IS NOT NULL", UUID.class)
                .setParameter("id", idProcedimiento)
                .getResultList();
        return todos.stream()
                .filter(pp -> !dependientes.contains(pp.getIdProcedimientoPaso()))
                .collect(java.util.stream.Collectors.toList());
    }

// Clínica de la consulta (la de su persona/rol), o null si no se puede determinar.
    private UUID clinicaDe(ConsultaProcedimiento cp) {
        if (cp.getIdConsulta() == null || cp.getIdConsulta().getIdConsulta() == null) {
            return null;
        }
        Consulta c = em.find(Consulta.class, cp.getIdConsulta().getIdConsulta());
        if (c == null || c.getIdPersonaRol() == null || c.getIdPersonaRol().getIdClinica() == null) {
            return null;
        }
        return c.getIdPersonaRol().getIdClinica().getIdClinica();
    }

    // TODOS los pasos del procedimiento (iniciales y dependientes)
    public List<ProcedimientoPaso> findTodos(UUID idProcedimiento) {
        return em.createQuery(
                "SELECT pp FROM ProcedimientoPaso pp "
                + "WHERE pp.idProcedimiento.idProcedimiento = :id ORDER BY pp.nombre",
                ProcedimientoPaso.class)
                .setParameter("id", idProcedimiento)
                .getResultList();
    }

// Quien atiende el paso: alguien con el rol del paso asignado EN LA MISMA
    // clinica de la consulta. Devuelve null si el rol no tiene a nadie aqui: la
    // decision de cancelar queda en generarPasos, que revisa todos los pasos
    // antes de persistir ninguno.
    private PersonaRol responsableDe(ProcedimientoPaso definido, UUID idClinica) {
        if (definido.getIdRol() == null) {
            throw new IllegalStateException("El paso \"" + definido.getNombre() + "\" no tiene rol asignado");
        }
        List<PersonaRol> candidatos = em.createQuery(
                "SELECT pr FROM PersonaRol pr LEFT JOIN FETCH pr.idClinica "
                + "WHERE pr.idRol.idRol = :rol ORDER BY pr.fechaCreacion", PersonaRol.class)
                .setParameter("rol", definido.getIdRol().getIdRol())
                .setMaxResults(50)
                .getResultList();
        return candidatos.stream()
                .filter(pr -> pr.getIdPersona() != null // <-- NUEVO
                && idClinica != null && pr.getIdClinica() != null
                && idClinica.equals(pr.getIdClinica().getIdClinica()))
                .findFirst()
                .orElse(null);
    }

    // Como los pasos ahora se crean solos, al eliminar el procedimiento hay
    // que borrar primero sus pasos (si no, la llave foránea lo impide). Si
    // alguno de los pasos ya tiene órdenes de examen, no se permite borrar.
    @Override
    public void eliminar(UUID id) throws IllegalArgumentException, IllegalStateException {
        if (id != null) {
            Long ordenes = em.createQuery(
                    "SELECT COUNT(o) FROM OrdenExamen o "
                    + "WHERE o.idConsultaProcedimientoPaso.idConsultaProcedimiento.idConsultaProcedimiento = :id",
                    Long.class)
                    .setParameter("id", id)
                    .getSingleResult();
            if (ordenes != null && ordenes > 0) {
                throw new IllegalStateException(
                        "No se puede eliminar el procedimiento porque alguno de sus pasos ya tiene órdenes de examen.");
            }
            em.createQuery("DELETE FROM ConsultaProcedimientoPaso p "
                    + "WHERE p.idConsultaProcedimiento.idConsultaProcedimiento = :id")
                    .setParameter("id", id)
                    .executeUpdate();
        }
        super.eliminar(id);
    }

}
