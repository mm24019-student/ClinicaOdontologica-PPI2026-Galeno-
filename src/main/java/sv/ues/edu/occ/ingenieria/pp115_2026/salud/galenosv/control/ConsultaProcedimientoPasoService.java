package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control;

import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Stateless
@LocalBean
public class ConsultaProcedimientoPasoService extends DefaultService<ConsultaProcedimientoPaso> {

    private static final String JPQL_SIGUIENTES = "SELECT s FROM ProcedimientoPasoSecuencia s "
            + "WHERE s.idProcedimientoPasoReferencia = :id";
    private static final String JPQL_ANTERIORES = "SELECT s FROM ProcedimientoPasoSecuencia s "
            + "WHERE s.idProcedimientoPaso.idProcedimientoPaso = :id "
            + "AND s.idProcedimientoPasoReferencia IS NOT NULL";
    private static final String JPQL_PASO_DE_CONSULTA = "SELECT p FROM ConsultaProcedimientoPaso p "
            + "WHERE p.idConsultaProcedimiento.idConsultaProcedimiento = :idConsulta "
            + "AND p.idProcedimientoPaso.idProcedimientoPaso = :idPaso";
    private static final String JPQL_ROL_ACTOR = "SELECT pr.idRol.idRol FROM PersonaRol pr "
            + "WHERE pr.idPersonaRol = :id";

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
                + "LEFT JOIN FETCH e.idProcedimientoPaso pp "
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

    public ConsultaProcedimientoPaso avanzar(UUID id) {
        return avanzar(id, null);
    }

    public ConsultaProcedimientoPaso avanzar(UUID id, UUID idActor) {
        ConsultaProcedimientoPaso actual = pasoPorId(id);
        validarActor(actual, idActor);
        if (actual.getIdProcedimientoPaso() == null) {
            throw new IllegalStateException("El paso actual no tiene un paso definido");
        }
        ProcedimientoPaso siguiente = siguienteDe(actual.getIdProcedimientoPaso());
        if (siguiente == null) {
            throw new IllegalStateException("No hay un paso siguiente para este procedimiento");
        }
        actual.setEstado("COMPLETADO");
        actual.setFechaFin(new Date());
        super.actualizar(actual);
        return activar(siguiente, actual.getIdConsultaProcedimiento());
    }

    public ConsultaProcedimientoPaso retroceder(UUID id) {
        return retroceder(id, null);
    }

    public ConsultaProcedimientoPaso retroceder(UUID id, UUID idActor) {
        return retroceder(id, idActor, null);
    }

    public ConsultaProcedimientoPaso retroceder(UUID id, UUID idActor, String motivo) {
        ConsultaProcedimientoPaso actual = pasoPorId(id);
        validarActor(actual, idActor);
        if (actual.getIdProcedimientoPaso() == null) {
            throw new IllegalStateException("El paso actual no tiene un paso definido");
        }
        ProcedimientoPaso anterior = anteriorDe(actual.getIdProcedimientoPaso());
        if (anterior == null) {
            throw new IllegalStateException("No hay un paso anterior para este procedimiento");
        }
        actual.setEstado("PENDIENTE");
        actual.setFechaFin(null);
        super.actualizar(actual);
        ConsultaProcedimientoPaso activado = activar(anterior, actual.getIdConsultaProcedimiento());
        if (motivo != null && !motivo.isBlank()) {
            activado.setValor(unirMotivo(activado.getValor(), motivo.trim()));
            actualizar(activado);
        }
        return activado;
    }

    private static String unirMotivo(String valorActual, String motivo) {
        String valor = valorActual == null ? "" : valorActual.trim();
        return valor.isEmpty() ? motivo : valor + "\n" + motivo;
    }

    public void validarActor(ConsultaProcedimientoPaso paso, UUID idActor) {
        if (idActor == null || paso == null || paso.getIdProcedimientoPaso() == null
                || paso.getIdProcedimientoPaso().getIdRol() == null) {
            return;
        }
        UUID idRolPaso = paso.getIdProcedimientoPaso().getIdRol().getIdRol();
        UUID idRolActor = getEntityManager().createQuery(JPQL_ROL_ACTOR, UUID.class)
                .setParameter("id", idActor)
                .getResultList().stream().findFirst().orElse(null);
        if (idRolActor == null) {
            throw new IllegalStateException("El rol seleccionado no es válido");
        }
        if (!Objects.equals(idRolPaso, idRolActor)) {
            throw new IllegalStateException("El rol seleccionado no corresponde al paso actual");
        }
    }

    public UUID idRolDe(ConsultaProcedimientoPaso paso) {
        return paso == null ? null : idRolDe(paso.getIdProcedimientoPaso());
    }

