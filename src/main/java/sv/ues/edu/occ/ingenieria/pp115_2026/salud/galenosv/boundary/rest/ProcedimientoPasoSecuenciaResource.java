package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@Path("procedimientopasosecuencia")
@RequestScoped
public class ProcedimientoPasoSecuenciaResource extends AbstractResource<ProcedimientoPasoSecuencia> {
    
     @Inject
    ProcedimientoPasoSecuenciaDAO dao;

    public ProcedimientoPasoSecuenciaResource() {
        super(ProcedimientoPasoSecuencia.class);
    }

    @Override
    protected DefaultDAO<ProcedimientoPasoSecuencia> dao() {
        return dao;
    }

    // idProcedimientoPasoReferencia es un UUID suelto (sin @ManyToOne): se comprueba que exista
    @Override
    protected String validarNegocio(ProcedimientoPasoSecuencia r, boolean esNuevo) {
        if (r.getIdProcedimientoPasoReferencia() != null
                && em.find(ProcedimientoPaso.class, r.getIdProcedimientoPasoReferencia()) == null) {
            return "idProcedimientoPasoReferencia";
        }
        return null;
    }

    @GET
    @Path("procedimientopaso/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porProcedimientoPaso(@PathParam("id") String id) {
        return listaPor(id, dao::findByProcedimientoPaso);
    }

    @GET
    @Path("procedimiento/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response porProcedimiento(@PathParam("id") String id) {
        return listaPor(id, dao::findByProcedimiento);
    }
    
}
