package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("tipodocumento")
@RequestScoped
public class TipoDocumentoResource extends AbstractResource<TipoDocumento> {

    @Inject
    TipoDocumentoDAO dao;

    public TipoDocumentoResource() {
        super(TipoDocumento.class);
    }

    @Override
    protected DefaultDAO<TipoDocumento> dao() {
        return dao;
    }

    @Override
    protected String validarNegocio(TipoDocumento r, boolean esNuevo) {
        return dao.existeNombre(r.getNombre(), r.getIdTipoDocumento()) ? "nombre" : null;
    }
}