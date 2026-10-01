package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
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

    public List<PersonaRol> getOpciones() {
        if (opciones == null) {
            opciones = personaRolDAO.listarConDetalle().stream()
                    .filter(pr -> pr.getIdRol() == null || !Boolean.FALSE.equals(pr.getIdRol().getActivo()))
                    .filter(pr -> pr.getIdClinica() == null || !Boolean.FALSE.equals(pr.getIdClinica().getActivo()))
                    .collect(java.util.stream.Collectors.toList());
        }
        return opciones;
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
