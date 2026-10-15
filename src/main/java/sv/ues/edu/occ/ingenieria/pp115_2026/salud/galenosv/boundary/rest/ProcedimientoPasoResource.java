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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@Path("procedimientopaso")
@RequestScoped
public class ProcedimientoPasoResource extends AbstractResource<ProcedimientoPaso> {
    
 
    @Inject
    ProcedimientoPasoDAO dao;

    public ProcedimientoPasoResource() {
        super(ProcedimientoPaso.class);
    }

    @Override
    protected DefaultDAO<ProcedimientoPaso> dao() {
        return dao;
    }

    @GET
    @Path("procedimiento/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porProcedimiento(@PathParam("id") String id) {
        return listaPor(id, dao::findByProcedimiento);
    }
    
}
