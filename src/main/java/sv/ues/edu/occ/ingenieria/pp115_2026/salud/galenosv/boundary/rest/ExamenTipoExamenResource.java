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
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenTipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ExamenTipoExamen;

/**
 *
 * @author antonio
 */
@Path("examentipoexamen")
@RequestScoped
public class ExamenTipoExamenResource extends AbstractResource<ExamenTipoExamen>{
 
    
    @Inject
    ExamenTipoExamenDAO dao;

    public ExamenTipoExamenResource() {
        super(ExamenTipoExamen.class);
    }

    @Override
    protected DefaultDAO<ExamenTipoExamen> dao() {
        return dao;
    }

    @GET
    @Path("examen/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porExamen(@PathParam("id") String id) {
        return listaPor(id, dao::findByExamen);
    }
    
}
