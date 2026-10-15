package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

/**
 *
 * @author antonio
 */
@Path("tipoexamen")
@RequestScoped
public class TipoExamenResource extends AbstractResource<TipoExamen>{
    
       @Inject
    TipoExamenDAO dao;

    public TipoExamenResource() {
        super(TipoExamen.class);
    }

    @Override
    protected DefaultDAO<TipoExamen> dao() {
        return dao;
    }
    
}
