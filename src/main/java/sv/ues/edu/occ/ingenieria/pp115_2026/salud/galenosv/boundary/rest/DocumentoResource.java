package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("documento")
@RequestScoped
public class DocumentoResource extends AbstractResource<Documento> {

    @Inject
    DocumentoService dao;

    public DocumentoResource() {
        super(Documento.class);
    }

    @Override
    protected DefaultService<Documento> dao() {
        return dao;
    }

    @Override
    protected String validarNegocio(Documento r, boolean esNuevo) {
        if (r.getIdPersona() == null) {
            return "idPersona";
        }
        if (r.getIdTipoDocumento() == null) {
            return "idTipoDocumento";
        }
        UUID persona = r.getIdPersona().getIdPersona();
        UUID tipo = r.getIdTipoDocumento().getIdTipoDocumento();
        if (dao.existeTipoParaPersona(persona, tipo, r.getIdDocumento())) {
            return "idTipoDocumento";
        }
        if (dao.existeValorParaTipo(r.getValor(), tipo, r.getIdDocumento())) {
            return "valor";
        }
        return null;
    }

    @GET
    @Path("buscar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(@QueryParam("valor") String valor,
            @QueryParam("idTipoDocumento") String idTipoDocumento) {
        if (valor == null || valor.isBlank()) {
            return Response.ok("[]", MediaType.APPLICATION_JSON).build();
        }
        try {
            UUID tipo = uuidOpcional(idTipoDocumento);
            return ok(dao.buscarPorValor(valor, tipo));
        } catch (Exception ex) {
            return manejar(ex, "valor,idTipoDocumento");
        }
    }

    @GET
    @Path("persona/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porPersona(@PathParam("id") String id) {
        return listaPor(id, dao::findByPersona);
    }
}