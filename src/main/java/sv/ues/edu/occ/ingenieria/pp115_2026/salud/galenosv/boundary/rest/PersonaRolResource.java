package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("personarol")
@RequestScoped
public class PersonaRolResource extends AbstractResource<PersonaRol> {

    @Inject
    PersonaRolDAO dao;

    public PersonaRolResource() {
        super(PersonaRol.class);
    }

    @Override
    protected DefaultDAO<PersonaRol> dao() {
        return dao;
    }
}