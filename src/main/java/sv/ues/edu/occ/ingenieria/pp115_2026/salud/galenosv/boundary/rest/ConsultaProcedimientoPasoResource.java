
package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.*;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.*;

@Path("consultaprocedimientopaso")
@RequestScoped
public class ConsultaProcedimientoPasoResource extends AbstractResource<ConsultaProcedimientoPaso> {

    @Inject
    ConsultaProcedimientoPasoDAO dao;

    public ConsultaProcedimientoPasoResource() {
        super(ConsultaProcedimientoPaso.class);
    }

    @Override
    protected DefaultDAO<ConsultaProcedimientoPaso> dao() {
        return dao;
    }
}
