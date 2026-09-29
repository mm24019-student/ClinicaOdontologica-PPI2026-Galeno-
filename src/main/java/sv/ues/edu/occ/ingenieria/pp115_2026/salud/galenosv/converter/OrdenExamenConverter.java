package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ApplicationScoped
public class OrdenExamenConverter extends AbstractEntityConverter<OrdenExamen> {

    @Inject
    private OrdenExamenDAO dao;

    @Override
    protected InterfaceDAO<OrdenExamen> dao() {
        return dao;
    }

    @Override
    protected UUID id(OrdenExamen entidad) {
        return entidad.getIdOrdenExamen();
    }
}