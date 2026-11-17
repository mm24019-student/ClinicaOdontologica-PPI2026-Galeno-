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

@Path("consultaprocedimiento")
@RequestScoped
public class ConsultaProcedimientoResource extends AbstractResource<ConsultaProcedimiento> {

    @Inject
    ConsultaProcedimientoService dao;

    public ConsultaProcedimientoResource() {
        super(ConsultaProcedimiento.class);
    }

    @Override
    protected DefaultService<ConsultaProcedimiento> dao() {
        return dao;
    }

    @GET
    @Path("consulta/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porConsulta(@PathParam("id") String id) {
        return listaPor(id, dao::findByConsulta);
    }
}