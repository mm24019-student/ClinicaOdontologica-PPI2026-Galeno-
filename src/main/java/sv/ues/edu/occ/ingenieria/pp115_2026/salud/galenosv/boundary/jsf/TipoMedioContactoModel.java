package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 * Managed Bean (backing bean) para el CRUD de TipoMedioContacto.
 * @Named lo expone en las vistas .xhtml como "tipoMedioContactoModel"
 * (el nombre por defecto es el nombre de la clase con la primera letra
 * en minúscula).
 * @ViewScoped hace que la instancia viva mientras el usuario esté en la
 * misma vista/página, y se destruya al navegar a otra.
 *
 * @author oscar
 */
@Named
@ViewScoped
public class TipoMedioContactoModel extends AbstracCrudModel<TipoMedioContacto> {

    // Se inyecta el DAO específico de esta entidad; es el que hace el
    // trabajo real contra la base de datos (crear, buscar, actualizar, eliminar).
    @Inject
    private TipoMedioContactoDAO tmcDAO;

    // AbstracCrudModel necesita saber cuál DAO usar; se lo devolvemos aquí.
    @Override
    protected InterfaceDAO<TipoMedioContacto> getDAO() {
        return tmcDAO;
    }

    // Se llama cuando el usuario presiona "Nuevo": crea un registro en
    // blanco con un UUID ya generado (porque el id no es autoincremental)
    // y lo deja "Activo" por defecto.
    @Override
    protected TipoMedioContacto crearRegistroNuevo() {
        TipoMedioContacto t = new TipoMedioContacto(UUID.randomUUID());
        t.setActivo(Boolean.TRUE);
        return t;
    }

    // AbstracCrudModel necesita una forma genérica de leer el id de
    // cualquier registro (para el rowKey de la tabla, buscar, eliminar, etc).
    @Override
    protected UUID obtenerId(TipoMedioContacto registro) {
        return registro.getIdTipoMedioContacto();
    }
}