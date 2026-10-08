package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

public class GalenoClient {

    // Ajusta puerto y contexto a como despliegas el WAR
    private static final String BASE_URL = "http://localhost:9080/GalenoSV-1.0-SNAPSHOT/resources";

    private final Client client = ClientBuilder.newClient();
    private final String baseUrl;

    public GalenoClient() {
        this(BASE_URL);
    }

    public GalenoClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    // GET -> lista de tipos de documento (vacía si el servidor responde error)
    public List<TipoDocumento> listarTipoDocumento(int first, int max) {
        Response r = client.target(baseUrl)
                .path("tipodocumento")
                .queryParam("first", first)
                .queryParam("max", max)
                .request(MediaType.APPLICATION_JSON)
                .get();
        try {
            if (r.getStatus() == 200) {
                return r.readEntity(new GenericType<List<TipoDocumento>>() {
                });
            }
            return Collections.emptyList();
        } finally {
            r.close();
        }
    }

    // POST -> devuelve el Location del nuevo recurso, o null si falló
    public URI crearTipoDocumento(TipoDocumento tipoDocumento) {
        Response r = client.target(baseUrl)
                .path("tipodocumento")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(tipoDocumento, MediaType.APPLICATION_JSON));
        try {
            if (r.getStatus() == 201) {
                return r.getLocation();
            }
            // 422 -> campo con problema en la cabecera Wrong-Parameter
            String campo = r.getHeaderString("Wrong-Parameter");
            System.err.println("Error " + r.getStatus() + (campo != null ? " campo: " + campo : ""));
            return null;
        } finally {
            r.close();
        }
    }

    public void cerrar() {
        client.close();
    }
}