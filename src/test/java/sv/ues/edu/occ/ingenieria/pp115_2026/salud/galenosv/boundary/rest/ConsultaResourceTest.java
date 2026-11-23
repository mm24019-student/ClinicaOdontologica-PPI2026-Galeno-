package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.core.Response;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ConsultaService;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ConsultaResourceTest {

    @Mock
    private ConsultaService dao;

    @InjectMocks
    private ConsultaResource recurso;

    @Test
    public void buscar_conDesdeInvalido_devuelve422SinConsultar() {
        Response respuesta = recurso.buscar(null, null, null, null, null, null, "2026-02-30", null, 100);

        assertEquals(422, respuesta.getStatus());
        assertEquals("filtros", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    public void buscar_conHastaInvalido_devuelve422SinConsultar() {
        Response respuesta = recurso.buscar(null, null, null, null, null, null, null, "fecha-mal-escrita", 100);

        assertEquals(422, respuesta.getStatus());
        assertEquals("filtros", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    public void buscar_conMaxInvalido_devuelve422SinConsultar() {
        Response respuesta = recurso.buscar(null, null, null, null, null, null, null, null, 0);

        assertEquals(422, respuesta.getStatus());
        assertEquals("max", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    public void buscar_conIdPersonaYIdPersonaRol_losPasaAlService() {
        UUID idClinica = UUID.randomUUID();
        UUID idPersona = UUID.randomUUID();
        UUID idPersonaRol = UUID.randomUUID();
        when(dao.buscarCitas(null, null, idClinica, null, idPersona, idPersonaRol, null, null, 100))
                .thenThrow(new IllegalArgumentException("fallo"));

        Response respuesta = recurso.buscar(null, null, idClinica.toString(),
                idPersona.toString(), idPersonaRol.toString(), null, null, null, 100);

        assertEquals(422, respuesta.getStatus());
        verify(dao).buscarCitas(null, null, idClinica, null, idPersona, idPersonaRol, null, null, 100);
    }
}
