package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "procedimientoConverter", managed = true)

public class ProcedimientoConverter implements Converter<Procedimiento> {

    @Inject
    private ProcedimientoDAO dao;

    @Override
    public Procedimiento getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, Procedimiento p) {
        return (p == null || p.getIdProcedimiento() == null) ? "" : p.getIdProcedimiento().toString();
    }
}
