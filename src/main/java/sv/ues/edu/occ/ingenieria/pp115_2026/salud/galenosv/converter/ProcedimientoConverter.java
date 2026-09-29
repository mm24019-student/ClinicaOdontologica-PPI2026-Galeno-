package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoConverter", managed = true)
public class ProcedimientoConverter extends AbstractEntityConverter<Procedimiento> {

    @Inject
    private ProcedimientoDAO dao;

    @Override
    protected InterfaceDAO<Procedimiento> dao() {
        return dao;
    }

    @Override
    protected UUID id(Procedimiento entidad) {
        return entidad.getIdProcedimiento();
    }
}