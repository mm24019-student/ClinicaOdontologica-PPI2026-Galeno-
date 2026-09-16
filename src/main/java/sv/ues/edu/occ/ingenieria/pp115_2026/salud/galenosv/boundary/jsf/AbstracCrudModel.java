package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;

/**
 *
 * @author antonio
 */
public abstract class AbstracCrudModel<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Estado_Crud estado = Estado_Crud.NINGUNO;
    protected List<T> registros;
    protected T registro;

    abstract InterfaceDAO<T> getDAO();
    abstract FacesContext getFacesContext();
    abstract T crearNuevaInstancia(UUID id);
    protected abstract UUID obtenerId(T registro);

    protected void configurarNuevoRegistro(T nuevoRegistro) {
    }

    protected void inicializar() {
        this.registros = getDAO().findRange(0, 100);
    }

    public void btnNuevoHandler(ActionEvent ae) {
        this.registro = crearNuevaInstancia(UUID.randomUUID());
        configurarNuevoRegistro(this.registro);
        this.estado = Estado_Crud.CREAR;
    }

    public void btnSeleccionarRegistro(UUID id) {
        if (this.registros != null && !this.registros.isEmpty() && id != null) {
            this.registro = this.registros.stream()
                    .filter(r -> obtenerId(r).equals(id))
                    .collect(Collectors.toList()).getFirst();
            this.estado = Estado_Crud.MODIFICAR;
        }
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
                this.registros = getDAO().findRange(0, 100);
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede guardar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Ingrese algun registro");
        }
        getFacesContext().addMessage(null, mensaje);
    }

    public void btnModificarHandler(ActionEvent ae) {
        FacesMessage mensaje;
        if (this.registro != null) {
            try {
                this.registro = getDAO().actualizar(registro);
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro actualizado con exito", "Registro guardado");
                this.estado = Estado_Crud.NINGUNO;
                this.registro = null;
                this.registros = getDAO().findRange(0, 100);
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede actualizar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algun registro");
        }
        getFacesContext().addMessage(null, mensaje);
    }

    public void btnEliminarHandler(UUID id) {
        FacesMessage mensaje;
        if (id != null) {
            try {
                getDAO().eliminar(id);
                mensaje = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registro eliminado con exito", "Registro borrado");
                this.estado = Estado_Crud.NINGUNO;
                this.registro = null;
                this.registros = getDAO().findRange(0, 100);
            } catch (Exception ex) {
                mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "No se puede eliminar el registro", ex.getMessage());
            }
        } else {
            mensaje = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Registro no puede ser nulo", "Seleccione algun registro");
        }
        getFacesContext().addMessage(null, mensaje);
    }

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

    public List<T> getRegistros() {
        return registros;
    }

    public void setRegistros(List<T> registros) {
        this.registros = registros;
    }
}