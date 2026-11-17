package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.converter;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/*
 * Prueba de TipoDocumentoConverter. La lógica común (getAsObject / getAsString) ya
 * queda cubierta por AbstractEntityConverterTest; aquí solo se prueba lo
 * propio: el Service que usa, la llave primaria que expone y que todo funciona
 * de punta a punta con esa entidad.
 *
 * @author antonio
 */
@ExtendWith(MockitoExtension.class)
public class TipoDocumentoConverterTest {
 
    @Mock
    private TipoDocumentoService tipoDocumentoService;
 
    @InjectMocks
    private TipoDocumentoConverter converter;
 
    @Test
    public void dao_devuelveElDaoInyectado() {
        assertSame(tipoDocumentoService, converter.dao());
    }
 
    @Test
    public void id_devuelveElIdTipoDocumento() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id, converter.id(new TipoDocumento(id)));
    }
 
    @Test
    public void getAsObject_uuidValido_buscaLaEntidadEnElDao() {
        UUID id = UUID.randomUUID();
        TipoDocumento esperada = new TipoDocumento(id);
        when(tipoDocumentoService.buscar(id)).thenReturn(esperada);
 
        assertSame(esperada, converter.getAsObject(null, null, id.toString()));
    }
 
    @Test
    public void getAsString_entidadConId_devuelveElUuidComoTexto() {
        UUID id = UUID.randomUUID();
 
        assertEquals(id.toString(), converter.getAsString(null, null, new TipoDocumento(id)));
    }
 
    @Test
    public void getAsString_entidadSinId_devuelveCadenaVacia() {
        assertEquals("", converter.getAsString(null, null, new TipoDocumento()));
    }
}
 