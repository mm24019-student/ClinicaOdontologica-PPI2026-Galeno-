package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author antonio
 */
@Path("procedimientopasoexamen")
@RequestScoped
public class ProcedimientoPasoExamenResource extends AbstractResource<ProcedimientoPasoExamen>{
   
    
    @Inject
    ProcedimientoPasoExamenDAO dao;

    public ProcedimientoPasoExamenResource() {
        super(ProcedimientoPasoExamen.class);
    }

    @Override
    protected DefaultDAO<ProcedimientoPasoExamen> dao() {
        return dao;
    }

    @GET
    @Path("procedimientopaso/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porProcedimientoPaso(@PathParam("id") String id) {
        return listaPor(id, dao::findByProcedimientoPaso);
    }
    
}
