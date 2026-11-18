
package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consultaprocedimientopaso")
@RequestScoped
public class ConsultaProcedimientoPasoResource extends AbstractResource<ConsultaProcedimientoPaso> {

    @Inject
    ConsultaProcedimientoPasoService dao;

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

    @POST
    @Path("{id}/avanzar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response avanzar(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            return Response.ok(dao.avanzar(uuid), MediaType.APPLICATION_JSON).build();
        } catch (IllegalStateException ex) {
            return ex.getCause() == null ? conflicto(ex) : manejar(ex, "id");
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    @POST
    @Path("{id}/retroceder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retroceder(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            return Response.ok(dao.retroceder(uuid), MediaType.APPLICATION_JSON).build();
        } catch (IllegalStateException ex) {
            return ex.getCause() == null ? conflicto(ex) : manejar(ex, "id");
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
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
