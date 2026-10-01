package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

/**
 *
 * @author antonio
 */
@Named
@SessionScoped
public class LocalBean implements Serializable {

    private String idiomaActual = "es";

    public String getIdiomaActual() {
        return idiomaActual;
    }

    public void setIdiomaActual(String idiomaActual) {
        this.idiomaActual = idiomaActual;
    }

    // Locale correspondiente al idioma elegido; lo usa <f:view> en general.xhtml
    public Locale getLocale() {
        return switch (idiomaActual) {
            case "en" ->
                Locale.of("en", "US");
            case "zh" ->
                Locale.of("zh", "TW");
            default ->
                Locale.of("es");
        };
    }

    public void onIdiomaChange(AjaxBehaviorEvent event) {
        FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());
    }
}