package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.model.ListDataModel;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.SelectableDataModel;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

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
 * @param <T> entidad que administra el bean
 * @author oscar
 */
public abstract class AbstracCrudModel<T> extends ListDataModel<T> implements SelectableDataModel<T>, Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesContext fc;

    protected Estado_Crud estado = Estado_Crud.NINGUNO;

    protected T registro;

    // ---- Métodos que cada subclase debe implementar ----
    protected abstract InterfaceDAO<T> getDAO();

    protected abstract T crearRegistroNuevo();

    protected abstract UUID obtenerId(T registro);

    // Hook opcional: las subclases pueden sobreescribirlo para inicializar
    // valores por defecto en el registro recién creado. Por defecto no hace nada.
    protected void configurarNuevoRegistro(T nuevoRegistro) {
    }

    public T getRegistro() {
        return registro;
    }

    public void setRegistro(T registro) {
        this.registro = registro;
    }

    public void btnNuevoHandler(ActionEvent ae) {
        this.registro = crearRegistroNuevo();
        configurarNuevoRegistro(this.registro);
        this.estado = Estado_Crud.CREAR;
    }

    public void btnSeleccionarRegistro(UUID id) {
        List<T> lista = getregistros();
        if (lista != null && !lista.isEmpty() && id != null) {
            this.registro = lista.stream()
                    .filter(r -> obtenerId(r).equals(id))
                    .collect(Collectors.toList()).getFirst();
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

    public void btnModificarHandler() {
        FacesMessage mensaje;
        if (this.registro != null) {
            try {
                getDAO().actualizar(registro);
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro actualizado con exito", "Registro guardado");
                this.estado = Estado_Crud.NINGUNO;
                this.registro = null;
                setWrappedData(getDAO().findRange(0, 100));
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede actualizar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algun registro");
        }
        fc.addMessage(null, mensaje);
    }

    public void btnEliminarHandler(UUID id) {
        FacesMessage mensaje;
        List<T> lista = getregistros();
        if (lista != null && !lista.isEmpty() && id != null) {
            try {
                getDAO().eliminar(id);
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro eliminado con exito", "Registro borrado");
                this.estado = Estado_Crud.NINGUNO;
                this.registro = null;
                setWrappedData(getDAO().findRange(0, 100));
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede eliminar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algun registro");
        }
        fc.addMessage(null, mensaje);
    }

    public void btnEliminarHandler() {
        if (this.registro != null) {
            btnEliminarHandler(obtenerId(this.registro));
        }
    }

    public Estado_Crud getEstado() {
        return estado;
    }

    public void setEstado(Estado_Crud estado) {
        this.estado = estado;
    }

    public void btnCancelar() {
        this.registro = null;
        this.estado = Estado_Crud.NINGUNO;
       
    }

    public void btnCrearhandler(ActionEvent ae) {
        FacesMessage mensaje;
        if (this.registro != null) {
            try {
                getDAO().crear(registro);
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro creado con exito", "Registro guardado");
                this.estado = Estado_Crud.NINGUNO;
                this.registro = null;
                setWrappedData(getDAO().findRange(0, 100));
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede guardar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Ingrese algun registro");
        }
        fc.addMessage(null, mensaje);
    }

    @PostConstruct
    public void inicializar() {
        setWrappedData(getDAO().findRange(0, 100));
    }

    @SuppressWarnings("unchecked")
    public List<T> getregistros() {
        return (List<T>) getWrappedData();
    }

    public void setRegistros(List<T> registros) {
        setWrappedData(registros);
    }

    // ---- SelectableDataModel<T>: puente objeto <-> texto para el rowSelect ----
    @Override
    public T getRowData(String rowKey) {
        List<T> lista = getregistros();
        if (lista == null) {
            return null;
        }
        return lista.stream()
                .filter(r -> obtenerId(r).toString().equals(rowKey))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getRowKey(T t) {
        return obtenerId(t).toString();
    }
}