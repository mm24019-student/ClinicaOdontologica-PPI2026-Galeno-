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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Path("ordenes-examen")
public class OrdenExamenResource {

    private static final String HEADER_ERROR = "error-mensaje";

    @Inject
    private OrdenExamenDAO dao;

    private Map<String, Object> aMapa(OrdenExamen o) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getIdOrdenExamen());
        m.put("fechaCreacion", o.getFechaCreacion());
        m.put("indicaciones", o.getIndicaciones());
        if (o.getIdConsultaProcedimientoPaso() != null) {
            m.put("consultaProcedimientoPasoId", o.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso());
            m.put("consultaProcedimientoPasoEstado", o.getIdConsultaProcedimientoPaso().getEstado());
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
            List<OrdenExamen> lista = dao.findRangeConRelaciones(first, max);
            List<Map<String, Object>> salida = new ArrayList<>();
            for (OrdenExamen o : lista) {
                salida.add(aMapa(o));
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
            OrdenExamen encontrado = dao.buscarConRelaciones(id);
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
    public Response crear(OrdenExamen entrada, @Context UriInfo uriInfo) {
        if (entrada == null || entrada.getIdConsultaProcedimientoPaso() == null
                || entrada.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso() == null) {
            return Response.status(422).header(HEADER_ERROR, "Se requiere idConsultaProcedimientoPaso.idConsultaProcedimientoPaso").build();
        }
        try {
            if (entrada.getIdOrdenExamen() == null) {
                entrada.setIdOrdenExamen(UUID.randomUUID());
            }
            entrada.setIdConsultaProcedimientoPaso(
                    dao.referenciaConsultaProcedimientoPaso(entrada.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso()));
            dao.crear(entrada);
            URI ubicacion = uriInfo.getAbsolutePathBuilder().path(entrada.getIdOrdenExamen().toString()).build();
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
    public Response actualizar(@PathParam("id") UUID id, OrdenExamen entrada) {
        if (entrada == null || entrada.getIdConsultaProcedimientoPaso() == null
                || entrada.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso() == null) {
            return Response.status(422).header(HEADER_ERROR, "Se requiere idConsultaProcedimientoPaso.idConsultaProcedimientoPaso").build();
        }
        try {
            OrdenExamen existente = dao.buscar(id);
            if (existente == null) {
                return Response.status(404).header(HEADER_ERROR, "No existe ese registro").build();
            }
            entrada.setIdOrdenExamen(id);
            entrada.setIdConsultaProcedimientoPaso(
                    dao.referenciaConsultaProcedimientoPaso(entrada.getIdConsultaProcedimientoPaso().getIdConsultaProcedimientoPaso()));
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
            OrdenExamen existente = dao.buscar(id);
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