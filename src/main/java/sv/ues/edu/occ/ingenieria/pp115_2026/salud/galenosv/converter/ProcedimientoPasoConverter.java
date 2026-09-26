package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoConverter", managed = true)
public class ProcedimientoPasoConverter implements Converter<ProcedimientoPaso>{
    
      @Inject
    private ProcedimientoPasoDAO dao;

    @Override
    public ProcedimientoPaso getAsObject(
            FacesContext context,
            UIComponent component,
            String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return dao.buscar(UUID.fromString(value));
    }

    @Override
    public String getAsString(
            FacesContext context,
            UIComponent component,
            ProcedimientoPaso paso) {

        if (paso == null || paso.getIdProcedimientoPaso() == null) {
            return "";
        }

        return paso.getIdProcedimientoPaso().toString();
    }
}
