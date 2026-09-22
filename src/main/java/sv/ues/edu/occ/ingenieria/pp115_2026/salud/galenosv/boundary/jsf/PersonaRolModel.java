package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Managed Bean para el CRUD de PersonaRol. Los combos del formulario no
 * manejan el objeto Persona/Rol/Clinica directamente (eso obligaría a usar
 * un Converter de JSF); manejan solo el ID como texto, y este Model se
 * encarga de resolverlo al objeto real usando PersonaRolDAO cuando el
 * usuario selecciona algo.
 *
 * @author oscar
 */
@Named
@ViewScoped
public class PersonaRolModel extends AbstracCrudModel<PersonaRol> {

    @Inject
    private PersonaRolDAO personaRolDAO;

    private List<Persona> personas;
    private List<Rol> roles;
    private List<Clinica> clinicas;

    // Lo que el usuario tiene seleccionado en cada combo, como texto (el ID)
    private String idPersonaSeleccionada;
    private String idRolSeleccionado;
    private String idClinicaSeleccionada;

    // Distinto de inicializar() (ya está en AbstracCrudModel con
    // @PostConstruct); este solo agrega la carga de los combos.
    @PostConstruct
    public void cargarCombos() {
        this.personas = personaRolDAO.listarPersonas();
        this.roles = personaRolDAO.listarRoles();
        this.clinicas = personaRolDAO.listarClinicas();
    }

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

    // Se llama al presionar "Nuevo" (después de crearRegistroNuevo):
    // limpia los combos para que no queden pegados valores de una edición anterior.
    @Override
    protected void configurarNuevoRegistro(PersonaRol nuevoRegistro) {
        this.idPersonaSeleccionada = null;
        this.idRolSeleccionado = null;
        this.idClinicaSeleccionada = null;
    }

    // Al seleccionar una fila de la tabla, además de cargar el registro
    // (lo hace el super), hay que precargar los 3 combos con lo que ya
    // tiene guardado ese PersonaRol.
    @Override
    public void onRowSelect(SelectEvent<PersonaRol> event) {
        super.onRowSelect(event);
        this.idPersonaSeleccionada = (registro.getIdPersona() != null) ? registro.getIdPersona().getIdPersona().toString() : null;
        this.idRolSeleccionado = (registro.getIdRol() != null) ? registro.getIdRol().getIdRol().toString() : null;
        this.idClinicaSeleccionada = (registro.getIdClinica() != null) ? registro.getIdClinica().getIdClinica().toString() : null;
    }

    @Override
    protected UUID obtenerId(PersonaRol registro) {
        return registro.getIdPersonaRol();
    }

    // ---- Se disparan cuando el usuario cambia cada combo (p:ajax) ----
    public void onPersonaChange() {
        UUID id = (idPersonaSeleccionada != null && !idPersonaSeleccionada.isBlank()) ? UUID.fromString(idPersonaSeleccionada) : null;
        registro.setIdPersona(personaRolDAO.buscarPersona(id));
    }

    public void onRolChange() {
        UUID id = (idRolSeleccionado != null && !idRolSeleccionado.isBlank()) ? UUID.fromString(idRolSeleccionado) : null;
        registro.setIdRol(personaRolDAO.buscarRol(id));
    }

    public void onClinicaChange() {
        UUID id = (idClinicaSeleccionada != null && !idClinicaSeleccionada.isBlank()) ? UUID.fromString(idClinicaSeleccionada) : null;
        registro.setIdClinica(personaRolDAO.buscarClinica(id));
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public List<Clinica> getClinicas() {
        return clinicas;
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdRolSeleccionado() {
        return idRolSeleccionado;
    }

    public void setIdRolSeleccionado(String idRolSeleccionado) {
        this.idRolSeleccionado = idRolSeleccionado;
    }

    public String getIdClinicaSeleccionada() {
        return idClinicaSeleccionada;
    }

    public void setIdClinicaSeleccionada(String idClinicaSeleccionada) {
        this.idClinicaSeleccionada = idClinicaSeleccionada;
    }
}