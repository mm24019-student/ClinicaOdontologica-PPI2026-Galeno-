package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;

@Named
@ApplicationScoped
public class ClinicaConverter implements Converter<Clinica> {

    @Inject
    private ClinicaDAO cDAO;

    @Override
    public Clinica getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return cDAO.findRange(0, 100).stream()
                .filter(c -> c.getIdClinica().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Clinica c) {
        if (c == null || c.getIdClinica() == null) {
            return "";
        }
        return c.getIdClinica().toString();
    }
}