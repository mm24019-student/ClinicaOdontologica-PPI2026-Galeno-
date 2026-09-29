package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 *
 * @author antonio
 */
@FacesConverter(value = "personaRolConverter", managed = true)
public class PersonaRolConverter extends AbstractEntityConverter<PersonaRol> {

    @Inject
    private PersonaRolDAO dao;

    @Override
    protected InterfaceDAO<PersonaRol> dao() {
        return dao;
    }

    @Override
    protected UUID id(PersonaRol entidad) {
        return entidad.getIdPersonaRol();
    }

    @Override
    protected String mensajeValorInvalido() {
        return "Seleccione una persona/rol válida de la lista";
    }
}