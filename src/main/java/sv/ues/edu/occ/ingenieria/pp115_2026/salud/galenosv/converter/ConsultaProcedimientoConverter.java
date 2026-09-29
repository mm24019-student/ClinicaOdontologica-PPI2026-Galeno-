package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;


import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "consultaProcedimientoConverter", managed = true)
public class ConsultaProcedimientoConverter extends AbstractEntityConverter<ConsultaProcedimiento> {

    @Inject
    private ConsultaProcedimientoDAO dao;

    @Override
    protected InterfaceDAO<ConsultaProcedimiento> dao() {
        return dao;
    }

    @Override
    protected UUID id(ConsultaProcedimiento entidad) {
        return entidad.getIdConsultaProcedimiento();
    }
}
