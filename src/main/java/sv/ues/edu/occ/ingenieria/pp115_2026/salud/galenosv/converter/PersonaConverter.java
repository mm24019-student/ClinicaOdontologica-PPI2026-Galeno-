package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

// Mismo patrón que ExamenConverter: JSF necesita convertir el objeto que
// viaja en el <p:selectOneMenu> hacia un String para pintarlo en el HTML
// (getAsString) y, al enviar el formulario, reconstruir el objeto real a
// partir de ese String (getAsObject). Sin esto, el value del combo solo
// podría ser texto/primitivos, nunca un objeto Persona.
@Named
@ApplicationScoped
public class PersonaConverter extends AbstractEntityConverter<Persona> {

    @Inject
    private PersonaDAO dao;

    @Override
    protected InterfaceDAO<Persona> dao() {
        return dao;
    }

    @Override
    protected UUID id(Persona entidad) {
        return entidad.getIdPersona();
    }
}