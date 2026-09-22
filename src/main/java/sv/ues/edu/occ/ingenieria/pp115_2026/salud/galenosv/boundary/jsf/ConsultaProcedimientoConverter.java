package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimiento;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "consultaProcedimientoConverter", managed = true)

public class ConsultaProcedimientoConverter implements Converter<ConsultaProcedimiento> {

    @Inject
    private ConsultaProcedimientoDAO dao;

    @Override
    public ConsultaProcedimiento getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, ConsultaProcedimiento cp) {
        return (cp == null || cp.getIdConsultaProcedimiento() == null) ? "" : cp.getIdConsultaProcedimiento().toString();
    }
}
