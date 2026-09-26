package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPasoExamen;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoPasoExamenConverter", managed = true)
public class ProcedimientoPasoExamenConverter implements Converter<ProcedimientoPasoExamen> {

    @Inject
    private ProcedimientoPasoExamenDAO dao;

    @Override
    public ProcedimientoPasoExamen getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, ProcedimientoPasoExamen registro) {
        return (registro == null || registro.getIdProcedimientoPasoExamen() == null)
                ? "" : registro.getIdProcedimientoPasoExamen().toString();
    }
}
