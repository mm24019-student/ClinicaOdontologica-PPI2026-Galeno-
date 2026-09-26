package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ClinicaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

@Named
@ViewScoped
public class PersonaModel extends AbstracCrudModel<Persona> {

    @Inject
    private PersonaDAO personaDAO;

    @Inject
    private PersonaRolDAO personaRolDAO;
    @Inject
    private RolDAO rolDAO;
    @Inject
    private ClinicaDAO clinicaDAO;

    private List<PersonaRol> rolesDePersona = new ArrayList<>();

    private Rol nuevoRol;
    private Clinica nuevaClinica;

    private List<Rol> catalogoRoles;
    private List<Clinica> catalogoClinicas;

    // Recuerda en qué pestaña estaba el usuario para que los refrescos
    // ajax (seleccionar fila, crear/actualizar rol, etc.) no lo regresen
    // siempre a "Datos de Persona".
    private int tabActivo = 0;

    public int getTabActivo() {
        return tabActivo;
    }

    public void setTabActivo(int tabActivo) {
        this.tabActivo = tabActivo;
    }

    @Override
    protected InterfaceDAO<Persona> getDAO() {
        return personaDAO;
    }

    @Override
    protected Persona crearRegistroNuevo() {
        Persona p = new Persona(UUID.randomUUID());
        p.setFechaCreacion(new Date());
        rolesDePersona = new ArrayList<>();
        nuevoRol = null;
        nuevaClinica = null;
        tabActivo = 0;
        return p;
    }

    @Override
    protected UUID obtenerId(Persona registro) {
        return registro.getIdPersona();
    }

    // La tabla sigue usando p:dataTable + rowSelect, tal cual ya lo tenias.
    // Solo agregamos: despues de que AbstracCrudModel hace lo suyo (fija
    // registro y pone estado=MODIFICAR), cargamos los roles de esa persona
    // para dejarlos listos en la pestana "Asignaciones".
    @Override
    public void onRowSelect(SelectEvent<Persona> event) {
        super.onRowSelect(event);
        // Al cambiar de persona, cualquier rol/clinica que hubiera quedado
        // seleccionado en las pestañas de Rol/Clinica ya no aplica.
        nuevoRol = null;
        nuevaClinica = null;
        tabActivo = 0;
        cargarRolesDePersona();
    }

    private void cargarRolesDePersona() {
        if (registro != null && registro.getIdPersona() != null) {
            rolesDePersona = personaRolDAO.findByPersona(registro.getIdPersona());
        } else {
            rolesDePersona = new ArrayList<>();
        }
    }

    public List<PersonaRol> getRolesDePersona() {
        return rolesDePersona;
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

    // === Selección de Rol/Clinica desde sus pestañas embebidas ===
    // Se llaman desde el botón "Usar este rol"/"Usar esta clinica" de las
    // pestañas Rol y Clinica, pasando el registro que esté cargado en el
    // formulario de detalle de rolModel/clinicaModel (rolModel.registro,
    // clinicaModel.registro) en ese momento.
    public void usarRol(Rol r) {
        if (r == null || r.getIdRol() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Seleccione un rol", "Elija o guarde un rol en la pestaña Rol antes de usarlo"));
            return;
        }
        this.nuevoRol = r;
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Rol listo para asignar", r.getNombre()));
    }

    public void usarClinica(Clinica c) {
        if (c == null || c.getIdClinica() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Seleccione una clinica", "Elija o guarde una clinica en la pestaña Clinica antes de usarla"));
            return;
        }
        this.nuevaClinica = c;
        fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Clinica lista para asignar", c.getNombre()));
    }

    // Se dispara cuando el usuario hace clic en una sugerencia del
    // autocomplete (evento itemSelect), no basta con escribir el texto.
    public void onRolAutocompleteSelect(org.primefaces.event.SelectEvent<Rol> event) {
        this.nuevoRol = event.getObject();
    }

    public void onClinicaAutocompleteSelect(org.primefaces.event.SelectEvent<Clinica> event) {
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
        if (registro == null || registro.getIdPersona() == null) {
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
        boolean yaAsignado = rolesDePersona.stream().anyMatch(pr
                -> nuevoRol.getIdRol().equals(pr.getIdRol().getIdRol())
                && nuevaClinica.getIdClinica().equals(pr.getIdClinica().getIdClinica()));
        if (yaAsignado) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Ese rol ya esta asignado", "La persona ya tiene ese rol en esa clinica"));
            return;
        }
        try {
            PersonaRol pr = new PersonaRol(UUID.randomUUID());
            pr.setFechaCreacion(new Date());
            pr.setIdPersona(registro);
            pr.setIdRol(nuevoRol);
            pr.setIdClinica(nuevaClinica);
            personaRolDAO.crear(pr);
            mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Rol agregado con exito", "Rol asignado");
            cargarRolesDePersona();
            nuevoRol = null;
            nuevaClinica = null;
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
            cargarRolesDePersona();
        } catch (Exception ex) {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se pudo quitar el rol", ex.getMessage());
        }
        fc.addMessage(null, mensaje);
    }

    public List<Clinica> getClinicasTabla() {
        return clinicaDAO.findRange(0, 100);
    }
}
