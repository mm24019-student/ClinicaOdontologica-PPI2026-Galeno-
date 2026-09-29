package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

@Named
@ApplicationScoped
public class TipoDocumentoConverter extends AbstractEntityConverter<TipoDocumento> {

    @Inject
    private TipoDocumentoDAO dao;

    @Override
    protected InterfaceDAO<TipoDocumento> dao() {
        return dao;
    }

    @Override
    protected UUID id(TipoDocumento entidad) {
        return entidad.getIdTipoDocumento();
    }
}