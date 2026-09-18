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

    public void onIdiomaChange(AjaxBehaviorEvent event) {
        Locale locale = switch (idiomaActual) {
            case "en" ->
                Locale.of("en", "US");
            case "zh" ->
                Locale.of("zh", "TW");
            default ->
                Locale.of("es");
        };
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
}
