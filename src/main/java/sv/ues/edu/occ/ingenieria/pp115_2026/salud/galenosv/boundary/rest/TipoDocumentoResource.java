package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

@Path("tipodocumento")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TipoDocumentoResource {

    @Inject
    TipoDocumentoDAO tipoDocumentoDAO;

    @Inject
    Validator validator;

    @Context
    UriInfo uriInfo;

    // GET /resources/tipodocumento?first=0&max=50
    @GET
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        try {
            List<TipoDocumento> lista = tipoDocumentoDAO.findRange(first, max);
            // se arma el JSON a mano para no serializar documentoList (ciclo con Documento)
            JsonArrayBuilder arreglo = Json.createArrayBuilder();
            for (TipoDocumento t : lista) {
                arreglo.add(Json.createObjectBuilder()
                        .add("idTipoDocumento", t.getIdTipoDocumento().toString())
                        .add("nombre", t.getNombre() != null ? t.getNombre() : "")
                        .add("indicaciones", t.getIndicaciones() != null ? t.getIndicaciones() : "")
                        .add("expresionRegular", t.getExpresionRegular() != null ? t.getExpresionRegular() : "")
                        .add("activo", Boolean.TRUE.equals(t.getActivo())));
            }
            return Response.ok(arreglo.build()).build();                            // 200
        } catch (IllegalArgumentException ex) {
            return Response.status(422)
                    .header("Wrong-Parameter", "first,max")
                    .build();                                                        // 422
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();  // 500
        }
    }

    // POST /resources/tipodocumento
    @POST
    public Response crear(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            return Response.status(422)
                    .header("Wrong-Parameter", "tipoDocumento")
                    .build();
        }
        try {
            if (tipoDocumento.getIdTipoDocumento() == null) {
                tipoDocumento.setIdTipoDocumento(UUID.randomUUID());
            }
            if (tipoDocumento.getActivo() == null) {
                tipoDocumento.setActivo(Boolean.TRUE);
            }
            // validaciones de la entidad (@NotBlank, @Size)
            Set<ConstraintViolation<TipoDocumento>> errores = validator.validate(tipoDocumento);
            if (!errores.isEmpty()) {
                String campo = errores.iterator().next().getPropertyPath().toString();
                return Response.status(422)
                        .header("Wrong-Parameter", campo)
                        .build();
            }
            // nombre duplicado
            if (tipoDocumentoDAO.existeNombre(tipoDocumento.getNombre(), null)) {
                return Response.status(422)
                        .header("Wrong-Parameter", "nombre")
                        .build();
            }
            tipoDocumentoDAO.crear(tipoDocumento);
            URI url = uriInfo.getAbsolutePathBuilder()
                    .path(tipoDocumento.getIdTipoDocumento().toString())
                    .build();
            return Response.created(url).build();                                    // 201
        } catch (IllegalArgumentException ex) {
            return Response.status(422)
                    .header("Wrong-Parameter", "tipoDocumento")
                    .build();
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();  // 500
        }
    }
}