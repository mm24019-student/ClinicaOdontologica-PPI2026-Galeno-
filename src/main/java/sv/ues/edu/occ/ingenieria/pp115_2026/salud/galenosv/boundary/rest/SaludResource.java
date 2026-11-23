package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("salud")
@RequestScoped
public class SaludResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response revisar() {
        return Response.ok(Map.of("estado", "ok"), MediaType.APPLICATION_JSON).build();
    }
}
