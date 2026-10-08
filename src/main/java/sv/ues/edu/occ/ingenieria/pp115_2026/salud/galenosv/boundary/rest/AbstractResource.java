package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.rest;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DefaultDAO;

// Resource generico: GET (lista), GET {id}, POST, PUT {id} y DELETE {id}.
// Codigos: 200 ok, 201 creado, 404 no existe, 422 dato invalido o regla de
// negocio (cabecera Wrong-Parameter con el campo; el cuerpo trae el mensaje),
// 500 error del servidor.
public abstract class AbstractResource<T> {

    @PersistenceContext(unitName = "Galeno-PU")
    protected EntityManager em;

    @Inject
    protected Validator validator;

    @Context
    protected UriInfo uriInfo;

    private final Class<T> clase;

    protected AbstractResource(Class<T> clase) {
        this.clase = clase;
    }

    protected abstract DefaultDAO<T> dao();

    // Regla de negocio propia de la entidad: devuelve el nombre del campo con
    // problema (-> 422) o null si todo esta bien.
    protected String validarNegocio(T registro, boolean esNuevo) {
        return null;
    }

    // GET /resources/<recurso>?first=0&max=50
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRange(@QueryParam("first") @DefaultValue("0") int first,
            @QueryParam("max") @DefaultValue("50") int max) {
        if (first < 0 || max <= 0) {
            return wrong("first,max");
        }
        try {
            return ok(dao().findRange(first, max));
        } catch (Exception ex) {
            return manejar(ex, "first,max");
        }
    }

