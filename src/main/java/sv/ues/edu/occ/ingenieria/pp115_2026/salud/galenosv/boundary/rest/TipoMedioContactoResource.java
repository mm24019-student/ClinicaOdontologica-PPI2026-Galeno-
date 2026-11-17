package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("tipomediocontacto")
@RequestScoped
public class TipoMedioContactoResource extends AbstractResource<TipoMedioContacto> {

    @Inject
    TipoMedioContactoService dao;

    public TipoMedioContactoResource() {
        super(TipoMedioContacto.class);
    }

    @Override
    protected DefaultService<TipoMedioContacto> dao() {
        return dao;
    }
}