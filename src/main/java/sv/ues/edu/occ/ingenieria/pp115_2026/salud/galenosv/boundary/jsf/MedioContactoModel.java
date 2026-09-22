package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.primefaces.event.SelectEvent;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

/**
 * @author oscar
 */
@Named
@ViewScoped
public class MedioContactoModel extends AbstracCrudModel<MedioContacto> {

    @Inject
    private MedioContactoDAO medioContactoDAO;

    private List<Persona> personas;
    private List<TipoMedioContacto> tiposMedioContacto;

    private String idPersonaSeleccionada;
    private String idTipoMedioContactoSeleccionado;

    @PostConstruct
    public void cargarCombos() {
        this.personas = medioContactoDAO.listarPersonas();
        this.tiposMedioContacto = medioContactoDAO.listarTiposMedioContacto();
    }

    @Override
    protected InterfaceDAO<MedioContacto> getDAO() {
        return medioContactoDAO;
    }

    @Override
    protected MedioContacto crearRegistroNuevo() {
        MedioContacto mc = new MedioContacto(UUID.randomUUID());
        mc.setFechaCreacion(new Date());
        return mc;
    }

    @Override
    protected void configurarNuevoRegistro(MedioContacto nuevoRegistro) {
        this.idPersonaSeleccionada = null;
        this.idTipoMedioContactoSeleccionado = null;
    }

    @Override
    public void onRowSelect(SelectEvent<MedioContacto> event) {
        super.onRowSelect(event);
        this.idPersonaSeleccionada = (registro.getIdPersona() != null) ? registro.getIdPersona().getIdPersona().toString() : null;
        this.idTipoMedioContactoSeleccionado = (registro.getIdTipoMedioContacto() != null) ? registro.getIdTipoMedioContacto().getIdTipoMedioContacto().toString() : null;
    }

    @Override
    protected UUID obtenerId(MedioContacto registro) {
        return registro.getIdMedioContacto();
    }

    public void onPersonaChange() {
        UUID id = (idPersonaSeleccionada != null && !idPersonaSeleccionada.isBlank()) ? UUID.fromString(idPersonaSeleccionada) : null;
        registro.setIdPersona(medioContactoDAO.buscarPersona(id));
    }

    public void onTipoMedioContactoChange() {
        UUID id = (idTipoMedioContactoSeleccionado != null && !idTipoMedioContactoSeleccionado.isBlank()) ? UUID.fromString(idTipoMedioContactoSeleccionado) : null;
        registro.setIdTipoMedioContacto(medioContactoDAO.buscarTipoMedioContacto(id));
    }

    public List<Persona> getPersonas() {
        return personas;
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        return tiposMedioContacto;
    }

    public String getIdPersonaSeleccionada() {
        return idPersonaSeleccionada;
    }

    public void setIdPersonaSeleccionada(String idPersonaSeleccionada) {
        this.idPersonaSeleccionada = idPersonaSeleccionada;
    }

    public String getIdTipoMedioContactoSeleccionado() {
        return idTipoMedioContactoSeleccionado;
    }

    public void setIdTipoMedioContactoSeleccionado(String idTipoMedioContactoSeleccionado) {
        this.idTipoMedioContactoSeleccionado = idTipoMedioContactoSeleccionado;
    }
}