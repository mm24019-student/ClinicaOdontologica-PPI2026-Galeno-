package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DocumentoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoDocumentoService;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

@Named
@ViewScoped
public class DocumentoModel extends AbstracCrudModel<Documento> {

    @Inject
    private DocumentoService documentoService;

    @Inject
    private TipoDocumentoService tipoDocumentoService;


    @Override
    protected InterfaceService<Documento> getService() {
        return documentoService;
    }

    @Override
    protected Documento crearRegistroNuevo() {
        return new Documento(UUID.randomUUID());
    }

    @Override
    protected UUID obtenerId(Documento registro) {
        return registro.getIdDocumento();
    }
    
        @Override
    protected boolean validarAntesDeGuardar() {
        return requerir(registro.getIdPersona(), "Seleccione una persona", "La persona es obligatoria")
                && requerir(registro.getIdTipoDocumento(), "Seleccione un tipo de documento", "El tipo es obligatorio")
                && validarFormatoDelTipo()
                && validarSinDuplicados();
    }

    // Valida el valor contra la expresion regular del TipoDocumento, leida de la
    // base de datos en este momento (no de la lista cacheada en el bean).
    private boolean validarFormatoDelTipo() {
        TipoDocumento tipo = tipoDocumentoService.buscar(registro.getIdTipoDocumento().getIdTipoDocumento());
        if (tipo == null) {
            tipo = registro.getIdTipoDocumento();
        }
        return validarFormatoConRegex(registro.getValor(), tipo.getExpresionRegular(),
                tipo.getNombre(), tipo.getIndicaciones());
    }

    // Evita datos repetidos: una persona no puede tener dos documentos del mismo
    // tipo, y el mismo numero de un tipo no puede estar en dos documentos.
    private boolean validarSinDuplicados() {
        UUID idTipo = registro.getIdTipoDocumento().getIdTipoDocumento();
        if (documentoService.existeTipoParaPersona(registro.getIdPersona().getIdPersona(), idTipo, registro.getIdDocumento())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Documento duplicado",
                    "Esta persona ya tiene registrado un documento de tipo " + registro.getIdTipoDocumento().getNombre());
            return false;
        }
        if (documentoService.existeValorParaTipo(registro.getValor(), idTipo, registro.getIdDocumento())) {
            mensaje(FacesMessage.SEVERITY_ERROR, "Documento duplicado",
                    "Ya existe un documento de tipo " + registro.getIdTipoDocumento().getNombre()
                    + " con el número " + registro.getValor());
            return false;
        }
        return true;
    }
}