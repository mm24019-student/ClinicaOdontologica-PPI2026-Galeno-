package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.convert.Converter;
import java.util.UUID;

/**
 * Convertidor JSF para campos de tipo UUID. Con forClass = UUID.class se aplica
 * solo,
 * a cualquier campo UUID de las vistas, sin declararlo en el XHTML.
 * Traduce texto <-> UUID sin usar ningún DAO (a diferencia de
 * AbstractEntityConverter,
 * que busca una entidad completa).
 *
 * @author antonio
 */
@FacesConverter(forClass = UUID.class)
public class UUIDFacesConverter implements Converter<UUID> {

    // Texto del formulario -> UUID. Vacío o nulo devuelve null; si no es un UUID
    // lanza
    // ConverterException con mensaje de error.
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

    // UUID -> Texto del formulario. null devuelve cadena vacía.
    @Override
    public String getAsString(FacesContext ctx, UIComponent c, UUID valor) {
        return valor == null ? "" : valor.toString();
    }
}
