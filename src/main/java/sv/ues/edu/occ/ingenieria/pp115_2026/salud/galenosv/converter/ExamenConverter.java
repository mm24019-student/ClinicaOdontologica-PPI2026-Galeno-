package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Examen;

@Named
@ApplicationScoped
public class ExamenConverter implements Converter<Examen> {

    @Inject
    private ExamenDAO eDAO;

    @Override
    public Examen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return eDAO.findRange(0, 100).stream()
                .filter(e -> e.getIdExamen().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Examen e) {
        if (e == null || e.getIdExamen() == null) {
            return "";
        }
        return e.getIdExamen().toString();
    }
}