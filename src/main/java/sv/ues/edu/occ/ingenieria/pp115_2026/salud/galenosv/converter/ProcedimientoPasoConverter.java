package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoConverter", managed = true)
public class ProcedimientoPasoConverter extends AbstractEntityConverter<ProcedimientoPaso> {

    @Inject
    private ProcedimientoPasoDAO dao;

    @Override
    protected InterfaceDAO<ProcedimientoPaso> dao() {
        return dao;
    }

    @Override
    protected UUID id(ProcedimientoPaso entidad) {
        return entidad.getIdProcedimientoPaso();
    }
}