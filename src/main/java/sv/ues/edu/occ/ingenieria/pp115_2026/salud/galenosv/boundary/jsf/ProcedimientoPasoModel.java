package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoPasoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.RolDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.ProcedimientoPaso;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Rol;

/**
 * Managed bean de la pestaña "Pasos" dentro de Procedimiento.xhtml: CRUD de los
 * ProcedimientoPaso de UN procedimiento (el padre lo entrega ProcedimientoModel
 * llamando a cargarDe).
 *
 * Hereda de AbstracdetallecrudModel: de ahí sale cargar solo los hijos del
 * padre,
 * asignar el padre a cada paso nuevo y volver a filtrar tras guardar o
 * eliminar.
 * Aquí se indica cómo buscar por padre (ProcedimientoPasoDAO), cómo nace un
 * paso
 * nuevo y se ofrece el autocompletado de Rol (RolDAO).
 *
 * @author antonio
 */
@Named
@ViewScoped
public class ProcedimientoPasoModel extends AbstracdetallecrudModel<ProcedimientoPaso, Procedimiento> {

    // Inyectamos el DAO, que es quien guarda, busca y elimina
    // registros de ProcedimientoPaso en la base de datos.
    @Inject
    private ProcedimientoPasoDAO pdDAO;

    // Inyectamos el DAO de Procedimiento por que necesitamos el UUId
    @Inject
    private RolDAO rolDAO;

    private List<Rol> roles;

    // Le decimos a la clase padre qué DAO debe usar para las operaciones del CRUD.
    @Override
    protected InterfaceDAO<ProcedimientoPaso> getDAO() {
        return pdDAO;
    }

    // ---- Los 3 métodos que pide AbstracDetalleCrudModel ----
    @Override
    protected List<ProcedimientoPaso> buscarPorPadre(UUID idPadre) {
        return pdDAO.findByProcedimiento(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Procedimiento padre) {
        return padre.getIdProcedimiento();
    }

    @Override
    protected void asignarPadre(ProcedimientoPaso hijo, Procedimiento padre) {
        hijo.setIdProcedimiento(padre);
    }

    // completeMethod del p:autoComplete de Rol: carga los roles la primera vez y
    // devuelve los que contienen el texto escrito (sin distinguir mayúsculas).
    public List<Rol> completarRoles(String query) {
        if (roles == null) {
            roles = rolDAO.findRange(0, 100);
        }
        return filtrar(roles, query, Rol::getNombre);
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
