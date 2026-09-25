package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "personaRolConverter", managed = true)
public class PersonaRolConverter implements Converter<PersonaRol> {

    @Inject
    private PersonaRolDAO dao;

    @Override
    public PersonaRol getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return dao.buscar(UUID.fromString(valor));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, PersonaRol pr) {
        return (pr == null || pr.getIdPersonaRol() == null) ? "" : pr.getIdPersonaRol().toString();
    }
}