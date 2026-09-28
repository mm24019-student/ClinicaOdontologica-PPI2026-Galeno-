package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author antonio
 */
/**
 * Superclase para los Managed Beans de una entidad "detalle" que siempre vive
 * dentro de un padre (ProcedimientoPaso dentro de Procedimiento,
 * ConsultaProcedimiento dentro de Consulta, etc). Se usa cuando el CRUD de la
 * entidad hija va anidado como pestaña/subtabla en la pantalla del padre, en
 * vez de tener su propio menú.
 *
 * Reúne lo que se repetiría en cada Model de detalle: - cargar solo los hijos
 * del padre actual (no los 100 de todos los padres) - asignar el padre
 * automáticamente a cada hijo nuevo (sin combo) - después de
 * crear/modificar/eliminar, volver a filtrar en vez de quedarse con la lista
 * completa que devuelve AbstracCrudModel
 *
 * @param <T> entidad hija (la que administra este bean)
 * @param <P> entidad padre de la que depende T
 */
public abstract class AbstracdetallecrudModel<T, P> extends AbstracCrudModel<T> {

    private static final long serialVersionUID = 1L;

    // AbstracCrudModel.inicializar() trae 100 filas al crear el bean (@PostConstruct),
    // pensado para una pantalla independiente con su propia tabla. Aquí no tiene
    // sentido: mientras no haya un padre seleccionado no hay nada que mostrar,
    // así que arrancamos vacíos y dejamos que cargarDe(padre) traiga los datos
    // reales cuando el usuario entra a la pestaña.
    @Override
    public void inicializar() {
        setWrappedData(Collections.emptyList());
    }

    protected P padreActual;

    // ---- Lo único que cada subclase de detalle debe implementar ----
    // Trae de la base solo los hijos del padre indicado (una query en el DAO).
    protected abstract List<T> buscarPorPadre(UUID idPadre);

    // UUID (llave primaria) del padre.
    protected abstract UUID obtenerIdPadre(P padre);

    // Cómo asignar el padre al hijo nuevo, ej: hijo.setIdProcedimiento(padre).
    protected abstract void asignarPadre(T hijo, P padre);

    /**
     * Se llama desde la vista (p:ajax de rowSelect del padre, o tabChange de la
     * pestaña) cada vez que cambia o se guarda el padre actual.
     */
    public void cargarDe(P padre) {
        this.padreActual = padre;
        if (padre != null && obtenerIdPadre(padre) != null) {
            setWrappedData(buscarPorPadre(obtenerIdPadre(padre)));
        } else {
            setWrappedData(Collections.emptyList());
        }
        this.estado = Estado_Crud.NINGUNO;
        this.registro = null;
    }

    // AbstracCrudModel llama a este hook justo después de crearRegistroNuevo(),
    // así que cada hijo nuevo ya nace ligado al padre actual.
    @Override
    protected void configurarNuevoRegistro(T nuevoRegistro) {
        asignarPadre(nuevoRegistro, padreActual);
    }
    
        // Tras crear/modificar/eliminar con éxito, AbstracCrudModel llama a este
    // hook: aquí volvemos a filtrar por el padre en vez de traer los 100
    // registros de todos los padres. Al ir dentro del flujo de éxito, una
    // validación fallida ya NO reinicia el formulario ni pierde lo escrito.
    @Override
    protected void recargarLista() {
        cargarDe(padreActual);
    }
}

