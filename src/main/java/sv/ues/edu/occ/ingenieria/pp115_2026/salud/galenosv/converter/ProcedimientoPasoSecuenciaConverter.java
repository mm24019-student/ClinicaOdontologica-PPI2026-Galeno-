package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoSecuenciaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoSecuencia;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoSecuenciaConverter", managed = true) 
public class ProcedimientoPasoSecuenciaConverter extends AbstractEntityConverter<ProcedimientoPasoSecuencia> {

    @Inject
    private ProcedimientoPasoSecuenciaDAO dao;

    @Override
    protected InterfaceDAO<ProcedimientoPasoSecuencia> dao() {
        return dao;
    }

    @Override
    protected UUID id(ProcedimientoPasoSecuencia entidad) {
        return entidad.getIdProcedimientoPasoSecuencia();
    }
 }

