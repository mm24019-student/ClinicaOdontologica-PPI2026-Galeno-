package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.DocumentoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Documento;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoDocumento;

/**
 * @author oscar
 */
@Named
@ViewScoped
public class DocumentoModel extends AbstracCrudModel<Documento> {

    @Inject
    private DocumentoDAO documentoDAO;

    private List<Persona> personas;
    private List<TipoDocumento> tiposDocumento;

    private String idPersonaSeleccionada;
    private String idTipoDocumentoSeleccionado;

    @PostConstruct
    public void cargarCombos() {
        this.personas = documentoDAO.listarPersonas();
        this.tiposDocumento = documentoDAO.listarTiposDocumento();
    }

    @Override
    protected InterfaceDAO<Documento> getDAO() {
        return documentoDAO;
    }

    @Override
    protected Documento crearRegistroNuevo() {
        return new Documento(UUID.randomUUID());
    }

    @Override
    protected void configurarNuevoRegistro(Documento nuevoRegistro) {
        this.idPersonaSeleccionada = null;
        this.idTipoDocumentoSeleccionado = null;
    }

    @Override
    public void onRowSelect(SelectEvent<Documento> event) {
        super.onRowSelect(event);
        this.idPersonaSeleccionada = (registro.getIdPersona() != null) ? registro.getIdPersona().getIdPersona().toString() : null;
        this.idTipoDocumentoSeleccionado = (registro.getIdTipoDocumento() != null) ? registro.getIdTipoDocumento().getIdTipoDocumento().toString() : null;
    }

    @Override
    protected UUID obtenerId(Documento registro) {
        return registro.getIdDocumento();
    }

    public void onPersonaChange() {
        UUID id = (idPersonaSeleccionada != null && !idPersonaSeleccionada.isBlank()) ? UUID.fromString(idPersonaSeleccionada) : null;
        registro.setIdPersona(documentoDAO.buscarPersona(id));
    }

    public void onTipoDocumentoChange() {
        UUID id = (idTipoDocumentoSeleccionado != null && !idTipoDocumentoSeleccionado.isBlank()) ? UUID.fromString(idTipoDocumentoSeleccionado) : null;
        registro.setIdTipoDocumento(documentoDAO.buscarTipoDocumento(id));
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public List<TipoDocumento> getTiposDocumento() {
        return tiposDocumento;
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdTipoDocumentoSeleccionado() {
        return idTipoDocumentoSeleccionado;
    }

    public void setIdTipoDocumentoSeleccionado(String idTipoDocumentoSeleccionado) {
        this.idTipoDocumentoSeleccionado = idTipoDocumentoSeleccionado;
    }
}