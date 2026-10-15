package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
@Path("procedimiento")
@RequestScoped
public class ProcedimientoResource extends AbstractResource<Procedimiento> {
    
    
     @Inject
    ProcedimientoDAO dao;

    public ProcedimientoResource() {
        super(Procedimiento.class);
    }

    @Override
    protected DefaultDAO<Procedimiento> dao() {
        return dao;
    }
    
}
