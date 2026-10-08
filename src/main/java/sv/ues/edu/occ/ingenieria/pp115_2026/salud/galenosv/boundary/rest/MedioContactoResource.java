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

@Path("mediocontacto")
@RequestScoped
public class MedioContactoResource extends AbstractResource<MedioContacto> {

    @Inject
    MedioContactoDAO dao;

    public MedioContactoResource() {
        super(MedioContacto.class);
    }

    @Override
    protected DefaultDAO<MedioContacto> dao() {
        return dao;
    }

    @Override
    protected String validarNegocio(MedioContacto r, boolean esNuevo) {
        if (r.getIdPersona() == null) {
            return "idPersona";
        }
        if (r.getIdTipoMedioContacto() == null) {
            return "idTipoMedioContacto";
        }
        return dao.existeMedioParaPersona(r.getIdPersona().getIdPersona(),
                r.getIdTipoMedioContacto().getIdTipoMedioContacto(),
                r.getValor(), r.getIdMedioContacto()) ? "valor" : null;
    }

    @GET
    @Path("persona/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porPersona(@PathParam("id") String id) {
        return listaPor(id, dao::findByPersona);
    }
}