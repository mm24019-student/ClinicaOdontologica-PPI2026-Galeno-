
package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consultaprocedimientopaso")
@RequestScoped
public class ConsultaProcedimientoPasoResource extends AbstractResource<ConsultaProcedimientoPaso> {

    @Inject
    ConsultaProcedimientoPasoService dao;

    @HeaderParam("X-Id-Persona-Rol")
    private String idActorHeader;

    public ConsultaProcedimientoPasoResource() {
        super(ConsultaProcedimientoPaso.class);
    }

    @Override
    protected DefaultService<ConsultaProcedimientoPaso> dao() {
        return dao;
    }

    @GET
    @Path("consultaprocedimiento/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porConsultaProcedimiento(@PathParam("id") String id) {
        return listaPor(id, dao::findByConsultaProcedimiento);
    }

    public Response avanzar(@PathParam("id") String id) {
        return avanzar(id, null);
    }

    @POST
    @Path("{id}/avanzar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response avanzar(@PathParam("id") String id,
            @HeaderParam("X-Id-Persona-Rol") String idPersonaRol) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        UUID actor = uuidOpcional(idPersonaRol);
        try {
            ConsultaProcedimientoPaso resultado = actor == null
                    ? dao.avanzar(uuid) : dao.avanzar(uuid, actor);
            return respuestaPaso(resultado, actor);
        } catch (IllegalStateException ex) {
            return ex.getCause() == null ? conflicto(ex) : manejar(ex, "id");
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    public Response retroceder(@PathParam("id") String id) {
        return retroceder(id, null, null);
    }

    @POST
    @Path("{id}/retroceder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retroceder(@PathParam("id") String id,
            @HeaderParam("X-Id-Persona-Rol") String idPersonaRol,
            @QueryParam("motivo") String motivo) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        UUID actor = uuidOpcional(idPersonaRol);
        try {
            ConsultaProcedimientoPaso resultado = actor == null
                    ? dao.retroceder(uuid, null, motivo) : dao.retroceder(uuid, actor, motivo);
            return respuestaPaso(resultado, actor);
        } catch (IllegalStateException ex) {
            return ex.getCause() == null ? conflicto(ex) : manejar(ex, "id");
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    @Override
    protected String validarNegocio(ConsultaProcedimientoPaso registro, boolean esNuevo) {
        if (!esNuevo && registro != null) {
            UUID actor = uuidOpcional(idActorHeader);
            if (actor != null) {
                try {
                    dao.validarActor(registro, actor);
                } catch (RuntimeException ex) {
                    return "idPersonaRol";
                }
            }
        }
        return null;
    }

    private Response respuestaPaso(ConsultaProcedimientoPaso paso, UUID idActor) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("idConsultaProcedimientoPaso", String.valueOf(paso.getIdConsultaProcedimientoPaso()));
        datos.put("estado", paso.getEstado());
        datos.put("valor", paso.getValor());
        boolean esPrimero = dao.esPasoInicial(paso);
        datos.put("esPrimero", esPrimero);
        datos.put("estadoMostrado", paso.getEstado());
        datos.put("salirAtencion", dao.debeSalirTrasAvanzar(paso, idActor));
        ProcedimientoPaso destino = paso.getIdProcedimientoPaso();
        datos.put("idRolSiguiente", destino == null || destino.getIdRol() == null
                ? null : String.valueOf(destino.getIdRol().getIdRol()));
        datos.put("nombreRolSiguiente", dao.nombreRolDe(destino));
        return Response.ok(datos, MediaType.APPLICATION_JSON).build();
    }

    private static Response conflicto(IllegalStateException ex) {
        String mensaje = ex.getMessage() == null ? "No se puede cambiar de paso" : ex.getMessage();
        return Response.status(Response.Status.CONFLICT)
                .header("error-mensaje", mensaje)
                .type("text/plain; charset=UTF-8")
                .entity(mensaje)
                .build();
    }
}
