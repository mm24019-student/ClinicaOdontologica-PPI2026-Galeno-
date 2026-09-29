package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;


import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "rolConverter", managed = true)
public class RolConverter extends AbstractEntityConverter<Rol> {

    @Inject
    private RolDAO dao;

    @Override
    protected InterfaceDAO<Rol> dao() {
        return dao;
    }

    @Override
    protected UUID id(Rol entidad) {
        return entidad.getIdRol();
    }
}