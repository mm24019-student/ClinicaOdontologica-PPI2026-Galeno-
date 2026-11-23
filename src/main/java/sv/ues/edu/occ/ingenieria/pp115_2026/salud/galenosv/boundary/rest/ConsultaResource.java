package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.TypedQuery;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consulta")
@RequestScoped
public class ConsultaResource extends AbstractResource<Consulta> {

    @Inject
    ConsultaService dao;

    public ConsultaResource() {
        super(Consulta.class);
    }

    @Override
    protected DefaultService<Consulta> dao() {
        return dao;
    }

    // GET /resources/consulta/buscar
    //   ?tipoDocumento=<uuid>&valorDocumento=<texto>&idClinica=<uuid>
    //   &idPersona=<uuid>&idPersonaRol=<uuid>&idRol=<uuid>
    //   &desde=yyyy-MM-dd&hasta=yyyy-MM-dd&max=100
    // Cualquier filtro vacio simplemente no se aplica.
    public Response buscar(String tipoDocumento, String valorDocumento, String idClinica,
            String idPersona, String idPersonaRol, String idRol, String desde, String hasta, int max) {
        if (max <= 0) {
            return wrong("max");
        }
        try {
            UUID tipo = uuidOpcional(tipoDocumento);
            UUID clinica = uuidOpcional(idClinica);
            UUID persona = uuidOpcional(idPersona);
            UUID personaRol = uuidOpcional(idPersonaRol);
            UUID rol = uuidOpcional(idRol);
            Date inicio = inicioDelDia(desde);
            Date fin = inicioDelDiaSiguiente(hasta);
            List<Consulta> citas = dao.buscarCitas(tipo, valorDocumento, clinica, rol,
                    persona, personaRol, inicio, fin, max);
            return ok(citas);
        } catch (Exception ex) {
            return manejar(ex, "filtros");
        }
    }

    @GET
    @Path("buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(@QueryParam("tipoDocumento") String tipoDocumento,
            @QueryParam("valorDocumento") String valorDocumento,
            @QueryParam("idClinica") String idClinica,
            @QueryParam("idPersona") String idPersona,
            @QueryParam("idPersonaRol") String idPersonaRol,
            @QueryParam("idRol") String idRol,
            @QueryParam("desde") String desde,
            @QueryParam("hasta") String hasta,
            @QueryParam("max") @DefaultValue("100") int max,
            @QueryParam("ocultarTerminadas") @DefaultValue("false") boolean ocultarTerminadas,
            @QueryParam("soloPendientesParaRol") @DefaultValue("false") boolean soloPendientesParaRol,
            @HeaderParam("X-Id-Persona-Rol") String idActorPersonaRol) {
        if (max <= 0) {
            return wrong("max");
        }
        try {
            UUID tipo = uuidOpcional(tipoDocumento);
            UUID clinica = uuidOpcional(idClinica);
            UUID persona = uuidOpcional(idPersona);
            UUID personaRol = uuidOpcional(idPersonaRol);
            UUID rol = uuidOpcional(idRol);
            Date inicio = inicioDelDia(desde);
            Date fin = inicioDelDiaSiguiente(hasta);
            List<Consulta> citas = dao.buscarCitas(tipo, valorDocumento, clinica, rol,
                    persona, personaRol, inicio, fin, max,
                    ocultarTerminadas, soloPendientesParaRol, uuidOpcional(idActorPersonaRol));
            return ok(citas);
        } catch (Exception ex) {
            return manejar(ex, "filtros");
        }
    }

