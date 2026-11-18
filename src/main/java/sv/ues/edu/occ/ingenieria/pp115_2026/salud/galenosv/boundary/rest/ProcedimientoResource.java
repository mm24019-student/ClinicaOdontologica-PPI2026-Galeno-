package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Path("procedimiento")
@RequestScoped
public class ProcedimientoResource extends AbstractResource<Procedimiento> {
    
    
      @Inject
    ProcedimientoService dao;

    @Inject
    ProcedimientoPasoService pasos;

    @Inject
    ProcedimientoPasoSecuenciaService secuencias;

    public ProcedimientoResource() {
        super(Procedimiento.class);
    }

    @Override
    protected DefaultService<Procedimiento> dao() {
        return dao;
    }

    @GET
    @Path("{id}/arbol")
    @Produces(MediaType.APPLICATION_JSON)
    public Response arbol(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            if (dao.buscar(uuid) == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            List<ProcedimientoPaso> todos = pasos.findByProcedimiento(uuid);
            List<ProcedimientoPasoSecuencia> relaciones = secuencias.findByProcedimiento(uuid);
            Map<UUID, ProcedimientoPaso> porId = new LinkedHashMap<>();
            for (ProcedimientoPaso paso : todos) {
                porId.put(paso.getIdProcedimientoPaso(), paso);
            }
            Set<UUID> conPadre = new HashSet<>();
            for (ProcedimientoPasoSecuencia relacion : relaciones) {
                if (relacion.getIdProcedimientoPasoReferencia() != null && relacion.getIdProcedimientoPaso() != null) {
                    conPadre.add(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso());
                }
            }
            List<Map<String, Object>> raices = new ArrayList<>();
            for (ProcedimientoPaso paso : todos) {
                if (!conPadre.contains(paso.getIdProcedimientoPaso())) {
                    raices.add(nodo(paso, porId, relaciones, new HashSet<>()));
                }
            }
            Map<String, Object> salida = new LinkedHashMap<>();
            salida.put("idProcedimiento", uuid);
            salida.put("pasos", raices);
            return Response.ok(salida, MediaType.APPLICATION_JSON).build();
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    private static Map<String, Object> nodo(ProcedimientoPaso paso, Map<UUID, ProcedimientoPaso> porId,
            List<ProcedimientoPasoSecuencia> relaciones, Set<UUID> visitados) {
        Map<String, Object> actual = new LinkedHashMap<>();
        actual.put("idPaso", paso.getIdProcedimientoPaso());
        actual.put("nombre", paso.getNombre());
        actual.put("indicaFin", paso.getIndicaFin());
        actual.put("idRol", paso.getIdRol() == null ? null : paso.getIdRol().getIdRol());
        List<Map<String, Object>> hijos = new ArrayList<>();
        if (paso.getIdProcedimientoPaso() != null && visitados.add(paso.getIdProcedimientoPaso())) {
            for (ProcedimientoPasoSecuencia relacion : relaciones) {
                if (!paso.getIdProcedimientoPaso().equals(relacion.getIdProcedimientoPasoReferencia())
                        || relacion.getIdProcedimientoPaso() == null) {
                    continue;
                }
                ProcedimientoPaso hijo = porId.get(relacion.getIdProcedimientoPaso().getIdProcedimientoPaso());
                if (hijo != null) {
                    hijos.add(nodo(hijo, porId, relaciones, visitados));
                }
            }
        }
        actual.put("hijos", hijos);
        return actual;
    }
}
