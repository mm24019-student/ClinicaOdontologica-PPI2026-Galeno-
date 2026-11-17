package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

@Path("examenes")
public class ExamenResource {

    private static final String HEADER_ERROR = "error-mensaje";

    @Inject
    private ExamenService examenService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(@QueryParam("first") @DefaultValue("0") int first,
                            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0 || max > 100) {
            return Response.status(422)
                    .header(HEADER_ERROR, "first debe ser >= 0 y max entre 1 y 100")
                    .build();
        }
        try {
            List<Examen> lista = examenService.findRange(first, max);
            return Response.ok(lista).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @GET
    @Path("contar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contar() {
        try {
            return Response.ok(examenService.contar()).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(@PathParam("id") UUID id) {
        try {
            Examen encontrado = examenService.buscar(id);
            if (encontrado == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe un examen con ese id").build();
            }
            return Response.ok(encontrado).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(Examen examen, @Context UriInfo uriInfo) {
        if (examen == null) {
            return Response.status(422).header(HEADER_ERROR, "El examen no puede venir vacio").build();
        }
        try {
            if (examen.getIdExamen() == null) {
                examen.setIdExamen(UUID.randomUUID());
            }
            examenService.crear(examen);
            URI ubicacion = uriInfo.getAbsolutePathBuilder()
                    .path(examen.getIdExamen().toString())
                    .build();
            return Response.created(ubicacion).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(422).header(HEADER_ERROR, ex.getMessage()).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") UUID id, Examen examen) {
        if (examen == null) {
            return Response.status(422).header(HEADER_ERROR, "El examen no puede venir vacio").build();
        }
        try {
            Examen existente = examenService.buscar(id);
            if (existente == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe un examen con ese id").build();
            }
            examen.setIdExamen(id);
            examenService.actualizar(examen);
            return Response.noContent().build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        try {
            Examen existente = examenService.buscar(id);
            if (existente == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe un examen con ese id").build();
            }
            examenService.eliminar(id);
            return Response.noContent().build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }
}