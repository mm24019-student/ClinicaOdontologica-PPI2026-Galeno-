package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("persona")
@RequestScoped
public class PersonaResource extends AbstractResource<Persona> {

    @Inject
    PersonaDAO dao;

    public PersonaResource() {
        super(Persona.class);
    }

    @Override
    protected DefaultDAO<Persona> dao() {
        return dao;
    }
}