package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author antonio
 */
@Path("tipoexamen")
@RequestScoped
public class TipoExamenResource extends AbstractResource<TipoExamen>{
    
       @Inject
    TipoExamenService dao;

    public TipoExamenResource() {
        super(TipoExamen.class);
    }

    @Override
    protected DefaultService<TipoExamen> dao() {
        return dao;
    }
    
}
