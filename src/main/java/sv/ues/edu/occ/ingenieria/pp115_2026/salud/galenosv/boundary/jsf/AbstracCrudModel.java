package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.ListDataModel;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.SelectableDataModel;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.PersonaRol;

/**
 * Superclase abstracta para los Managed Beans de CRUD, siguiendo el mismo
 * estilo visto en clase (Estado_Crud, btnXxxHandler, etc).
 *
 * Extiende ListDataModel e implementa SelectableDataModel para que el propio
 * bean pueda usarse como "value" del p:dataTable y soporte el evento rowSelect:
 * PrimeFaces necesita convertir cada fila a una clave de texto (getRowKey) y, a
 * la inversa, reconstruir el objeto a partir de esa clave (getRowData) en cada
 * petición AJAX.
 *
 * Patrón usado: Template Method. Lo común (crear, modificar, eliminar,
 * seleccionar, diálogo, mensajes) vive aquí una sola vez; cada subclase solo
 * implementa los métodos abstractos y, si lo necesita, los hooks.
 *
 * @param <T> entidad que administra el bean
 * @author oscar
 */
public abstract class AbstracCrudModel<T> extends ListDataModel<T> implements SelectableDataModel<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final int MAX_REGISTROS = 100;

    // Contexto de JSF: se usa para agregar mensajes (FacesMessage) que se ven en la
    // pantalla.
    @Inject
    protected FacesContext fc;

    // Estado actual de la pantalla (NINGUNO, CREAR o MODIFICAR).
    protected Estado_Crud estado = Estado_Crud.NINGUNO;

    // Registro que se está creando o editando en el formulario (null si no hay
    // ninguno).
    protected T registro;

    protected boolean mostrarDialogo = false;

    // =====================================================================
    // Métodos que cada subclase DEBE implementar
    // =====================================================================
    protected abstract InterfaceDAO<T> getDAO();

    protected abstract T crearRegistroNuevo();

    protected abstract UUID obtenerId(T registro);

    // =====================================================================
    // Hooks opcionales (las subclases los sobreescriben si los necesitan)
    // =====================================================================
    /**
     * Por defecto asumimos que la entidad no tiene hijos. Las subclases cuya
     * entidad SÍ puede tener registros dependientes (ej. OrdenExamen tiene
     * ExamenResultado, Procedimiento tiene ProcedimientoPaso) lo sobreescriben
     * para chequear antes de intentar borrar.
     */
    protected boolean tieneRegistrosDependientes(T registro) {
        return false;
    }

    /**
     * Inicializa valores por defecto en el registro recién creado. Por defecto
     * no hace nada.
     */
    protected void configurarNuevoRegistro(T nuevoRegistro) {
    }

    /**
     * Patrón "validar antes de guardar". Las subclases lo sobreescriben para
     * validar su registro. Debe agregar por sí mismo el FacesMessage de error
     * (ver requerir) y devolver false si algo falla. Solo se invoca con
     * registro != null. Por defecto no valida nada.
     */
    protected boolean validarAntesDeGuardar() {
        return true;
    }
    
        /**
     * Hook: permite o bloquea las acciones de escritura (Nuevo, Guardar,
     * Actualizar y Eliminar). Si devuelve false, debe agregar su propio mensaje.
     * Por defecto permite todo, así que las demás pantallas no cambian.
     */
    protected boolean permitirAccion() {
        return true;
    }

    /**
     * Helper para las pantallas que exigen sesión iniciada.
     */
    protected boolean exigirSesion(SesionBean sesion) {
        if (sesion == null || !sesion.isAutenticado()) {
            mensaje(FacesMessage.SEVERITY_WARN, "Sin sesión",
                    "Inicie sesión en el selector de la parte superior para realizar esta acción");
            return false;
        }
        return true;
    }

    /**
     * Cómo se refresca la lista tras crear/modificar/eliminar. Las pantallas de
     * detalle lo sobreescriben para volver a filtrar por padre.
     */
    protected void recargarLista() {
        setWrappedData(getDAO().findRange(0, MAX_REGISTROS));
    }

    // =====================================================================
    // Helpers
    // =====================================================================
    /**
     * Agrega un mensaje global a la vista.
     */
    protected void mensaje(FacesMessage.Severity severidad, String resumen, String detalle) {
        fc.addMessage(null, new FacesMessage(severidad, resumen, detalle));
    }

    /**
     * Helper de validación: si el valor viene nulo agrega el mensaje de error y
     * devuelve false. Permite encadenar con && (se detiene en el primer error).
     */
    protected boolean requerir(Object valor, String resumen, String detalle) {
        if (valor == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, resumen, detalle);
            return false;
        }
        return true;
    }
    
    protected boolean requerirActivo(Boolean activo, String resumen, String detalle) {
    if (!esActivo(activo)) {
        mensaje(FacesMessage.SEVERITY_ERROR, resumen, detalle);
        return false;
    }
    return true;
}

    /**
     * Deja el bean sin registro seleccionado y sin estado de edición.
     */
    private void limpiarSeleccion() {
        this.registro = null;
        this.estado = Estado_Crud.NINGUNO;
    }

    // =====================================================================
    // Ciclo de vida
    // =====================================================================
    @PostConstruct
    public void inicializar() {
        recargarLista();
    }

    // =====================================================================
    // Diálogo
    // =====================================================================
    public boolean isMostrarDialogo() {
        return mostrarDialogo;
    }

    public void setMostrarDialogo(boolean mostrarDialogo) {
        this.mostrarDialogo = mostrarDialogo;
    }

    public void btnAbrirDialogo() {
        this.mostrarDialogo = true;
    }

    /**
     * Se usa tanto desde un botón "Cerrar" como desde el evento close del
     * propio p:dialog (ícono X), para que el estado del bean no quede
     * desincronizado del diálogo cuando el usuario lo cierra sin guardar.
     */
    public void btnCerrarDialogo() {
        this.mostrarDialogo = false;
        limpiarSeleccion();
    }

    // =====================================================================
    // Selección
    // =====================================================================
    public void btnNuevoHandler(ActionEvent ae) {
         if (!permitirAccion()) {
            return;
        }
        this.registro = crearRegistroNuevo();
        configurarNuevoRegistro(this.registro);
        this.estado = Estado_Crud.CREAR;
    }

    public void btnSeleccionarRegistro(UUID id) {
        if (id == null) {
            return;
        }
        T encontrado = getRowData(id.toString());
        if (encontrado != null) {
            this.registro = encontrado;
            this.estado = Estado_Crud.MODIFICAR;
        }
    }

    /**
     * Listener del evento rowSelect del p:dataTable. PrimeFaces ya resolvió la
     * fila real usando getRowData(String) antes de invocar esto.
     */
    public void onRowSelect(SelectEvent<T> event) {
        this.registro = event.getObject();
        this.estado = Estado_Crud.MODIFICAR;
    }

    public void btnCancelar() {
        
        limpiarSeleccion();
    }

    // =====================================================================
    // Crear / Modificar (comparten la misma lógica en guardar)
    // =====================================================================
    // Botón "Guardar" de un registro nuevo: llama a guardar(true), que hace persist
    // en el DAO.
    public void btnCrearhandler(ActionEvent ae) {
        guardar(true);
    }

    // Botón "Guardar" de un registro existente: llama a guardar(false), que hace
    // merge en el DAO.
    public void btnModificarHandler() {
        guardar(false);
    }

    // Lógica común de crear y modificar: valida, llama al DAO, muestra el mensaje
    // de éxito o
    // error y recarga la lista. esNuevo = true crea; false actualiza.
    private void guardar(boolean esNuevo) {
         if (!permitirAccion()) {
            return;
        }
        if (this.registro == null) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo",
                    esNuevo ? "Ingrese algun registro" : "Seleccione algun registro");
            return;
        }
        // Si falla, validarAntesDeGuardar ya agregó su propio mensaje de error.
        if (!validarAntesDeGuardar()) {
            return;
        }
        try {
            if (esNuevo) {
                getDAO().crear(registro);
            } else {
                getDAO().actualizar(registro);
            }
            mensaje(FacesMessage.SEVERITY_INFO,
                    esNuevo ? "Registro creado con exito" : "Registro actualizado con exito",
                    "Registro guardado");
            limpiarSeleccion();
            recargarLista();
        } catch (Exception ex) {
            mensaje(FacesMessage.SEVERITY_ERROR,
                    esNuevo ? "No se puede guardar el registro" : "No se puede actualizar el registro",
                    ex.getMessage());
        }
    }

    // =====================================================================
    // Eliminar
    // =====================================================================
    // Elimina el registro con ese id: primero revisa si tiene dependientes y luego
    // llama al DAO.
    public void btnEliminarHandler(UUID id) {
         if (!permitirAccion()) {
            return;
        }
        List<T> lista = getregistros();
        if (id == null || lista == null || lista.isEmpty()) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algun registro");
            return;
        }

        // Se valida sobre el registro que realmente se va a borrar (el de ese
        // id), no sobre el que casualmente esté seleccionado en this.registro.
        T objetivo = getRowData(id.toString());
        if (objetivo != null && tieneRegistrosDependientes(objetivo)) {
            mensaje(FacesMessage.SEVERITY_WARN, "No se puede eliminar el registro",
                    "Tiene registros relacionados que dependen de él. Elimínelos primero.");
            return;
        }

        try {
            getDAO().eliminar(id);
            mensaje(FacesMessage.SEVERITY_INFO, "Registro eliminado con exito", "Registro borrado");
            limpiarSeleccion();
            recargarLista();
        } catch (Exception ex) {
            mensaje(FacesMessage.SEVERITY_ERROR, "No se puede eliminar el registro",
                    "Tiene registros relacionados que dependen de él, o ocurrió un error al eliminarlo.");
        }
    }

    // Elimina el registro que está seleccionado en el formulario.
    public void btnEliminarHandler() {
        if (this.registro != null) {
            btnEliminarHandler(obtenerId(this.registro));
        }
    }

    // =====================================================================
    // Filtro para los completeMethod de p:autoComplete
    // =====================================================================
    // Devuelve los elementos de "fuente" cuya etiqueta contiene el texto
    // escrito (sin distinguir mayúsculas). Una etiqueta nula cuenta como "".
    protected <E> List<E> filtrar(List<E> fuente, String query, Function<E, String> etiqueta) {
        if (fuente == null) {
            return Collections.emptyList();
        }
        String texto = query == null ? "" : query.trim().toLowerCase();
        return fuente.stream()
                .filter(e -> Objects.toString(etiqueta.apply(e), "").toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }

    // =====================================================================
    //Filtrado de ACtivos y Inactivos
    // =====================================================================
        // true si el valor NO es false (null cuenta como activo)
    protected boolean esActivo(Boolean activo) {
        return !Boolean.FALSE.equals(activo);
    }

    // Igual que filtrar(), pero omite los registros INACTIVOS.
    // Uso: filtrarActivos(lista, query, X::getNombre, X::getActivo)
    protected <E> List<E> filtrarActivos(List<E> fuente, String query,
            Function<E, String> etiqueta, Function<E, Boolean> activo) {
        return filtrar(fuente, query, etiqueta).stream()
                .filter(e -> esActivo(activo.apply(e)))
                .collect(Collectors.toList());
    }

    // Misma versión, pero además limita la cantidad de sugerencias.
    // Uso: filtrarActivos(lista, query, X::getNombre, X::getActivo, 20)
    protected <E> List<E> filtrarActivos(List<E> fuente, String query,
            Function<E, String> etiqueta, Function<E, Boolean> activo, int limite) {
        return filtrarActivos(fuente, query, etiqueta, activo).stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    // Una asignación Persona/Rol está activa si su rol y su clínica lo están
    // (PersonaRol no tiene campo "activo" propio).
    protected boolean personaRolActivo(PersonaRol pr) {
        return pr != null
                && (pr.getIdRol() == null || esActivo(pr.getIdRol().getActivo()))
                && (pr.getIdClinica() == null || esActivo(pr.getIdClinica().getActivo()));
    }
    
    // =====================================================================
    // Getters / Setters
    // =====================================================================
    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public Estado_Crud getEstado() {
        return estado;
    }

    public void setEstado(Estado_Crud estado) {
        this.estado = estado;
    }

    @SuppressWarnings("unchecked")
    public List<T> getregistros() {
        return (List<T>) getWrappedData();
    }

    public void setRegistros(List<T> registros) {
        setWrappedData(registros);
    }

    // =====================================================================
    // SelectableDataModel<T>: puente objeto <-> texto para el rowSelect
    // =====================================================================
    // PrimeFaces manda solo el texto (rowKey) de la fila; aquí se busca el objeto
    // real en la lista.
    @Override
    public T getRowData(String rowKey) {
        List<T> lista = getregistros();
        if (lista == null || rowKey == null) {
            return null;
        }
        return lista.stream()
                .filter(r -> obtenerId(r).toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    // Texto que identifica la fila (su UUID) para PrimeFaces.
    @Override
    public String getRowKey(T t) {
        return obtenerId(t).toString();
    }
}
