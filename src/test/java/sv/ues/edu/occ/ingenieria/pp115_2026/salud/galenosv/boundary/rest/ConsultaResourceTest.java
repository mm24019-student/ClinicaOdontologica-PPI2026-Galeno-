package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verifyNoInteractions;
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
        Response respuesta = recurso.buscar(null, null, null, null, "2026-02-30", null, 100);

        assertEquals(422, respuesta.getStatus());
        assertEquals("filtros", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    public void buscar_conHastaInvalido_devuelve422SinConsultar() {
        Response respuesta = recurso.buscar(null, null, null, null, null, "fecha-mal-escrita", 100);

        assertEquals(422, respuesta.getStatus());
        assertEquals("filtros", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }

    @Test
    public void buscar_conMaxInvalido_devuelve422SinConsultar() {
        Response respuesta = recurso.buscar(null, null, null, null, null, null, 0);

        assertEquals(422, respuesta.getStatus());
        assertEquals("max", respuesta.getHeaderString("Wrong-Parameter"));
        verifyNoInteractions(dao);
    }
}
