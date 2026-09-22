package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;

/**
 * Managed Bean para el CRUD de Persona.
 * Expuesto en las vistas como "personaModel".
 *
 * @author oscar
 */
@Named
@ViewScoped
public class PersonaModel extends AbstracCrudModel<Persona> {

    @Inject
    private PersonaDAO personaDAO;

    @Override
    protected InterfaceDAO<Persona> getDAO() {
        return personaDAO;
    }

    // Al crear un registro nuevo: genera el UUID y deja la fecha de
    // creación en el momento actual (fecha_nacimiento la llena el usuario).
    @Override
    protected Persona crearRegistroNuevo() {
        Persona p = new Persona(UUID.randomUUID());
        p.setFechaCreacion(new Date());
        return p;
    }

    @Override
    protected UUID obtenerId(Persona registro) {
        return registro.getIdPersona();
    }
}