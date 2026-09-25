package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named
@ApplicationScoped
public class TipoMedioContactoConverter implements Converter<TipoMedioContacto> {

    @Inject
    private TipoMedioContactoDAO tmcDAO;

    @Override
    public TipoMedioContacto getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return tmcDAO.findRange(0, 100).stream()
                .filter(t -> t.getIdTipoMedioContacto().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TipoMedioContacto t) {
        if (t == null || t.getIdTipoMedioContacto() == null) {
            return "";
        }
        return t.getIdTipoMedioContacto().toString();
    }
}