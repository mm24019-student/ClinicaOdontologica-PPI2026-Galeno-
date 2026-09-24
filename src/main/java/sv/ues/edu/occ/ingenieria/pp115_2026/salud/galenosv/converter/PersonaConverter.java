package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

// Mismo patrón que ExamenConverter: JSF necesita convertir el objeto que
// viaja en el <p:selectOneMenu> hacia un String para pintarlo en el HTML
// (getAsString) y, al enviar el formulario, reconstruir el objeto real a
// partir de ese String (getAsObject). Sin esto, el value del combo solo
// podría ser texto/primitivos, nunca un objeto Persona.
@Named
@ApplicationScoped
public class PersonaConverter implements Converter<Persona> {

    @Inject
    private PersonaDAO pDAO;

    @Override
    public Persona getAsObject(FacesContext context, UIComponent component, String value) {
        // JSF llama esto al hacer submit: "value" es el idPersona (como texto)
        // que quedó seleccionado en el combo. Si no seleccionaron nada, no hay
        // nada que buscar.
        if (value == null || value.isBlank()) {
            return null;
        }
        // Buscamos entre las personas cargadas cuál tiene ese id y esa es
        // la que se asigna de vuelta a registro.idPersona.
        return pDAO.findRange(0, 100).stream()
                .filter(p -> p.getIdPersona().toString().equals(value))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Persona p) {
        // JSF llama esto para pintar cada <f:selectItem>: convierte el objeto
        // Persona real en el valor que va dentro del <option value="...">.
        if (p == null || p.getIdPersona() == null) {
            return "";
        }
        return p.getIdPersona().toString();
    }
}