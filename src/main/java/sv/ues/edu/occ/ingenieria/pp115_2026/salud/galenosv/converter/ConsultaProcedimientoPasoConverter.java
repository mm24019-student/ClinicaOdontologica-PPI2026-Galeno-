package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

@Named
@ApplicationScoped
public class ConsultaProcedimientoPasoConverter implements Converter<ConsultaProcedimientoPaso> {

    @Inject
    private ConsultaProcedimientoPasoDAO cppDAO;

    @Override
    public ConsultaProcedimientoPaso getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cppDAO.findRange(0, 100).stream()
                .filter(c -> c.getIdConsultaProcedimientoPaso().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, ConsultaProcedimientoPaso c) {
        if (c == null || c.getIdConsultaProcedimientoPaso() == null) {
            return "";
        }
        return c.getIdConsultaProcedimientoPaso().toString();
    }
}