package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.convert.ConverterException;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

/**
 * Prueba de AbstractEntityConverter con un converter mínimo de prueba. Aquí
 * se cubre la lógica común (getAsObject / getAsString / mensaje por defecto);
 * los converters concretos solo prueban lo propio (dao(), id() y, si aplica,
 * su mensaje de error).
 *
 * @author antonio
 */
@ExtendWith(MockitoExtension.class)
public class AbstractEntityConverterTest {
 
    static class Entidad {
 
        private final UUID id;
 
        Entidad(UUID id) {
            this.id = id;
        }
 
        UUID getId() {
            return id;
        }
    }
 
    static class ConverterDePrueba extends AbstractEntityConverter<Entidad> {
 
        private final InterfaceDAO<Entidad> dao;
 
        ConverterDePrueba(InterfaceDAO<Entidad> dao) {
            this.dao = dao;
        }
 
        @Override
        protected InterfaceDAO<Entidad> dao() {
            return dao;
        }
 
        @Override
        protected UUID id(Entidad entidad) {
            return entidad.getId();
        }
    }
 
    static class ConverterConMensajePropio extends ConverterDePrueba {
 
        ConverterConMensajePropio(InterfaceDAO<Entidad> dao) {
            super(dao);
        }
 
        @Override
        protected String mensajeValorInvalido() {
            return "Mensaje propio";
        }
    }
 
    @Mock
    private InterfaceDAO<Entidad> dao;
 
    private ConverterDePrueba converter;
 
    @BeforeEach
    public void setUp() {
        converter = new ConverterDePrueba(dao);
    }
 
    // ---- mensajeValorInvalido() ----
 
    @Test
    public void mensajeValorInvalido_porDefecto_pideSeleccionarUnValorDeLaLista() {
        assertEquals("Seleccione un valor válido de la lista", converter.mensajeValorInvalido());
    }
 
    // ---- getAsObject() ----
 
    @Test
    public void getAsObject_valorNulo_devuelveNullSinConsultarElDao() {
        assertNull(converter.getAsObject(null, null, null));
 
        verifyNoInteractions(dao);
    }
 
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "\t"})
    public void getAsObject_valorVacioOSoloEspacios_devuelveNullSinConsultarElDao(String valor) {
        assertNull(converter.getAsObject(null, null, valor));
 
        verifyNoInteractions(dao);
    }
 
    @Test
    public void getAsObject_uuidValido_devuelveLaEntidadQueEncuentraElDao() {
        UUID id = UUID.randomUUID();
        Entidad esperada = new Entidad(id);
        when(dao.buscar(id)).thenReturn(esperada);
 
        assertSame(esperada, converter.getAsObject(null, null, id.toString()));
    }
 
    @Test
    public void getAsObject_uuidConEspaciosAlrededor_lohaceTrimAntesDeBuscar() {
        UUID id = UUID.randomUUID();
        Entidad esperada = new Entidad(id);
        when(dao.buscar(id)).thenReturn(esperada);
 
        assertSame(esperada, converter.getAsObject(null, null, "  " + id + "  "));
 
        verify(dao).buscar(id);
    }
 
    @Test
    public void getAsObject_uuidQueNoExiste_devuelveNull() {
        UUID id = UUID.randomUUID();
        when(dao.buscar(id)).thenReturn(null);
 
        assertNull(converter.getAsObject(null, null, id.toString()));
    }
 
    @ParameterizedTest
    @ValueSource(strings = {"abc", "12345", "no-es-un-uuid"})
    public void getAsObject_valorQueNoEsUuid_lanzaConverterExceptionSinConsultarElDao(String valor) {
        ConverterException ex = assertThrows(ConverterException.class,
                () -> converter.getAsObject(null, null, valor));
 
        FacesMessage mensaje = ex.getFacesMessage();
        assertEquals(FacesMessage.SEVERITY_ERROR, mensaje.getSeverity());
        assertEquals("Seleccione un valor válido de la lista", mensaje.getSummary());
        assertEquals("El valor ingresado no corresponde a una selección válida", mensaje.getDetail());
        verifyNoInteractions(dao);
    }
 
    @Test
    public void getAsObject_valorQueNoEsUuid_usaElMensajeSobreescritoPorLaSubclase() {
        ConverterConMensajePropio conMensajePropio = new ConverterConMensajePropio(dao);
 
        ConverterException ex = assertThrows(ConverterException.class,
                () -> conMensajePropio.getAsObject(null, null, "abc"));
 
        assertEquals("Mensaje propio", ex.getFacesMessage().getSummary());
    }
 
    // ---- getAsString() ----
 
    @Test
    public void getAsString_entidadNula_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, null));
    }
 
    @Test
    public void getAsString_entidadSinId_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, new Entidad(null)));
    }
 
    @Test
    public void getAsString_entidadConId_devuelveElUuidComoTexto() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id.toString(), converter.getAsString(null, null, new Entidad(id)));
    }
}
 