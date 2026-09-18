package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

@Named
@ViewScoped
public class TipoExamenModel extends AbstracCrudModel<TipoExamen> {

    @Inject
    private TipoExamenDAO teDAO;

    @Override
    protected InterfaceDAO<TipoExamen> getDAO() {
        return teDAO;
    }

    @Override
    protected TipoExamen crearRegistroNuevo() {
        TipoExamen t = new TipoExamen(UUID.randomUUID());
        t.setActivo(Boolean.TRUE);
        return t;
    }

    @Override
    protected UUID obtenerId(TipoExamen registro) {
        return registro.getIdTipoExamen();
    }
}