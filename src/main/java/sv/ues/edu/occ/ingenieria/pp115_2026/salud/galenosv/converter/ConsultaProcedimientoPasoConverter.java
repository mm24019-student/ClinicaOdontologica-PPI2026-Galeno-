package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

@Named
@ApplicationScoped
public class ConsultaProcedimientoPasoConverter extends AbstractEntityConverter<ConsultaProcedimientoPaso> {

    @Inject
    private ConsultaProcedimientoPasoDAO dao;

    @Override
    protected InterfaceDAO<ConsultaProcedimientoPaso> dao() {
        return dao;
    }

    @Override
    protected UUID id(ConsultaProcedimientoPaso entidad) {
        return entidad.getIdConsultaProcedimientoPaso();
    }
}