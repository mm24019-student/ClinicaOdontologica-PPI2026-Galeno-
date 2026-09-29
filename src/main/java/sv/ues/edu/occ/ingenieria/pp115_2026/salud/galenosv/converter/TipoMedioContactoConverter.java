package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named
@ApplicationScoped
public class TipoMedioContactoConverter extends AbstractEntityConverter<TipoMedioContacto> {

    @Inject
    private TipoMedioContactoDAO dao;

    @Override
    protected InterfaceDAO<TipoMedioContacto> dao() {
        return dao;
    }

    @Override
    protected UUID id(TipoMedioContacto entidad) {
        return entidad.getIdTipoMedioContacto();
    }
}