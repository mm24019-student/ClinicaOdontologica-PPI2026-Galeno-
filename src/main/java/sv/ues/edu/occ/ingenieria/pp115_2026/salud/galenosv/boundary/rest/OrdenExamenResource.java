package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("ordenexamen")
@RequestScoped
public class OrdenExamenResource extends AbstractResource<OrdenExamen> {

    @Inject
    OrdenExamenDAO dao;

    public OrdenExamenResource() {
        super(OrdenExamen.class);
    }

    @Override
    protected DefaultDAO<OrdenExamen> dao() {
        return dao;
    }

    @GET
    @Path("consultaprocedimientopaso/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porPaso(@PathParam("id") String id) {
        return listaPor(id, dao::findByConsultaProcedimientoPaso);
    }
}