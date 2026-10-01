package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
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

    //Llamado a la funcion filtrarActivos de AbstarcCrudModel para que filtre Procedimientos activos y inactivos
    public List<TipoDocumento> completarTiposDocumento(String query) {
        return filtrarActivos(getTiposDocumento(), query, TipoDocumento::getNombre, TipoDocumento::getActivo);
    }

    @Override
    protected boolean validarAntesDeGuardar() {
        // La persona no se valida: en este detalle el padre ya viene fijo.
        if (!requerir(registro.getIdTipoDocumento(), "Seleccione un tipo de documento", "El tipo es obligatorio")) {
            return false;
        }
        return validarFormatoDelTipo() && validarSinDuplicados();
    }

    // Valida el valor contra la expresion regular del TipoDocumento, leida de la
    // base de datos en este momento (no de la lista cacheada en el bean).
    private boolean validarFormatoDelTipo() {
        TipoDocumento tipo = tipoDocumentoDAO.buscar(registro.getIdTipoDocumento().getIdTipoDocumento());
        if (tipo == null) {
            tipo = registro.getIdTipoDocumento();
        }
        return validarFormatoConRegex(registro.getValor(), tipo.getExpresionRegular(),
                tipo.getNombre(), tipo.getIndicaciones());
    }

    // Evita datos repetidos: la persona no puede tener dos documentos del mismo
    // tipo (ej. dos DUI), y el mismo numero de un tipo no puede estar repetido.
    private boolean validarSinDuplicados() {
        UUID idTipo = registro.getIdTipoDocumento().getIdTipoDocumento();
        UUID idPersona = padreActual != null ? obtenerIdPadre(padreActual) : null;
        if (idPersona != null
                && documentoDAO.existeTipoParaPersona(idPersona, idTipo, registro.getIdDocumento())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Documento duplicado",
                    "Esta persona ya tiene registrado un documento de tipo " + registro.getIdTipoDocumento().getNombre());
            return false;
        }
        if (documentoDAO.existeValorParaTipo(registro.getValor(), idTipo, registro.getIdDocumento())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Documento duplicado",
                    "Ya existe un documento de tipo " + registro.getIdTipoDocumento().getNombre()
                    + " con el número " + registro.getValor());
            return false;
        }
        return true;
    }
}