package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

/**
 * Superclase de todos los convertidores JSF de entidades. Un combo o
 * autocomplete
 * solo envía texto, así que el convertidor traduce entre la entidad y su UUID:
 * getAsString (entidad -> texto) y getAsObject (texto -> entidad, buscándola
 * con el DAO).
 *
 * Patrón Template Method: aquí vive la lógica común (valor vacío, texto que no
 * es
 * UUID -> ConverterException); cada convertidor concreto solo implementa dao()
 * e id()
 * y, si quiere, mensajeValorInvalido().
 *
 * @author antonio
 */
public abstract class AbstractEntityConverter<T> implements Converter<T> {

    // DAO con el que se busca la entidad por su id.
    protected abstract InterfaceDAO<T> dao();

    // Llave primaria (UUID) de la entidad.
    protected abstract UUID id(T entidad);

    // Texto del error cuando el valor recibido no es un UUID. Los converters
    // pueden sobreescribirlo con un mensaje más específico.
    protected String mensajeValorInvalido() {
        return "Seleccione un valor válido de la lista";
    }

    // Texto del formulario -> entidad. Vacío o nulo devuelve null; si no es un UUID
    // lanza
    // ConverterException con mensaje de error; si lo es, la busca con
    // dao().buscar(uuid).
    @Override
    public T getAsObject(FacesContext ctx, UIComponent c, String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new ConverterException(new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    mensajeValorInvalido(),
                    "El valor ingresado no corresponde a una selección válida"));
        }
        return dao().buscar(uuid);
    }

    // Entidad -> texto para el formulario: su UUID, o "" si la entidad o su id son
    // nulos.
    @Override
    public String getAsString(FacesContext ctx, UIComponent c, T entidad) {
        if (entidad == null || id(entidad) == null) {
            return "";
        }
        return id(entidad).toString();
    }
}
