package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/**
 * Managed Bean para el CRUD de TipoDocumento.
 * @Named lo expone en las vistas .xhtml como "tipoDocumentoModel".
 * @ViewScoped: vive mientras el usuario está en esta misma página.
 *
 * @author oscar
 */
@Named
@ViewScoped
public class TipoDocumentoModel extends AbstracCrudModel<TipoDocumento> {

    // DAO específico de esta entidad, inyectado por CDI.
    @Inject
    private TipoDocumentoDAO tdDAO;

    // AbstracCrudModel necesita saber qué DAO usar internamente.
    @Override
    protected InterfaceDAO<TipoDocumento> getDAO() {
        return tdDAO;
    }

    // Se ejecuta al presionar "Nuevo": genera el UUID a mano (no es
    // autoincremental en la BD) y deja el registro activo por defecto.
    @Override
    protected TipoDocumento crearRegistroNuevo() {
        TipoDocumento t = new TipoDocumento(UUID.randomUUID());
        t.setActivo(Boolean.TRUE);
        return t;
    }

    // Forma genérica de leer el id de un registro (para la tabla,
    // seleccionar, eliminar, etc).
    @Override
    protected UUID obtenerId(TipoDocumento registro) {
        return registro.getIdTipoDocumento();
    }
}