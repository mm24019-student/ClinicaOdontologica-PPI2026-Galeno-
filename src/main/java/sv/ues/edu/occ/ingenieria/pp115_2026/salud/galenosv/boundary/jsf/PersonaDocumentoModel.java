package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/**
 * Bean de detalle para la pestaña "Documentos" dentro de la pantalla de
 * Persona. A diferencia de PersonaRolDetalleModel (que necesitaba dos catálogos
 * elegidos vía autocomplete antes de poder "crear"), Documento es el caso
 * simple para el que AbstracdetallecrudModel ya está pensado: un solo combo
 * (TipoDocumento) + un par de campos propios, con el flujo normal Nuevo ->
 * llenar formulario -> Crear/Actualizar/Eliminar.
 *
 * No se llama "DocumentoModel" porque ese nombre ya lo tiene el CRUD standalone
 * de Documento (su propia pantalla en el menú).
 */
@Named
@ViewScoped
public class PersonaDocumentoModel extends AbstracdetallecrudModel<Documento, Persona> {

    private static final long serialVersionUID = 1L;

    @Inject
    private DocumentoDAO documentoDAO;
    @Inject
    private TipoDocumentoDAO tipoDocumentoDAO;

    private List<TipoDocumento> tiposDocumento;

    @Override
    protected InterfaceDAO<Documento> getDAO() {
        return documentoDAO;
    }

    @Override
    protected Documento crearRegistroNuevo() {
        return new Documento(UUID.randomUUID());
    }

    @Override
    protected UUID obtenerId(Documento registro) {
        return registro.getIdDocumento();
    }

    // ---- Contrato con AbstracdetallecrudModel: Persona es el padre ----
    @Override
    protected List<Documento> buscarPorPadre(UUID idPadre) {
        return documentoDAO.findByPersona(idPadre);
    }

    @Override
    protected UUID obtenerIdPadre(Persona padre) {
        return padre.getIdPersona();
    }

    @Override
    protected void asignarPadre(Documento hijo, Persona padre) {
        hijo.setIdPersona(padre);
    }

    public List<TipoDocumento> getTiposDocumento() {
        if (tiposDocumento == null) {
            tiposDocumento = tipoDocumentoDAO.findRange(0, 100);
        }
        return tiposDocumento;
    }

    // completeMethod del autocomplete "Tipo de Documento" (mismo patrón que
    // completarRoles/completarClinicas en PersonaRolDetalleModel).
    public List<TipoDocumento> completarTiposDocumento(String query) {
        String texto = query == null ? "" : query.trim().toLowerCase();
        return getTiposDocumento().stream()
                .filter(t -> t.getNombre() != null && t.getNombre().toLowerCase().contains(texto))
                .collect(Collectors.toList());
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        // La persona no se valida: en este detalle el padre ya viene fijo.
        return requerir(registro.getIdTipoDocumento(), "Seleccione un tipo de documento", "El tipo es obligatorio");
    }
}
