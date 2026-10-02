package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import org.primefaces.PrimeFaces;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaRolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Clinica;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

@Named
@SessionScoped
public class SesionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private PersonaRolDAO personaRolDAO;

    private PersonaRol personaRolActual;
    private String idSeleccionado;
    private List<PersonaRol> opciones;

     // Las opciones NO se guardan en el bean (que dura toda la sesion): si se
    // guardaran, un rol o clinica que se active despues no aparecería hasta
    // cerrar sesion. Se consultan en cada render del selector (maximo 200 filas).
    //
    // Sin sesión: se ven todos los usuarios activos de todas las clínicas.
    // Con sesión: el rol elegido ya trae su clínica, así que solo se ven los
    // usuarios de ESA clínica. Para cambiar de clínica hay que elegir primero
    // "Sin sesión" (o cerrar sesión).
    public List<PersonaRol> getOpciones() {
        return personaRolDAO.listarConDetalle().stream()
                .filter(pr -> pr.getIdRol() == null || !Boolean.FALSE.equals(pr.getIdRol().getActivo()))
                .filter(pr -> pr.getIdClinica() == null || !Boolean.FALSE.equals(pr.getIdClinica().getActivo()))
                .filter(this::perteneceAClinicaActual)
                .collect(java.util.stream.Collectors.toList());
    }
    
    // Clínica del usuario con sesión (null si no hay sesión o si su rol no
    // está ligado a una clínica).
    public Clinica getClinicaActual() {
        return personaRolActual == null ? null : personaRolActual.getIdClinica();
    }
 
    // true si el PersonaRol puede usarse en la sesión actual: siempre que no
    // haya clínica fijada (sin sesión), y si la hay, solo los de esa clínica.
    // Lo usan el selector de arriba y los autocompletes de Persona/Rol.
    public boolean perteneceAClinicaActual(PersonaRol pr) {
        Clinica actual = getClinicaActual();
        if (actual == null) {
            return true;
        }
        return pr != null && actual.equals(pr.getIdClinica());
    }
    
    // Se llama cuando cambia algo que afecta al selector (activar/desactivar un
    // rol o una clínica). Si la sesión abierta quedó con un rol/clínica que ya no
    // está activo, la cierra; y vuelve a dibujar el selector de arriba (frmSesion)
    // dentro de la misma petición AJAX.
    public void refrescar() {
        if (personaRolActual != null) {
            UUID id = personaRolActual.getIdPersonaRol();
            boolean sigueActivo = getOpciones().stream()
                    .anyMatch(pr -> pr.getIdPersonaRol().equals(id));
            if (!sigueActivo) {
                personaRolActual = null;
                idSeleccionado = null;
            }
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getPartialViewContext().isAjaxRequest()) {
            PrimeFaces.current().ajax().update("frmSesion");
        }
    }

    public void cambiar() {
        if (idSeleccionado == null || idSeleccionado.isBlank()) {
            personaRolActual = null;
            return;
        }
        personaRolActual = getOpciones().stream()
                .filter(pr -> pr.getIdPersonaRol().toString().equals(idSeleccionado))
                .findFirst()
                .orElse(null);
    }

    public void cerrarSesion() throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        String destino = ec.getRequestContextPath() + fc.getViewRoot().getViewId();
        personaRolActual = null;
        idSeleccionado = null;
        opciones = null;
        ec.invalidateSession();
        ec.redirect(destino);
    }

    public boolean isAutenticado() {
        return personaRolActual != null;
    }

    public String etiqueta(PersonaRol pr) {
        if (pr == null) {
            return "";
        }
        String s = pr.getIdPersona().getNombres() + " " + pr.getIdPersona().getApellidos()
                + " - " + pr.getIdRol().getNombre();
        if (pr.getIdClinica() != null) {
            s += " (" + pr.getIdClinica().getNombre() + ")";
        }
        return s;
    }

    public String getIdSeleccionado() {
        return idSeleccionado;
    }

    public PersonaRol getPersonaRolActual() {
        return personaRolActual;
    }

    public void setIdSeleccionado(String idSeleccionado) {
        this.idSeleccionado = idSeleccionado;
    }
}
