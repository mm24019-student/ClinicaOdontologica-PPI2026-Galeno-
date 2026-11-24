package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaProcedimientoPasoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ConsultaProcedimientoPaso;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ConsultaProcedimientoPasoResourceTest {

    @Mock
    private ConsultaProcedimientoPasoService dao;

    @InjectMocks
    private ConsultaProcedimientoPasoResource recurso;

    @Test
    public void avanzar_conSiguiente_devuelve200ConElPasoActivado() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimientoPaso siguiente = new ConsultaProcedimientoPaso(UUID.randomUUID());
        siguiente.setEstado("EN_PROCESO");
        when(dao.avanzar(id)).thenReturn(siguiente);

        Response respuesta = recurso.avanzar(id.toString());

        assertEquals(200, respuesta.getStatus());
        Map<?, ?> datos = (Map<?, ?>) respuesta.getEntity();
        assertEquals(siguiente.getIdConsultaProcedimientoPaso().toString(), datos.get("idConsultaProcedimientoPaso"));
        assertEquals("EN_PROCESO", datos.get("estado"));
        verify(dao).avanzar(id);
    }

    @Test
    public void avanzar_sinSiguiente_devuelve409ConMensaje() {
        UUID id = UUID.randomUUID();
        when(dao.avanzar(id)).thenThrow(new IllegalStateException("No hay un paso siguiente para este procedimiento"));

        Response respuesta = recurso.avanzar(id.toString());

        assertEquals(409, respuesta.getStatus());
        assertEquals("No hay un paso siguiente para este procedimiento",
                respuesta.getHeaderString("error-mensaje"));
        assertTrue(respuesta.getEntity().toString().contains("siguiente"));
    }

    @Test
    public void retroceder_conAnterior_devuelve200ConElPasoActivado() {
        UUID id = UUID.randomUUID();
        ConsultaProcedimientoPaso anterior = new ConsultaProcedimientoPaso(UUID.randomUUID());
        anterior.setEstado("EN_PROCESO");
        when(dao.retroceder(id, null, null)).thenReturn(anterior);

        Response respuesta = recurso.retroceder(id.toString());

        assertEquals(200, respuesta.getStatus());
        Map<?, ?> datos = (Map<?, ?>) respuesta.getEntity();
        assertEquals(anterior.getIdConsultaProcedimientoPaso().toString(), datos.get("idConsultaProcedimientoPaso"));
        assertEquals("EN_PROCESO", datos.get("estado"));
        verify(dao).retroceder(id, null, null);
    }

    @Test
    public void retroceder_sinAnterior_devuelve409ConMensaje() {
        UUID id = UUID.randomUUID();
        when(dao.retroceder(id, null, null)).thenThrow(new IllegalStateException("No hay un paso anterior para este procedimiento"));

        Response respuesta = recurso.retroceder(id.toString());

        assertEquals(409, respuesta.getStatus());
        assertEquals("No hay un paso anterior para este procedimiento",
                respuesta.getHeaderString("error-mensaje"));
        assertTrue(respuesta.getEntity().toString().contains("anterior"));
    }

    @Test
    public void avanzar_conIdInvalido_devuelve422() {
        Response respuesta = recurso.avanzar("no-uuid");

        assertEquals(422, respuesta.getStatus());
        assertEquals("id", respuesta.getHeaderString("Wrong-Parameter"));
    }
}
