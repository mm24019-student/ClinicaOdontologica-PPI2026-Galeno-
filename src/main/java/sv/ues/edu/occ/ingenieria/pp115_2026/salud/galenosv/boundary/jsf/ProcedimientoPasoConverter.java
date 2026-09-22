package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

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
    public ProcedimientoPaso getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, ProcedimientoPaso p) {
        return (p == null || p.getIdProcedimientoPaso() == null) ? "" : p.getIdProcedimientoPaso().toString();
    }
}
