
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

@Path("consultaprocedimientopaso")
@RequestScoped
public class ConsultaProcedimientoPasoResource extends AbstractResource<ConsultaProcedimientoPaso> {

    @Inject
    ConsultaProcedimientoPasoDAO dao;

    public ConsultaProcedimientoPasoResource() {
        super(ConsultaProcedimientoPaso.class);
    }

    @Override
    protected DefaultDAO<ConsultaProcedimientoPaso> dao() {
        return dao;
    }

    @GET
    @Path("consultaprocedimiento/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porConsultaProcedimiento(@PathParam("id") String id) {
        return listaPor(id, dao::findByConsultaProcedimiento);
    }
}