    // GET /resources/consulta/{id} -> cita con paciente, clinica y rol cargados
    @Override
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            Consulta consulta = cargarConsulta(uuid);
            return consulta == null
                    ? Response.status(Response.Status.NOT_FOUND).build()
                    : ok(consulta);
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    // GET /resources/consulta/{id}/ticket -> comprobante PDF de la cita
    @GET
    @Path("{id}/ticket")
    @Produces("application/pdf")
    public Response ticket(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            Consulta consulta = cargarConsulta(uuid);
            if (consulta == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            byte[] pdf = TicketPdf.generar(consulta, procedimientosDe(uuid));
            return Response.ok(pdf, "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"cita-" + uuid + ".pdf\"")
                    .build();
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    private Consulta cargarConsulta(UUID id) {
        TypedQuery<Consulta> q = em.createQuery(
                "SELECT DISTINCT c FROM Consulta c "
                + "LEFT JOIN FETCH c.idPersonaRol pr "
                + "LEFT JOIN FETCH pr.idPersona p "
                + "LEFT JOIN FETCH p.documentoList d "
                + "LEFT JOIN FETCH d.idTipoDocumento "
                + "LEFT JOIN FETCH pr.idRol "
                + "LEFT JOIN FETCH pr.idClinica "
                + "WHERE c.idConsulta = :id", Consulta.class);
        q.setParameter("id", id);
        List<Consulta> resultado = q.getResultList();
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    private List<TicketPdf.Procedimiento> procedimientosDe(UUID idConsulta) {
        List<ConsultaProcedimiento> procedimientos = em.createQuery(
                "SELECT cp FROM ConsultaProcedimiento cp "
                + "WHERE cp.idConsulta.idConsulta = :id ORDER BY cp.fechaInicio", ConsultaProcedimiento.class)
                .setParameter("id", idConsulta)
                .getResultList();
        List<TicketPdf.Procedimiento> salida = new ArrayList<>();
        for (ConsultaProcedimiento cp : procedimientos) {
            String nombre = cp.getIdProcedimiento() == null ? "Procedimiento"
                    : nombreDe(Procedimiento.class, cp.getIdProcedimiento());
            TicketPdf.Procedimiento item = new TicketPdf.Procedimiento(nombre, cp.getFechaInicio());
            List<ConsultaProcedimientoPaso> pasos = em.createQuery(
                    "SELECT p FROM ConsultaProcedimientoPaso p "
                    + "LEFT JOIN FETCH p.idProcedimientoPaso pp "
                    + "LEFT JOIN FETCH p.idPersonaRol pr "
                    + "LEFT JOIN FETCH pr.idPersona "
                    + "LEFT JOIN FETCH pr.idRol "
                    + "WHERE p.idConsultaProcedimiento.idConsultaProcedimiento = :id "
                    + "ORDER BY p.fechaInicio", ConsultaProcedimientoPaso.class)
                    .setParameter("id", cp.getIdConsultaProcedimiento())
                    .getResultList();
            for (ConsultaProcedimientoPaso paso : pasos) {
                item.agregarPaso(new TicketPdf.Paso(
                        paso.getIdProcedimientoPaso() == null ? "Paso"
                                : paso.getIdProcedimientoPaso().getNombre(),
                        paso.getEstado(),
                        paso.getValor(),
                        paso.getFechaInicio(),
                        paso.getIdPersonaRol() == null ? null
                                : paso.getIdPersonaRol().getIdPersona() == null ? null
                                : (texto(paso.getIdPersonaRol().getIdPersona().getNombres()) + " "
                                + texto(paso.getIdPersonaRol().getIdPersona().getApellidos())).trim(),
                        paso.getIdPersonaRol() == null || paso.getIdPersonaRol().getIdRol() == null
                                ? null : paso.getIdPersonaRol().getIdRol().getNombre()));
            }
            salida.add(item);
        }
        return salida;
    }

    private <T> String nombreDe(Class<T> clase, UUID id) {
        Object entidad = em.find(clase, id);
        try {
            Object nombre = clase.getMethod("getNombre").invoke(entidad);
            return nombre == null ? "Procedimiento" : nombre.toString();
        } catch (Exception ex) {
            return "Procedimiento";
        }
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    private static Date inicioDelDia(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            LocalDate dia = LocalDate.parse(texto.trim());
            return Date.from(dia.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha '" + texto.trim() + "' no tiene el formato yyyy-MM-dd", ex);
        }
    }

    private static Date inicioDelDiaSiguiente(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            LocalDate dia = LocalDate.parse(texto.trim()).plusDays(1);
            return Date.from(dia.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("La fecha '" + texto.trim() + "' no tiene el formato yyyy-MM-dd", ex);
        }
    }
}
