package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.jupiter.api.Assertions.*;

public class CorsResponseFilterTest {

    private MultivaluedMap<String, Object> filtrar(MultivaluedMap<String, String> cabecerasRequest) throws IOException {
        ContainerRequestContext req = mock(ContainerRequestContext.class);
        ContainerResponseContext resp = mock(ContainerResponseContext.class);
        MultivaluedMap<String, Object> cabeceras = new MultivaluedHashMap<>();
        when(req.getHeaders()).thenReturn(cabecerasRequest);
        when(resp.getHeaders()).thenReturn(cabeceras);
        new CorsResponseFilter().filter(req, resp);
        return cabeceras;
    }

    @Test
    public void filter_conOriginComodin_noExponeCredencialesYPublicaLosHeadersDeError() throws IOException {
        MultivaluedMap<String, Object> cabeceras = filtrar(new MultivaluedHashMap<>());

        assertEquals("*", cabeceras.getFirst("Access-Control-Allow-Origin"));
        assertNull(cabeceras.getFirst("Access-Control-Allow-Credentials"));
        String expuestas = String.valueOf(cabeceras.getFirst("Access-Control-Expose-Headers"));
        assertTrue(expuestas.contains("Wrong-Parameter"));
        assertTrue(expuestas.contains("error-mensaje"));
    }

    @Test
    public void filter_conAccessControlRequestHeaders_construyeAllowHeaders() throws IOException {
        MultivaluedMap<String, String> request = new MultivaluedHashMap<>();
        request.add("Access-Control-Request-Headers", "X-Token, Content-Type");

        MultivaluedMap<String, Object> cabeceras = filtrar(request);

        String permitidas = String.valueOf(cabeceras.getFirst("Access-Control-Allow-Headers"));
        assertTrue(permitidas.startsWith("X-Token, Content-Type,"));
        assertTrue(permitidas.contains(CorsResponseFilter.DEFAULT_ALLOWED_HEADERS));
    }

    @Test
    public void filter_sinAccessControlRequestHeaders_usaLosDefaults() throws IOException {
        MultivaluedMap<String, Object> cabeceras = filtrar(new MultivaluedHashMap<>());

        assertEquals(CorsResponseFilter.DEFAULT_ALLOWED_HEADERS,
                cabeceras.getFirst("Access-Control-Allow-Headers"));
        assertEquals(CorsResponseFilter.METODOS, cabeceras.getFirst("Access-Control-Allow-Methods"));
    }
}
