package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

@Path("pruebacliente")
public class PruebaClienteResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String probar() {
        GalenoClient c = new GalenoClient();
        StringBuilder sb = new StringBuilder();
        try {
            // 1. POST valido (nombre unico para que no choque con el duplicado)
            TipoDocumento t = new TipoDocumento();
            t.setNombre("PruebaCliente " + System.currentTimeMillis());
            t.setIndicaciones("creado desde GalenoClient");
            t.setExpresionRegular("^[0-9]{8,9}$");
            URI location = c.crearTipoDocumento(t);
            sb.append("POST valido -> Location: ").append(location).append("\n");

            // 2. POST con nombre repetido (debe devolver null por el 422)
            TipoDocumento repetido = new TipoDocumento();
            repetido.setNombre("Dui");
            repetido.setIndicaciones("repetido");
            repetido.setExpresionRegular("^[0-9]{8}-[0-9]$");
            sb.append("POST repetido -> Location: ").append(c.crearTipoDocumento(repetido))
                    .append(" (null = rechazado)\n");

            // 3. GET
            List<TipoDocumento> lista = c.listarTipoDocumento(0, 50);
            sb.append("GET -> ").append(lista.size()).append(" registros\n");
            lista.forEach(x -> sb.append(" - ").append(x.getNombre())
                    .append(" (activo=").append(x.getActivo()).append(")\n"));
        } finally {
            c.cerrar();
        }
        return sb.toString();
    }
}