package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Bean de detalle para la pestaña "Rol y Clinica" / "Asignaciones" dentro de
 * la pantalla de Persona. Antes esta lógica (rolesDePersona, agregarRol,
 * eliminarRol, autocompletes, etc.) vivía completa dentro de PersonaModel,
 * que terminó haciendo demasiadas cosas (God Class / violación de
 * Responsabilidad Única).
 *
 * Se apoya en AbstracdetallecrudModel<PersonaRol, Persona> (la misma clase
 * base que usan las demás pestañas de detalle del proyecto) para el trabajo
 * repetitivo de: cargar solo los PersonaRol de la persona actual, asignar la
 * persona automáticamente a cada asignación nueva, y refrescar la lista
 * filtrada después de crear/eliminar.
 *
 * Lo que NO encaja en esa clase base (elegir Rol y Clinica desde catálogos
 * vía autocomplete antes de poder "crear" el registro) se queda aquí como
 * lógica propia, igual que antes vivía en PersonaModel.
 */
@Named
@ViewScoped
public class PersonaRolDetalleModel extends AbstracdetallecrudModel<PersonaRol, Persona> {

    private static final long serialVersionUID = 1L;

    @Inject
    private PersonaRolDAO personaRolDAO;
    @Inject
    private RolDAO rolDAO;
    @Inject
    private ClinicaDAO clinicaDAO;

    private Rol nuevoRol;
    private Clinica nuevaClinica;

    private List<Rol> catalogoRoles;
    private List<Clinica> catalogoClinicas;

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

    // ---- Contrato con AbstracdetallecrudModel: Persona es el padre ----
    @Override
    protected List<PersonaRol> buscarPorPadre(UUID idPadre) {
        return personaRolDAO.findByPersona(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Persona padre) {
        return padre.getIdPersona();
    }

    @Override
    protected void asignarPadre(PersonaRol hijo, Persona padre) {
        hijo.setIdPersona(padre);
    }

    // PersonaModel llama esto desde onRowSelect / al crear una persona nueva.
    // Aprovechamos para limpiar cualquier rol/clinica que hubiera quedado
    // seleccionado de la persona anterior (antes esto se hacía a mano en
    // PersonaModel cada vez que se llamaba a cargarRolesDePersona()).
    @Override
    public void cargarDe(Persona padre) {
        super.cargarDe(padre);
        nuevoRol = null;
        nuevaClinica = null;
    }

    public List<PersonaRol> getRolesDePersona() {
        return getregistros();
    }

    public Rol getNuevoRol() {
        return nuevoRol;
    }

    public void setNuevoRol(Rol nuevoRol) {
        this.nuevoRol = nuevoRol;
    }

    public Clinica getNuevaClinica() {
        return nuevaClinica;
    }

    public void setNuevaClinica(Clinica nuevaClinica) {
        this.nuevaClinica = nuevaClinica;
    }

    public List<Rol> completarRoles(String query) {
        if (catalogoRoles == null) {
            catalogoRoles = rolDAO.findRange(0, 100);
        }
        String texto = query == null ? "" : query.trim().toLowerCase();
        return catalogoRoles.stream()
                .filter(r -> r.getNombre() != null && r.getNombre().toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }

    public List<Clinica> completarClinicas(String query) {
        if (catalogoClinicas == null) {
            catalogoClinicas = clinicaDAO.findRange(0, 100);
        }
        String texto = query == null ? "" : query.trim().toLowerCase();
        return catalogoClinicas.stream()
                .filter(c -> c.getNombre() != null && c.getNombre().toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }

    // Se dispara cuando el usuario hace clic en una sugerencia del
    // autocomplete (evento itemSelect), no basta con escribir el texto.
    public void onRolAutocompleteSelect(SelectEvent<Rol> event) {
        this.nuevoRol = event.getObject();
    }

    public void onClinicaAutocompleteSelect(SelectEvent<Clinica> event) {
        this.nuevaClinica = event.getObject();
    }

    public void limpiarRolSeleccionado() {
        this.nuevoRol = null;
    }

    public void limpiarClinicaSeleccionada() {
        this.nuevaClinica = null;
    }

    public void agregarRol() {
        FacesMessage mensaje;
        if (padreActual == null || obtenerIdPadre(padreActual) == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Guarde primero los datos de la persona", "No hay una persona seleccionada"));
            return;
        }
        if (nuevoRol == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un rol", "El rol es obligatorio"));
            return;
        }
        if (nuevaClinica == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione una clinica", "La clinica es obligatoria"));
            return;
        }
        boolean yaAsignado = getRolesDePersona().stream().anyMatch(pr
                -> nuevoRol.getIdRol().equals(pr.getIdRol().getIdRol())
                && nuevaClinica.getIdClinica().equals(pr.getIdClinica().getIdClinica()));
        if (yaAsignado) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Ese rol ya esta asignado", "La persona ya tiene ese rol en esa clinica"));
            return;
        }
        try {
            PersonaRol pr = crearRegistroNuevo();
            asignarPadre(pr, padreActual);
            pr.setIdRol(nuevoRol);
            pr.setIdClinica(nuevaClinica);
            personaRolDAO.crear(pr);
            mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Rol agregado con exito", "Rol asignado");
            cargarDe(padreActual);
        } catch (Exception ex) {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo agregar el rol", ex.getMessage());
        }
        fc.addMessage(null, mensaje);
    }

    public void eliminarRol(PersonaRol pr) {
        FacesMessage mensaje;
        if (pr == null || pr.getIdPersonaRol() == null) {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Rol no valido", "No se pudo identificar el rol a quitar");
            fc.addMessage(null, mensaje);
            return;
        }
        try {
            personaRolDAO.eliminar(pr.getIdPersonaRol());
            mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Rol quitado con exito", "Rol eliminado");
            cargarDe(padreActual);
        } catch (Exception ex) {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo quitar el rol", ex.getMessage());
        }
        fc.addMessage(null, mensaje);
    }
}