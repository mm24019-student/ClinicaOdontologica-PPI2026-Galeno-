package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

@Named
@ApplicationScoped
public class TipoDocumentoConverter implements Converter<TipoDocumento> {

    @Inject
    private TipoDocumentoDAO tdDAO;

    @Override
    public TipoDocumento getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return tdDAO.findRange(0, 100).stream()
                .filter(t -> t.getIdTipoDocumento().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoDocumento t) {
        if (t == null || t.getIdTipoDocumento() == null) {
            return "";
        }
        return t.getIdTipoDocumento().toString();
    }
}