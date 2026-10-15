package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

/**
 *
 * @author antonio
 */
@Path("examen")
@RequestScoped
public class ExamenResource extends AbstractResource<Examen>{
    
    
    @Inject
    ExamenDAO dao;

    public ExamenResource() {
        super(Examen.class);
    }

    @Override
    protected DefaultDAO<Examen> dao() {
        return dao;
    }
    
}
