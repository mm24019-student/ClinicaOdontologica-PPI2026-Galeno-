package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

@Named
@ApplicationScoped
public class TipoExamenConverter extends AbstractEntityConverter<TipoExamen> {

    @Inject
    private TipoExamenDAO dao;

    @Override
    protected InterfaceDAO<TipoExamen> dao() {
        return dao;
    }

    @Override
    protected UUID id(TipoExamen entidad) {
        return entidad.getIdTipoExamen();
    }
}