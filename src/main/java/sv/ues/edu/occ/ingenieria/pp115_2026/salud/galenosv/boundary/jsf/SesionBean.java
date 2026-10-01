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
    public List<PersonaRol> getOpciones() {
        return personaRolDAO.listarConDetalle().stream()
                .filter(pr -> pr.getIdRol() == null || !Boolean.FALSE.equals(pr.getIdRol().getActivo()))
                .filter(pr -> pr.getIdClinica() == null || !Boolean.FALSE.equals(pr.getIdClinica().getActivo()))
                .collect(java.util.stream.Collectors.toList());
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