    // GET /resources/<recurso>/{id}
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscar(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            T registro = dao().buscar(uuid);
            return registro == null ? Response.status(Response.Status.NOT_FOUND).build() : ok(registro);
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    // POST /resources/<recurso>
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crear(String cuerpo) {
        T registro = leer(cuerpo);
        if (registro == null) {
            return wrong(nombreParametro());
        }
        try {
            UUID id = idDe(registro);
            if (id == null) {
                ponerId(registro, UUID.randomUUID());
            } else if (dao().buscar(id) != null) {
                return wrong("id");
            }
            String error = preparar(registro, true);
            if (error != null) {
                return wrong(error);
            }
            dao().crear(registro);
            URI url = uriInfo.getAbsolutePathBuilder().path(idDe(registro).toString()).build();
            return Response.created(url).build();
        } catch (Exception ex) {
            return manejar(ex, nombreParametro());
        }
    }

    // PUT /resources/<recurso>/{id}  (hay que mandar el objeto completo)
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response actualizar(@PathParam("id") String id, String cuerpo) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        T registro = leer(cuerpo);
        if (registro == null) {
            return wrong(nombreParametro());
        }
        try {
            if (dao().buscar(uuid) == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            ponerId(registro, uuid);
            String error = preparar(registro, false);
            if (error != null) {
                return wrong(error);
            }
            return ok(dao().actualizar(registro));
        } catch (Exception ex) {
            return manejar(ex, nombreParametro());
        }
    }

    // DELETE /resources/<recurso>/{id}
    @DELETE
    @Path("{id}")
    public Response eliminar(@PathParam("id") String id) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            if (dao().buscar(uuid) == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            dao().eliminar(uuid);
            return Response.ok().build();
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    // ------------------------------------------------- apoyo para subclases
    // GET de una lista filtrada por el id de un padre
    protected Response listaPor(String id, Function<UUID, ?> consulta) {
        UUID uuid = aUuid(id);
        if (uuid == null) {
            return wrong("id");
        }
        try {
            return ok(consulta.apply(uuid));
        } catch (Exception ex) {
            return manejar(ex, "id");
        }
    }

    // UUID opcional de un query param: null si viene vacio; lanza
    // IllegalArgumentException si viene mal escrito
    protected static UUID uuidOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : UUID.fromString(texto);
    }

    protected Response ok(Object entidad) {
        return Response.ok(GalenoJson.get().toJson(entidad), MediaType.APPLICATION_JSON).build(); // 200
    }

    protected Response wrong(String campo) {
        return Response.status(422).header("Wrong-Parameter", campo).build();                    // 422
    }

    protected Response wrong(String campo, String mensaje) {
        return Response.status(422).header("Wrong-Parameter", campo)
                .type("text/plain; charset=UTF-8").entity(mensaje).build();                      // 422
    }

    protected static UUID aUuid(String id) {
        try {
            return UUID.fromString(id);
        } catch (Exception ex) {
            return null;
        }
    }

    // Traduce excepciones a codigos:
    //  - IllegalArgumentException            -> 422
    //  - IllegalStateException SIN causa     -> 422 (regla de negocio del DAO, con su mensaje)
    //  - violacion de integridad de la BD    -> 422 (duplicado o registro en uso)
    //  - cualquier otra                      -> 500
    protected Response manejar(Exception ex, String campo) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof IllegalArgumentException) {
                return wrong(campo);
            }
            if (t instanceof IllegalStateException && t.getCause() == null && t.getMessage() != null) {
                return wrong(campo, t.getMessage());
            }
            if (t instanceof SQLException) {
                String estado = ((SQLException) t).getSQLState();
                if (estado != null && estado.startsWith("23")) {
                    return wrong(campo, "El registro esta en uso por otros datos o ya existe");
                }
            }
        }
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, ex.getMessage(), ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();                   // 500
    }

    // ---------------------------------------------------------------- interno
    private String preparar(T registro, boolean esNuevo) throws Exception {
        // "activo" por defecto en true si la entidad lo tiene y no vino
        try {
            PropertyDescriptor pd = new PropertyDescriptor("activo", clase);
            if (pd.getReadMethod().invoke(registro) == null) {
                pd.getWriteMethod().invoke(registro, Boolean.TRUE);
            }
        } catch (IntrospectionException ex) {
            // la entidad no tiene "activo"
        }
        String relacion = resolverRelaciones(registro);
        if (relacion != null) {
            return relacion;
        }
        Set<ConstraintViolation<T>> errores = validator.validate(registro);
        if (!errores.isEmpty()) {
            return errores.iterator().next().getPropertyPath().toString();
        }
        return validarNegocio(registro, esNuevo);
    }

    // Cada @ManyToOne llega como {"idX": "..."} con solo el id; se reemplaza por
    // la entidad real de la BD. Si no existe -> devuelve el nombre del campo.
    private String resolverRelaciones(T registro) throws Exception {
        for (Field f : clase.getDeclaredFields()) {
            if (!f.isAnnotationPresent(ManyToOne.class)) {
                continue;
            }
            PropertyDescriptor pd = new PropertyDescriptor(f.getName(), clase);
            Object valor = pd.getReadMethod().invoke(registro);
            if (valor == null) {
                continue;
            }
            UUID id = idDe(valor);
            Object real = id == null ? null : em.find(f.getType(), id);
            if (real == null) {
                return f.getName();
            }
            pd.getWriteMethod().invoke(registro, real);
        }
        return null;
    }

    private static Field campoId(Class<?> c) {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            for (Field f : k.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    return f;
                }
            }
        }
        throw new IllegalStateException("La clase " + c.getName() + " no tiene @Id");
    }

    private static UUID idDe(Object o) throws Exception {
        Field f = campoId(o.getClass());
        return (UUID) new PropertyDescriptor(f.getName(), o.getClass()).getReadMethod().invoke(o);
    }

    private static void ponerId(Object o, UUID id) throws Exception {
        Field f = campoId(o.getClass());
        new PropertyDescriptor(f.getName(), o.getClass()).getWriteMethod().invoke(o, id);
    }

    private T leer(String cuerpo) {
        try {
            return cuerpo == null || cuerpo.isBlank() ? null : GalenoJson.get().fromJson(cuerpo, clase);
        } catch (Exception ex) {
            return null;
        }
    }

    private String nombreParametro() {
        String n = clase.getSimpleName();
        return Character.toLowerCase(n.charAt(0)) + n.substring(1);
    }
}