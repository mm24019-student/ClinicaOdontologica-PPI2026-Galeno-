package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

@Named
@ApplicationScoped
public class ExamenConverter extends AbstractEntityConverter<Examen> {

    @Inject
    private ExamenDAO dao;

    @Override
    protected InterfaceDAO<Examen> dao() {
        return dao;
    }

    @Override
    protected UUID id(Examen entidad) {
        return entidad.getIdExamen();
    }
}