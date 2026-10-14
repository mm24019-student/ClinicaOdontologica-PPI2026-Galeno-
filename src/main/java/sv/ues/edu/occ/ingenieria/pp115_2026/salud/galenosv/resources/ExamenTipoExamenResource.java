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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;

@Path("examenes-tipo-examen")
public class ExamenTipoExamenResource {

    private static final String HEADER_ERROR = "error-mensaje";

    @Inject
    private ExamenTipoExamenDAO dao;

    private Map<String, Object> aMapa(ExamenTipoExamen e) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", e.getIdExamenTipoExamen());
        m.put("fechaCreacion", e.getFechaCreacion());
        m.put("observaciones", e.getObservaciones());
        if (e.getIdExamen() != null) {
            m.put("examenId", e.getIdExamen().getIdExamen());
            m.put("examenNombre", e.getIdExamen().getNombre());
        }
        if (e.getIdTipoExamen() != null) {
            m.put("tipoExamenId", e.getIdTipoExamen().getIdTipoExamen());
            m.put("tipoExamenNombre", e.getIdTipoExamen().getNombre());
        }
        return m;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listar(@QueryParam("first") @DefaultValue("0") int first,
                            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0 || max > 100) {
            return Response.status(422).header(HEADER_ERROR, "first debe ser >= 0 y max entre 1 y 100").build();
        }
        try {
            List<ExamenTipoExamen> lista = dao.findRangeConRelaciones(first, max);
            List<Map<String, Object>> salida = new ArrayList<>();
            for (ExamenTipoExamen e : lista) {
                salida.add(aMapa(e));
            }
            return Response.ok(salida).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @GET
    @Path("contar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response contar() {
        try {
            return Response.ok(dao.contar()).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(@PathParam("id") UUID id) {
        try {
            ExamenTipoExamen encontrado = dao.buscarConRelaciones(id);
            if (encontrado == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe ese registro").build();
            }
            return Response.ok(aMapa(encontrado)).build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response crear(ExamenTipoExamen entrada, @Context UriInfo uriInfo) {
        if (entrada == null || entrada.getIdExamen() == null || entrada.getIdExamen().getIdExamen() == null
                || entrada.getIdTipoExamen() == null || entrada.getIdTipoExamen().getIdTipoExamen() == null) {
            return Response.status(422)
                    .header(HEADER_ERROR, "Se requiere idExamen.idExamen e idTipoExamen.idTipoExamen")
                    .build();
        }
        try {
            if (entrada.getIdExamenTipoExamen() == null) {
                entrada.setIdExamenTipoExamen(UUID.randomUUID());
            }
            entrada.setIdExamen(dao.referenciaExamen(entrada.getIdExamen().getIdExamen()));
            entrada.setIdTipoExamen(dao.referenciaTipoExamen(entrada.getIdTipoExamen().getIdTipoExamen()));
            dao.crear(entrada);
            URI ubicacion = uriInfo.getAbsolutePathBuilder().path(entrada.getIdExamenTipoExamen().toString()).build();
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
    public Response actualizar(@PathParam("id") UUID id, ExamenTipoExamen entrada) {
        if (entrada == null || entrada.getIdExamen() == null || entrada.getIdExamen().getIdExamen() == null
                || entrada.getIdTipoExamen() == null || entrada.getIdTipoExamen().getIdTipoExamen() == null) {
            return Response.status(422)
                    .header(HEADER_ERROR, "Se requiere idExamen.idExamen e idTipoExamen.idTipoExamen")
                    .build();
        }
        try {
            ExamenTipoExamen existente = dao.buscar(id);
            if (existente == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe ese registro").build();
            }
            entrada.setIdExamenTipoExamen(id);
            entrada.setIdExamen(dao.referenciaExamen(entrada.getIdExamen().getIdExamen()));
            entrada.setIdTipoExamen(dao.referenciaTipoExamen(entrada.getIdTipoExamen().getIdTipoExamen()));
            dao.actualizar(entrada);
            return Response.noContent().build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }

    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") UUID id) {
        try {
            ExamenTipoExamen existente = dao.buscar(id);
            if (existente == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe ese registro").build();
            }
            dao.eliminar(id);
            return Response.noContent().build();
        } catch (Exception ex) {
            return Response.status(500).header(HEADER_ERROR, ex.getMessage()).build();
        }
    }
}