package sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.boundary.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.InterfaceDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.MedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.PersonaDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.control.TipoMedioContactoDAO;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.MedioContacto;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.Persona;
import sv.ues.edu.occ.ingenieria.pp115_2026.salud.galenosv.entity.TipoMedioContacto;

@Named
@ViewScoped
public class MedioContactoModel extends AbstracCrudModel<MedioContacto> {

    @Inject
    private MedioContactoDAO medioContactoDAO;

    @Inject
    private PersonaDAO personaDAO;
    @Inject
    private TipoMedioContactoDAO tipoMedioContactoDAO;

    private List<Persona> personas;
    private List<TipoMedioContacto> tiposMedioContacto;

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
    protected UUID obtenerId(MedioContacto registro) {
        return registro.getIdMedioContacto();
    }

    @Override
    public void btnCrearhandler(ActionEvent ae) {
        if (relacionesCompletas()) {
            super.btnCrearhandler(ae);
        }
    }

    @Override
    public void btnModificarHandler() {
        if (relacionesCompletas()) {
            super.btnModificarHandler();
        }
    }

    private boolean relacionesCompletas() {
        if (registro == null) {
            return true;
        }
        if (registro.getIdPersona() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione una persona", "La persona es obligatoria"));
            return false;
        }
        if (registro.getIdTipoMedioContacto() == null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Seleccione un tipo de medio de contacto", "El tipo es obligatorio"));
            return false;
        }
        return true;
    }

    public List<Persona> getPersonas() {
        if (personas == null) {
            personas = personaDAO.findRange(0, 100);
        }
        return personas;
    }

    public List<TipoMedioContacto> getTiposMedioContacto() {
        if (tiposMedioContacto == null) {
            tiposMedioContacto = tipoMedioContactoDAO.findRange(0, 100);
        }
        return tiposMedioContacto;
    }
}