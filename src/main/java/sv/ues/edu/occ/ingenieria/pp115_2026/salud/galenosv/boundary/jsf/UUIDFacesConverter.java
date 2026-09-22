package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.convert.Converter;
import java.util.UUID;

/**
 *
 * @author antonio
 */
@FacesConverter(forClass = UUID.class)
public class UUIDFacesConverter implements Converter<UUID> {

    @Override
    public UUID getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR, "El id no es un UUID válido", null));
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent c, UUID valor) {
        return valor == null ? "" : valor.toString();
    }
}
