package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 * Padre Bean de la Pantalla Procedimiento.xhtml: CRUD del procedimiento (el
 * padre) con pestañas; la pestaña "Pasos" la maneja ProcedimientoPasoModel.
 *
 * Hereda de AbstracCrudTabsModel (que a su vez hereda de AbstracCrudModel): de
 * ahí
 * salen los botones Nuevo/Guardar/Modificar/Eliminar, la lista de registros y
 * el
 * manejo de pestañas. Aquí solo se indica qué DAO usar, cómo nace un registro
 * nuevo
 * y qué hijo hay que limpiar (resetearHijos).
 *
 * @author antonio
 */
// Utilizamos Named para dar un identificador a la clase llamado
// procedimientoModel.
@Named
// Utilizamos ViewScoped para que la clase tenga un alcance de vista, es decir,
// que se
// mantenga viva mientras el usuario esté en la misma vista.
@ViewScoped
public class ProcedimientoModel extends AbstracCrudTabsModel<Procedimiento> {

    // Inyectamos el ProcedimientoDAO para poder utilizar sus métodos en la clase
    // ProcedimientoModel.
    // Es el DAO que AbstracCrudModel usa para crear, actualizar, eliminar y listar
    // (ver getDAO).
    @Inject
    private ProcedimientoDAO pdDAO;

    // Bean hijo de la pestaña "Pasos". Se inyecta para poder limpiarlo
    // (cargarDe(null))
    // cada vez que cambia el procedimiento seleccionado.
    @Inject
    private ProcedimientoPasoModel ProcedimientoPasoModel;

    // Hook de AbstracCrudTabsModel: se llama al pulsar Nuevo o Cancelar para dejar
    // vacío el bean hijo (los pasos).
    @Override
    protected void resetearHijos() {
        // Si no hay procedimiento seleccionado (o se va a crear uno nuevo),
        // la pestaña de Pasos no debe conservar el paso ni el estado del
        // procedimiento anterior.
        ProcedimientoPasoModel.cargarDe(null);
    }

    // Este es el método getDAO() que devuelve el ProcedimientoDAO inyectado,
    // para que la clase AbstracCrudModel pueda utilizarlo para realizar operaciones
    // CRUD sobre la entidad Procedimiento.
    @Override
    protected InterfaceDAO<Procedimiento> getDAO() {
        return pdDAO;
    }

    // Este es el método crearRegistroNuevoBase() (lo pide AbstracCrudTabsModel, que
    // llama a resetTab y
    // resetearHijos alrededor). Crea un nuevo objeto Procedimiento con un UUID
    // generado aleatoriamente y
    // lo marca como activo.
    @Override
    protected Procedimiento crearRegistroNuevoBase() {
        Procedimiento p = new Procedimiento(UUID.randomUUID());
        p.setActivo(Boolean.TRUE);
        return p;
    }

    // Este es el método obtenerId() que devuelve el UUID del objeto Procedimiento
    // pasado como parámetro,
    // para que la clase AbstracCrudModel pueda utilizarlo para identificar de
    // manera única cada registro de la entidad Procedimiento.
    @Override
    protected UUID obtenerId(Procedimiento registro) {
        return registro.getIdProcedimiento();
    }

}
