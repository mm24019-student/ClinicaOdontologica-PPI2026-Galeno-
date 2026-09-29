package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoExamenConverter", managed = true)
public class ProcedimientoPasoExamenConverter extends AbstractEntityConverter<ProcedimientoPasoExamen> {

    @Inject
    private ProcedimientoPasoExamenDAO dao;

    @Override
    protected InterfaceDAO<ProcedimientoPasoExamen> dao() {
        return dao;
    }

    @Override
    protected UUID id(ProcedimientoPasoExamen entidad) {
        return entidad.getIdProcedimientoPasoExamen();
    }
}
