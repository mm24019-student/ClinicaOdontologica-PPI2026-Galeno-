package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consultaprocedimiento")
@RequestScoped
public class ConsultaProcedimientoResource extends AbstractResource<ConsultaProcedimiento> {

    @Inject
    ConsultaProcedimientoDAO dao;

    public ConsultaProcedimientoResource() {
        super(ConsultaProcedimiento.class);
    }

    @Override
    protected DefaultDAO<ConsultaProcedimiento> dao() {
        return dao;
    }
}