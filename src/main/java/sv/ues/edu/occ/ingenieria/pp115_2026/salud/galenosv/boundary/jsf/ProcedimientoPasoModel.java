package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoModel extends AbstracCrudModel<ProcedimientoPaso> {

    // Inyectamos el DAO, que es quien guarda, busca y elimina
    // registros de ProcedimientoPaso en la base de datos.
    @Inject
    private ProcedimientoPasoDAO pdDAO;

    //Inyectamos el DAO de Procedimiento por que necesitamos el UUId
    @Inject
    private ProcedimientoDAO pdeDAO;

    @Inject
    private RolDAO rolDAO;

    private List<Procedimiento> procedimientos;

    private List<Rol> roles;

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<ProcedimientoPaso> getDAO() {
        return pdDAO;
    }
    
    public List<Procedimiento> getProcedimientos() {
        if (procedimientos == null) {
            procedimientos = pdeDAO.findRange(0, 100);
        }
        return procedimientos;
    }

    public List<Rol> getRoles() {
        if (roles == null) {
            roles = rolDAO.findRange(0, 100);
        }
        return roles;
    }

    // Se ejecuta al pulsar "Nuevo": crea un registro vacío con un UUID generado
    // automáticamente y con "Activo" marcado por defecto. La clase padre lo
    // guarda en "registro" y el formulario lo muestra.
    @Override
    protected ProcedimientoPaso crearRegistroNuevo() {
        ProcedimientoPaso p = new ProcedimientoPaso(UUID.randomUUID());
        p.setIndicaFin(Boolean.TRUE);
        return p;
    }

    // Devuelve el UUID (llave primaria) del registro. La clase padre lo usa para
    // identificar cada fila de la tabla, seleccionar una y eliminarla.
    @Override
    protected UUID obtenerId(ProcedimientoPaso registro) {
        return registro.getIdProcedimientoPaso();
    }

}
