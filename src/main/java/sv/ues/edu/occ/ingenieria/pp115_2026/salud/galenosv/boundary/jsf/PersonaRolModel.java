package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Managed Bean para el CRUD de PersonaRol. Ahora los combos SÍ manejan el
 * objeto (Persona/Rol/Clinica) directamente: el binding es
 * registro.idPersona / registro.idRol / registro.idClinica y cada
 * <p:selectOneMenu> usa su Converter (personaConverter, rolConverter,
 * clinicaConverter). Ya no hace falta guardar el id como texto aparte ni
 * escuchar el evento "change" para ir a buscar el objeto a mano: el
 * Converter hace ese trabajo solo cuando el formulario hace submit.
 */
@Named
@ViewScoped
public class PersonaRolModel extends AbstracCrudModel<PersonaRol> {

    @Inject
    private PersonaRolDAO personaRolDAO;

    // Estos DAOs son solo para llenar los combos (igual que
    // ExamenTipoExamenModel inyecta ExamenDAO y TipoExamenDAO además de
    // ExamenTipoExamenDAO).
    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private RolDAO rolDAO;
    @Inject
    private ClinicaDAO clinicaDAO;

    private List<Persona> personas;
    private List<Rol> roles;
    private List<Clinica> clinicas;

    @Override
    protected InterfaceDAO<PersonaRol> getDAO() {
        return personaRolDAO;
    }

    @Override
    protected PersonaRol crearRegistroNuevo() {
        PersonaRol pr = new PersonaRol(UUID.randomUUID());
        pr.setFechaCreacion(new Date());
        return pr;
    }

    @Override
    protected UUID obtenerId(PersonaRol registro) {
        return registro.getIdPersonaRol();
    }

    // Opciones de los combos: se cargan la primera vez que la vista las pide
    // y quedan en memoria mientras dure la vista (ViewScoped).
    public List<Persona> getPersonas() {
        if (personas == null) {
            personas = personaDAO.findRange(0, 100);
        }
        return personas;
    }

    public List<Rol> getRoles() {
        if (roles == null) {
            roles = rolDAO.findRange(0, 100);
        }
        return roles;
    }

    public List<Clinica> getClinicas() {
        if (clinicas == null) {
            clinicas = clinicaDAO.findRange(0, 100);
        }
        return clinicas;
    }
    
        @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdPersona(), "Seleccione una persona", "La persona es obligatoria")
                && requerir(registro.getIdRol(), "Seleccione un rol", "El rol es obligatorio")
                && requerir(registro.getIdClinica(), "Seleccione una clínica", "La clínica es obligatoria");
    }
}