    public UUID idRolDe(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdRol() == null) {
            return null;
        }
        return paso.getIdRol().getIdRol();
    }

    public String nombreRolDe(ProcedimientoPaso paso) {
        if (paso == null || paso.getIdRol() == null) {
            return null;
        }
        return paso.getIdRol().getNombre();
    }

    public boolean esPasoInicial(ConsultaProcedimientoPaso paso) {
        return paso == null || paso.getIdProcedimientoPaso() == null
                || anteriorDe(paso.getIdProcedimientoPaso()) == null;
    }

    public boolean debeSalirTrasAvanzar(ConsultaProcedimientoPaso paso, UUID idActor) {
        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return false;
        }
        UUID idRolDestino = idRolDe(paso);
        UUID idRolReferencia = idActor == null
                ? idRolDe(anteriorDe(paso.getIdProcedimientoPaso()))
                : idRolActor(idActor);
        return idRolDestino != null && idRolReferencia != null
                && !Objects.equals(idRolDestino, idRolReferencia);
    }

    private UUID idRolActor(UUID idActor) {
        return getEntityManager().createQuery(JPQL_ROL_ACTOR, UUID.class)
                .setParameter("id", idActor)
                .getResultList().stream().findFirst().orElse(null);
    }

    private ConsultaProcedimientoPaso pasoPorId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("El id del paso es obligatorio");
        }
        ConsultaProcedimientoPaso paso = buscar(id);
        if (paso == null) {
            throw new IllegalArgumentException("No existe el paso indicado");
        }
        return paso;
    }

    public ProcedimientoPaso siguienteDe(ProcedimientoPaso actual) {
        if (actual == null || actual.getIdProcedimientoPaso() == null) {
            return null;
        }
        if (Boolean.TRUE.equals(actual.getIndicaFin())) {
            return null;
        }
        List<ProcedimientoPasoSecuencia> secuencias = getEntityManager()
                .createQuery(JPQL_SIGUIENTES, ProcedimientoPasoSecuencia.class)
                .setParameter("id", actual.getIdProcedimientoPaso())
                .getResultList();
        return secuencias.stream()
                .filter(s -> !"FIN".equalsIgnoreCase(tipoDe(s)))
                .sorted(Comparator.comparingInt(s -> "SIGUIENTE".equalsIgnoreCase(tipoDe(s)) ? 0 : 1))
                .map(ProcedimientoPasoSecuencia::getIdProcedimientoPaso)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public ProcedimientoPaso anteriorDe(ProcedimientoPaso actual) {
        if (actual == null || actual.getIdProcedimientoPaso() == null) {
            return null;
        }
        List<ProcedimientoPasoSecuencia> secuencias = getEntityManager()
                .createQuery(JPQL_ANTERIORES, ProcedimientoPasoSecuencia.class)
                .setParameter("id", actual.getIdProcedimientoPaso())
                .getResultList();
        UUID idAnterior = secuencias.stream()
                .sorted(Comparator.comparingInt(s -> "ANTERIOR".equalsIgnoreCase(tipoDe(s)) ? 0 : 1))
                .map(ProcedimientoPasoSecuencia::getIdProcedimientoPasoReferencia)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return idAnterior == null ? null : getEntityManager().find(ProcedimientoPaso.class, idAnterior);
    }

    private static String tipoDe(ProcedimientoPasoSecuencia secuencia) {
        return secuencia.getTipoSecuencia() == null ? "" : secuencia.getTipoSecuencia().trim();
    }

    private ConsultaProcedimientoPaso activar(ProcedimientoPaso definicion, ConsultaProcedimiento cp) {
        if (cp == null || cp.getIdConsultaProcedimiento() == null || definicion.getIdProcedimientoPaso() == null) {
            throw new IllegalStateException("No se puede activar el paso porque falta el procedimiento de la consulta");
        }
        ConsultaProcedimientoPaso existente = getEntityManager()
                .createQuery(JPQL_PASO_DE_CONSULTA, ConsultaProcedimientoPaso.class)
                .setParameter("idConsulta", cp.getIdConsultaProcedimiento())
                .setParameter("idPaso", definicion.getIdProcedimientoPaso())
                .getResultList().stream().findFirst().orElse(null);
        if (existente != null) {
            existente.setEstado("EN_PROCESO");
            existente.setFechaInicio(new Date());
            existente.setFechaFin(null);
            return actualizar(existente);
        }
        ConsultaProcedimientoPaso nuevo = new ConsultaProcedimientoPaso(UUID.randomUUID());
        nuevo.setIdConsultaProcedimiento(cp);
        nuevo.setIdProcedimientoPaso(definicion);
        nuevo.setEstado("EN_PROCESO");
        nuevo.setFechaInicio(new Date());
        crear(nuevo);
        return nuevo;
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

    @Override
    public ConsultaProcedimientoPaso actualizar(ConsultaProcedimientoPaso registro) {
        if (registro == null || registro.getIdConsultaProcedimientoPaso() == null) {
            return super.actualizar(registro);
        }
        ConsultaProcedimientoPaso anterior = buscar(registro.getIdConsultaProcedimientoPaso());
        if (anterior != null) {
            if (registro.getIdPersonaRol() == null) {
                registro.setIdPersonaRol(anterior.getIdPersonaRol());
            } else if (anterior.getIdPersonaRol() != null
                    && !Objects.equals(anterior.getIdPersonaRol().getIdPersonaRol(),
                            registro.getIdPersonaRol().getIdPersonaRol())) {
                throw new IllegalStateException("No se puede cambiar el responsable del paso");
            }
        }
        return super.actualizar(registro);
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
