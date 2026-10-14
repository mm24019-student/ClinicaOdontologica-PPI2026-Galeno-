package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consulta")
@RequestScoped
public class ConsultaResource extends AbstractResource<Consulta> {

    @Inject
    ConsultaDAO dao;

    public ConsultaResource() {
        super(Consulta.class);
    }

    @Override
    protected DefaultDAO<Consulta> dao() {
        return dao;
    }
}