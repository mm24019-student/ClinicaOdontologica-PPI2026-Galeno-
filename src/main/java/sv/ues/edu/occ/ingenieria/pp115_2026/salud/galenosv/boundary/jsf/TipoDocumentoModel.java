package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoService;
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

    // Service específico de esta entidad, inyectado por CDI.
    @Inject
    private TipoDocumentoService tdService;

    // AbstracCrudModel necesita saber qué Service usar internamente.
    @Override
    protected InterfaceService<TipoDocumento> getService() {
        return tdService;
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

    // Evita datos repetidos: no se puede registrar dos veces el mismo tipo de
    // documento (mismo nombre), ni al crear ni al modificar.
    @Override
    protected boolean validarAntesDeGuardar() {
        if (tdService.existeNombre(registro.getNombre(), registro.getIdTipoDocumento())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Tipo de documento duplicado",
                    "Ya existe un tipo de documento llamado " + registro.getNombre().trim());
            return false;
        }
        return validarSintaxisRegex(registro.getExpresionRegular());
    }
}