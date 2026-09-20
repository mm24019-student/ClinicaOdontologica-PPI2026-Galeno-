package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.OrdenExamenDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.OrdenExamen;

@Named
@ApplicationScoped
public class OrdenExamenConverter implements Converter<OrdenExamen> {

    @Inject
    private OrdenExamenDAO oeDAO;

    @Override
    public OrdenExamen getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return oeDAO.findRange(0, 100).stream()
                .filter(o -> o.getIdOrdenExamen().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, OrdenExamen o) {
        if (o == null || o.getIdOrdenExamen() == null) {
            return "";
        }
        return o.getIdOrdenExamen().toString();
    }
}