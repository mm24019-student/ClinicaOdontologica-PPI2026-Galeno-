package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("rol")
@RequestScoped
public class RolResource extends AbstractResource<Rol> {

    @Inject
    RolDAO dao;

    public RolResource() {
        super(Rol.class);
    }

    @Override
    protected DefaultDAO<Rol> dao() {
        return dao;
    }
}