package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
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

    // Antes de crear o modificar, valida que las 3 relaciones vengan
    // seleccionadas (mismo patrón que relacionesCompletas() en
    // ExamenTipoExamenModel).
    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (relacionesCompletas()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (relacionesCompletas()) {
            super.btnModificarHandler();
        }
    }

    private boolean relacionesCompletas() {
        if (registro == null) {
            return true; // el método base ya avisa que el registro es nulo
        }
        if (registro.getIdPersona() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione una persona", "La persona es obligatoria"));
            return false;
        }
        if (registro.getIdRol() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un rol", "El rol es obligatorio"));
            return false;
        }
        if (registro.getIdClinica() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione una clínica", "La clínica es obligatoria"));
            return false;
        }
        return true;
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
}