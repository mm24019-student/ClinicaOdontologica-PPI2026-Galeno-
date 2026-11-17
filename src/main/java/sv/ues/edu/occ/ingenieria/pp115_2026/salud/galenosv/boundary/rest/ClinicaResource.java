package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("clinica")
@RequestScoped
public class ClinicaResource extends AbstractResource<Clinica> {

    @Inject
    ClinicaService dao;

    public ClinicaResource() {
        super(Clinica.class);
    }

    @Override
    protected DefaultService<Clinica> dao() {
        return dao;
    }
}
