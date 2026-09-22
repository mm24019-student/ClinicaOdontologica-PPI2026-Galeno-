package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoExamen;

@Named
@ApplicationScoped
public class TipoExamenConverter implements Converter<TipoExamen> {

    @Inject
    private TipoExamenDAO teDAO;

    @Override
    public TipoExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return teDAO.findRange(0, 100).stream()
                .filter(t -> t.getIdTipoExamen().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoExamen t) {
        if (t == null || t.getIdTipoExamen() == null) {
            return "";
        }
        return t.getIdTipoExamen().toString();
    }
}