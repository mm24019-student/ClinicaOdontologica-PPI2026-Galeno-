package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.convert.ConverterException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Prueba de UUIDFacesConverter (convierte String <-> UUID sin usar DAO).
 *
 * @author antonio
 */
public class UUIDFacesConverterTest {
 
    private final UUIDFacesConverter converter = new UUIDFacesConverter();
 
    // ---- getAsObject() ----
 
    @Test
    public void getAsObject_valorNulo_devuelveNull() {
        assertNull(converter.getAsObject(null, null, null));
    }
 
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    public void getAsObject_valorVacioOSoloEspacios_devuelveNull(String valor) {
        assertNull(converter.getAsObject(null, null, valor));
    }
 
    @Test
    public void getAsObject_uuidValido_devuelveElUuid() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, converter.getAsObject(null, null, id.toString()));
    }
 
    @Test
    public void getAsObject_uuidConEspaciosAlrededor_lohaceTrim() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, converter.getAsObject(null, null, "  " + id + "  "));
    }
 
    @ParameterizedTest
    @ValueSource(strings = {"abc", "12345", "no-es-un-uuid"})
    public void getAsObject_valorQueNoEsUuid_lanzaConverterExceptionConMensajeDeError(String valor) {
        ConverterException ex = assertThrows(ConverterException.class,
                () -> converter.getAsObject(null, null, valor));
 
        assertEquals(FacesMessage.SEVERITY_ERROR, ex.getFacesMessage().getSeverity());
        assertEquals("El id no es un UUID válido", ex.getFacesMessage().getSummary());
    }
 
    // ---- getAsString() ----
 
    @Test
    public void getAsString_valorNulo_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, null));
    }
 
    @Test
    public void getAsString_uuid_devuelveElUuidComoTexto() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id.toString(), converter.getAsString(null, null, id));
    }
 
    @Test
    public void ida_y_vuelta_devuelveElMismoUuid() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, converter.getAsObject(null, null, converter.getAsString(null, null, id)));
    }
}
