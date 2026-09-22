package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.ProcedimientoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Procedimiento;

/**
 *
 * @author antonio
 */
//Utilizamos Named para dar un identificador a la clase llamado procedimientoModel.
@Named
//Utilizamos ViewScoped para que la clase tenga un alcance de vista, es decir, que se mantenga viva mientras el usuario esté en la misma vista.
@ViewScoped
public class ProcedimientoModel extends AbstracCrudModel<Procedimiento> {

//Inyectamos el ProcedimientoDAO para poder utilizar sus métodos en la clase ProcedimientoModel.
    @Inject
    private ProcedimientoDAO pdDAO;

//Este es el método getDAO() que devuelve el ProcedimientoDAO inyectado, 
//para que la clase AbstracCrudModel pueda utilizarlo para realizar operaciones CRUD sobre la entidad Procedimiento.
    @Override
    protected InterfaceDAO<Procedimiento> getDAO() {
        return pdDAO;
    }

//Este es el método crearRegistroNuevo() que crea un nuevo objeto Procedimiento con un UUID generado aleatoriamente y
//lo marca como activo,
    @Override
    protected Procedimiento crearRegistroNuevo() {
        Procedimiento p = new Procedimiento(UUID.randomUUID());
        p.setActivo(Boolean.TRUE);
        return p;
    }

//Este es el método obtenerId() que devuelve el UUID del objeto Procedimiento pasado como parámetro,
//para que la clase AbstracCrudModel pueda utilizarlo para identificar de manera única cada registro de la entidad Procedimiento.
    @Override
    protected UUID obtenerId(Procedimiento registro) {
        return registro.getIdProcedimiento();
    }

}
